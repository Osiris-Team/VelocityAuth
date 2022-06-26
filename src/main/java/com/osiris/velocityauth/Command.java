package com.osiris.velocityauth;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.SimpleCommand;

public interface Command extends SimpleCommand {
    String command();

    String[] aliases();

    String permission();

    default void register() {
        CommandManager commandManager = Main.INSTANCE.server.getCommandManager();
        commandManager.register(commandManager.metaBuilder(command()).aliases(aliases()).build(), this);
    }

    @Override
    default boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(permission());
    }

    /**
     * Actual execution code in here (independent from velocity api).
     *
     * @param args arguments.
     * @return error message, null if no error.
     * @throws Exception if something went really wrong.
     */
    String execute(Object... args) throws Exception;
}
