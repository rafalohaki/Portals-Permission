package org.rafalohaki.portalsPermission.events;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.rafalohaki.portalsPermission.services.IPortalSecurityService;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlayerMovementSecurityListener
 * Testy jednostkowe dla PlayerMovementSecurityListener
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlayerMovementSecurityListenerTest {
    
    @Mock
    private JavaPlugin plugin;
    
    @Mock
    private IPortalSecurityService portalSecurityService;
    
    @Mock
    private Logger logger;
    
    @Mock
    private Player player;
    
    @Mock
    private World world;
    
    @Mock
    private Location fromLocation;
    
    @Mock
    private Location toLocation;
    
    @Mock
    private EntityToggleGlideEvent glideEvent;
    
    @Mock
    private PlayerElytraBoostEvent elytraBoostEvent;
    
    @Mock
    private PlayerMoveEvent moveEvent;
    
    @Mock
    private PlayerVelocityEvent velocityEvent;
    
    private PlayerMovementSecurityListener listener;
    private UUID testPlayerId;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        testPlayerId = UUID.randomUUID();
        
        // Setup plugin mock
        when(plugin.getLogger()).thenReturn(logger);
        
        // Setup player mock
        when(player.getUniqueId()).thenReturn(testPlayerId);
        when(player.getName()).thenReturn("TestPlayer");
        when(player.getWorld()).thenReturn(world);
        
        // Setup location mocks
        when(fromLocation.getWorld()).thenReturn(world);
        when(toLocation.getWorld()).thenReturn(world);
        
        // Create listener instance
        listener = new PlayerMovementSecurityListener(plugin, portalSecurityService);
    }
    
    @Test
    void testOnEntityToggleGlide_WithValidEvent_ShouldCallSecurityService() {
        when(glideEvent.getEntity()).thenReturn(player);
        when(glideEvent.isGliding()).thenReturn(true);
        
        listener.onEntityToggleGlide(glideEvent);
        
        verify(portalSecurityService).handlePlayerGlideToggle(player, true);
    }
    
    @Test
    void testOnEntityToggleGlide_WithException_ShouldLogError() {
        when(glideEvent.getEntity()).thenReturn(player);
        when(glideEvent.isGliding()).thenReturn(true);
        when(player.getName()).thenReturn("TestPlayer");
        doThrow(new RuntimeException("Test exception")).when(portalSecurityService).handlePlayerGlideToggle(any(), anyBoolean());
        
        assertDoesNotThrow(() -> listener.onEntityToggleGlide(glideEvent));
        
        verify(logger).log(eq(Level.SEVERE), contains("Error handling EntityToggleGlideEvent for player: TestPlayer"), any(RuntimeException.class));
    }
    
    @Test
    void testOnPlayerElytraBoost_WithValidEvent_ShouldCallSecurityService() {
        when(elytraBoostEvent.getPlayer()).thenReturn(player);
        
        listener.onPlayerElytraBoost(elytraBoostEvent);
        
        verify(portalSecurityService).handlePlayerElytraBoost(player);
    }
    
    @Test
    void testOnPlayerElytraBoost_WithException_ShouldLogError() {
        when(elytraBoostEvent.getPlayer()).thenReturn(player);
        when(player.getName()).thenReturn("TestPlayer");
        doThrow(new RuntimeException("Test exception")).when(portalSecurityService).handlePlayerElytraBoost(any());
        
        assertDoesNotThrow(() -> listener.onPlayerElytraBoost(elytraBoostEvent));
        
        verify(logger).log(eq(Level.SEVERE), contains("Error handling PlayerElytraBoostEvent for player: TestPlayer"), any(RuntimeException.class));
    }
    
    @Test
    void testOnPlayerMove_WithValidEvent_ShouldCallSecurityService() {
        when(moveEvent.getPlayer()).thenReturn(player);
        when(moveEvent.getFrom()).thenReturn(fromLocation);
        when(moveEvent.getTo()).thenReturn(toLocation);
        when(moveEvent.hasChangedBlock()).thenReturn(true);
        
        listener.onPlayerMove(moveEvent);
        
        verify(portalSecurityService).handlePlayerMovement(player, fromLocation, toLocation);
    }
    
    @Test
    void testOnPlayerMove_WithNullTo_ShouldStillCallService() {
        when(moveEvent.getPlayer()).thenReturn(player);
        when(moveEvent.getFrom()).thenReturn(fromLocation);
        when(moveEvent.getTo()).thenReturn(null);
        when(moveEvent.hasChangedBlock()).thenReturn(true);
        
        listener.onPlayerMove(moveEvent);
        
        verify(portalSecurityService).handlePlayerMovement(player, fromLocation, null);
    }
    
    @Test
    void testOnPlayerMove_WithException_ShouldLogError() {
        when(moveEvent.getPlayer()).thenReturn(player);
        when(moveEvent.getFrom()).thenReturn(fromLocation);
        when(moveEvent.getTo()).thenReturn(toLocation);
        when(moveEvent.hasChangedBlock()).thenReturn(true);
        when(player.getName()).thenReturn("TestPlayer");
        doThrow(new RuntimeException("Test exception")).when(portalSecurityService).handlePlayerMovement(any(), any(), any());
        
        assertDoesNotThrow(() -> listener.onPlayerMove(moveEvent));
        
        verify(logger).log(eq(Level.SEVERE), contains("Error handling PlayerMoveEvent for player: TestPlayer"), any(RuntimeException.class));
    }
    
    @Test
    void testOnPlayerVelocity_WithValidEvent_ShouldCallSecurityService() {
        Vector velocity = new Vector(1.0, 0.5, 0.0);
        when(velocityEvent.getPlayer()).thenReturn(player);
        when(velocityEvent.getVelocity()).thenReturn(velocity);
        
        listener.onPlayerVelocity(velocityEvent);
        
        verify(portalSecurityService).handlePlayerVelocityChange(player, velocity);
    }
    
    @Test
    void testOnPlayerVelocity_WithException_ShouldLogError() {
        Vector velocity = new Vector(1.0, 0.5, 0.0);
        when(velocityEvent.getPlayer()).thenReturn(player);
        when(velocityEvent.getVelocity()).thenReturn(velocity);
        when(player.getName()).thenReturn("TestPlayer");
        doThrow(new RuntimeException("Test exception")).when(portalSecurityService).handlePlayerVelocityChange(any(), any());
        
        assertDoesNotThrow(() -> listener.onPlayerVelocity(velocityEvent));
        
        verify(logger).log(eq(Level.SEVERE), contains("Error handling PlayerVelocityEvent for player: TestPlayer"), any(RuntimeException.class));
    }
    
    @Test
    void testConstructor_WithValidParameters_ShouldCreateInstance() {
        PlayerMovementSecurityListener newListener = new PlayerMovementSecurityListener(plugin, portalSecurityService);
        
        assertNotNull(newListener);
    }
    
    @Test
    void testOnEntityToggleGlide_WithNonPlayerEntity_ShouldNotCallService() {
        when(glideEvent.getEntity()).thenReturn(mock(org.bukkit.entity.Entity.class));
        
        listener.onEntityToggleGlide(glideEvent);
        
        verify(portalSecurityService, never()).handlePlayerGlideToggle(any(), anyBoolean());
    }
    
    @Test
    void testOnEntityToggleGlide_WithGlidingFalse_ShouldCallServiceWithFalse() {
        when(glideEvent.getEntity()).thenReturn(player);
        when(glideEvent.isGliding()).thenReturn(false);
        
        listener.onEntityToggleGlide(glideEvent);
        
        verify(portalSecurityService).handlePlayerGlideToggle(player, false);
    }
    
    @Test
    void testOnPlayerVelocity_WithZeroVelocity_ShouldStillCallService() {
        Vector zeroVelocity = new Vector(0.0, 0.0, 0.0);
        when(velocityEvent.getPlayer()).thenReturn(player);
        when(velocityEvent.getVelocity()).thenReturn(zeroVelocity);
        
        listener.onPlayerVelocity(velocityEvent);
        
        verify(portalSecurityService).handlePlayerVelocityChange(player, zeroVelocity);
    }
    
    @Test
    void testOnPlayerMove_WithSameFromAndTo_ShouldStillCallService() {
        when(moveEvent.getPlayer()).thenReturn(player);
        when(moveEvent.getFrom()).thenReturn(fromLocation);
        when(moveEvent.getTo()).thenReturn(fromLocation); // Same location
        when(moveEvent.hasChangedBlock()).thenReturn(true);
        
        listener.onPlayerMove(moveEvent);
        
        verify(portalSecurityService).handlePlayerMovement(player, fromLocation, fromLocation);
    }
}