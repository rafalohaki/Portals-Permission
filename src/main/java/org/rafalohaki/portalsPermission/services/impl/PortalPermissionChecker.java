package org.rafalohaki.portalsPermission.services.impl;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPortalEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rafalohaki.portalsPermission.managers.ConfigManager;
import org.rafalohaki.portalsPermission.services.IPortalPermissionChecker;

/**
 * Implementation of portal permission checking service
 * Implementacja serwisu sprawdzania uprawnień portali
 */
public class PortalPermissionChecker implements IPortalPermissionChecker {
    
    private final ConfigManager configManager;
    
    /**
     * Constructor for PortalPermissionChecker
     * Konstruktor dla PortalPermissionChecker
     * 
     * @param configManager The configuration manager
     */
    public PortalPermissionChecker(@NotNull ConfigManager configManager) {
        this.configManager = configManager;
    }
    
    /**
     * Checks if player has permission to use the portal
     * Sprawdza czy gracz ma uprawnienia do użycia portalu
     * 
     * @param player The player to check
     * @param event The portal event
     * @return true if player has permission, false otherwise
     */
    @Override
    public boolean hasPortalPermission(@NotNull Player player, @NotNull PlayerPortalEvent event) {
        if (hasBypassPermission(player)) {
            return true;
        }
        
        // Use portal type environment for permission checking, not target environment
        World.Environment portalTypeEnvironment = getPortalTypeEnvironment(event);
        
        if (isPortalTypeBlocked(portalTypeEnvironment)) {
            return false;
        }
        
        String requiredPermission = getRequiredPermission(portalTypeEnvironment);
        return requiredPermission == null || player.hasPermission(requiredPermission);
    }
    
    /**
     * Checks if player has bypass permission
     * Sprawdza czy gracz ma uprawnienia do ominięcia
     * 
     * @param player The player to check
     * @return true if player has bypass permission, false otherwise
     */
    @Override
    public boolean hasBypassPermission(@NotNull Player player) {
        return player.hasPermission("portals.bypass");
    }
    
    /**
     * Gets the target environment for the portal
     * Pobiera docelowe środowisko dla portalu
     * 
     * @param event The portal event
     * @return The target environment or null if unknown
     */
    @Override
    @Nullable
    public World.Environment getTargetEnvironment(@NotNull PlayerPortalEvent event) {
        Material portalMaterial = event.getFrom().getBlock().getType();
        
        switch (portalMaterial) {
            case NETHER_PORTAL:
                return event.getFrom().getWorld().getEnvironment() == World.Environment.NETHER 
                    ? World.Environment.NORMAL 
                    : World.Environment.NETHER;
            case END_PORTAL:
                return World.Environment.THE_END;
            case END_GATEWAY:
                return event.getFrom().getWorld().getEnvironment() == World.Environment.THE_END 
                    ? World.Environment.NORMAL 
                    : World.Environment.THE_END;
            default:
                return null;
        }
    }
    
    /**
     * Gets the portal type environment for permission checking
     * Pobiera środowisko typu portalu dla sprawdzania uprawnień
     * 
     * @param event The portal event
     * @return The portal type environment for permission checking
     */
    @Nullable
    public World.Environment getPortalTypeEnvironment(@NotNull PlayerPortalEvent event) {
        Material portalMaterial = event.getFrom().getBlock().getType();
        
        switch (portalMaterial) {
            case NETHER_PORTAL:
                return World.Environment.NETHER; // Always check nether permission for nether portals
            case END_PORTAL:
            case END_GATEWAY:
                return World.Environment.THE_END; // Always check end permission for end portals
            default:
                return World.Environment.NORMAL; // Custom portals use normal/custom permission
        }
    }
    
    /**
     * Gets required permission for portal type
     * Pobiera wymagane uprawnienie dla typu portalu
     * 
     * @param environment The target environment
     * @return The required permission or null if not found
     */
    @Override
    @Nullable
    public String getRequiredPermission(@Nullable World.Environment environment) {
        if (environment == null) {
            return null;
        }
        
        switch (environment) {
            case NETHER:
                return "portals.nether";
            case THE_END:
                return "portals.end";
            case NORMAL:
                return "portals.custom";
            default:
                return null;
        }
    }
    
    /**
     * Gets message key for portal type
     * Pobiera klucz wiadomości dla typu portalu
     * 
     * @param environment The target environment
     * @return The message key
     */
    @Override
    @NotNull
    public String getMessageKey(@Nullable World.Environment environment) {
        if (environment == null) {
            return "no_permission_custom";
        }
        
        switch (environment) {
            case NETHER:
                return "no_permission_nether";
            case THE_END:
                return "no_permission_end";
            case NORMAL:
                return "no_permission_custom";
            default:
                return "no_permission_custom";
        }
    }
    
    /**
     * Checks if portal type is blocked in configuration
     * Sprawdza czy typ portalu jest zablokowany w konfiguracji
     * 
     * @param environment The target environment
     * @return true if portal type is blocked, false otherwise
     */
    @Override
    public boolean isPortalTypeBlocked(@Nullable World.Environment environment) {
        if (environment == null) {
            return false;
        }
        
        switch (environment) {
            case NETHER:
                return configManager.isNetherBlocked();
            case THE_END:
                return configManager.isEndBlocked();
            case NORMAL:
            case CUSTOM:
                return configManager.isCustomBlocked();
            default:
                return false;
        }
    }
}