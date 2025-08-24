package org.rafalohaki.portalsPermission.services.impl;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rafalohaki.portalsPermission.services.ISoundService;

/**
 * Implementation of sound service
 * Implementacja serwisu dźwiękowego
 */
public class SoundService implements ISoundService {
    
    @Override
    @NotNull
    public Object getSoundFromName(@NotNull String soundName) {
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown sound: " + soundName, e);
        }
    }
    
    @Override
    public void playSound(@NotNull Player player, @NotNull Location location, @NotNull Object sound, float volume, float pitch) {
        if (sound instanceof Sound) {
            player.playSound(location, (Sound) sound, volume, pitch);
        }
    }
}