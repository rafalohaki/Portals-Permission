package org.rafalohaki.portalsPermission.services.impl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPortalEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.rafalohaki.portalsPermission.managers.ConfigManager;
import org.rafalohaki.portalsPermission.services.IPortalPermissionChecker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PortalPermissionChecker
 * Testy jednostkowe dla PortalPermissionChecker
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PortalPermissionCheckerTest {
    
    @Mock
    private ConfigManager configManager;
    
    @Mock
    private Player player;
    
    @Mock
    private PlayerPortalEvent event;
    
    private IPortalPermissionChecker permissionChecker;
    
    @BeforeEach
    void setUp() {
        permissionChecker = new PortalPermissionChecker(configManager);
    }
    
    @Test
    void testHasPortalPermission_WithValidNetherPermission_ShouldReturnTrue() {
        // Given
        Location fromLocation = mock(Location.class);
        World fromWorld = mock(World.class);
        Block block = mock(Block.class);
        when(fromLocation.getBlock()).thenReturn(block);
        when(fromLocation.getWorld()).thenReturn(fromWorld);
        when(fromWorld.getEnvironment()).thenReturn(World.Environment.NORMAL);
        when(block.getType()).thenReturn(Material.NETHER_PORTAL);
        when(event.getFrom()).thenReturn(fromLocation);
        when(player.hasPermission("portals.nether")).thenReturn(true);
        when(player.hasPermission("portals.bypass")).thenReturn(false);
        when(configManager.isNetherBlocked()).thenReturn(false);
        
        // When
        boolean result = permissionChecker.hasPortalPermission(player, event);
        
        // Then
        assertTrue(result, "Player with nether permission should be allowed to use nether portal");
    }
    
    @Test
    void testHasPortalPermission_WithoutNetherPermission_ShouldReturnFalse() {
        // Given
        Location fromLocation = mock(Location.class);
        World fromWorld = mock(World.class);
        Block block = mock(Block.class);
        when(fromLocation.getBlock()).thenReturn(block);
        when(fromLocation.getWorld()).thenReturn(fromWorld);
        when(fromWorld.getEnvironment()).thenReturn(World.Environment.NORMAL);
        when(block.getType()).thenReturn(Material.NETHER_PORTAL);
        when(event.getFrom()).thenReturn(fromLocation);
        when(player.hasPermission("portals.nether")).thenReturn(false);
        when(player.hasPermission("portals.bypass")).thenReturn(false);
        when(configManager.isNetherBlocked()).thenReturn(false);
        
        // When
        boolean result = permissionChecker.hasPortalPermission(player, event);
        
        // Then
        assertFalse(result, "Player without nether permission should not be allowed to use nether portal");
    }
    
    @Test
    void testHasPortalPermission_WithBlockedNether_ShouldReturnFalse() {
        // Given
        Location fromLocation = mock(Location.class);
        World fromWorld = mock(World.class);
        Block block = mock(Block.class);
        when(fromLocation.getBlock()).thenReturn(block);
        when(fromLocation.getWorld()).thenReturn(fromWorld);
        when(fromWorld.getEnvironment()).thenReturn(World.Environment.NORMAL);
        when(block.getType()).thenReturn(Material.NETHER_PORTAL);
        when(event.getFrom()).thenReturn(fromLocation);
        when(player.hasPermission("portals.bypass")).thenReturn(false);
        when(configManager.isNetherBlocked()).thenReturn(true);
        
        // When
        boolean result = permissionChecker.hasPortalPermission(player, event);
        
        // Then
        assertFalse(result, "Player should not be allowed to use blocked nether portal");
    }
    
    @Test
    void testHasPortalPermission_WithValidEndPermission_ShouldReturnTrue() {
        // Given
        Location fromLocation = mock(Location.class);
        World fromWorld = mock(World.class);
        Block block = mock(Block.class);
        when(fromLocation.getBlock()).thenReturn(block);
        when(fromLocation.getWorld()).thenReturn(fromWorld);
        when(fromWorld.getEnvironment()).thenReturn(World.Environment.NORMAL);
        when(block.getType()).thenReturn(Material.END_PORTAL);
        when(event.getFrom()).thenReturn(fromLocation);
        when(player.hasPermission("portals.end")).thenReturn(true);
        when(player.hasPermission("portals.bypass")).thenReturn(false);
        when(configManager.isEndBlocked()).thenReturn(false);
        
        // When
        boolean result = permissionChecker.hasPortalPermission(player, event);
        
        // Then
        assertTrue(result, "Player with end permission should be allowed to use end portal");
    }
    
    @Test
    void testHasPortalPermission_WithoutEndPermission_ShouldReturnFalse() {
        // Given
        Location fromLocation = mock(Location.class);
        World fromWorld = mock(World.class);
        Block block = mock(Block.class);
        when(fromLocation.getBlock()).thenReturn(block);
        when(fromLocation.getWorld()).thenReturn(fromWorld);
        when(fromWorld.getEnvironment()).thenReturn(World.Environment.NORMAL);
        when(block.getType()).thenReturn(Material.END_PORTAL);
        when(event.getFrom()).thenReturn(fromLocation);
        when(player.hasPermission("portals.end")).thenReturn(false);
        
        // When
        boolean result = permissionChecker.hasPortalPermission(player, event);
        
        // Then
        assertFalse(result, "Player without end permission should not be allowed to use end portal");
    }
    
    @Test
    void testHasPortalPermission_WithBlockedEnd_ShouldReturnFalse() {
        // Given
        Location fromLocation = mock(Location.class);
        World fromWorld = mock(World.class);
        Block block = mock(Block.class);
        when(fromLocation.getBlock()).thenReturn(block);
        when(fromLocation.getWorld()).thenReturn(fromWorld);
        when(fromWorld.getEnvironment()).thenReturn(World.Environment.NORMAL);
        when(block.getType()).thenReturn(Material.END_PORTAL);
        when(event.getFrom()).thenReturn(fromLocation);
        when(configManager.isEndBlocked()).thenReturn(true);
        
        // When
        boolean result = permissionChecker.hasPortalPermission(player, event);
        
        // Then
        assertFalse(result, "Player should not be allowed to use blocked end portal");
    }
    
    @Test
    void testHasBypassPermission_WithBypassPermission_ShouldReturnTrue() {
        // Given
        when(player.hasPermission("portals.bypass")).thenReturn(true);
        
        // When
        boolean result = permissionChecker.hasBypassPermission(player);
        
        // Then
        assertTrue(result, "Player with bypass permission should be allowed");
    }
    
    @Test
    void testHasBypassPermission_WithoutBypassPermission_ShouldReturnFalse() {
        // Given
        when(player.hasPermission("portals.bypass")).thenReturn(false);
        
        // When
        boolean result = permissionChecker.hasBypassPermission(player);
        
        // Then
        assertFalse(result, "Player without bypass permission should not be allowed");
    }
    
    @Test
    void testGetTargetEnvironment_WithNetherEvent_ShouldReturnNether() {
        // Given
        Location fromLocation = mock(Location.class);
        World fromWorld = mock(World.class);
        Block block = mock(Block.class);
        when(fromLocation.getBlock()).thenReturn(block);
        when(fromLocation.getWorld()).thenReturn(fromWorld);
        when(fromWorld.getEnvironment()).thenReturn(World.Environment.NORMAL);
        when(block.getType()).thenReturn(Material.NETHER_PORTAL);
        when(event.getFrom()).thenReturn(fromLocation);
        
        // When
        World.Environment result = permissionChecker.getTargetEnvironment(event);
        
        // Then
        assertEquals(World.Environment.NETHER, result, "Should return NETHER environment");
    }
    
    @Test
    void testGetTargetEnvironment_WithEndEvent_ShouldReturnEnd() {
        // Given
        Location fromLocation = mock(Location.class);
        World fromWorld = mock(World.class);
        Block block = mock(Block.class);
        when(fromLocation.getBlock()).thenReturn(block);
        when(fromLocation.getWorld()).thenReturn(fromWorld);
        when(fromWorld.getEnvironment()).thenReturn(World.Environment.NORMAL);
        when(block.getType()).thenReturn(Material.END_PORTAL);
        when(event.getFrom()).thenReturn(fromLocation);
        
        // When
        World.Environment result = permissionChecker.getTargetEnvironment(event);
        
        // Then
        assertEquals(World.Environment.THE_END, result, "Should return THE_END environment");
    }
    
    @Test
    void testGetRequiredPermission_WithNetherEnvironment_ShouldReturnNetherPermission() {
        // Given
        World.Environment environment = World.Environment.NETHER;
        
        // When
        String result = permissionChecker.getRequiredPermission(environment);
        
        // Then
        assertEquals("portals.nether", result, "Should return nether permission");
    }
    
    @Test
    void testGetRequiredPermission_WithEndEnvironment_ShouldReturnEndPermission() {
        // Given
        World.Environment environment = World.Environment.THE_END;
        
        // When
        String result = permissionChecker.getRequiredPermission(environment);
        
        // Then
        assertEquals("portals.end", result, "Should return end permission");
    }
    
    @Test
    void testGetRequiredPermission_WithNormalEnvironment_ShouldReturnCustomPermission() {
        // Given
        World.Environment environment = World.Environment.NORMAL;
        
        // When
        String result = permissionChecker.getRequiredPermission(environment);
        
        // Then
        assertEquals("portals.custom", result, "Should return custom permission for normal world");
    }
    
    @Test
    void testGetMessageKey_WithNetherEnvironment_ShouldReturnNetherMessage() {
        // When
        String result = permissionChecker.getMessageKey(World.Environment.NETHER);
        
        // Then
        assertEquals("no_permission_nether", result, "Should return nether message key");
    }
    
    @Test
    void testGetMessageKey_WithEndEnvironment_ShouldReturnEndMessage() {
        // When
        String result = permissionChecker.getMessageKey(World.Environment.THE_END);
        
        // Then
        assertEquals("no_permission_end", result, "Should return end message key");
    }
    
    @Test
    void testGetMessageKey_WithNormalEnvironment_ShouldReturnDefaultMessage() {
        // When
        String result = permissionChecker.getMessageKey(World.Environment.NORMAL);
        
        // Then
        assertEquals("no_permission_custom", result, "Should return default message key for normal environment");
    }
    
    @Test
    void testIsPortalTypeBlocked_WithBlockedNether_ShouldReturnTrue() {
        // Given
        when(configManager.isNetherBlocked()).thenReturn(true);
        
        // When
        boolean result = permissionChecker.isPortalTypeBlocked(World.Environment.NETHER);
        
        // Then
        assertTrue(result, "Nether portal should be blocked when configured");
    }
    
    @Test
    void testIsPortalTypeBlocked_WithUnblockedNether_ShouldReturnFalse() {
        // Given
        when(configManager.isNetherBlocked()).thenReturn(false);
        
        // When
        boolean result = permissionChecker.isPortalTypeBlocked(World.Environment.NETHER);
        
        // Then
        assertFalse(result, "Nether portal should not be blocked when not configured");
    }
    
    @Test
    void testIsPortalTypeBlocked_WithBlockedEnd_ShouldReturnTrue() {
        // Given
        when(configManager.isEndBlocked()).thenReturn(true);
        
        // When
        boolean result = permissionChecker.isPortalTypeBlocked(World.Environment.THE_END);
        
        // Then
        assertTrue(result, "End portal should be blocked when configured");
    }
    
    @Test
    void testIsPortalTypeBlocked_WithUnblockedEnd_ShouldReturnFalse() {
        // Given
        when(configManager.isEndBlocked()).thenReturn(false);
        
        // When
        boolean result = permissionChecker.isPortalTypeBlocked(World.Environment.THE_END);
        
        // Then
        assertFalse(result, "End portal should not be blocked when not configured");
    }
    
    @Test
    void testIsPortalTypeBlocked_WithCustomPortal_ShouldReturnCustomBlockedStatus() {
        // Given
        when(configManager.isCustomBlocked()).thenReturn(true);
        
        // When
        boolean result = permissionChecker.isPortalTypeBlocked(World.Environment.NORMAL);
        
        // Then
        assertTrue(result, "Custom portal should be blocked when configured");
    }
}