package com.osiris.velocityauth.command;

import com.osiris.velocityauth.Command;
import com.osiris.velocityauth.Main;
import com.osiris.velocityauth.RegisteredUser;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import java.util.Objects;

public class RegisterCommand implements Command {
    @Override
    public String command() {
        return "register";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.register";
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if(args.length != 2){
            source.sendMessage(Component.text("Failed! Requires 2 arguments: <password> <confirm-password>"));
            return;
        }
        String password = args[0];
        String confirmPassword = args[1];
        if(Objects.equals(password, confirmPassword)){
            source.sendMessage(Component.text("Failed! <password> does not match <confirm-password>"));
            return;
        }
        if(source instanceof Player){
            Player player = (Player) source;
            String encodedPassword = new Pbkdf2PasswordEncoder().encode(password);
            try {
                RegisteredUser.add(
                        RegisteredUser.create(player.getUsername(), encodedPassword));
            } catch (Exception e) {
                e.printStackTrace();
                source.sendMessage(Component.text("Failed! Details could not be added to the database."));
                return;
            }
        } else
            Main.INSTANCE.logger.error("Failed! Must be player to execute this command.");
    }
}
