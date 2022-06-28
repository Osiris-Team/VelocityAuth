package com.osiris.velocityauth.commands;

import com.osiris.velocityauth.Main;
import com.osiris.velocityauth.database.RegisteredUser;
import com.osiris.velocityauth.database.Session;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import java.util.List;
import java.util.Random;

public final class AdminLoginCommand implements Command {

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length != 3) {
            source.sendMessage(Component.text("Failed! Requires 3 arguments: <username> <password> <ip-address>"));
            return;
        }
        String username = args[0];
        String password = args[1];
        String ipAddress = args[2];
        try {
            String error = execute(username, password, ipAddress);
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
        return "a_login";
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
        if (args.length != 3) return "Failed! Required 3 arguments: <username> <password> <ip-address>";
        String username = ((String) args[0]).trim();
        String password = ((String) args[1]).trim();
        String ipAddress = ((String) args[2]).trim();
        if (Main.INSTANCE.hasValidSession(username, ipAddress))
            return "Failed! Already logged in. Your current session: "+Main.INSTANCE.getValidSession(username, ipAddress).toPrintString();
        List<RegisteredUser> registeredUsers = RegisteredUser.get("username=?", username);
        if (registeredUsers.isEmpty())
            return "Failed! Could not find registered user named '" + username + "' in database.";
        if (registeredUsers.size() > 1)
            throw new Exception("There are multiple (" + registeredUsers.size() + ") registered players named '" + username
                    + "'! Its highly recommended to fix this issue.");
        if (!new Pbkdf2PasswordEncoder().matches(password, registeredUsers.get(0).password))
            return "Failed! Invalid credentials!";
        // Login success
        try {
            Thread.sleep(new Random().nextInt(1000)); // Prevent password spoofing via timings
            long now = System.currentTimeMillis();
            RegisteredUser user = registeredUsers.get(0);
            RegisteredUser.update(user);
            List<Session> sessions = Session.get("username=? AND ipAddress=?", user.username, ipAddress);
            Session session = null;
            if (sessions.isEmpty()) {
                session = Session.create(user.id, ipAddress,
                        now + (Main.INSTANCE.sessionMaxHours * 3600000L), (byte) 1, username);
                Session.add(session);
            } else {
                session = sessions.get(0);
                session.isActive = 1;
                Session.update(session);
            }
            Main.INSTANCE.logger.info("Login success for '" + username + "', using session " + session.id);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed! Database details could not updated.";
        }
        return null;
    }

}
