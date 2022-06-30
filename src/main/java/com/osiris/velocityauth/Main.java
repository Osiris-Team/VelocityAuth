package com.osiris.velocityauth;

import com.google.inject.Inject;
import com.osiris.dyml.exceptions.*;
import com.osiris.velocityauth.commands.*;
import com.osiris.velocityauth.database.BannedUser;
import com.osiris.velocityauth.database.Database;
import com.osiris.velocityauth.database.RegisteredUser;
import com.osiris.velocityauth.database.Session;
import com.osiris.velocityauth.perms.MutablePermissionProvider;
import com.osiris.velocityauth.perms.NoPermissionPlayer;
import com.osiris.velocityauth.utils.UtilsTime;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Predicate;

@Plugin(id = "velocityauth", name = "VelocityAuth", version = "0.4",
        url = "https://github.com/Osiris-Team", description = "Auth manager for velocity.", authors = {"Osiris-Team", "RGoth", "HasX"})
public class Main {
    public static Main INSTANCE;

    public final ProxyServer proxy;
    public final Logger logger;
    public final Path dataDirectory;
    public boolean isWhitelistMode = false;
    public LimboServer limboServer;
    public int sessionMaxHours;
    public List<NoPermissionPlayer> noPermissionPlayers = new CopyOnWriteArrayList<>();
    public RegisteredServer authServer;
    public int minFailedLoginsForBan;
    public int failedLoginBanTimeSeconds;
    public int minPasswordLength;
    public ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    @Inject
    public Main(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        INSTANCE = this;
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event)
            throws NotLoadedException, YamlReaderException, YamlWriterException,
            IOException, IllegalKeyException, DuplicateKeyException,
            IllegalListException, URISyntaxException {
        long now = System.currentTimeMillis();
        long start = System.currentTimeMillis();
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
        minFailedLoginsForBan = config.minFailedLoginsForBan.asInt();
        failedLoginBanTimeSeconds = config.failedLoginBanTime.asInt();
        minPasswordLength = config.minPasswordLength.asInt();
        logger.info("Loaded configuration. " + (System.currentTimeMillis() - now) + "ms");
        now = System.currentTimeMillis();

        if (config.debugAuthServerName.asString() != null) {
            authServer = proxy.getServer(config.debugAuthServerName.asString()).get();
            logger.info("Using alternative/custom auth-server (" + authServer.getServerInfo().getAddress().toString() +
                    "). " + (System.currentTimeMillis() - now) + "ms");
        } else {
            limboServer = new LimboServer();
            limboServer.start();
            authServer = limboServer.registeredServer;
            logger.info("Started limbo auth-server (localhost:" + limboServer.port + "/running:" + limboServer.process.isAlive() + "). "
                    + (System.currentTimeMillis() - now) + "ms");
        }
        now = System.currentTimeMillis();

        Database.create();
        logger.info("Database connected. " + (System.currentTimeMillis() - now) + "ms");
        now = System.currentTimeMillis();

        proxy.getEventManager().register(this, PreLoginEvent.class, PostOrder.FIRST, e -> {
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
        proxy.getEventManager().register(this, LoginEvent.class, PostOrder.FIRST, e -> {
            try {
                if(BannedUser.isBanned(getPlayerIp(e.getPlayer()), e.getPlayer().getUniqueId().toString())){
                    BannedUser bannedUser = BannedUser.getBanned(getPlayerIp(e.getPlayer()), e.getPlayer().getUniqueId().toString());
                    Component message = new BanCommand().getBanText(bannedUser.timestampExpires, bannedUser.reason);
                    e.getPlayer().disconnect(message);
                    logger.info("Blocked connection for " + e.getPlayer().getUsername()+"/"+e.getPlayer().getUniqueId().toString()
                            + ". Player is banned for "+new UtilsTime().getFormattedString(bannedUser.timestampExpires)+".");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        proxy.getEventManager().register(this, ServerPreConnectEvent.class, PostOrder.FIRST, e -> {
            try {
                // Forward to limbo server for login/registration
                // This server allows multiple players with the same username online
                // at the same time and thus is perfect for safe authentication
                // on offline (as well as online) servers.
                if (!hasValidSession(e.getPlayer())) {
                    e.setResult(ServerPreConnectEvent.ServerResult.allowed(authServer));
                    logger.info("Blocked connect to '" + e.getOriginalServer().getServerInfo().getName()
                            + "' and forwarded " + e.getPlayer().getUsername() + " to '" +
                            authServer.getServerInfo().getName() + "'. Player not logged in.");
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        proxy.getEventManager().register(this, PermissionsSetupEvent.class, PostOrder.FIRST, e -> {
            // Called once at permissions init for anything that can have permissions
            // like the VelocityConsole or the Player object.
            // At this state, the player is not logged in.
            try {
                // Remove all permissions of the user, if not logged in
                // and restore them later, when logged in.
                if (e.getSubject() instanceof Player) {

                    // Make sure that all permission providers for players are mutable
                    MutablePermissionProvider permissionProvider =
                            new MutablePermissionProvider(permission -> e.getSubject().hasPermission(permission));
                    e.setProvider(permissionProvider);

                    Player player = (Player) e.getSubject();
                    if (!hasValidSession(player)) {
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
        proxy.getEventManager().register(this, ServerConnectedEvent.class, PostOrder.FIRST, e -> {
            executor.execute(() -> {
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
                        if (!e.getPlayer().isActive() || hasValidSession(e.getPlayer()))
                            break;
                        e.getPlayer().sendActionBar(Component.text(i + " seconds remaining to: /login <password>", TextColor.color(184, 25, 43)));
                        if (i == 0) {
                            e.getPlayer().disconnect(Component.text("Please login within " + maxSeconds + " seconds after joining the server.",
                                    TextColor.color(184, 25, 43)));
                        }
                        Thread.sleep(1000);
                    }

                    Session session = getValidSession(e.getPlayer());
                    if(session != null){
                        session.isActive = 1;
                        Session.update(session);
                    }
                } catch (InterruptedException ignored) {
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            });
        });
        proxy.getEventManager().register(this, DisconnectEvent.class, PostOrder.LAST, e -> {
            try {
                long now2 = System.currentTimeMillis();
                for (Session session : Session.get("username=?", e.getPlayer().getUsername())) {
                    session.isActive = 0;
                    if (now2 > session.timestampExpires)
                        Session.remove(session);
                    else
                        Session.update(session);
                }
                noPermissionPlayers.removeIf(perm -> Objects.equals(perm.player.getUniqueId(), e.getPlayer().getUniqueId()));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        logger.info("Listeners registered. " + (System.currentTimeMillis() - now) + "ms");
        now = System.currentTimeMillis();

        new AdminRegisterCommand().register();
        new AdminUnRegisterCommand().register();
        new AdminLoginCommand().register();
        new RegisterCommand().register();
        new LoginCommand().register();
        new BanCommand().register();
        new UnbanCommand().register();
        new ListSessionsCommand().register();
        new ClearSessionsCommand().register();
        logger.info("Commands registered. " + (System.currentTimeMillis() - now) + "ms");

        logger.info("Initialised successfully! " + (System.currentTimeMillis() - start) + "ms");
    }

    public String getPlayerIp(Player player){
        return player.getRemoteAddress().getAddress().getHostAddress();
    }

    public boolean hasValidSession(Player player) throws Exception {
        return getValidSession(player) != null;
    }
    public Session getValidSession(Player player) throws Exception {
        return getValidSession(player.getUsername(), getPlayerIp(player));
    }

    public boolean hasValidSession(String username, String ipAddress) throws Exception {
        return getValidSession(username, ipAddress) != null;
    }
    /**
     * Returns true, if this username/ip-address has no session, aka
     * the player never logged in, or another older session expired.
     */
    public Session getValidSession(String username, String ipAddress) throws Exception {
        List<Session> sessions = Session.get("username=? AND ipAddress=?", username, ipAddress);
        if (sessions.isEmpty()) {
            return null;
        }
        if(sessions.size() > 1) throw new RuntimeException("Cannot have multiple("+sessions.size()
                +") sessions for one username("+username+")/ip-address("+ipAddress+").");
        return sessions.get(0);
    }

    public Player findPlayerByUsername(String username) {
        Player player = null;
        for (Player p : proxy.getAllPlayers()) {
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
