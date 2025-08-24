package org.rafalohaki.portalsPermission.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.rafalohaki.portalsPermission.services.IPortalSecurityService;

import java.util.logging.Level;

/**
 * Listener for vehicle security events to prevent portal bypasses
 * Listener dla zdarzeń zabezpieczeń pojazdów zapobiegający bypassom portali
 */
public class VehicleSecurityListener implements Listener {
    
    private final JavaPlugin plugin;
    private final IPortalSecurityService portalSecurityService;
    
    /**
     * Constructor for VehicleSecurityListener
     * Konstruktor dla VehicleSecurityListener
     */
    public VehicleSecurityListener(@NotNull JavaPlugin plugin, @NotNull IPortalSecurityService portalSecurityService) {
        this.plugin = plugin;
        this.portalSecurityService = portalSecurityService;
    }
    
    /**
     * Handles vehicle enter events to track players in vehicles near portals
     * Obsługuje zdarzenia wejścia do pojazdu aby śledzić graczy w pojazdach blisko portali
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleEnter(@NotNull VehicleEnterEvent event) {
        try {
            portalSecurityService.handleVehicleEnter(event);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error handling VehicleEnterEvent for entity: " + event.getEntered().getUniqueId(), e);
        }
    }
    
    /**
     * Handles vehicle exit events to stop tracking players who left vehicles
     * Obsługuje zdarzenia wyjścia z pojazdu aby przestać śledzić graczy którzy opuścili pojazdy
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleExit(@NotNull VehicleExitEvent event) {
        try {
            portalSecurityService.handleVehicleExit(event);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error handling VehicleExitEvent for entity: " + event.getExited().getUniqueId(), e);
        }
    }
}