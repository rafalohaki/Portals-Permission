package org.rafalohaki.portalsPermission.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.rafalohaki.portalsPermission.managers.ConfigManager;
import org.rafalohaki.portalsPermission.managers.CooldownManager;
import org.rafalohaki.portalsPermission.services.IPortalKnockbackService;
import org.rafalohaki.portalsPermission.services.IPortalMessageService;
import org.rafalohaki.portalsPermission.services.IPortalPermissionChecker;

/**
 * Refactored portal access listener following SOLID principles
 * Zrefaktoryzowany listener dostępu do portali zgodny z zasadami SOLID
 */
public class RefactoredPortalAccessListener implements Listener {
    
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final CooldownManager cooldownManager;
    private final IPortalPermissionChecker permissionChecker;
    private final IPortalKnockbackService knockbackService;
    private final IPortalMessageService messageService;
    
    /**
     * Constructor for RefactoredPortalAccessListener
     * Konstruktor dla RefactoredPortalAccessListener
     * 
     * @param plugin The plugin instance
     * @param configManager The configuration manager
     * @param cooldownManager The cooldown manager
     * @param permissionChecker The permission checker service
     * @param knockbackService The knockback service
     * @param messageService The message service
     */
    public RefactoredPortalAccessListener(@NotNull JavaPlugin plugin,
                                        @NotNull ConfigManager configManager,
                                        @NotNull CooldownManager cooldownManager,
                                        @NotNull IPortalPermissionChecker permissionChecker,
                                        @NotNull IPortalKnockbackService knockbackService,
                                        @NotNull IPortalMessageService messageService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.cooldownManager = cooldownManager;
        this.permissionChecker = permissionChecker;
        this.knockbackService = knockbackService;
        this.messageService = messageService;
    }
    
    /**
     * Handles player portal events with permission checking and cooldowns
     * Obsługuje zdarzenia portali graczy ze sprawdzaniem uprawnień i cooldownów
     * 
     * @param event The portal event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPortal(@NotNull PlayerPortalEvent event) {
        if (!configManager.isEnabled()) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Debug logging
        if (configManager.isDebugMode()) {
            plugin.getLogger().info(String.format(
                "Portal event for player %s at %s", 
                player.getName(), 
                event.getFrom().toString()
            ));
        }
        
        // Check cooldown first
        if (handleCooldown(player, event)) {
            return; // Event cancelled due to cooldown
        }
        
        // Check permissions
        if (handlePermissions(player, event)) {
            return; // Event cancelled due to lack of permissions
        }
        
        // Apply cooldown for successful portal use
        applyCooldown(player);
    }
    
    /**
     * Handles cooldown checking and messaging
     * Obsługuje sprawdzanie cooldownu i wiadomości
     * 
     * @param player The player
     * @param event The portal event
     * @return true if event should be cancelled due to cooldown
     */
    private boolean handleCooldown(@NotNull Player player, @NotNull PlayerPortalEvent event) {
        if (!configManager.isCooldownEnabled()) {
            return false;
        }
        
        if (cooldownManager.hasCooldown(player)) {
            int remainingTime = cooldownManager.getRemainingCooldown(player);
            
            // Send cooldown message
            messageService.sendCooldownMessage(player, remainingTime);
            
            // Apply knockback
            knockbackService.applyKnockbackAsync(player, event.getFrom());
            
            // Cancel event
            event.setCancelled(true);
            return true;
        }
        
        return false;
    }
    
    /**
     * Handles permission checking and messaging
     * Obsługuje sprawdzanie uprawnień i wiadomości
     * 
     * @param player The player
     * @param event The portal event
     * @return true if event should be cancelled due to lack of permissions
     */
    private boolean handlePermissions(@NotNull Player player, @NotNull PlayerPortalEvent event) {
        if (!permissionChecker.hasPortalPermission(player, event)) {
            // Get appropriate message key based on portal type, not target environment
            String messageKey = permissionChecker.getMessageKey(
                permissionChecker.getPortalTypeEnvironment(event)
            );
            
            // Send permission denied message
            messageService.sendPermissionDeniedMessage(player, messageKey);
            
            // Apply knockback
            knockbackService.applyKnockbackAsync(player, event.getFrom());
            
            // Cancel event
            event.setCancelled(true);
            return true;
        }
        
        return false;
    }
    
    /**
     * Applies cooldown to player asynchronously
     * Stosuje cooldown do gracza asynchronicznie
     * 
     * @param player The player
     */
    private void applyCooldown(@NotNull Player player) {
        if (configManager.isCooldownEnabled()) {
            cooldownManager.setCooldownAsync(player);
            
            if (configManager.isDebugMode()) {
                int cooldownTime = configManager.getCooldownTime();
                plugin.getLogger().info(String.format(
                    "Applied %d second cooldown to player %s", 
                    cooldownTime, 
                    player.getName()
                ));
            }
        }
    }
}