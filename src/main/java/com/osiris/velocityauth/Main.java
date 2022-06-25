package com.osiris.velocityauth;

import com.google.inject.Inject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.osiris.dyml.exceptions.*;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Scanner;

@Plugin(id = "velocityauth", name = "VelocityAuth", version = "0.1",
        url = "https://github.com/Osiris-Team", description = "Auth manager for velocity.", authors = {"Osiris-Team"})
public class Main {
    public static Main INSTANCE;

    public final ProxyServer server;
    public final Logger logger;
    public boolean isWhitelistMode = false;
    public final Path dataDirectory;

    @Inject
    public Main(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        INSTANCE = this;
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) throws NotLoadedException, YamlReaderException, YamlWriterException, IOException, IllegalKeyException, DuplicateKeyException, IllegalListException {
        Config config = new Config();
        if(config.databaseUsername.asString() == null){
            logger.info("Welcome! Looks like this is your first run.");
            logger.info("This plugin requires access to your SQL database.");
            logger.info("Please enter your SQL database username below and press enter:");
            String username = null;
            while (username == null || username.trim().isEmpty()){
                username = new Scanner(System.in).nextLine();
            }
            config.databaseUsername.setValues(username);
            config.save();
        }
        if(config.databasePassword.asString() == null){
            logger.info("Please enter your SQL database password below and press enter:");
            String password = null;
            while (password == null || password.trim().isEmpty()){
                password = new Scanner(System.in).nextLine();
            }
            config.databasePassword.setValues(password);
            config.save();
        }

        Database.username = config.databaseUsername.asString();
        Database.password = config.databasePassword.asString();
        isWhitelistMode = config.whitelistMode.asBoolean();
        logger.info("Loaded configuration.");


        Database.create();
        logger.info("Database connected.");


        if(server.getConfiguration().isOnlineMode())
            server.getEventManager().register(this, LoginEvent.class, PostOrder.FIRST, e -> {
                try {
                    checkPlayer(e.getPlayer());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        else
            server.getEventManager().register(this, PreLoginEvent.class, PostOrder.FIRST, e -> {
                try {
                    Player player = findPlayerByUsername(e.getUsername());
                    if(player == null) throw new NullPointerException("Failed to find player" +
                            " named '"+e.getUsername()+"', thus failed to block connection!");
                    checkPlayer(player);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        logger.info("Listeners registered.");


        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder("velocityauth register")
                .build(), new RegisterUserCommand());
        server.getCommandManager().register(server.getCommandManager()
                .metaBuilder("velocityauth login")
                .build(), new LoginUserCommand());
        logger.info("Commands registered.");
    }

    private void checkPlayer(Player player) throws Exception {
        if(isWhitelistMode && !isUsernameInTable(player.getUsername())){
            player.disconnect(Component.text("You must be registered to join this server!"));
            logger.info("Blocked connection for "+player.getUsername()+". Player not registered.");
        }
    }

    private Player findPlayerByUsername(String username) {
        Player player = null;
        for (Player p : server.getAllPlayers()) {
            if(Objects.equals(p.getUsername(), username)){
                player = p;
                break;
            }
        }
        return player;
    }

    public boolean isUsernameInTable(String username) throws Exception {
        return !RegisteredUser.get("username=?", username).isEmpty();
    }


}
