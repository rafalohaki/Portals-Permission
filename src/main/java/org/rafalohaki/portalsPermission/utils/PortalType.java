package org.rafalohaki.portalsPermission.utils;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Sealed class representing different portal types using modern Java 21 features
 * Sealed class reprezentująca różne typy portali używając nowoczesnych funkcji Java 21
 */
public sealed interface PortalType permits PortalType.NetherPortal, PortalType.EndPortal, PortalType.CustomPortal {
    
    /**
     * Gets the permission key for this portal type
     * Pobiera klucz uprawnień dla tego typu portalu
     */
    @NotNull String getPermissionKey();
    
    /**
     * Gets the message key for this portal type
     * Pobiera klucz wiadomości dla tego typu portalu
     */
    @NotNull String getMessageKey();
    
    /**
     * Creates a PortalType from World.Environment
     * Tworzy PortalType z World.Environment
     */
    static @Nullable PortalType fromEnvironment(@Nullable World.Environment environment) {
        return switch (environment) {
            case null -> null;
            case NETHER -> new NetherPortal();
            case THE_END -> new EndPortal();
            case NORMAL, CUSTOM -> new CustomPortal();
        };
    }
    
    /**
     * Nether portal implementation
     * Implementacja portalu do Netheru
     */
    record NetherPortal() implements PortalType {
        @Override
        public @NotNull String getPermissionKey() {
            return "nether";
        }
        
        @Override
        public @NotNull String getMessageKey() {
            return "no_permission_nether";
        }
    }
    
    /**
     * End portal implementation
     * Implementacja portalu do Endu
     */
    record EndPortal() implements PortalType {
        @Override
        public @NotNull String getPermissionKey() {
            return "end";
        }
        
        @Override
        public @NotNull String getMessageKey() {
            return "no_permission_end";
        }
    }
    
    /**
     * Custom portal implementation
     * Implementacja portalu niestandardowego
     */
    record CustomPortal() implements PortalType {
        @Override
        public @NotNull String getPermissionKey() {
            return "custom";
        }
        
        @Override
        public @NotNull String getMessageKey() {
            return "no_permission_custom";
        }
    }
}