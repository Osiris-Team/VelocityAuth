package com.osiris.velocityauth.commands;

import com.osiris.velocityauth.database.Session;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;

public class ClearSessionsCommand implements Command {
    @Override
    public String command() {
        return "clear_sessions";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.clear.sessions";
    }

    @Override
    public String execute(Object... args) throws Exception {
        return null;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        int countSessions = 0, countSessionsRemove = 0;
        String username = null;
        if (args.length == 1) {
            username = ((String)args[0]).trim();
            try {
                List<Session> list = Session.get("username=?", username);
                countSessions = list.size();
                for (Session session : list) {
                    Session.remove(session);
                    countSessionsRemove++;
                }
                source.sendMessage(Component.text("Removed "+countSessionsRemove+"/"+countSessions+" sessions! "));
            } catch (Exception e) {
                e.printStackTrace();
                source.sendMessage(Component.text("Failed! Removed "+countSessionsRemove+"/"+countSessions+" sessions! " + e.getMessage(), TextColor.color(255, 0, 0)));
            }
        }
        else{
            try {
                List<Session> list = Session.get();
                countSessions = list.size();
                for (Session session : list) {
                    Session.remove(session);
                    countSessionsRemove++;
                }
                source.sendMessage(Component.text("Removed "+countSessionsRemove+"/"+countSessions+" sessions! "));
            } catch (Exception e) {
                e.printStackTrace();
                source.sendMessage(Component.text("Failed! Removed "+countSessionsRemove+"/"+countSessions+" sessions! " + e.getMessage(), TextColor.color(255, 0, 0)));
            }
        }
    }
}
