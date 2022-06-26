package com.osiris.velocityauth.command;

import com.osiris.velocityauth.Command;
import com.osiris.velocityauth.Main;
import com.osiris.velocityauth.RegisteredUser;
import com.osiris.velocityauth.Session;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import java.util.List;

public final class AdminLoginCommand implements Command {

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length != 2) {
            source.sendMessage(Component.text("Failed! Requires 2 arguments: <username> <password>"));
            return;
        }
        String username = args[0];
        String password = args[1];
        String encodedPassword = new Pbkdf2PasswordEncoder().encode(password);
        try {
            String error = execute(username, encodedPassword);
            if (error == null) {
                source.sendMessage(Component.text("Login success!"));
            } else {
                source.sendMessage(Component.text(error, TextColor.color(255, 0, 0)));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            source.sendMessage(Component.text("Failed! Database error."));
            return;
        }
    }

    @Override
    public String command() {
        return "alogin";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.admin.login";
    }

    @Override
    public String execute(Object... args) throws Exception {
        String username = (String) args[0];
        String encodedPassword = (String) args[1];
        String ipAddress = (String) args[2];

        List<RegisteredUser> registeredUsers = RegisteredUser.get("username=", username);
        if (registeredUsers.isEmpty())
            return "Failed! Could not find registered user named '" + username + "' in database.";
        if (RegisteredUser.get("username=? AND password=?", username, encodedPassword).isEmpty())
            return "Failed! Invalid credentials!";
        // Login success
        try {
            long now = System.currentTimeMillis();
            RegisteredUser user = registeredUsers.get(0);
            RegisteredUser.update(user);
            List<Session> sessions = Session.get("username=? AND ipAddress=?", user.username, ipAddress);
            if (sessions.isEmpty()) {
                Session.add(Session.create(user.id, ipAddress, now + Main.INSTANCE.sessionMaxHours, (byte) 1));
            } else {
                Session session = sessions.get(0);
                session.isLoggedIn = 1;
                Session.update(session);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed! Database details could not updated.";
        }
        return null;
    }

}
