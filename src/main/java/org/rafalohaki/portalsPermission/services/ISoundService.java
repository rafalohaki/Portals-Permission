package org.rafalohaki.portalsPermission.services;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Service interface for sound operations
 * Interfejs serwisu dla operacji dźwiękowych
 */
public interface ISoundService {
    
    /**
     * Gets Sound from name
     * Pobiera Sound z nazwy
     * 
     * @param soundName The sound name
     * @return The Sound object
     * @throws IllegalArgumentException if sound not found
     */
    @NotNull Object getSoundFromName(@NotNull String soundName);
    
    /**
     * Plays sound for player at location
     * Odtwarza dźwięk dla gracza w lokalizacji
     * 
     * @param player The player
     * @param location The location
     * @param sound The sound
     * @param volume The volume
     * @param pitch The pitch
     */
    void playSound(@NotNull Player player, @NotNull Location location, @NotNull Object sound, float volume, float pitch);
}