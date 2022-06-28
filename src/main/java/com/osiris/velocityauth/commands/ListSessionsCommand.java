package com.osiris.velocityauth.commands;

import com.osiris.velocityauth.Main;
import com.osiris.velocityauth.database.Session;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.Objects;

public class ListSessionsCommand implements Command {
    @Override
    public String command() {
        return "list sessions";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public String permission() {
        return "velocityauth.list.sessions";
    }

    @Override
    public String execute(Object... args) throws Exception {
        return null;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        String username = null;
        if (args.length == 1) {
            username = ((String)args[0]).trim();
            try {
                for (Session session : Session.get("username=?", username)) {
                    source.sendMessage(Component.text(session.toPrintString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                source.sendMessage(Component.text("Failed! " + e.getMessage(), TextColor.color(255, 0, 0)));
            }
        }
        else{
            try {
                for (Session session : Session.get()) {
                    source.sendMessage(Component.text(session.toPrintString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                source.sendMessage(Component.text("Failed! " + e.getMessage(), TextColor.color(255, 0, 0)));
            }
        }
    }
}
