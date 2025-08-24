package org.rafalohaki.portalsPermission.services.impl;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rafalohaki.portalsPermission.managers.ConfigManager;
import org.rafalohaki.portalsPermission.services.IPortalSecurityService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Implementation of portal security service for bypass prevention
 * Implementacja serwisu zabezpieczeń portali zapobiegającego bypassom
 */
public class PortalSecurityService implements IPortalSecurityService {
    
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final BukkitScheduler scheduler;
    private final ScheduledExecutorService asyncExecutor;
    
    // Entity portal cooldowns (UUID -> cooldown end time in ticks)
    private final ConcurrentHashMap<UUID, Long> entityPortalCooldowns;
    
    // Entity portal entry tracking (UUID -> entry time in milliseconds)
    private final ConcurrentHashMap<UUID, Long> entityPortalEntryTimes;
    
    // Players in vehicles within portal areas
    private final ConcurrentHashMap<UUID, Boolean> playersInVehicleInPortal;
    
    // Players using elytra near portals
    private final ConcurrentHashMap<UUID, Boolean> playersGlidingNearPortal;
    
    // Configuration constants
    private static final int DEFAULT_ENTITY_PORTAL_COOLDOWN_TICKS = 100; // 5 seconds
    private static final long MAX_PORTAL_STAY_TIME_MS = 30000; // 30 seconds
    private static final int CLEANUP_INTERVAL_SECONDS = 60;
    private static final double MAX_MOVEMENT_SPEED_NEAR_PORTAL = 0.5; // blocks per tick
    private static final double MAX_VELOCITY_NEAR_PORTAL = 2.0; // blocks per second
    
