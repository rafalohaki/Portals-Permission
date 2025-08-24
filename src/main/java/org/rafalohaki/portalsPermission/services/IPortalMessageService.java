package org.rafalohaki.portalsPermission.services;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for portal message service
 * Interfejs dla serwisu wiadomości portali
 */
public interface IPortalMessageService {
    
    /**
     * Sends a message to player using Adventure Components
     * Wysyła wiadomość do gracza używając Adventure Components
     * 
     * @param player The player to send message to
     * @param message The message to send
     */
    void sendMessage(@NotNull Player player, @NotNull String message);
    
    /**
     * Sends a cooldown message to player
     * Wysyła wiadomość o cooldownie do gracza
     * 
     * @param player The player to send message to
     * @param remainingTime The remaining cooldown time in seconds
     */
    void sendCooldownMessage(@NotNull Player player, int remainingTime);
    
    /**
     * Sends a permission denied message to player
     * Wysyła wiadomość o braku uprawnień do gracza
     * 
     * @param player The player to send message to
     * @param messageKey The message key from configuration
     */
    void sendPermissionDeniedMessage(@NotNull Player player, @NotNull String messageKey);
    
    /**
     * Checks if cooldown messages are enabled
     * Sprawdza czy wiadomości o cooldownie są włączone
     * 
     * @return true if cooldown messages are enabled, false otherwise
     */
    boolean isCooldownMessageEnabled();
}