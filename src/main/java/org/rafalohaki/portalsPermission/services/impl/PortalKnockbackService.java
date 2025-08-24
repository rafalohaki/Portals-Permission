package org.rafalohaki.portalsPermission.services.impl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rafalohaki.portalsPermission.managers.ConfigManager;
import org.rafalohaki.portalsPermission.services.IPortalKnockbackService;
import org.rafalohaki.portalsPermission.services.ISoundService;

import java.util.concurrent.CompletableFuture;

/**
 * Implementation of portal knockback service
 * Implementacja serwisu knockbacku portali
 */
public class PortalKnockbackService implements IPortalKnockbackService {
    
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final ISoundService soundService;
    
    /**
     * Constructor for PortalKnockbackService
     * Konstruktor dla PortalKnockbackService
     * 
     * @param plugin The plugin instance
     * @param configManager The configuration manager
     * @param soundService The sound service
     */
    public PortalKnockbackService(@NotNull JavaPlugin plugin, @NotNull ConfigManager configManager, @NotNull ISoundService soundService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.soundService = soundService;
    }
    
    @Override
    @NotNull
    public CompletableFuture<Void> applyKnockbackAsync(@NotNull Player player, @NotNull Location portalLocation) {
        return CompletableFuture.runAsync(() -> {
            if (!isKnockbackEnabled()) {
                return;
            }
            
            World.Environment targetEnvironment = getTargetEnvironmentFromLocation(portalLocation);
            double strength = configManager.getKnockbackStrength();
            double height = configManager.getKnockbackHeight();
            
            Vector knockback = calculateKnockbackVector(
                player.getLocation(), 
                portalLocation, 
                targetEnvironment, 
                strength, 
                height
            );
            
            // Apply knockback on main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                applyDamageBasedKnockback(player, knockback);
                playKnockbackSound(player);
            });
        });
    }
    
    @Override
    @NotNull
    public Vector calculateKnockbackVector(@NotNull Location playerLocation, 
                                         @NotNull Location portalLocation,
                                         @Nullable World.Environment targetEnvironment, 
                                         double strength, 
                                         double height) {
        // Calculate direction from portal center to player (push away from portal)
        Vector knockbackDirection = playerLocation.toVector().subtract(portalLocation.toVector());
        
        // If player is at the exact same location as portal, use default direction
        if (knockbackDirection.lengthSquared() < 0.001) {
            knockbackDirection = new Vector(0, 0, 1); // Default direction (south)
        } else {
            // Normalize the direction vector
            knockbackDirection.normalize();
            
            // Make knockback more horizontal by reducing Y component
            // This ensures player gets pushed to the nearest edge rather than straight up
            knockbackDirection.setY(knockbackDirection.getY() * 0.2);
            
            // Re-normalize after Y adjustment to maintain consistent strength
            double horizontalLength = Math.sqrt(knockbackDirection.getX() * knockbackDirection.getX() + 
                                               knockbackDirection.getZ() * knockbackDirection.getZ());
            if (horizontalLength > 0.001) {
                // Scale horizontal components to maintain unit vector length
                double scale = 1.0 / Math.sqrt(knockbackDirection.lengthSquared());
                knockbackDirection.multiply(scale);
            }
        }
        
        // Apply strength multiplier
        knockbackDirection.multiply(strength);
        
        // Add minimum vertical component for knockback effect
        knockbackDirection.setY(Math.max(knockbackDirection.getY(), height * 0.3));
        
        // Environment-specific adjustments
        if (targetEnvironment == World.Environment.NETHER) {
            // Stronger horizontal knockback for Nether portals
            knockbackDirection.multiply(1.2);
            knockbackDirection.setY(knockbackDirection.getY() + height * 0.2);
        } else if (targetEnvironment == World.Environment.THE_END) {
            // Moderate horizontal with some vertical for End portals
            knockbackDirection.multiply(1.1);
            knockbackDirection.setY(knockbackDirection.getY() + height * 0.4);
        } else {
            // Normal world - balanced knockback
            knockbackDirection.setY(knockbackDirection.getY() + height * 0.3);
        }
        
        return knockbackDirection;
    }
    
    @Override
    @Nullable
    public World.Environment getTargetEnvironmentFromLocation(@NotNull Location portalLocation) {
        Material portalMaterial = portalLocation.getBlock().getType();
        
        switch (portalMaterial) {
            case NETHER_PORTAL:
                return portalLocation.getWorld().getEnvironment() == World.Environment.NETHER 
                    ? World.Environment.NORMAL 
                    : World.Environment.NETHER;
            case END_PORTAL:
                return World.Environment.THE_END;
            case END_GATEWAY:
                return portalLocation.getWorld().getEnvironment() == World.Environment.THE_END 
                    ? World.Environment.NORMAL 
                    : World.Environment.THE_END;
            default:
                return null;
        }
    }
    
    @Override
    public void applyDamageBasedKnockback(@NotNull Player player, @NotNull Vector knockback) {
        // Modern Paper API 1.21+ compatible knockback using velocity
        player.setVelocity(knockback);
        
        // Alternative method for older versions or if velocity doesn't work
        // Use a small amount of damage to trigger knockback
        if (knockback.lengthSquared() > 0) {
            player.damage(0.01); // Minimal damage to trigger knockback mechanics
            player.setVelocity(knockback);
        }
    }
    
    @Override
    public void playKnockbackSound(@NotNull Player player) {
        if (!configManager.isKnockbackSoundEnabled()) {
            return;
        }
        
        try {
            String soundName = configManager.getKnockbackSoundType();
            if (soundName == null || soundName.trim().isEmpty()) {
                return;
            }
            
            Object sound = soundService.getSoundFromName(soundName);
            float volume = configManager.getKnockbackSoundVolume();
            float pitch = configManager.getKnockbackSoundPitch();
            
            soundService.playSound(player, player.getLocation(), sound, volume, pitch);
        } catch (Exception e) {
            // Fallback to default sound if configured sound is invalid
            try {
                Object fallbackSound = soundService.getSoundFromName("ENTITY_PLAYER_HURT");
                soundService.playSound(player, player.getLocation(), fallbackSound, 0.5f, 1.0f);
                plugin.getLogger().warning("Invalid knockback sound configured, using fallback: " + e.getMessage());
            } catch (Exception fallbackException) {
                // If even fallback fails, just log and continue
                plugin.getLogger().warning("Failed to play knockback sound: " + fallbackException.getMessage());
            }
        }
    }
    
    @Override
    public boolean isKnockbackEnabled() {
        return configManager.isKnockbackEnabled();
    }
}