package org.rafalohaki.portalsPermission.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.rafalohaki.portalsPermission.services.IPortalSecurityService;

import java.util.logging.Level;

/**
 * Listener for player movement security events to prevent portal bypasses
 * Listener dla zdarzeń zabezpieczeń ruchu gracza zapobiegający bypassom portali
 */
public class PlayerMovementSecurityListener implements Listener {
    
    private final JavaPlugin plugin;
    private final IPortalSecurityService portalSecurityService;

    /**
     * Constructor for PlayerMovementSecurityListener
     * Konstruktor dla PlayerMovementSecurityListener
     */
    public PlayerMovementSecurityListener(@NotNull JavaPlugin plugin, @NotNull IPortalSecurityService portalSecurityService) {
        this.plugin = plugin;
        this.portalSecurityService = portalSecurityService;
    }

    /**
     * Handles entity toggle glide events to prevent elytra bypasses
     * Obsługuje zdarzenia przełączania szybowania aby zapobiec bypassom z elytrą
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityToggleGlide(@NotNull EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        try {
            portalSecurityService.handlePlayerGlideToggle(player, event.isGliding());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error handling EntityToggleGlideEvent for player: " + player.getName(), e);
        }
    }

    /**
     * Handles player elytra boost events to prevent elytra bypasses
     * Obsługuje zdarzenia boostowania elytr aby zapobiec bypassom z elytrą
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerElytraBoost(@NotNull PlayerElytraBoostEvent event) {
        Player player = event.getPlayer();
        
        try {
            portalSecurityService.handlePlayerElytraBoost(player);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error handling PlayerElytraBoostEvent for player: " + player.getName(), e);
        }
    }

    /**
     * Handles player move events to prevent fast movement bypasses
     * Obsługuje zdarzenia ruchu gracza aby zapobiec bypassom przez szybki ruch
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Only check if player actually moved to a different block
        if (!event.hasChangedBlock()) {
            return;
        }
        
        try {
            portalSecurityService.handlePlayerMovement(player, event.getFrom(), event.getTo());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error handling PlayerMoveEvent for player: " + player.getName(), e);
        }
    }

    /**
     * Handles player velocity events to prevent velocity-based bypasses
     * Obsługuje zdarzenia prędkości gracza aby zapobiec bypassom opartym na prędkości
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerVelocity(@NotNull PlayerVelocityEvent event) {
        Player player = event.getPlayer();
        
        try {
            portalSecurityService.handlePlayerVelocityChange(player, event.getVelocity());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error handling PlayerVelocityEvent for player: " + player.getName(), e);
        }
    }
}