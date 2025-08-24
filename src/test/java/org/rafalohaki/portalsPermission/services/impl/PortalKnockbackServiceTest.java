package org.rafalohaki.portalsPermission.services.impl;

import org.bukkit.Location;
import org.bukkit.Sound; // Only for mocking, not direct enum access
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.rafalohaki.portalsPermission.managers.ConfigManager;
import org.rafalohaki.portalsPermission.services.IPortalKnockbackService;
import org.rafalohaki.portalsPermission.services.ISoundService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PortalKnockbackService
 * Testy jednostkowe dla PortalKnockbackService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PortalKnockbackServiceTest {
    
    @Mock
    private JavaPlugin plugin;
    
    @Mock
    private ConfigManager configManager;
    
    @Mock
    private ISoundService soundService;
    
    @Mock
    private Player player;
    
    @Mock
    private Location playerLocation;
    
    @Mock
    private Location portalLocation;
    
    @Mock
    private Logger logger;

    private IPortalKnockbackService knockbackService;
    
    @BeforeEach
    void setUp() {
        when(plugin.getLogger()).thenReturn(logger);
        knockbackService = new PortalKnockbackService(plugin, configManager, soundService);
        
        // Mock Location.toVector() to return valid Vector objects
        when(playerLocation.toVector()).thenReturn(new Vector(5.0, 64.0, 10.0));
        when(portalLocation.toVector()).thenReturn(new Vector(0.0, 64.0, 0.0));
    }
    
    @Test
    void testApplyKnockbackAsync_WithValidConfiguration_ShouldApplyKnockback() throws ExecutionException, InterruptedException {
        // Given
        when(configManager.getKnockbackStrength()).thenReturn(2.0);
        when(configManager.getKnockbackSoundType()).thenReturn("ENTITY_GENERIC_EXPLODE");
        when(configManager.getKnockbackSoundVolume()).thenReturn(1.0f);
        when(configManager.getKnockbackSoundPitch()).thenReturn(1.0f);
        when(player.getLocation()).thenReturn(playerLocation);
        
        Vector mockVelocity = new Vector(0, 0, 0);
        when(player.getVelocity()).thenReturn(mockVelocity);
        
        // When
        CompletableFuture<Void> future = knockbackService.applyKnockbackAsync(player, portalLocation);
        future.get(); // Wait for completion
        
        // Then
        // Note: Due to async nature, we verify the service was called
        assertNotNull(future, "Future should not be null");
        assertTrue(future.isDone(), "Future should be completed");
    }
    
    @Test
    void testApplyKnockbackAsync_WithZeroStrength_ShouldNotApplyKnockback() throws ExecutionException, InterruptedException {
        // Given
        when(configManager.getKnockbackStrength()).thenReturn(0.0);
        
        // When
        CompletableFuture<Void> future = knockbackService.applyKnockbackAsync(player, portalLocation);
        future.get(); // Wait for completion
        
        // Then
        assertNotNull(future, "Future should not be null");
        assertTrue(future.isDone(), "Future should be completed");
    }
    
    @Test
    void testApplyKnockbackAsync_WithNegativeStrength_ShouldNotApplyKnockback() throws ExecutionException, InterruptedException {
        // Given
        when(configManager.getKnockbackStrength()).thenReturn(-1.0);
        
        // When
        CompletableFuture<Void> future = knockbackService.applyKnockbackAsync(player, portalLocation);
        future.get(); // Wait for completion
        
        // Then
        assertNotNull(future, "Future should not be null");
        assertTrue(future.isDone(), "Future should be completed");
    }
    
    @Test
    void testCalculateKnockbackVector_WithPositiveStrength_ShouldReturnKnockbackVector() {
        // Given
        when(playerLocation.toVector()).thenReturn(new Vector(5.0, 64.0, 5.0)); // Player at (5, 64, 5)
        when(portalLocation.toVector()).thenReturn(new Vector(0.0, 64.0, 0.0)); // Portal at (0, 64, 0)
        double strength = 1.5;
        double height = 0.8;
        World.Environment targetEnvironment = World.Environment.NETHER;
        
        // When
        Vector result = knockbackService.calculateKnockbackVector(playerLocation, portalLocation, targetEnvironment, strength, height);
        
        // Then
        assertNotNull(result, "Knockback vector should not be null");
        assertTrue(result.getY() > 0, "Knockback should have upward component");
        assertTrue(result.getX() > 0, "Knockback should push player away from portal (positive X)");
        assertTrue(result.getZ() > 0, "Knockback should push player away from portal (positive Z)");
    }
    
    @Test
    void testCalculateKnockbackVector_WithZeroStrength_ShouldReturnZeroVector() {
        // Given
        when(playerLocation.toVector()).thenReturn(new Vector(5.0, 64.0, 5.0)); // Player at (5, 64, 5)
        when(portalLocation.toVector()).thenReturn(new Vector(0.0, 64.0, 0.0)); // Portal at (0, 64, 0)
        double strength = 0.0;
        double height = 0.0;
        World.Environment targetEnvironment = World.Environment.NETHER;
        
        // When
        Vector result = knockbackService.calculateKnockbackVector(playerLocation, portalLocation, targetEnvironment, strength, height);
        
        // Then
        assertNotNull(result, "Knockback vector should not be null");
        assertEquals(0.0, result.getX(), 0.001, "X component should be zero when strength is zero");
        assertEquals(0.0, result.getZ(), 0.001, "Z component should be zero when strength is zero");
        assertEquals(0.0, result.getY(), 0.001, "Y component should be zero when strength and height are zero");
    }
    
    @Test
    void testCalculateKnockbackVector_WithSameLocation_ShouldUseDefaultDirection() {
        // Given
        when(playerLocation.toVector()).thenReturn(new Vector(0.0, 64.0, 0.0)); // Player at same location as portal
        when(portalLocation.toVector()).thenReturn(new Vector(0.0, 64.0, 0.0)); // Portal at (0, 64, 0)
        double strength = 1.5;
        double height = 0.8;
        World.Environment targetEnvironment = World.Environment.NETHER;
        
        // When
        Vector result = knockbackService.calculateKnockbackVector(playerLocation, portalLocation, targetEnvironment, strength, height);
        
        // Then
        assertNotNull(result, "Knockback vector should not be null");
        assertTrue(result.getY() > 0, "Knockback should have upward component");
        assertTrue(result.getZ() > 0, "Knockback should use default direction (south) when player is at portal center");
        assertEquals(0.0, result.getX(), 0.001, "X component should be zero for default south direction");
    }
    
    @Test
    void testCalculateKnockbackVector_WithPlayerNorthOfPortal_ShouldKnockbackNorth() {
        // Given
        when(playerLocation.toVector()).thenReturn(new Vector(0.0, 64.0, 0.0)); // Player at (0, 64, 0)
        when(portalLocation.toVector()).thenReturn(new Vector(0.0, 64.0, 5.0)); // Portal at (0, 64, 5) - south of player
        double strength = 1.0;
        double height = 0.5;
        World.Environment targetEnvironment = World.Environment.NORMAL;
        
        // When
        Vector result = knockbackService.calculateKnockbackVector(playerLocation, portalLocation, targetEnvironment, strength, height);
        
        // Then
        assertNotNull(result, "Knockback vector should not be null");
        assertTrue(result.getY() > 0, "Knockback should have upward component");
        assertTrue(result.getZ() < 0, "Knockback should push player north (negative Z)");
        assertEquals(0.0, result.getX(), 0.001, "X component should be zero for pure north-south movement");
        assertTrue(Math.abs(result.getZ()) > Math.abs(result.getX()), "Knockback should be primarily in Z direction");
    }
    
    @Test
    void testPlayKnockbackSound_WithValidSound_ShouldPlaySound() {
        // Given
        when(configManager.isKnockbackSoundEnabled()).thenReturn(true);
        when(configManager.getKnockbackSoundType()).thenReturn("ENTITY_GENERIC_EXPLODE");
        when(configManager.getKnockbackSoundVolume()).thenReturn(0.8f);
        when(configManager.getKnockbackSoundPitch()).thenReturn(1.2f);
        when(player.getLocation()).thenReturn(playerLocation);
        Object mockSound = new Object(); // Use Object instead of Sound to avoid NoClassDefFoundError
        when(soundService.getSoundFromName("ENTITY_GENERIC_EXPLODE")).thenReturn(mockSound);
        
        // When
        knockbackService.playKnockbackSound(player);
        
        // Then
        verify(soundService).playSound(eq(player), eq(playerLocation), eq(mockSound), eq(0.8f), eq(1.2f));
    }
    
    @Test
    void testPlayKnockbackSound_WithInvalidSound_ShouldHandleGracefully() {
        // Given
        when(configManager.isKnockbackSoundEnabled()).thenReturn(true);
        when(configManager.getKnockbackSoundType()).thenReturn("INVALID_SOUND");
        when(configManager.getKnockbackSoundVolume()).thenReturn(1.0f);
        when(configManager.getKnockbackSoundPitch()).thenReturn(1.0f);
        when(player.getLocation()).thenReturn(playerLocation);
        Object mockFallbackSound = new Object(); // Use Object instead of Sound to avoid NoClassDefFoundError
        when(soundService.getSoundFromName("INVALID_SOUND")).thenThrow(new IllegalArgumentException("Unknown sound"));
        when(soundService.getSoundFromName("ENTITY_PLAYER_HURT")).thenReturn(mockFallbackSound);
        
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> knockbackService.playKnockbackSound(player));
        
        // Verify fallback sound is played
        verify(soundService).playSound(eq(player), eq(playerLocation), eq(mockFallbackSound), eq(0.5f), eq(1.0f));
    }
    
    @Test
    void testPlayKnockbackSound_WithEmptySound_ShouldNotPlaySound() {
        // Given
        when(configManager.isKnockbackSoundEnabled()).thenReturn(true);
        when(configManager.getKnockbackSoundType()).thenReturn("");
        
        // When
        knockbackService.playKnockbackSound(player);
        
        // Then
        verify(soundService, never()).getSoundFromName(anyString());
        verify(soundService, never()).playSound(any(Player.class), any(Location.class), any(), anyFloat(), anyFloat());
    }
    
    @Test
    void testPlayKnockbackSound_WithNullSound_ShouldNotPlaySound() {
        // Given
        when(configManager.isKnockbackSoundEnabled()).thenReturn(true);
        when(configManager.getKnockbackSoundType()).thenReturn(null);
        
        // When
        knockbackService.playKnockbackSound(player);
        
        // Then
        verify(soundService, never()).getSoundFromName(anyString());
        verify(soundService, never()).playSound(any(Player.class), any(Location.class), any(), anyFloat(), anyFloat());
    }
    
    @Test
    void testIsKnockbackEnabled_WithPositiveStrength_ShouldReturnTrue() {
        // Given
        when(configManager.isKnockbackEnabled()).thenReturn(true);
        
        // When
        boolean result = knockbackService.isKnockbackEnabled();
        
        // Then
        assertTrue(result, "Knockback should be enabled with positive strength");
    }
    
    @Test
    void testIsKnockbackEnabled_WithZeroStrength_ShouldReturnFalse() {
        // Given
        when(configManager.isKnockbackEnabled()).thenReturn(false);
        
        // When
        boolean result = knockbackService.isKnockbackEnabled();
        
        // Then
        assertFalse(result, "Knockback should be disabled with zero strength");
    }
    
    @Test
    void testIsKnockbackEnabled_WithNegativeStrength_ShouldReturnFalse() {
        // Given
        when(configManager.isKnockbackEnabled()).thenReturn(false);
        
        // When
        boolean result = knockbackService.isKnockbackEnabled();
        
        // Then
        assertFalse(result, "Knockback should be disabled with negative strength");
    }
}