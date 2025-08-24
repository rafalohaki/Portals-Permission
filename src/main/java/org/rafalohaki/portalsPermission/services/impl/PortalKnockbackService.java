package org.rafalohaki.portalsPermission.services.impl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rafalohaki.portalsPermission.managers.ConfigManager;
import org.rafalohaki.portalsPermission.services.IPortalKnockbackService;
import org.rafalohaki.portalsPermission.services.ISoundService;

import java.util.Objects;
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
     * @throws IllegalArgumentException if any parameter is null
     */
    public PortalKnockbackService(@NotNull JavaPlugin plugin, @NotNull ConfigManager configManager, @NotNull ISoundService soundService) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.configManager = Objects.requireNonNull(configManager, "ConfigManager cannot be null");
        this.soundService = Objects.requireNonNull(soundService, "SoundService cannot be null");
    }
    
    @Override
    @NotNull
    public CompletableFuture<Void> applyKnockbackAsync(@NotNull Player player, @NotNull Location portalLocation) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(portalLocation, "Portal location cannot be null");
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        // Use BukkitScheduler for async operations instead of CompletableFuture.runAsync()
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!isKnockbackEnabled()) {
                    future.complete(null);
                    return;
                }
                
                // Additional null safety checks
                if (!player.isOnline() || player.isDead()) {
                    future.complete(null);
                    return;
                }
                
                Location playerLocation = player.getLocation();
                if (playerLocation == null || playerLocation.getWorld() == null) {
                    future.complete(null);
                    return;
                }
                
                World.Environment targetEnvironment = getTargetEnvironmentFromLocation(portalLocation);
                double strength = configManager.getKnockbackStrength();
                double height = configManager.getKnockbackHeight();
                
                Vector knockback = calculateKnockbackVector(
                    playerLocation, 
                    portalLocation, 
                    targetEnvironment, 
                    strength, 
                    height
                );
                
                // Apply knockback on main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        // Double-check player state on main thread
                        if (player.isOnline() && !player.isDead()) {
                            applyDamageBasedKnockback(player, knockback);
                            playKnockbackSound(player);
                        }
                        future.complete(null);
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                });
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }
    
    @Override
    @NotNull
    public Vector calculateKnockbackVector(@NotNull Location playerLocation, 
                                         @NotNull Location portalLocation,
                                         @Nullable World.Environment targetEnvironment, 
                                         double strength, 
                                         double height) {
        Objects.requireNonNull(playerLocation, "Player location cannot be null");
        Objects.requireNonNull(portalLocation, "Portal location cannot be null");
        
        // Validate numeric parameters
        if (Double.isNaN(strength) || Double.isInfinite(strength)) {
            strength = 1.0;
        }
        if (Double.isNaN(height) || Double.isInfinite(height)) {
            height = 0.5;
        }
        
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
    
    /**
     * Gets target environment based on portal location
     * Pobiera docelowe środowisko na podstawie lokalizacji portalu
     * 
     * @param portalLocation The portal location to analyze
     * @return The target environment or null if not a portal
     */
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
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(knockback, "Knockback vector cannot be null");
        
        // Additional safety checks
        if (!player.isOnline() || player.isDead()) {
            return;
        }
        
        // Validate knockback vector
        if (Double.isNaN(knockback.getX()) || Double.isNaN(knockback.getY()) || Double.isNaN(knockback.getZ()) ||
            Double.isInfinite(knockback.getX()) || Double.isInfinite(knockback.getY()) || Double.isInfinite(knockback.getZ())) {
            return; // Skip invalid knockback
        }
        
        try {
            // Modern Paper API 1.21+ compatible knockback using velocity
            player.setVelocity(knockback);
            
            // Alternative method for older versions or if velocity doesn't work
            // Use a small amount of damage to trigger knockback
            if (knockback.lengthSquared() > 0) {
                player.damage(0.01); // Minimal damage to trigger knockback mechanics
                player.setVelocity(knockback);
            }
        } catch (Exception e) {
            // Log error but don't throw - knockback is not critical functionality
            plugin.getLogger().warning("Failed to apply knockback to player " + player.getName() + ": " + e.getMessage());
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