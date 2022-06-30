package com.osiris.velocityauth.commands;

import com.osiris.velocityauth.Main;
import com.osiris.velocityauth.database.BannedUser;
import com.osiris.velocityauth.utils.Arr;
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
        Arr<String> args = new Arr<>(invocation.arguments());
        if (args.length < 1) {
            source.sendMessage(Component.text("Failed! Requires minimum 1 argument: <username> (<hours> <reason>)"));
            return;
        }
        String username = args.get(0);
        long timestampExpires = args.get(1) != null ? System.currentTimeMillis() + (Long.parseLong(args.get(1)) * 3600000) :
                System.currentTimeMillis() + 86400000; // 24h
        String reason = args.get(2) != null ? args.toPrintString(2, args.length-1) : "Your behavior violated our community guidelines and/or terms of service.";

        try {
            Player bannedPlayer = Main.INSTANCE.findPlayerByUsername(username);
            String error = execute(username, bannedPlayer.getUniqueId().toString(),
                    Main.INSTANCE.getPlayerIp(bannedPlayer), timestampExpires, reason);
            if (error == null) {
                source.sendMessage(Component.text("Ban success!"));
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
            return "Failed! Required 5 arguments: <username> <uuid> <ip-address> <timestamp-expires> <reason>";
        String username = ((String) args[0]).trim();
        String uuid = ((String) args[1]).trim();
        String ipAddress = ((String) args[2]).trim();
        long timestampExpires = args[3] instanceof String ?
                Long.parseLong(((String) args[3])) : (long) args[3];
        String reason = ((String) args[4]).trim();
        if (BannedUser.isBanned(ipAddress, uuid))
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
