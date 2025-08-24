package org.rafalohaki.portalsPermission.services;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for portal security service handling bypass prevention
 * Interfejs dla serwisu zabezpieczeń portali zapobiegającego bypassom
 */
public interface IPortalSecurityService {
    
    /**
     * Handles entity entering portal - prevents long-stay bypass
     * Obsługuje wchodzenie encji do portalu - zapobiega bypassowi długiego przebywania
     */
    void handleEntityPortalEnter(@NotNull EntityPortalEnterEvent event);
    
    /**
     * Handles entity portal ready state - controls teleportation readiness
     * Obsługuje stan gotowości portalu encji - kontroluje gotowość do teleportacji
     */
    void handleEntityPortalReady(@NotNull EntityPortalReadyEvent event);
    
    /**
     * Handles vehicle enter event - prevents boat portal bypass
     * Obsługuje zdarzenie wchodzenia do pojazdu - zapobiega bypassowi łodzi w portalu
     */
    void handleVehicleEnter(@NotNull VehicleEnterEvent event);
    
    /**
     * Handles vehicle exit event - manages portal state after vehicle exit
     * Obsługuje zdarzenie wychodzenia z pojazdu - zarządza stanem portalu po wyjściu z pojazdu
     */
    void handleVehicleExit(@NotNull VehicleExitEvent event);
    
    /**
     * Sets portal cooldown for entity asynchronously
     * Ustawia cooldown portalu dla encji asynchronicznie
     */
    CompletableFuture<Void> setEntityPortalCooldownAsync(@NotNull Entity entity, int cooldownTicks);
    
    /**
     * Checks if entity has active portal cooldown
     * Sprawdza czy encja ma aktywny cooldown portalu
     */
    boolean hasEntityPortalCooldown(@NotNull Entity entity);
    
    /**
     * Gets remaining portal cooldown for entity in ticks
     * Pobiera pozostały cooldown portalu dla encji w tickach
     */
    int getRemainingEntityPortalCooldown(@NotNull Entity entity);
    
    /**
     * Removes portal cooldown for entity
     * Usuwa cooldown portalu dla encji
     */
    void removeEntityPortalCooldown(@NotNull Entity entity);
    
    /**
     * Checks if player is in vehicle within portal area
     * Sprawdza czy gracz jest w pojeździe w obszarze portalu
     */
    boolean isPlayerInVehicleInPortal(@NotNull Player player);
    
    /**
     * Tracks entity portal entry time for long-stay detection
     * Śledzi czas wejścia encji do portalu dla wykrywania długiego przebywania
     */
    void trackEntityPortalEntry(@NotNull Entity entity);
    
    /**
     * Removes entity portal entry tracking
     * Usuwa śledzenie wejścia encji do portalu
     */
    void removeEntityPortalTracking(@NotNull Entity entity);
    
    /**
     * Checks if entity has been in portal too long
     * Sprawdza czy encja przebywa w portalu zbyt długo
     */
    boolean hasEntityBeenInPortalTooLong(@NotNull Entity entity);
    
    /**
     * Clears all portal security data
     * Czyści wszystkie dane zabezpieczeń portali
     */
    void clearAllSecurityData();
    
    /**
     * Handles player glide toggle (elytra) to prevent elytra bypasses
     * Obsługuje przełączanie szybowania gracza (elytra) aby zapobiec bypassom z elytrą
     */
    void handlePlayerGlideToggle(@NotNull Player player, boolean isGliding);
    
    /**
     * Handles player elytra boost to prevent elytra bypasses
     * Obsługuje boostowanie elytr gracza aby zapobiec bypassom z elytrą
     */
    void handlePlayerElytraBoost(@NotNull Player player);
    
    /**
     * Handles player movement to prevent fast movement bypasses
     * Obsługuje ruch gracza aby zapobiec bypassom przez szybki ruch
     */
    void handlePlayerMovement(@NotNull Player player, @NotNull Location from, @Nullable Location to);
    
    /**
     * Handles player velocity changes to prevent velocity-based bypasses
     * Obsługuje zmiany prędkości gracza aby zapobiec bypassom opartym na prędkości
     */
    void handlePlayerVelocityChange(@NotNull Player player, @NotNull Vector velocity);

    /**
     * Shuts down the security service
     * Wyłącza serwis zabezpieczeń
     */
    void shutdown();
}