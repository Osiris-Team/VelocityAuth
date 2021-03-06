package com.osiris.velocityauth.commands;

import com.osiris.velocityauth.Main;
import com.osiris.velocityauth.database.FailedLogin;
import com.osiris.velocityauth.perms.NoPermissionPlayer;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

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
        Main.INSTANCE.executor.execute(() -> {
            if (source instanceof Player) {
                Player player = (Player) source;
                try {
                    if (FailedLogin.get("(uuid=? OR ipAddress=?) AND timestamp > (?-60000)", player.getUniqueId().toString(),
                            Main.INSTANCE.getPlayerIp(player),
                            System.currentTimeMillis())
                            .size() >= Main.INSTANCE.minFailedLoginsForBan) {
                        new BanCommand().execute(player.getUsername(), player.getUniqueId().toString(), player.getRemoteAddress().getAddress().getHostAddress(),
                                System.currentTimeMillis()+(Main.INSTANCE.failedLoginBanTimeSeconds * 1000L), "Too many failed login attempts.");
                    }
                    if (args.length != 1) {
                        sendFailedLogin(player, "Failed! Requires 1 argument: <password>");
                        return;
                    }
                    String password = args[0];
                    try {
                        String error = new AdminLoginCommand().execute(player.getUsername(), password,
                                Main.INSTANCE.getPlayerIp(player));
                        if (error == null) {
                            source.sendMessage(Component.text("Logged in!"));
                        } else {
                            sendFailedLogin(player, error);
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendFailedLogin(player, "Failed! Details could not be added to the database.");
                        return;
                    }

                    // Restore default permission function
                    try {
                        for (NoPermissionPlayer perm : Main.INSTANCE.noPermissionPlayers) {
                            if (Objects.equals(perm.player.getUniqueId(), player.getUniqueId())) {
                                perm.permissionProvider.hasPermission = perm.oldPermissionFunction;
                                Main.INSTANCE.noPermissionPlayers.remove(perm);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendFailedLogin(player, "Failed! " + e.getMessage());
                    }

                    // Forward user to first server
                    for (RegisteredServer s : Main.INSTANCE.proxy.getAllServers()) {
                        if (!Objects.equals(s.getServerInfo().getName(), Main.INSTANCE.authServer.getServerInfo().getName())) {
                            player.createConnectionRequest(s).fireAndForget();
                            return;
                        }
                    }
                    source.sendMessage(Component.text("Unable to forward to another server, because there aren't any.", TextColor.color(255, 0, 0)));
                } catch (Exception e) {
                    e.printStackTrace();
                    sendFailedLogin(player, "Failed! " + e.getMessage());
                }
            } else
                Main.INSTANCE.logger.error("Failed! Must be player to execute this command.");
        });
    }

    private void sendFailedLogin(Player player, String reason) {
        try {
            player.sendMessage(Component.text(reason, TextColor.color(255, 0, 0)));
            FailedLogin.add(FailedLogin.create(player.getUsername(),
                    player.getRemoteAddress().getAddress().getHostAddress(),
                    System.currentTimeMillis(), reason, player.getUniqueId().toString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
