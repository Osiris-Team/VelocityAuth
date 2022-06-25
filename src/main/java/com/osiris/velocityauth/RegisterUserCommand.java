package com.osiris.velocityauth;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

public final class RegisterUserCommand implements SimpleCommand {

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
            RegisteredUser.add(
                    RegisteredUser.create(username, encodedPassword));
        } catch (Exception e) {
            e.printStackTrace();
            source.sendMessage(Component.text("Failed! Details could not be added to the database."));
            return;
        }
        source.sendMessage(Component.text("Success!"));
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("velocityauth.registeruser");
    }
}
