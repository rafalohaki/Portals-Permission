package org.rafalohaki.portalsPermission.services.impl;

import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rafalohaki.portalsPermission.managers.ConfigManager;
import org.rafalohaki.portalsPermission.services.IPortalMessageService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PortalMessageService
 * Testy jednostkowe dla PortalMessageService
 */
@ExtendWith(MockitoExtension.class)
class PortalMessageServiceTest {
    
    @Mock
    private ConfigManager configManager;
    
    @Mock
    private Player player;
    
    private IPortalMessageService messageService;
    
    @BeforeEach
    void setUp() {
        messageService = new PortalMessageService(configManager);
    }
    
    @Test
    void testSendMessage_WithValidMessage_ShouldSendToPlayer() {
        // Given
        String message = "Test message";
        
        // When
        messageService.sendMessage(player, message);
        
        // Then
        verify(player).sendMessage(any(Component.class));
    }
    
    @Test
    void testSendMessage_WithEmptyMessage_ShouldNotSendToPlayer() {
        // Given
        String message = "";
        
        // When
        messageService.sendMessage(player, message);
        
        // Then
        verify(player, never()).sendMessage(any(Component.class));
    }
    
    @Test
    void testSendPermissionDeniedMessage_WithValidMessageKey_ShouldSendFormattedMessage() {
        // Given
        String messageKey = "messages.nether-denied";
        String expectedMessage = "You don't have permission to use nether portals!";
        when(configManager.getMessage(messageKey)).thenReturn(expectedMessage);
        
        // When
        messageService.sendPermissionDeniedMessage(player, messageKey);
        
        // Then
        verify(player).sendMessage(any(Component.class));
        verify(configManager).getMessage(messageKey);
    }
    
    @Test
    void testSendCooldownMessage_WithValidCooldown_ShouldSendFormattedMessage() {
        // Given
        int cooldownSeconds = 30;
        String expectedMessage = "You must wait 30 seconds before using portals again!";
        when(configManager.getMessage("messages.cooldown", "{time}", "30")).thenReturn(expectedMessage);
        when(configManager.isCooldownMessageEnabled()).thenReturn(true);
        
        // When
        messageService.sendCooldownMessage(player, cooldownSeconds);
        
        // Then
        verify(player).sendMessage(any(Component.class));
        verify(configManager).getMessage("messages.cooldown", "{time}", "30");
        verify(configManager).isCooldownMessageEnabled();
    }
    
    @Test
    void testSendCooldownMessage_WithCooldownDisabled_ShouldNotSendMessage() {
        // Given
        int cooldownSeconds = 30;
        when(configManager.isCooldownMessageEnabled()).thenReturn(false);
        
        // When
        messageService.sendCooldownMessage(player, cooldownSeconds);
        
        // Then
        verify(player, never()).sendMessage(any(Component.class));
        verify(configManager).isCooldownMessageEnabled();
        verify(configManager, never()).getMessage(anyString(), anyString(), anyString());
    }
    
    @Test
    void testIsCooldownMessageEnabled_WhenEnabled_ShouldReturnTrue() {
        // Given
        when(configManager.isCooldownMessageEnabled()).thenReturn(true);
        
        // When
        boolean result = messageService.isCooldownMessageEnabled();
        
        // Then
        assertTrue(result, "Should return true when cooldown messages are enabled");
        verify(configManager).isCooldownMessageEnabled();
    }
    
    @Test
    void testIsCooldownMessageEnabled_WhenDisabled_ShouldReturnFalse() {
        // Given
        when(configManager.isCooldownMessageEnabled()).thenReturn(false);
        
        // When
        boolean result = messageService.isCooldownMessageEnabled();
        
        // Then
        assertFalse(result, "Should return false when cooldown messages are disabled");
        verify(configManager).isCooldownMessageEnabled();
    }
    
    @Test
    void testSendMessage_WithColorCodes_ShouldParseCorrectly() {
        // Given
        String messageWithColors = "&cRed text &aand green text";
        
        // When
        messageService.sendMessage(player, messageWithColors);
        
        // Then
        verify(player).sendMessage(any(Component.class));
    }
    
    @Test
    void testSendPermissionDeniedMessage_WithEmptyMessageKey_ShouldHandleGracefully() {
        // Given
        String messageKey = "";
        when(configManager.getMessage(messageKey)).thenReturn("");
        
        // When & Then
        assertDoesNotThrow(() -> messageService.sendPermissionDeniedMessage(player, messageKey));
        verify(configManager).getMessage(messageKey);
    }
}