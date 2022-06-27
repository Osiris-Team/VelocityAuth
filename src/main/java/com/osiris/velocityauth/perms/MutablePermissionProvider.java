package com.osiris.velocityauth.perms;

import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.PermissionSubject;
import com.velocitypowered.api.permission.Tristate;

import java.util.function.Predicate;

public class MutablePermissionProvider implements PermissionProvider {

    public Predicate<String> hasPermission;

    public MutablePermissionProvider(Predicate<String> hasPermission) {
        this.hasPermission = hasPermission;
    }

    /**
     * This permission function wraps around {@link #hasPermission}
     * and thus its logic is completely mutable.
     */
    private final PermissionFunction permissionFunction = new PermissionFunction() {
        @Override
        public Tristate getPermissionValue(String permission) {
            return hasPermission.test(permission) ? Tristate.TRUE : Tristate.FALSE;
        }
    };

    @Override
    public PermissionFunction createFunction(PermissionSubject subject) {
        return permissionFunction;
    }
}
