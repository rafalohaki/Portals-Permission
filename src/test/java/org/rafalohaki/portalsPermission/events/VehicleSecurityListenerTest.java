package org.rafalohaki.portalsPermission.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.rafalohaki.portalsPermission.services.IPortalSecurityService;

import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VehicleSecurityListener
 * Testy jednostkowe dla VehicleSecurityListener
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VehicleSecurityListenerTest {
    
    @Mock
    private JavaPlugin plugin;
    
    @Mock
    private IPortalSecurityService portalSecurityService;
    
    @Mock
    private Logger logger;
    
    @Mock
    private Vehicle vehicle;
    
    @Mock
    private Player player;
    
    @Mock
    private LivingEntity entity;
    
    @Mock
    private VehicleEnterEvent vehicleEnterEvent;
    
    @Mock
    private VehicleExitEvent vehicleExitEvent;
    
    private VehicleSecurityListener listener;
    private UUID testPlayerId;
    private UUID testEntityId;
    
    @BeforeEach
    void setUp() {
        testPlayerId = UUID.randomUUID();
        testEntityId = UUID.randomUUID();
        
        when(plugin.getLogger()).thenReturn(logger);
        
        when(player.getUniqueId()).thenReturn(testPlayerId);
        when(player.getName()).thenReturn("TestPlayer");
        when(player.getType()).thenReturn(EntityType.PLAYER);
        
        when(entity.getUniqueId()).thenReturn(testEntityId);
        when(entity.getType()).thenReturn(EntityType.ZOMBIE);
        
        when(vehicleEnterEvent.getVehicle()).thenReturn(vehicle);
        when(vehicleEnterEvent.getEntered()).thenReturn(player);
        
        when(vehicleExitEvent.getVehicle()).thenReturn(vehicle);
        when(vehicleExitEvent.getExited()).thenReturn(player);
        
        listener = new VehicleSecurityListener(plugin, portalSecurityService);
    }
    
    @Test
    void testOnVehicleEnter_WithValidEvent_ShouldCallSecurityService() {
        // When
        listener.onVehicleEnter(vehicleEnterEvent);
        
        // Then
        verify(portalSecurityService).handleVehicleEnter(vehicleEnterEvent);
    }
    
    @Test
    void testOnVehicleEnter_WithException_ShouldLogError() {
        // Given
        doThrow(new RuntimeException("Test exception")).when(portalSecurityService).handleVehicleEnter(vehicleEnterEvent);
        
        // When
        listener.onVehicleEnter(vehicleEnterEvent);
        
        // Then
        verify(logger).log(any(), contains("Error handling VehicleEnterEvent"), any(Exception.class));
    }
    
    @Test
    void testOnVehicleExit_WithValidEvent_ShouldCallSecurityService() {
        // When
        listener.onVehicleExit(vehicleExitEvent);
        
        // Then
        verify(portalSecurityService).handleVehicleExit(vehicleExitEvent);
    }
    
    @Test
    void testOnVehicleExit_WithException_ShouldLogError() {
        // Given
        doThrow(new RuntimeException("Test exception")).when(portalSecurityService).handleVehicleExit(vehicleExitEvent);
        
        // When
        listener.onVehicleExit(vehicleExitEvent);
        
        // Then
        verify(logger).log(any(), contains("Error handling VehicleExitEvent"), any(Exception.class));
    }
    
    @Test
    void testConstructor_WithValidParameters_ShouldCreateInstance() {
        // When
        VehicleSecurityListener newListener = new VehicleSecurityListener(plugin, portalSecurityService);
        
        // Then
        assertNotNull(newListener, "Listener should be created successfully");
    }
    
    @Test
    void testOnVehicleEnter_WithPlayerEntity_ShouldHandleCorrectly() {
        // When
        listener.onVehicleEnter(vehicleEnterEvent);
        
        // Then
        verify(portalSecurityService).handleVehicleEnter(vehicleEnterEvent);
    }
    
    @Test
    void testOnVehicleExit_WithPlayerEntity_ShouldHandleCorrectly() {
        // When
        listener.onVehicleExit(vehicleExitEvent);
        
        // Then
        verify(portalSecurityService).handleVehicleExit(vehicleExitEvent);
    }
    
    @Test
    void testOnVehicleEnter_WithNonPlayerEntity_ShouldStillCallService() {
        // Given
        when(vehicleEnterEvent.getEntered()).thenReturn(entity);
        
        // When
        listener.onVehicleEnter(vehicleEnterEvent);
        
        // Then
        verify(portalSecurityService).handleVehicleEnter(vehicleEnterEvent);
    }
    
    @Test
    void testOnVehicleExit_WithNonPlayerEntity_ShouldStillCallService() {
        // Given
        when(vehicleExitEvent.getExited()).thenReturn(entity);
        
        // When
        listener.onVehicleExit(vehicleExitEvent);
        
        // Then
        verify(portalSecurityService).handleVehicleExit(vehicleExitEvent);
    }
    
    @Test
    void testOnVehicleEnter_WithNullEntity_ShouldHandleGracefully() {
        // Given
        when(vehicleEnterEvent.getEntered()).thenReturn(null);
        
        // When
        listener.onVehicleEnter(vehicleEnterEvent);
        
        // Then
        verify(portalSecurityService).handleVehicleEnter(vehicleEnterEvent);
    }
    
    @Test
    void testOnVehicleExit_WithNullEntity_ShouldHandleGracefully() {
        // Given
        when(vehicleExitEvent.getExited()).thenReturn(null);
        
        // When
        listener.onVehicleExit(vehicleExitEvent);
        
        // Then
        verify(portalSecurityService).handleVehicleExit(vehicleExitEvent);
    }
}