package org.rafalohaki.portalsPermission.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.rafalohaki.portalsPermission.services.IPortalSecurityService;

import java.util.logging.Level;

/**
 * Listener for entity portal security events to prevent bypasses
 * Listener dla zdarzeń zabezpieczeń portali encji zapobiegający bypassom
 */
public class EntityPortalSecurityListener implements Listener {
    
    private final JavaPlugin plugin;
    private final IPortalSecurityService portalSecurityService;
    
    /**
     * Constructor for EntityPortalSecurityListener
     * Konstruktor dla EntityPortalSecurityListener
     */
    public EntityPortalSecurityListener(@NotNull JavaPlugin plugin, @NotNull IPortalSecurityService portalSecurityService) {
        this.plugin = plugin;
        this.portalSecurityService = portalSecurityService;
    }
    
    /**
     * Handles entity portal enter events to prevent long-stay bypasses
     * Obsługuje zdarzenia wejścia encji do portalu aby zapobiec bypassom przez długie przebywanie
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortalEnter(@NotNull EntityPortalEnterEvent event) {
        try {
            portalSecurityService.handleEntityPortalEnter(event);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error handling EntityPortalEnterEvent for entity: " + event.getEntity().getUniqueId(), e);
        }
    }
    
    /**
     * Handles entity portal ready events to prevent teleportation bypasses
     * Obsługuje zdarzenia gotowości portalu encji aby zapobiec bypassom teleportacji
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortalReady(@NotNull EntityPortalReadyEvent event) {
        try {
            portalSecurityService.handleEntityPortalReady(event);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error handling EntityPortalReadyEvent for entity: " + event.getEntity().getUniqueId(), e);
        }
    }
}