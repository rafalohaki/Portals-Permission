package org.rafalohaki.portalsPermission.managers;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manager for handling player cooldowns with async operations
 * Zarządza cooldown graczy z operacjami asynchronicznymi
 */
public class CooldownManager {
    
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final ConcurrentHashMap<UUID, Long> cooldowns;
    private final ScheduledExecutorService scheduler;
    
    public CooldownManager(@NotNull JavaPlugin plugin, @NotNull ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.cooldowns = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        // Start cleanup task
        startCleanupTask();
    }
    
    /**
     * Checks if player has active cooldown
     * Sprawdza czy gracz ma aktywny cooldown
     */
    public boolean hasCooldown(@NotNull Player player) {
        if (!configManager.isCooldownEnabled()) {
            return false;
        }
        
        UUID playerId = player.getUniqueId();
        Long cooldownEnd = cooldowns.get(playerId);
        
        if (cooldownEnd == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime >= cooldownEnd) {
            cooldowns.remove(playerId);
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets remaining cooldown time in seconds
     * Pobiera pozostały czas cooldown w sekundach
     */
    public int getRemainingCooldown(@NotNull Player player) {
        if (!configManager.isCooldownEnabled()) {
            return 0;
        }
        
        UUID playerId = player.getUniqueId();
        Long cooldownEnd = cooldowns.get(playerId);
        
        if (cooldownEnd == null) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime >= cooldownEnd) {
            cooldowns.remove(playerId);
            return 0;
        }
        
        return (int) Math.ceil((cooldownEnd - currentTime) / 1000.0);
    }
    
    /**
     * Sets cooldown for player asynchronously
     * Ustawia cooldown dla gracza asynchronicznie
     */
    public CompletableFuture<Void> setCooldownAsync(@NotNull Player player) {
        return CompletableFuture.runAsync(() -> {
            if (!configManager.isCooldownEnabled()) {
                return;
            }
            
            try {
                UUID playerId = player.getUniqueId();
                int cooldownSeconds = configManager.getCooldownTime();
                long cooldownEnd = System.currentTimeMillis() + (cooldownSeconds * 1000L);
                
                cooldowns.put(playerId, cooldownEnd);
                
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("Set cooldown for player " + player.getName() + " for " + cooldownSeconds + " seconds");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to set cooldown for player " + player.getName(), e);
            }
        });
    }
    
    /**
     * Removes cooldown for player
     * Usuwa cooldown dla gracza
     */
    public void removeCooldown(@NotNull Player player) {
        UUID playerId = player.getUniqueId();
        cooldowns.remove(playerId);
        
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("Removed cooldown for player " + player.getName());
        }
    }
    
    /**
     * Clears all cooldowns
     * Czyści wszystkie cooldown
     */
    public void clearAllCooldowns() {
        cooldowns.clear();
        
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("Cleared all cooldowns");
        }
    }
    
    /**
     * Gets total number of active cooldowns
     * Pobiera całkowitą liczbę aktywnych cooldown
     */
    public int getActiveCooldownCount() {
        return cooldowns.size();
    }
    
    /**
     * Starts periodic cleanup task to remove expired cooldowns
     * Uruchamia okresowe zadanie czyszczenia wygasłych cooldown
     */
    private void startCleanupTask() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                int removedCount = 0;
                
                cooldowns.entrySet().removeIf(entry -> {
                    if (currentTime >= entry.getValue()) {
                        return true;
                    }
                    return false;
                });
                
                if (configManager.isDebugMode() && removedCount > 0) {
                    plugin.getLogger().info("Cleaned up " + removedCount + " expired cooldowns");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error during cooldown cleanup", e);
            }
        }, 30, 30, TimeUnit.SECONDS); // Cleanup every 30 seconds
    }
    
    /**
     * Shuts down the cooldown manager
     * Wyłącza menedżer cooldown
     */
    public void shutdown() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            cooldowns.clear();
            
            plugin.getLogger().info("CooldownManager shut down successfully");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            plugin.getLogger().log(Level.WARNING, "CooldownManager shutdown interrupted", e);
        }
    }
}