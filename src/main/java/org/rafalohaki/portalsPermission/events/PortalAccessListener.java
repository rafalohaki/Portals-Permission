package org.rafalohaki.portalsPermission.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.rafalohaki.portalsPermission.managers.ConfigManager;
import org.rafalohaki.portalsPermission.managers.CooldownManager;
import org.rafalohaki.portalsPermission.managers.SoundConfig;
import org.rafalohaki.portalsPermission.utils.PortalType;

import java.util.logging.Level;

/**
 * Listener for portal access events with permission control and knockback
 * Listener dla wydarzeń dostępu do portali z kontrolą uprawnień i knockback
 */
public class PortalAccessListener implements Listener {
    
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final CooldownManager cooldownManager;
    private final BukkitScheduler scheduler;
    private final MiniMessage miniMessage;
    
    public PortalAccessListener(@NotNull JavaPlugin plugin, @NotNull ConfigManager configManager, @NotNull CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.cooldownManager = cooldownManager;
        this.scheduler = plugin.getServer().getScheduler();
        this.miniMessage = MiniMessage.miniMessage();
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPortal(@NotNull PlayerPortalEvent event) {
        Player player = event.getPlayer();
        
        // Check if plugin is enabled
        if (!configManager.isEnabled()) {
            return;
        }
        
        // Check if player has bypass permission
        if (player.hasPermission(configManager.getPermission("bypass"))) {
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("Player " + player.getName() + " bypassed portal restrictions");
            }
            return;
        }
        
        // Check cooldown
        if (cooldownManager.hasCooldown(player)) {
            event.setCancelled(true);
            
            if (configManager.isCooldownMessageEnabled()) {
                int remainingTime = cooldownManager.getRemainingCooldown(player);
                String message = configManager.getMessage("cooldown_active", "time", String.valueOf(remainingTime));
                sendMessage(player, message);
            }
            
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("Player " + player.getName() + " blocked due to cooldown");
            }
            return;
        }
        
        // Determine portal type and check permissions
        World.Environment targetEnvironment = getTargetEnvironment(event);
        String requiredPermission = getRequiredPermission(targetEnvironment);
        String messageKey = getMessageKey(targetEnvironment);
        
        if (requiredPermission == null) {
            // Unknown portal type, allow if custom portals are not blocked
            if (!configManager.isCustomBlocked()) {
                return;
            }
            requiredPermission = configManager.getPermission("custom");
            messageKey = "no_permission_custom";
        }
        
        // Check if this portal type is blocked
        if (!isPortalTypeBlocked(targetEnvironment)) {
            return;
        }
        
