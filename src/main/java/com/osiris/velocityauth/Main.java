package com.osiris.velocityauth;

import com.google.inject.Inject;
import com.osiris.dyml.exceptions.*;
import com.osiris.velocityauth.commands.AdminLoginCommand;
import com.osiris.velocityauth.commands.AdminRegisterCommand;
import com.osiris.velocityauth.commands.LoginCommand;
import com.osiris.velocityauth.commands.RegisterCommand;
import com.osiris.velocityauth.database.Database;
import com.osiris.velocityauth.database.RegisteredUser;
import com.osiris.velocityauth.database.Session;
import com.osiris.velocityauth.perms.NoPermissionPlayer;
import com.osiris.velocityauth.perms.MutablePermissionProvider;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

@Plugin(id = "velocityauth", name = "VelocityAuth", version = "0.4",
        url = "https://github.com/Osiris-Team", description = "Auth manager for velocity.", authors = {"Osiris-Team, HasX"})
public class Main {
    public static Main INSTANCE;

    public final ProxyServer server;
    public final Logger logger;
    public final Path dataDirectory;
    public boolean isWhitelistMode = false;
    public LimboServer limboServer;
    public int sessionMaxHours;
    public List<NoPermissionPlayer> noPermissionPlayers = new CopyOnWriteArrayList<>();

    @Inject
    public Main(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        INSTANCE = this;
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) throws NotLoadedException, YamlReaderException, YamlWriterException, IOException, IllegalKeyException, DuplicateKeyException, IllegalListException, URISyntaxException {
        Config config = new Config();
        if (config.databaseUsername.asString() == null) {
            logger.info("Welcome! Looks like this is your first run.");
            logger.info("This plugin requires access to your SQL database.");
            logger.info("Please enter your SQL database username below and press enter:");
            String username = null;
            while (username == null || username.trim().isEmpty()) {
                username = new Scanner(System.in).nextLine();
            }
            config.databaseUsername.setValues(username);
            config.save();
        }
        if (config.databasePassword.asString() == null) {
            logger.info("Please enter your SQL database password below and press enter:");
            String password = null;
            while (password == null || password.trim().isEmpty()) {
                password = new Scanner(System.in).nextLine();
            }
            config.databasePassword.setValues(password);
            config.save();
        }

        Database.rawUrl = config.databaseRawUrl.asString();
        Database.url = config.databaseUrl.asString();
        Database.username = config.databaseUsername.asString();
        Database.password = config.databasePassword.asString();
        isWhitelistMode = config.whitelistMode.asBoolean();
        sessionMaxHours = config.sessionMaxHours.asInt();
        logger.info("Loaded configuration.");

        Database.create();
        logger.info("Database connected.");


        server.getEventManager().register(this, PreLoginEvent.class, PostOrder.FIRST, e -> {
            try {
                if (isWhitelistMode && !isRegistered(e.getUsername())) {
                    e.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                            Component.text("You must be registered to join this server!")
                    ));
                    logger.info("Blocked connection for " + e.getUsername() + ". Player not registered (whitelist-mode).");
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        server.getEventManager().register(this, ServerPreConnectEvent.class, PostOrder.FIRST, e -> {
            try {
                // Forward to limbo server for login/registration
                // This server allows multiple players with the same username online
                // at the same time and thus is perfect for safe authentication
                // on offline (as well as online) servers.
                if (!isLoggedIn(e.getPlayer().getUsername(), e.getPlayer().getRemoteAddress().getAddress().getHostName())) {
                    e.setResult(ServerPreConnectEvent.ServerResult.allowed(Main.INSTANCE.limboServer.registeredServer));
                    logger.info("Blocked connect to '" + e.getOriginalServer().getServerInfo().getName()
                            + "' and forwarded " + e.getPlayer().getUsername() + " to '" +
                            Main.INSTANCE.limboServer.registeredServer.getServerInfo().getName() + "'. Player not logged in.");
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        server.getEventManager().register(this, PermissionsSetupEvent.class, PostOrder.FIRST, e -> {
            // Called once at permissions init.
            // At this state, the player is not logged in.
            try{
                // Make sure that all permission providers mutable
                MutablePermissionProvider permissionProvider =
                        new MutablePermissionProvider(permission -> e.getSubject().hasPermission(permission));
                e.setProvider(permissionProvider);

                // Remove all permissions of the user, if not logged in
                // and restore them later, when logged in.
                if(e.getSubject() instanceof Player){
                    Player player = (Player) e.getSubject();
                    if (!isLoggedIn(player.getUsername(), player.getRemoteAddress().getAddress().getHostName())){
                        Predicate<String> oldPermissionFunction = permissionProvider.hasPermission;
                        permissionProvider.hasPermission = NoPermissionPlayer.tempPermissionFunction;
                        noPermissionPlayers.add(new NoPermissionPlayer(
                                player,
                                permissionProvider,
                                oldPermissionFunction));
                    }
                }
                // else do nothing, since only relevant for players
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        server.getEventManager().register(this, ServerConnectedEvent.class, PostOrder.FIRST, e -> {
            try {
                int maxSeconds = 60;
                for (int i = maxSeconds; i >= 0; i--) {
                    if (!e.getPlayer().isActive() || isRegistered(e.getPlayer().getUsername())) break;
                    e.getPlayer().sendActionBar(Component.text(i + " seconds remaining to: /register <password> <confirm-password>",
                            TextColor.color(184, 25, 43)));
                    if (i == 0) {
                        e.getPlayer().disconnect(Component.text("Please register within " + maxSeconds + " seconds after joining the server.",
                                TextColor.color(184, 25, 43)));
                    }
                    Thread.sleep(1000);
                }
                for (int i = maxSeconds; i >= 0; i--) {
                    if (!e.getPlayer().isActive() || isLoggedIn(e.getPlayer().getUsername(), e.getPlayer().getRemoteAddress().getAddress().getHostName()))
                        break;
                    e.getPlayer().sendActionBar(Component.text(i + " seconds remaining to: /login <password>", TextColor.color(184, 25, 43)));
                    if (i == 0) {
                        e.getPlayer().disconnect(Component.text("Please login within " + maxSeconds + " seconds after joining the server.",
                                TextColor.color(184, 25, 43)));
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        server.getEventManager().register(this, DisconnectEvent.class, PostOrder.LAST, e -> {
            try {
                long now = System.currentTimeMillis();
                for (Session session : Session.get("username=?", e.getPlayer().getUsername())) {
                    session.isLoggedIn = 0;
                    if (now > session.timestampExpires)
                        Session.remove(session);
                    else
                        Session.update(session);
                }
                noPermissionPlayers.removeIf(perm -> Objects.equals(perm.player.getUniqueId(), e.getPlayer().getUniqueId()));
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

        limboServer = new LimboServer();
        limboServer.start();
        logger.info("Started limbo auth-server (localhost:"+limboServer.port+"/running:"+limboServer.process.isAlive()+").");

        logger.info("Initialised successfully!");
    }

    private boolean isLoggedIn(String username, String ipAddress) throws Exception {
        List<Session> sessions = Session.get("username=? AND ipAddress=?", username, ipAddress);
        if (sessions.isEmpty()) return false;
        Session session = null;
        for (Session s : sessions) {
            if (s.isLoggedIn == 1) {
                session = s;
                break;
            }
        }
        return session != null;
    }

    private Player findPlayerByUsername(String username) {
        Player player = null;
        for (Player p : server.getAllPlayers()) {
            if (Objects.equals(p.getUsername(), username)) {
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
