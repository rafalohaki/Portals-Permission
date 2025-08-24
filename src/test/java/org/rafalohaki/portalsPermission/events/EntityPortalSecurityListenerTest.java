package org.rafalohaki.portalsPermission.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
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
 * Unit tests for EntityPortalSecurityListener
 * Testy jednostkowe dla EntityPortalSecurityListener
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EntityPortalSecurityListenerTest {
    
    @Mock
    private JavaPlugin plugin;
    
    @Mock
    private IPortalSecurityService portalSecurityService;
    
    @Mock
    private Logger logger;
    
    @Mock
    private Entity entity;
    
    @Mock
    private EntityPortalEnterEvent portalEnterEvent;
    
    @Mock
    private EntityPortalReadyEvent portalReadyEvent;
    
    private EntityPortalSecurityListener listener;
    private UUID testEntityId;
    
    @BeforeEach
    void setUp() {
        testEntityId = UUID.randomUUID();
        
        when(plugin.getLogger()).thenReturn(logger);
        when(entity.getUniqueId()).thenReturn(testEntityId);
        when(entity.getType()).thenReturn(EntityType.ZOMBIE);
        
        when(portalEnterEvent.getEntity()).thenReturn(entity);
        when(portalReadyEvent.getEntity()).thenReturn(entity);
        
        listener = new EntityPortalSecurityListener(plugin, portalSecurityService);
    }
    
    @Test
    void testOnEntityPortalEnter_WithValidEvent_ShouldCallSecurityService() {
        // When
        listener.onEntityPortalEnter(portalEnterEvent);
        
        // Then
        verify(portalSecurityService).handleEntityPortalEnter(portalEnterEvent);
    }
    
    @Test
    void testOnEntityPortalEnter_WithException_ShouldLogError() {
        // Given
        doThrow(new RuntimeException("Test exception")).when(portalSecurityService).handleEntityPortalEnter(portalEnterEvent);
        
        // When
        listener.onEntityPortalEnter(portalEnterEvent);
        
        // Then
        verify(logger).log(any(), contains("Error handling EntityPortalEnterEvent"), any(Exception.class));
    }
    
    @Test
    void testOnEntityPortalReady_WithValidEvent_ShouldCallSecurityService() {
        // When
        listener.onEntityPortalReady(portalReadyEvent);
        
        // Then
        verify(portalSecurityService).handleEntityPortalReady(portalReadyEvent);
    }
    
    @Test
    void testOnEntityPortalReady_WithException_ShouldLogError() {
        // Given
        doThrow(new RuntimeException("Test exception")).when(portalSecurityService).handleEntityPortalReady(portalReadyEvent);
        
        // When
        listener.onEntityPortalReady(portalReadyEvent);
        
        // Then
        verify(logger).log(any(), contains("Error handling EntityPortalReadyEvent"), any(Exception.class));
    }
    
    @Test
    void testConstructor_WithValidParameters_ShouldCreateInstance() {
        // When
        EntityPortalSecurityListener newListener = new EntityPortalSecurityListener(plugin, portalSecurityService);
        
        // Then
        assertNotNull(newListener, "Listener should be created successfully");
    }
    
    @Test
    void testOnEntityPortalEnter_WithNullEntity_ShouldHandleGracefully() {
        // Given
        when(portalEnterEvent.getEntity()).thenReturn(null);
        
        // When
        listener.onEntityPortalEnter(portalEnterEvent);
        
        // Then
        verify(portalSecurityService).handleEntityPortalEnter(portalEnterEvent);
    }
    
    @Test
    void testOnEntityPortalReady_WithNullEntity_ShouldHandleGracefully() {
        // Given
        when(portalReadyEvent.getEntity()).thenReturn(null);
        
        // When
        listener.onEntityPortalReady(portalReadyEvent);
        
        // Then
        verify(portalSecurityService).handleEntityPortalReady(portalReadyEvent);
    }
}