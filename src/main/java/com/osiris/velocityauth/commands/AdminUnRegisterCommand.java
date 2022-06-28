package com.osiris.velocityauth.commands;

import com.osiris.velocityauth.Main;
import com.osiris.velocityauth.database.RegisteredUser;
import com.osiris.velocityauth.database.Session;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

public final class AdminUnRegisterCommand implements Command {

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length != 1) {
            source.sendMessage(Component.text("Failed! Requires 1 argument: <username>"));
            return;
        }
        try {
            String error = execute(args[0]);
            if (error == null) {
                source.sendMessage(Component.text("Unregister success!"));
            } else {
                source.sendMessage(Component.text(error, TextColor.color(255, 0, 0)));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            source.sendMessage(Component.text("Failed! " + e.getMessage(), TextColor.color(255, 0, 0)));
        }
    }

    @Override
    public String command() {
        return "a_unregister";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.admin.unregister";
    }

    @Override
    public String execute(Object... args) throws Exception {
        if(args.length != 1) return "Failed! Required 1 arguments: <username>";
        String username = (String) args[0];
        if(!Main.INSTANCE.isRegistered(username))
            return "Failed! No registered player named '"+username+"' found!";
        try {
            RegisteredUser user = RegisteredUser.get("username=?", username).get(0);
            RegisteredUser.remove(user);
            for (Session session : Session.get("username=?", username)) {
                Session.remove(session);
            }
            Main.INSTANCE.logger.info("Unregister success for '"+username+"', removed id "+user.id+" and related sessions");
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed! Details could not be added to the database.";
        }
        return null;
    }
}
