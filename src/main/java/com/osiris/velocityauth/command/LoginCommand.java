package com.osiris.velocityauth.command;

import com.osiris.velocityauth.Command;
import com.osiris.velocityauth.Main;
import com.osiris.velocityauth.RegisteredUser;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import java.util.List;
import java.util.Objects;

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
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if(args.length != 1){
            source.sendMessage(Component.text("Failed! Requires 1 argument: <password>"));
            return;
        }
        String password = args[0];
        if(source instanceof Player){
            Player player = (Player) source;
            String encodedPassword = new Pbkdf2PasswordEncoder().encode(password);
            try {
                List<RegisteredUser> registeredUsers = RegisteredUser.get("username=", player.getUsername());
                if(registeredUsers.isEmpty()){
                    source.sendMessage(Component.text("Failed! Could not find registered user named '"+player.getUsername()+"' in database."));
                    return;
                }
                if(Objects.equals(registeredUsers.get(0).password, encodedPassword)){
                    try{
                        registeredUsers.get(0).isLoggedIn = 1; // true
                        RegisteredUser.update(registeredUsers.get(0));
                    } catch (Exception e) {
                        e.printStackTrace();
                        source.sendMessage(Component.text("Failed! Details could not updated in the database."));
                        return;
                    }
                    source.sendMessage(Component.text("Logged in!"));
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
