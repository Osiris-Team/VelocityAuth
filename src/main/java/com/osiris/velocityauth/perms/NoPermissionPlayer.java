package com.osiris.velocityauth.perms;

import com.osiris.velocityauth.commands.LoginCommand;
import com.osiris.velocityauth.commands.RegisterCommand;
import com.velocitypowered.api.proxy.Player;

import java.util.Objects;
import java.util.function.Predicate;

public class NoPermissionPlayer {
    public static final String registerPermission = new RegisterCommand().permission();
    public static final String loginPermission = new LoginCommand().permission();
    /**
     * Temporary permission function that gets set if the player is not logged in.
     * Only allows /register and /login commands of VelocityAuth to be executed.
     */
    public static final Predicate<String> tempPermissionFunction =
            permission -> Objects.equals(registerPermission, permission) ||
                    Objects.equals(loginPermission, permission);
    /**
     * Player that has blocked permissions.
     */
    public Player player;
    public MutablePermissionProvider permissionProvider;
    /**
     * Old permission function that is used,
     * to restore permissions after a successful login.
     */
    public Predicate<String> oldPermissionFunction;

    public NoPermissionPlayer(Player player, MutablePermissionProvider permissionProvider, Predicate<String> oldPermissionFunction) {
        this.player = player;
        this.permissionProvider = permissionProvider;
        this.oldPermissionFunction = oldPermissionFunction;
    }
}
