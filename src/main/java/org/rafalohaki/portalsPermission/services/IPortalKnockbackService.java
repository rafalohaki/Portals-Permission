package org.rafalohaki.portalsPermission.services;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for portal knockback service
 * Interfejs dla serwisu knockbacku portali
 */
public interface IPortalKnockbackService {
    
    /**
     * Applies knockback to player asynchronously
     * Stosuje knockback do gracza asynchronicznie
     * 
     * @param player The player to apply knockback to
     * @param portalLocation The portal location
     * @return CompletableFuture that completes when knockback is applied
     */
    @NotNull CompletableFuture<Void> applyKnockbackAsync(@NotNull Player player, @NotNull Location portalLocation);
    
    /**
     * Calculates knockback vector for player
     * Oblicza wektor knockbacku dla gracza
     * 
     * @param playerLocation The player's location
     * @param portalLocation The portal location
     * @param targetEnvironment The target environment
     * @param strength The knockback strength
     * @param height The knockback height
     * @return The calculated knockback vector
     */
    @NotNull Vector calculateKnockbackVector(@NotNull Location playerLocation, 
                                           @NotNull Location portalLocation,
                                           @Nullable World.Environment targetEnvironment, 
                                           double strength, 
                                           double height);
    
    /**
     * Gets target environment from portal location
     * Pobiera docelowe środowisko z lokalizacji portalu
     * 
     * @param portalLocation The portal location
     * @return The target environment or null if unknown
     */
    @Nullable World.Environment getTargetEnvironmentFromLocation(@NotNull Location portalLocation);
    
    /**
     * Applies damage-based knockback for Paper API 1.21+ compatibility
     * Stosuje knockback oparty na damage dla kompatybilności z Paper API 1.21+
     * 
     * @param player The player to apply knockback to
     * @param knockback The knockback vector
     */
    void applyDamageBasedKnockback(@NotNull Player player, @NotNull Vector knockback);
    
    /**
     * Plays knockback sound for player
     * Odtwarza dźwięk knockbacku dla gracza
     * 
     * @param player The player to play sound for
     */
    void playKnockbackSound(@NotNull Player player);
    
    /**
     * Checks if knockback is enabled in configuration
     * Sprawdza czy knockback jest włączony w konfiguracji
     * 
     * @return true if knockback is enabled, false otherwise
     */
    boolean isKnockbackEnabled();
}