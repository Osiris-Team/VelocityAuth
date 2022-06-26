package com.osiris.velocityauth;

import com.google.inject.Inject;
import com.osiris.dyml.exceptions.*;
import com.osiris.velocityauth.command.AdminLoginCommand;
import com.osiris.velocityauth.command.AdminRegisterCommand;
import com.osiris.velocityauth.command.LoginCommand;
import com.osiris.velocityauth.command.RegisterCommand;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
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


        server.getEventManager().register(this, LoginEvent.class, PostOrder.FIRST, e -> {
            try {
                if(isWhitelistMode && !isRegistered(e.getPlayer().getUsername())){
                    e.setResult(ResultedEvent.ComponentResult.denied(
                            Component.text("You must be registered to join this server!")
                    ));
                    logger.info("Blocked connection for "+e.getPlayer().getUsername()+". Player not registered.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        server.getEventManager().register(this, ServerConnectedEvent.class, PostOrder.FIRST, e -> {
            try{
                int maxSeconds = 60;
                for (int i = maxSeconds; i >= 0; i--) {
                    if(!e.getPlayer().isActive() || isRegistered(e.getPlayer().getUsername())) break;
                    e.getPlayer().sendActionBar(Component.text(i+" seconds remaining to: /register <password> <confirm-password>",
                            TextColor.color(184, 25, 43)));
                    if(i == 0){
                        e.getPlayer().disconnect(Component.text("Please register within "+maxSeconds+" seconds after joining the server.",
                                TextColor.color(184, 25, 43)));
                    }
                    Thread.sleep(1000);
                }
                for (int i = maxSeconds; i >= 0; i--) {
                    if(!e.getPlayer().isActive() || isLoggedIn(e.getPlayer().getUsername())) break;
                    e.getPlayer().sendActionBar(Component.text(i+" seconds remaining to: /login <password>", TextColor.color(184, 25, 43)));
                    if(i == 0){
                        e.getPlayer().disconnect(Component.text("Please login within "+maxSeconds+" seconds after joining the server.",
                                TextColor.color(184, 25, 43)));
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        server.getEventManager().register(this, DisconnectEvent.class, PostOrder.LAST, e -> {
            try{
                RegisteredUser registeredUser = RegisteredUser.get("username=?", e.getPlayer().getUsername()).get(0);
                registeredUser.isLoggedIn = 0;
                RegisteredUser.update(registeredUser);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        logger.info("Listeners registered.");

        new AdminRegisterCommand().register();
        new AdminLoginCommand().register();
        new RegisterCommand().register();
        new LoginCommand().register();
        logger.info("Commands registered.");
    }

    private boolean isLoggedIn(String username) throws Exception {
        List<RegisteredUser> registeredUsers = RegisteredUser.get("username=?", username);
        if(registeredUsers.isEmpty()) return false;
        return registeredUsers.get(0).isLoggedIn == 1;
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

    /**
     * Checks whether the provided username exists in the database.
     */
    public boolean isRegistered(String username) throws Exception {
        return !RegisteredUser.get("username=?", username).isEmpty();
    }


}
