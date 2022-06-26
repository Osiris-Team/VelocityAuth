package com.osiris.velocityauth.command;

import com.osiris.velocityauth.Command;
import com.osiris.velocityauth.Main;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

public class LoginCommand implements Command {
    @Override
    public String command() {
        return "login";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.login";
    }

    @Override
    public String execute(Object... args) throws Exception {
        return null;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length != 1) {
            source.sendMessage(Component.text("Failed! Requires 1 argument: <password>"));
            return;
        }
        String password = args[0];
        if (source instanceof Player) {
            Player player = (Player) source;
            String encodedPassword = new Pbkdf2PasswordEncoder().encode(password);
            try {
                String error = new AdminLoginCommand().execute(player.getUsername(), encodedPassword);
                if (error == null) {
                    source.sendMessage(Component.text("Logged in!"));
                } else {
                    source.sendMessage(Component.text(error, TextColor.color(255, 0, 0)));
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                source.sendMessage(Component.text("Failed! Details could not be added to the database."));
                return;
            }
        } else
            Main.INSTANCE.logger.error("Failed! Must be player to execute this command.");
    }
}