        // Check permission
        if (!player.hasPermission(requiredPermission)) {
            event.setCancelled(true);
            
            // Send message
            String message = configManager.getMessage(messageKey);
            sendMessage(player, message);
            
            // Apply knockback and cooldown asynchronously
            applyKnockbackAsync(player, event.getFrom());
            cooldownManager.setCooldownAsync(player);
            
            if (configManager.isDebugMode()) {
                plugin.getLogger().info("Player " + player.getName() + " denied access to " + targetEnvironment + " portal");
            }
        }
    }
    
    /**
     * Determines the target environment of the portal
     * Określa docelowe środowisko portalu
     */
    private World.Environment getTargetEnvironment(@NotNull PlayerPortalEvent event) {
        Location from = event.getFrom();
        
        // Check portal material at location
        Material portalMaterial = from.getBlock().getType();
        
        if (portalMaterial == Material.NETHER_PORTAL) {
            return from.getWorld().getEnvironment() == World.Environment.NORMAL ? 
                   World.Environment.NETHER : World.Environment.NORMAL;
        } else if (portalMaterial == Material.END_PORTAL || portalMaterial == Material.END_GATEWAY) {
            return World.Environment.THE_END;
        }
        
        // Check nearby blocks for portal detection
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Location checkLoc = from.clone().add(x, y, z);
                    Material material = checkLoc.getBlock().getType();
                    
                    if (material == Material.NETHER_PORTAL) {
                        return from.getWorld().getEnvironment() == World.Environment.NORMAL ? 
                               World.Environment.NETHER : World.Environment.NORMAL;
                    } else if (material == Material.END_PORTAL || material == Material.END_GATEWAY) {
                        return World.Environment.THE_END;
                    }
                }
            }
        }
        
        // Unknown portal type
        return null;
    }
    
    /**
     * Gets required permission for portal type using modern Java 21 sealed classes and pattern matching
     * Pobiera wymagane uprawnienie dla typu portalu używając nowoczesnych sealed classes i pattern matching Java 21
     */
    private String getRequiredPermission(World.Environment environment) {
        PortalType portalType = PortalType.fromEnvironment(environment);
        return portalType != null ? configManager.getPermission(portalType.getPermissionKey()) : null;
    }
    
    /**
     * Gets message key for portal type using modern Java 21 sealed classes and pattern matching
     * Pobiera klucz wiadomości dla typu portalu używając nowoczesnych sealed classes i pattern matching Java 21
     */
    private String getMessageKey(World.Environment environment) {
        PortalType portalType = PortalType.fromEnvironment(environment);
        return portalType != null ? portalType.getMessageKey() : "no_permission_custom";
    }
    
    /**
     * Checks if portal type is blocked in configuration
     * Sprawdza czy typ portalu jest zablokowany w konfiguracji
     */
    private boolean isPortalTypeBlocked(World.Environment environment) {
        if (environment == null) {
            return configManager.isCustomBlocked();
        }
        
        return switch (environment) {
            case NETHER -> configManager.isNetherBlocked();
            case THE_END -> configManager.isEndBlocked();
            default -> configManager.isCustomBlocked();
        };
    }
    
    /**
     * Applies knockback to player asynchronously
     * Aplikuje knockback do gracza asynchronicznie
     */
    /**
     * Applies knockback to player away from portal location
     * Stosuje knockback do gracza z dala od lokalizacji portalu
     */
    private void applyKnockbackAsync(@NotNull Player player, @NotNull Location portalLocation) {
        if (!configManager.isKnockbackEnabled()) {
            return;
        }
        
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                double strength = configManager.getKnockbackStrength();
                double height = configManager.getKnockbackHeight();
                
                // Calculate knockback direction (away from portal)
                Location playerLoc = player.getLocation();
                Vector rawDirection = playerLoc.toVector().subtract(portalLocation.toVector());
                
                // Calculate final direction vector
                final Vector direction;
                
                // Check if direction vector is valid (not zero length)
                if (rawDirection.lengthSquared() < 0.01) {
                    // Player is at same location as portal, use default direction
                    direction = new Vector(1.0, 0.0, 0.0);
                } else {
                    Vector normalizedDirection = rawDirection.normalize();
                    
                    // Ensure minimum horizontal knockback if player is directly above/below portal
                    if (Math.abs(normalizedDirection.getX()) < 0.1 && Math.abs(normalizedDirection.getZ()) < 0.1) {
                        // Create new horizontal direction vector
                        double angle = Math.random() * 2 * Math.PI;
                        direction = new Vector(Math.cos(angle), normalizedDirection.getY(), Math.sin(angle)).normalize();
                    } else {
                        direction = normalizedDirection;
                    }
                }
                
                // Validate direction components before creating knockback vector
                if (!Double.isFinite(direction.getX()) || !Double.isFinite(direction.getY()) || !Double.isFinite(direction.getZ())) {
                    plugin.getLogger().warning("Invalid direction vector calculated for knockback: " + direction);
                    return;
                }
                
                Vector knockback = direction.multiply(strength).setY(height);
                
                // Apply knockback on main thread
                scheduler.runTask(plugin, () -> {
                    try {
                        player.setVelocity(knockback);
                        
                        // Play sound if enabled
                        if (configManager.isKnockbackSoundEnabled()) {
                            playKnockbackSound(player);
                        }
                        
                        if (configManager.isDebugMode()) {
                            plugin.getLogger().info("Applied knockback to player " + player.getName() + 
                                " with direction: " + direction + " and strength: " + strength);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Failed to apply knockback to player " + player.getName(), e);
                    }
                });
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error calculating knockback for player " + player.getName(), e);
            }
        });
    }
    
    /**
     * Plays knockback sound to player using Adventure Sound API
     * Odtwarza dźwięk knockback dla gracza używając Adventure Sound API
     */
    private void playKnockbackSound(@NotNull Player player) {
        try {
            SoundConfig soundConfig = configManager.getKnockbackSoundConfig();
            if (!soundConfig.enabled()) {
                return;
            }
            
            // Convert legacy sound format to modern namespace format
            String soundName = convertLegacySoundFormat(soundConfig.type());
            
            // Validate sound name before creating Key
            if (soundName == null || soundName.trim().isEmpty()) {
                plugin.getLogger().warning("Invalid sound name: " + soundConfig.type());
                return;
            }
            
            // Use Adventure Sound API with proper namespace format and error handling
            try {
                net.kyori.adventure.key.Key soundKey = net.kyori.adventure.key.Key.key("minecraft", soundName);
                Sound adventureSound = Sound.sound(soundKey, Sound.Source.PLAYER, soundConfig.volume(), soundConfig.pitch());
                
                // Play sound at player location to avoid registry issues
                player.playSound(adventureSound, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
            } catch (IllegalStateException e) {
                // Fallback to Bukkit Sound API if Adventure fails
                plugin.getLogger().warning("Adventure Sound API failed, using Bukkit fallback: " + e.getMessage());
                playKnockbackSoundFallback(player, soundConfig);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to play knockback sound for player " + player.getName(), e);
        }
    }
    
    /**
     * Fallback method using Bukkit Sound API
     * Metoda fallback używająca Bukkit Sound API
     */
    private void playKnockbackSoundFallback(@NotNull Player player, @NotNull SoundConfig soundConfig) {
        try {
            // Convert sound name to proper format for Registry lookup
            String soundName = soundConfig.type().toLowerCase().replace("minecraft:", "");
            
            // Use modern Registry API to find sound
            org.bukkit.NamespacedKey soundKey = org.bukkit.NamespacedKey.minecraft(soundName);
            org.bukkit.Sound bukkitSound = org.bukkit.Registry.SOUNDS.get(soundKey);
            
            if (bukkitSound != null) {
                player.playSound(player.getLocation(), bukkitSound, soundConfig.volume(), soundConfig.pitch());
            } else {
                // If Registry lookup fails, use safe default sound
                plugin.getLogger().warning("Sound not found in registry: " + soundConfig.type() + ". Using default sound.");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_HURT, soundConfig.volume(), soundConfig.pitch());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error playing sound: " + soundConfig.type() + ". Using default sound. Error: " + e.getMessage());
            // Use a safe default sound
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_HURT, soundConfig.volume(), soundConfig.pitch());
        }
    }
    
    /**
     * Converts legacy sound format to modern namespace format
     * Konwertuje starszy format dźwięku na nowoczesny format namespace
     */
    private @NotNull String convertLegacySoundFormat(@NotNull String legacySound) {
        // Convert ENTITY_VILLAGER_NO to entity.villager.no
        return legacySound.toLowerCase().replace("_", ".");
    }
    
    /**
     * Sends message to player using Adventure Components
     * Wysyła wiadomość do gracza używając Adventure Components
     */
    /**
     * Sends a MiniMessage formatted message to the player
     * Wysyła sformatowaną wiadomość MiniMessage do gracza
     */
    private void sendMessage(@NotNull Player player, @NotNull String message) {
        try {
            Component component = miniMessage.deserialize(message);
            player.sendMessage(component);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to deserialize MiniMessage for player " + player.getName() + ": " + message, e);
            // Fallback to plain Component without formatting
            player.sendMessage(Component.text(message));
        }
    }
}