    /**
     * Constructor for PortalSecurityService
     * Konstruktor dla PortalSecurityService
     */
    public PortalSecurityService(@NotNull JavaPlugin plugin, @NotNull ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.scheduler = plugin.getServer().getScheduler();
        this.asyncExecutor = Executors.newScheduledThreadPool(2);
        
        this.entityPortalCooldowns = new ConcurrentHashMap<>();
        this.entityPortalEntryTimes = new ConcurrentHashMap<>();
        this.playersInVehicleInPortal = new ConcurrentHashMap<>();
        this.playersGlidingNearPortal = new ConcurrentHashMap<>();
        
        // Start cleanup tasks
        startCleanupTasks();
        
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("PortalSecurityService initialized successfully");
        }
    }
    
    @Override
    public void handleEntityPortalEnter(@NotNull EntityPortalEnterEvent event) {
        Entity entity = event.getEntity();
        
        if (!configManager.isEnabled()) {
            return;
        }
        
        // Track entity portal entry time for long-stay detection
        trackEntityPortalEntry(entity);
        
        // Check if entity has active portal cooldown
        if (hasEntityPortalCooldown(entity)) {
            // Cancel portal entry for entities with active cooldown
            event.setCancelled(true);
            
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("Entity " + entity.getType() + " (" + entity.getUniqueId() + ") blocked from portal due to cooldown");
            }
            return;
        }
        
        // Special handling for players in vehicles
        if (entity instanceof Player player) {
            if (isPlayerInVehicleInPortal(player)) {
                event.setCancelled(true);
                
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("Player " + player.getName() + " blocked from portal while in vehicle");
                }
                return;
            }
        }
        
        // Enhanced vehicle bypass prevention - check if entity is a vehicle with passengers
        if (entity instanceof org.bukkit.entity.Vehicle vehicle) {
            var passengers = vehicle.getPassengers();
            if (!passengers.isEmpty()) {
                // Block vehicle with passengers from entering portal
                event.setCancelled(true);
                
                // Set cooldown for the vehicle
                setEntityPortalCooldownAsync(vehicle, DEFAULT_ENTITY_PORTAL_COOLDOWN_TICKS);
                
                // Also set cooldown for all passengers to prevent immediate re-entry
                for (Entity passenger : passengers) {
                    if (passenger instanceof Player player) {
                        setEntityPortalCooldownAsync(passenger, DEFAULT_ENTITY_PORTAL_COOLDOWN_TICKS);
                        
                        if (configManager.isDebugMode()) {
                            plugin.getLogger().info("Player " + player.getName() + " and their vehicle " + vehicle.getType() + " blocked from portal entry");
                        }
                    }
                }
                
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("Vehicle " + vehicle.getType() + " with " + passengers.size() + " passengers blocked from portal");
                }
                return;
            }
        }
        
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("Entity " + entity.getType() + " entered portal area");
        }
    }
    
    @Override
    public void handleEntityPortalReady(@NotNull EntityPortalReadyEvent event) {
        Entity entity = event.getEntity();
        
        if (!configManager.isEnabled()) {
            return;
        }
        
        // Check if entity has been in portal too long (potential bypass attempt)
        if (hasEntityBeenInPortalTooLong(entity)) {
            event.setCancelled(true);
            
            // Set cooldown to prevent immediate re-entry
            setEntityPortalCooldownAsync(entity, DEFAULT_ENTITY_PORTAL_COOLDOWN_TICKS);
            
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("Entity " + entity.getType() + " (" + entity.getUniqueId() + ") blocked from portal teleportation due to long stay");
            }
            return;
        }
        
        // Check if entity has active portal cooldown
        if (hasEntityPortalCooldown(entity)) {
            event.setCancelled(true);
            
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("Entity " + entity.getType() + " (" + entity.getUniqueId() + ") blocked from portal teleportation due to cooldown");
            }
            return;
        }
        
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("Entity " + entity.getType() + " ready for portal teleportation");
        }
    }
    
    @Override
    public void handleVehicleEnter(@NotNull VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player player)) {
            return;
        }
        
        if (!configManager.isEnabled()) {
            return;
        }
        
        // Check if player is entering vehicle near portal
        if (isNearPortal(player)) {
            playersInVehicleInPortal.put(player.getUniqueId(), true);
            
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("Player " + player.getName() + " entered vehicle near portal");
            }
        }
    }
    
    @Override
    public void handleVehicleExit(@NotNull VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player player)) {
            return;
        }
        
        if (!configManager.isEnabled()) {
            return;
        }
        
        // Remove player from vehicle-in-portal tracking
        playersInVehicleInPortal.remove(player.getUniqueId());
        
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("Player " + player.getName() + " exited vehicle");
        }
    }
    
    @Override
    public CompletableFuture<Void> setEntityPortalCooldownAsync(@NotNull Entity entity, int cooldownTicks) {
        return CompletableFuture.runAsync(() -> {
            try {
                UUID entityId = entity.getUniqueId();
                long currentTick = plugin.getServer().getCurrentTick();
                long cooldownEnd = currentTick + cooldownTicks;
                
                entityPortalCooldowns.put(entityId, cooldownEnd);
                
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("Set portal cooldown for entity " + entity.getType() + " (" + entityId + ") for " + cooldownTicks + " ticks");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to set portal cooldown for entity " + entity.getUniqueId(), e);
            }
        }, asyncExecutor);
    }
    
    @Override
    public boolean hasEntityPortalCooldown(@NotNull Entity entity) {
        UUID entityId = entity.getUniqueId();
        Long cooldownEnd = entityPortalCooldowns.get(entityId);
        
        if (cooldownEnd == null) {
            return false;
        }
        
        long currentTick = plugin.getServer().getCurrentTick();
        if (currentTick >= cooldownEnd) {
            entityPortalCooldowns.remove(entityId);
            return false;
        }
        
        return true;
    }
    
    @Override
    public int getRemainingEntityPortalCooldown(@NotNull Entity entity) {
        UUID entityId = entity.getUniqueId();
        Long cooldownEnd = entityPortalCooldowns.get(entityId);
        
        if (cooldownEnd == null) {
            return 0;
        }
        
        long currentTick = plugin.getServer().getCurrentTick();
        if (currentTick >= cooldownEnd) {
            entityPortalCooldowns.remove(entityId);
            return 0;
        }
        
        return (int) (cooldownEnd - currentTick);
    }
    
    @Override
    public void removeEntityPortalCooldown(@NotNull Entity entity) {
        UUID entityId = entity.getUniqueId();
        entityPortalCooldowns.remove(entityId);
        
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("Removed portal cooldown for entity " + entity.getType() + " (" + entityId + ")");
        }
    }
    
    @Override
    public boolean isPlayerInVehicleInPortal(@NotNull Player player) {
        return playersInVehicleInPortal.getOrDefault(player.getUniqueId(), false);
    }
    
    @Override
    public void trackEntityPortalEntry(@NotNull Entity entity) {
        UUID entityId = entity.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        entityPortalEntryTimes.put(entityId, currentTime);
        
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("Tracking portal entry for entity " + entity.getType() + " (" + entityId + ")");
        }
    }
    
    @Override
    public void removeEntityPortalTracking(@NotNull Entity entity) {
        UUID entityId = entity.getUniqueId();
        entityPortalEntryTimes.remove(entityId);
        
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("Removed portal tracking for entity " + entity.getType() + " (" + entityId + ")");
        }
    }
    
    @Override
    public boolean hasEntityBeenInPortalTooLong(@NotNull Entity entity) {
        UUID entityId = entity.getUniqueId();
        Long entryTime = entityPortalEntryTimes.get(entityId);
        
        if (entryTime == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long timeInPortal = currentTime - entryTime;
        
        return timeInPortal > MAX_PORTAL_STAY_TIME_MS;
    }
    
    @Override
    public void handlePlayerGlideToggle(@NotNull Player player, boolean isGliding) {
        if (isGliding && isNearPortal(player)) {
            playersGlidingNearPortal.put(player.getUniqueId(), true);
            
            // Set cooldown to prevent immediate portal use after gliding
            setEntityPortalCooldownAsync(player, DEFAULT_ENTITY_PORTAL_COOLDOWN_TICKS);
            
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("Player " + player.getName() + " started gliding near portal - cooldown applied");
            }
        } else {
            playersGlidingNearPortal.remove(player.getUniqueId());
        }
    }
    
    @Override
    public void handlePlayerElytraBoost(@NotNull Player player) {
        if (isNearPortal(player)) {
            // Apply extended cooldown for elytra boost near portals
            setEntityPortalCooldownAsync(player, DEFAULT_ENTITY_PORTAL_COOLDOWN_TICKS * 2);
            
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("Player " + player.getName() + " used elytra boost near portal - extended cooldown applied");
            }
        }
    }
    
    @Override
    public void handlePlayerMovement(@NotNull Player player, @NotNull Location from, @Nullable Location to) {
        if (to == null) return;
        
        // Check if player is moving too fast near a portal
        if (isNearPortal(player)) {
            double distance = from.distance(to);
            
            // If movement speed exceeds threshold, apply cooldown
            if (distance > MAX_MOVEMENT_SPEED_NEAR_PORTAL) {
                setEntityPortalCooldownAsync(player, DEFAULT_ENTITY_PORTAL_COOLDOWN_TICKS);
                
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("Player " + player.getName() + " moving too fast near portal (" + 
                        String.format("%.2f", distance) + " blocks) - cooldown applied");
                }
            }
        }
    }
    
    @Override
    public void handlePlayerVelocityChange(@NotNull Player player, @NotNull Vector velocity) {
        if (isNearPortal(player)) {
            double velocityMagnitude = velocity.length();
            
            // If velocity exceeds threshold near portal, apply cooldown
            if (velocityMagnitude > MAX_VELOCITY_NEAR_PORTAL) {
                setEntityPortalCooldownAsync(player, DEFAULT_ENTITY_PORTAL_COOLDOWN_TICKS);
                
                if (configManager.isDebugMode()) {
                    plugin.getLogger().info("Player " + player.getName() + " high velocity near portal (" + 
                        String.format("%.2f", velocityMagnitude) + " blocks/s) - cooldown applied");
                }
            }
        }
    }

    @Override
    public void clearAllSecurityData() {
        entityPortalCooldowns.clear();
        entityPortalEntryTimes.clear();
        playersInVehicleInPortal.clear();
        playersGlidingNearPortal.clear();
        
        if (configManager.isDebugMode()) {
            plugin.getLogger().info("Cleared all portal security data");
        }
    }
    
    @Override
    public void shutdown() {
        try {
            asyncExecutor.shutdown();
            if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
            
            clearAllSecurityData();
            
            plugin.getLogger().info("PortalSecurityService shut down successfully");
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
            plugin.getLogger().log(Level.WARNING, "PortalSecurityService shutdown interrupted", e);
        }
    }
    
    /**
     * Checks if a player is near any portal block
     * @param player The player to check
     * @return true if player is near a portal, false otherwise
     */
    private boolean isNearPortal(@NotNull Player player) {
        Location playerLocation = player.getLocation();
        org.bukkit.World world = playerLocation.getWorld();
        
        if (world == null) {
            return false;
        }
        
        // Check 5x5x5 area around player for portal blocks (increased from 3x3x3)
        int radius = 2;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location checkLocation = playerLocation.clone().add(x, y, z);
                    org.bukkit.Material blockType = world.getBlockAt(checkLocation).getType();
                    
                    // Check for all portal types including End Portal frames
                    if (isPortalBlock(blockType)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Checks if a material is a portal-related block
     * @param material The material to check
     * @return true if material is portal-related, false otherwise
     */
    private boolean isPortalBlock(@NotNull org.bukkit.Material material) {
        return material == org.bukkit.Material.NETHER_PORTAL ||
               material == org.bukkit.Material.END_PORTAL ||
               material == org.bukkit.Material.END_PORTAL_FRAME ||
               material.name().contains("PORTAL");
    }
    
    /**
     * Starts periodic cleanup tasks for expired data
     * Uruchamia okresowe zadania czyszczenia wygasłych danych
     */
    private void startCleanupTasks() {
        asyncExecutor.scheduleAtFixedRate(() -> {
            try {
                cleanupExpiredCooldowns();
                cleanupExpiredPortalEntries();
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error during portal security cleanup", e);
            }
        }, CLEANUP_INTERVAL_SECONDS, CLEANUP_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }
    
    /**
     * Cleans up expired portal cooldowns
     * Czyści wygasłe cooldowny portali
     */
    private void cleanupExpiredCooldowns() {
        long currentTick = plugin.getServer().getCurrentTick();
        int removedCount = 0;
        
        var iterator = entityPortalCooldowns.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (currentTick >= entry.getValue()) {
                iterator.remove();
                removedCount++;
            }
        }
        
        if (configManager.isDebugMode() && removedCount > 0) {
            plugin.getLogger().info("Cleaned up " + removedCount + " expired portal cooldowns");
        }
    }
    
    /**
     * Cleans up expired portal entry tracking
     * Czyści wygasłe śledzenie wejść do portali
     */
    private void cleanupExpiredPortalEntries() {
        long currentTime = System.currentTimeMillis();
        int removedCount = 0;
        
        var iterator = entityPortalEntryTimes.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            long timeInPortal = currentTime - entry.getValue();
            
            // Remove entries older than max stay time + buffer
            if (timeInPortal > (MAX_PORTAL_STAY_TIME_MS + 60000)) { // 1 minute buffer
                iterator.remove();
                removedCount++;
            }
        }
        
        if (configManager.isDebugMode() && removedCount > 0) {
            plugin.getLogger().info("Cleaned up " + removedCount + " expired portal entry trackings");
        }
    }
}