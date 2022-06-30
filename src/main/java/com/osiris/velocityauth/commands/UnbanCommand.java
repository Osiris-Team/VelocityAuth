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
import java.util.List;
import java.util.Objects;

public final class UnbanCommand implements Command {

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        Arr<String> args = new Arr<>(invocation.arguments());
        if (args.length != 1) {
            source.sendMessage(Component.text("Failed! Requires 1 argument: <username> "));
            return;
        }
        String username = args.get(0);
        try {
            String error = execute(username);
            if (error == null) {
                source.sendMessage(Component.text("Unban success!"));
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
        return "unban";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.unban";
    }

    @Override
    public String execute(Object... args) throws Exception {
        if (args.length != 1)
            return "Failed! Required 1 argument: <username>";
        String username = ((String) args[0]).trim();
        try {
            List<BannedUser> bannedUsers = BannedUser.getBannedUsernames(username);
            if(bannedUsers.isEmpty()) return "Failed! No banned players by name '"+username+"' found!";
            for (BannedUser bannedUser : bannedUsers) {
                bannedUser.timestampExpires = System.currentTimeMillis();
                BannedUser.update(bannedUser);
            }
            Main.INSTANCE.logger.info("Unbanned '" + username + "' now.");
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed! Database details could not updated.";
        }
        return null;
    }

}
