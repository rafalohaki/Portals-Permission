package org.rafalohaki.portalsPermission.managers;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.scheduler.BukkitTask;

/**
 * Manager for handling player cooldowns with async operations
 * Zarządza cooldown graczy z operacjami asynchronicznymi
 */
public class CooldownManager {
    
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final ConcurrentHashMap<UUID, Long> cooldowns;
    private BukkitTask cleanupTask;
    
    public CooldownManager(@NotNull JavaPlugin plugin, @NotNull ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.cooldowns = new ConcurrentHashMap<>();
        
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
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        // Use BukkitScheduler for async operations
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!configManager.isCooldownEnabled()) {
                    future.complete(null);
                    return;
                }
                
                UUID playerId = player.getUniqueId();
                int cooldownSeconds = configManager.getCooldownTime();
                long cooldownEnd = System.currentTimeMillis() + (cooldownSeconds * 1000L);
                
                cooldowns.put(playerId, cooldownEnd);
                
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("Set cooldown for player " + player.getName() + " for " + cooldownSeconds + " seconds");
                }
                
                future.complete(null);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to set cooldown for player " + player.getName(), e);
                future.completeExceptionally(e);
            }
        });
        
        return future;
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
        // Use BukkitScheduler for repeating task - runs every 30 seconds (600 ticks)
        cleanupTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                long currentTime = System.currentTimeMillis();
                
                // Use AtomicInteger to count removed entries in lambda
                java.util.concurrent.atomic.AtomicInteger removedCount = new java.util.concurrent.atomic.AtomicInteger(0);
                
                cooldowns.entrySet().removeIf(entry -> {
                    if (currentTime >= entry.getValue()) {
                        removedCount.incrementAndGet();
                        return true;
                    }
                    return false;
                });
                
                // Only log if significant cleanup occurred (throttled logging)
                if (configManager.isDebugMode() && removedCount.get() >= 5) {
                    plugin.getLogger().info("Cleaned up " + removedCount.get() + " expired cooldowns");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error during cooldown cleanup", e);
            }
        }, 600L, 600L); // 30 seconds = 600 ticks
    }
    
    /**
     * Shuts down the cooldown manager
     * Wyłącza menedżer cooldown
     */
    public void shutdown() {
        try {
            if (cleanupTask != null && !cleanupTask.isCancelled()) {
                cleanupTask.cancel();
            }
            cooldowns.clear();
            
            plugin.getLogger().info("CooldownManager shut down successfully");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error during CooldownManager shutdown", e);
        }
    }
}