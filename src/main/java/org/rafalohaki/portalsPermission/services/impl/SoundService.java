package org.rafalohaki.portalsPermission.services.impl;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rafalohaki.portalsPermission.services.ISoundService;

import java.util.logging.Logger;

/**
 * Modern implementation of sound service using Paper 1.21+ Registry API
 * Nowoczesna implementacja serwisu dźwiękowego używająca Paper 1.21+ Registry API
 */
public class SoundService implements ISoundService {
    
    private static final Logger LOGGER = Logger.getLogger(SoundService.class.getName());
    
    /**
     * Gets Sound from name using modern Registry API instead of deprecated valueOf()
     * Pobiera Sound z nazwy używając nowoczesnego Registry API zamiast deprecated valueOf()
     * 
     * @param soundName The sound name to resolve
     * @return The Sound object from registry
     * @throws IllegalArgumentException if sound not found in registry
     */
    @Override
    @NotNull
    public Object getSoundFromName(@NotNull String soundName) {
        if (soundName == null || soundName.trim().isEmpty()) {
            throw new IllegalArgumentException("Sound name cannot be null or empty");
        }
        
        try {
            // Create NamespacedKey for the sound
            NamespacedKey soundKey = NamespacedKey.minecraft(soundName.toLowerCase());
            
            // Use modern Registry API instead of deprecated Sound.valueOf()
            Sound sound = Registry.SOUNDS.get(soundKey);
            
            if (sound != null) {
                return sound;
            }
            
            // Sound not found in registry
            LOGGER.warning("Sound not found in registry: " + soundName);
            throw new IllegalArgumentException("Unknown sound: " + soundName + ". Sound not found in registry.");
            
        } catch (Exception e) {
            LOGGER.warning("Failed to resolve sound: " + soundName + ", error: " + e.getMessage());
            throw new IllegalArgumentException("Failed to resolve sound: " + soundName, e);
        }
    }
    
    /**
     * Plays sound for player at location with proper null safety and error handling
     * Odtwarza dźwięk dla gracza w lokalizacji z właściwym zabezpieczeniem przed null i obsługą błędów
     * 
     * @param player The player to play sound for
     * @param location The location to play sound at
     * @param sound The sound object to play
     * @param volume The volume level (0.0-1.0)
     * @param pitch The pitch level (0.0-2.0)
     */
    @Override
    public void playSound(@NotNull Player player, @NotNull Location location, @NotNull Object sound, float volume, float pitch) {
        if (player == null) {
            LOGGER.warning("Cannot play sound: player is null");
            return;
        }
        
        if (location == null) {
            LOGGER.warning("Cannot play sound: location is null");
            return;
        }
        
        if (sound == null) {
            LOGGER.warning("Cannot play sound: sound object is null");
            return;
        }
        
        // Validate volume and pitch ranges
        if (volume < 0.0f || volume > 1.0f) {
            LOGGER.warning("Invalid volume: " + volume + ", clamping to valid range [0.0-1.0]");
            volume = Math.max(0.0f, Math.min(1.0f, volume));
        }
        
        if (pitch < 0.0f || pitch > 2.0f) {
            LOGGER.warning("Invalid pitch: " + pitch + ", clamping to valid range [0.0-2.0]");
            pitch = Math.max(0.0f, Math.min(2.0f, pitch));
        }
        
        try {
            if (sound instanceof Sound) {
                player.playSound(location, (Sound) sound, volume, pitch);
            } else {
                LOGGER.warning("Sound object is not an instance of org.bukkit.Sound: " + sound.getClass().getName());
            }
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Invalid sound parameters for player " + player.getName() + ": " + e.getMessage());
        } catch (IllegalStateException e) {
            LOGGER.warning("Player " + player.getName() + " is not in valid state for sound playback: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Unexpected error playing sound for player " + player.getName() + ": " + e.getMessage());
        }
    }
}