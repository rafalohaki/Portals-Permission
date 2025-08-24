package org.rafalohaki.portalsPermission.services;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPortalEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for portal permission checking service
 * Interfejs dla serwisu sprawdzania uprawnień portali
 */
public interface IPortalPermissionChecker {
    
    /**
     * Checks if player has permission to use the portal
     * Sprawdza czy gracz ma uprawnienia do użycia portalu
     * 
     * @param player The player to check
     * @param event The portal event
     * @return true if player has permission, false otherwise
     */
    boolean hasPortalPermission(@NotNull Player player, @NotNull PlayerPortalEvent event);
    
    /**
     * Checks if player has bypass permission
     * Sprawdza czy gracz ma uprawnienia do ominięcia
     * 
     * @param player The player to check
     * @return true if player has bypass permission, false otherwise
     */
    boolean hasBypassPermission(@NotNull Player player);
    
    /**
     * Gets the target environment for the portal
     * Pobiera docelowe środowisko dla portalu
     * 
     * @param event The portal event
     * @return The target environment or null if unknown
     */
    @Nullable World.Environment getTargetEnvironment(@NotNull PlayerPortalEvent event);
    
    /**
     * Gets the portal type environment for permission checking
     * Pobiera środowisko typu portalu dla sprawdzania uprawnień
     * 
     * @param event The portal event
     * @return The portal type environment for permission checking
     */
    @Nullable World.Environment getPortalTypeEnvironment(@NotNull PlayerPortalEvent event);
    
    /**
     * Gets required permission for portal type
     * Pobiera wymagane uprawnienie dla typu portalu
     * 
     * @param environment The target environment
     * @return The required permission or null if not found
     */
    @Nullable String getRequiredPermission(@Nullable World.Environment environment);
    
    /**
     * Gets message key for portal type
     * Pobiera klucz wiadomości dla typu portalu
     * 
     * @param environment The target environment
     * @return The message key
     */
    @NotNull String getMessageKey(@Nullable World.Environment environment);
    
    /**
     * Checks if portal type is blocked in configuration
     * Sprawdza czy typ portalu jest zablokowany w konfiguracji
     * 
     * @param environment The target environment
     * @return true if portal type is blocked, false otherwise
     */
    boolean isPortalTypeBlocked(@Nullable World.Environment environment);
}