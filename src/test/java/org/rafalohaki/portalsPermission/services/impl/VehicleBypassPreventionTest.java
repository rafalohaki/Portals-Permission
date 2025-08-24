package org.rafalohaki.portalsPermission.services.impl;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Boat;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Server;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.rafalohaki.portalsPermission.managers.ConfigManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for vehicle bypass prevention in PortalSecurityService
 * Testy jednostkowe dla zapobiegania bypassom pojazdami w PortalSecurityService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VehicleBypassPreventionTest {
    
    @Mock
    private JavaPlugin plugin;
    
    @Mock
    private Server server;
    
    @Mock
    private BukkitScheduler scheduler;
    
    @Mock
    private ConfigManager configManager;
    
    @Mock
    private Logger logger;
    
    @Mock
    private EntityPortalEnterEvent portalEnterEvent;
    
    @Mock
    private Vehicle vehicle;
    
    @Mock
    private Boat oakBoat;
    
    @Mock
    private Player player1;
    
    @Mock
    private Player player2;
    
    @Mock
    private Entity nonPlayerEntity;
    
    private PortalSecurityService portalSecurityService;
    private UUID testPlayer1Id;
    private UUID testPlayer2Id;
    private UUID testEntityId;
    
    @BeforeEach
    void setUp() {
        testPlayer1Id = UUID.randomUUID();
        testPlayer2Id = UUID.randomUUID();
        testEntityId = UUID.randomUUID();
        
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);
        when(configManager.isEnabled()).thenReturn(true);
        when(configManager.isDebugMode()).thenReturn(true);
        
        when(player1.getUniqueId()).thenReturn(testPlayer1Id);
        when(player1.getName()).thenReturn("TestPlayer1");
        
        when(player2.getUniqueId()).thenReturn(testPlayer2Id);
        when(player2.getName()).thenReturn("TestPlayer2");
        
        when(nonPlayerEntity.getUniqueId()).thenReturn(testEntityId);
        
        when(portalEnterEvent.getEntity()).thenReturn(vehicle);
        
        portalSecurityService = new PortalSecurityService(plugin, configManager);
    }
    
    @Test
    void testHandleEntityPortalEnter_WithVehicleAndSinglePlayerPassenger_ShouldBlockAndSetCooldown() {
        // Given
        UUID vehicleId = UUID.randomUUID();
        
        List<Entity> passengers = Collections.singletonList(player1);
        when(vehicle.getPassengers()).thenReturn(passengers);
        when(vehicle.getType()).thenReturn(org.bukkit.entity.EntityType.OAK_BOAT);
        when(vehicle.getUniqueId()).thenReturn(vehicleId);
        
        // When
        portalSecurityService.handleEntityPortalEnter(portalEnterEvent);
        
        // Then
        verify(portalEnterEvent).setCancelled(true);
        verify(logger).info(contains("Player TestPlayer1 and their vehicle OAK_BOAT blocked from portal entry"));
        verify(logger).info(contains("Vehicle OAK_BOAT with 1 passengers blocked from portal"));
    }
    
    @Test
    void testHandleEntityPortalEnter_WithOakBoatAndMultiplePassengers_ShouldBlockAll() {
        // Given
        UUID boatId = UUID.randomUUID();
        
        List<Entity> passengers = Arrays.asList(player1, player2);
        when(portalEnterEvent.getEntity()).thenReturn(oakBoat);
        when(oakBoat.getPassengers()).thenReturn(passengers);
        when(oakBoat.getType()).thenReturn(org.bukkit.entity.EntityType.OAK_BOAT);
        when(oakBoat.getUniqueId()).thenReturn(boatId);
        
        // When
        portalSecurityService.handleEntityPortalEnter(portalEnterEvent);
        
        // Then
        verify(portalEnterEvent).setCancelled(true);
        verify(logger).info(contains("Player TestPlayer1 and their vehicle OAK_BOAT blocked from portal entry"));
        verify(logger).info(contains("Player TestPlayer2 and their vehicle OAK_BOAT blocked from portal entry"));
        verify(logger).info(contains("Vehicle OAK_BOAT with 2 passengers blocked from portal"));
    }
    
    @Test
    void testHandleEntityPortalEnter_WithVehicleAndNonPlayerPassenger_ShouldStillBlock() {
        // Given
        UUID vehicleId = UUID.randomUUID();
        
        List<Entity> passengers = Collections.singletonList(nonPlayerEntity);
        when(vehicle.getPassengers()).thenReturn(passengers);
        when(vehicle.getType()).thenReturn(org.bukkit.entity.EntityType.MINECART);
        when(vehicle.getUniqueId()).thenReturn(vehicleId);
        
        // When
        portalSecurityService.handleEntityPortalEnter(portalEnterEvent);
        
        // Then
        verify(portalEnterEvent).setCancelled(true);
        verify(logger).info(contains("Vehicle MINECART with 1 passengers blocked from portal"));
    }
    
    @Test
    void testHandleEntityPortalEnter_WithEmptyVehicle_ShouldNotBlock() {
        // Given
        UUID vehicleId = UUID.randomUUID();
        
        List<Entity> passengers = Collections.emptyList();
        when(vehicle.getPassengers()).thenReturn(passengers);
        when(vehicle.getUniqueId()).thenReturn(vehicleId);
        
        // When
        portalSecurityService.handleEntityPortalEnter(portalEnterEvent);
        
        // Then
        verify(portalEnterEvent, never()).setCancelled(true);
        verify(logger).info(contains("Entity " + vehicle.getType() + " entered portal area"));
    }
    
    @Test
    void testHandleEntityPortalEnter_WithNonVehicleEntity_ShouldNotCheckPassengers() {
        // Given
        when(portalEnterEvent.getEntity()).thenReturn(player1);
        
        // When
        portalSecurityService.handleEntityPortalEnter(portalEnterEvent);
        
        // Then
        verify(portalEnterEvent, never()).setCancelled(true);
        // Should not try to get passengers from non-vehicle entity
    }
    
    @Test
    void testHandleEntityPortalEnter_WithDisabledConfig_ShouldNotProcess() {
        // Given
        when(configManager.isEnabled()).thenReturn(false);
        UUID vehicleId = UUID.randomUUID();
        
        List<Entity> passengers = Collections.singletonList(player1);
        when(vehicle.getPassengers()).thenReturn(passengers);
        when(vehicle.getUniqueId()).thenReturn(vehicleId);
        
        // Reset logger interactions after service initialization
        reset(logger);
        
        // When
        portalSecurityService.handleEntityPortalEnter(portalEnterEvent);
        
        // Then
        verify(portalEnterEvent, never()).setCancelled(true);
        verify(logger, never()).info(anyString());
    }
    
    @Test
    void testHandleEntityPortalEnter_WithMixedPassengers_ShouldHandleBoth() {
        // Given
        UUID vehicleId = UUID.randomUUID();
        
        List<Entity> passengers = Arrays.asList(player1, nonPlayerEntity, player2);
        when(vehicle.getPassengers()).thenReturn(passengers);
        when(vehicle.getType()).thenReturn(org.bukkit.entity.EntityType.OAK_BOAT);
        when(vehicle.getUniqueId()).thenReturn(vehicleId);
        
        // When
        portalSecurityService.handleEntityPortalEnter(portalEnterEvent);
        
        // Then
        verify(portalEnterEvent).setCancelled(true);
        verify(logger).info(contains("Player TestPlayer1 and their vehicle OAK_BOAT blocked from portal entry"));
        verify(logger).info(contains("Player TestPlayer2 and their vehicle OAK_BOAT blocked from portal entry"));
        verify(logger).info(contains("Vehicle OAK_BOAT with 3 passengers blocked from portal"));
    }
}