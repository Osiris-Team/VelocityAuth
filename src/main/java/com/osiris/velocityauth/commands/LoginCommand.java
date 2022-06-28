package com.osiris.velocityauth.commands;

import com.osiris.velocityauth.perms.NoPermissionPlayer;
import com.osiris.velocityauth.Main;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

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
            try {
                String error = new AdminLoginCommand().execute(player.getUsername(), password,
                        player.getRemoteAddress().getAddress().getHostAddress());
                if (error == null) {
                    source.sendMessage(Component.text("Logged in!"));
                } else {
                    source.sendMessage(Component.text(error, TextColor.color(255, 0, 0)));
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                source.sendMessage(Component.text("Failed! Details could not be added to the database.", TextColor.color(255, 0, 0)));
                return;
            }

            // Restore default permission function
            try{
                for (NoPermissionPlayer perm : Main.INSTANCE.noPermissionPlayers) {
                    if(Objects.equals(perm.player.getUniqueId(), player.getUniqueId())){
                        perm.permissionProvider.hasPermission = perm.oldPermissionFunction;
                        Main.INSTANCE.noPermissionPlayers.remove(perm);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                source.sendMessage(Component.text("Failed! "+e.getMessage(), TextColor.color(255, 0, 0)));
            }

            // Forward user to first server
            for (RegisteredServer s : Main.INSTANCE.proxy.getAllServers()) {
                player.createConnectionRequest(s).fireAndForget();
                break;
            }
        } else
            Main.INSTANCE.logger.error("Failed! Must be player to execute this command.");
    }
}
