package com.osiris.velocityauth;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Command extends SimpleCommand {
    String command();
    String[] aliases();
    String permission();

    default void register(){
        CommandManager commandManager = Main.INSTANCE.server.getCommandManager();
        commandManager.register(commandManager.metaBuilder(command()).aliases(aliases()).build(), this);
    }

    @Override
    default boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(permission());
    }
}
