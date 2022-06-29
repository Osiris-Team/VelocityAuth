package com.osiris.velocityauth.commands;

import com.osiris.velocityauth.Main;
import com.osiris.velocityauth.database.BannedUser;
import com.osiris.velocityauth.utils.UtilsTime;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.time.Instant;
import java.util.Objects;

public final class BanCommand implements Command {

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length != 3) {
            source.sendMessage(Component.text("Failed! Requires 3 arguments: <username> <uuid> <ip-address>"));
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
        return "ban";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.ban";
    }

    @Override
    public String execute(Object... args) throws Exception {
        if (args.length != 5)
            return "Failed! Required 4 arguments: <username> <uuid> <ip-address> <timestamp-expires> <reason>";
        String username = ((String) args[0]).trim();
        String uuid = ((String) args[1]).trim();
        String ipAddress = ((String) args[2]).trim();
        long timestampExpires = args[3] instanceof String ?
                Long.parseLong(((String) args[3])) : (long) args[3];
        String reason = ((String) args[4]).trim();
        if (BannedUser.isBanned(uuid, ipAddress))
            return "Failed! Already banned player.";
        try {
            BannedUser.add(BannedUser.create(username, ipAddress, timestampExpires, uuid, reason));
            for (Player p : Main.INSTANCE.proxy.getAllPlayers()) {
                if (Objects.equals(p.getUniqueId().toString(), uuid)) {
                    p.disconnect(getBanText(timestampExpires, reason));
                    break;
                }
            }
            Main.INSTANCE.logger.info("Banned '" + username + "/" + uuid + "' until " +
                    Instant.ofEpochMilli(timestampExpires).toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed! Database details could not updated.";
        }
        return null;
    }

    public Component getBanText(long timestampExpires, String reason) {
        return Component.text("You have been banned for " + new UtilsTime().getFormattedString(timestampExpires - System.currentTimeMillis())
                + ". Reason: " + reason, TextColor.color(255, 0, 0));
    }

}
