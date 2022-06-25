package com.osiris.velocityauth;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

public final class LoginUserCommand implements SimpleCommand {

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if(args.length != 2){
            source.sendMessage(Component.text("Failed! Requires 2 arguments: <username> <password>"));
            return;
        }
        String username = args[0];
        String password = args[1];
        String encodedPassword = new Pbkdf2PasswordEncoder().encode(password);
        try {
            if(RegisteredUser.get("username=? AND password=?", username, encodedPassword).isEmpty())
                source.sendMessage(Component.text("Login failed!"));
            else
                source.sendMessage(Component.text("Login success!"));
        } catch (Exception e) {
            e.printStackTrace();
            source.sendMessage(Component.text("Failed! Database error."));
            return;
        }
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("velocityauth.loginuser");
    }
}
