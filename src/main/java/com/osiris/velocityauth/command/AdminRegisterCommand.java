package com.osiris.velocityauth.command;

import com.osiris.velocityauth.Command;
import com.osiris.velocityauth.RegisteredUser;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

public final class AdminRegisterCommand implements Command {

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if(args.length != 2){
            source.sendMessage(Component.text("Failed! Requires 2 arguments: <username> <password>"));
            return;
        }
        try {
            String error = execute(args[0], args[1]);
            if(error == null){
                source.sendMessage(Component.text("Registration success!"));
            } else {
                source.sendMessage(Component.text(error, TextColor.color(255, 0, 0)));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            source.sendMessage(Component.text("Failed! "+e.getMessage(), TextColor.color(255, 0, 0)));
        }
    }

    @Override
    public String command() {
        return "aregister";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.admin.register";
    }

    @Override
    public String execute(Object... args) throws Exception {
        String username = (String) args[0];
        String password = (String) args[1];
        String encodedPassword = new Pbkdf2PasswordEncoder().encode(password);
        try {
            RegisteredUser.add(
                    RegisteredUser.create(username, encodedPassword));
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed! Details could not be added to the database.";
        }
        return null;
    }
}
