package org.rafalohaki.portalsPermission.services.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rafalohaki.portalsPermission.managers.ConfigManager;
import org.rafalohaki.portalsPermission.services.IPortalMessageService;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Implementation of portal message service using Adventure Components
 * Implementacja serwisu wiadomości portali używając Adventure Components
 */
public class PortalMessageService implements IPortalMessageService {
    
    private final ConfigManager configManager;
    private static final Logger LOGGER = Logger.getLogger(PortalMessageService.class.getName());
    
    /**
     * Constructor for PortalMessageService
     * Konstruktor dla PortalMessageService
     * 
     * @param configManager The configuration manager
     */
    public PortalMessageService(@NotNull ConfigManager configManager) {
        this.configManager = Objects.requireNonNull(configManager, "ConfigManager cannot be null");
    }
    
    /**
     * Sends a message to player using Adventure Components
     * Wysyła wiadomość do gracza używając Adventure Components
     * 
     * @param player The player to send message to
     * @param message The message to send
     */
    @Override
    public void sendMessage(@NotNull Player player, @NotNull String message) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(message, "Message cannot be null");
        
        if (!player.isOnline()) {
            return; // Skip sending message to offline player
        }
        
        if (message.isEmpty()) {
            return;
        }
        
        // Parse color codes and create Adventure Component
        Component component = parseMessage(message);
        player.sendMessage(component);
    }
    
    /**
     * Sends a cooldown message to player
     * Wysyła wiadomość o cooldownie do gracza
     * 
     * @param player The player to send message to
     * @param remainingTime The remaining cooldown time in seconds
     */
    @Override
    public void sendCooldownMessage(@NotNull Player player, int remainingTime) {
        Objects.requireNonNull(player, "Player cannot be null");
        
        if (!player.isOnline()) {
            return; // Skip sending message to offline player
        }
        
        if (!isCooldownMessageEnabled()) {
            return;
        }
        
        String cooldownMessage = configManager.getMessage("messages.cooldown", "{time}", String.valueOf(remainingTime));
        
        Component component = Component.text()
            .append(Component.text("⏰ ", NamedTextColor.YELLOW))
            .append(parseMessage(cooldownMessage))
            .color(NamedTextColor.GOLD)
            .build();
            
        player.sendMessage(component);
    }
    
    /**
     * Sends a permission denied message to player
     * Wysyła wiadomość o braku uprawnień do gracza
     * 
     * @param player The player to send message to
     * @param messageKey The message key from configuration
     */
    @Override
    public void sendPermissionDeniedMessage(@NotNull Player player, @NotNull String messageKey) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(messageKey, "Message key cannot be null");
        
        if (!player.isOnline()) {
            return; // Skip sending message to offline player
        }
        
        String message = configManager.getMessage(messageKey);
        
        Component component = Component.text()
            .append(Component.text("❌ ", NamedTextColor.RED))
            .append(parseMessage(message))
            .color(NamedTextColor.RED)
            .decorate(TextDecoration.BOLD)
            .build();
            
        player.sendMessage(component);
    }
    
    /**
     * Checks if cooldown messages are enabled
     * Sprawdza czy wiadomości o cooldownie są włączone
     * 
     * @return true if cooldown messages are enabled, false otherwise
     */
    @Override
    public boolean isCooldownMessageEnabled() {
        return configManager.isCooldownMessageEnabled();
    }
    
    /**
     * Parses message with color codes and creates Adventure Component
     * Parsuje wiadomość z kodami kolorów i tworzy Adventure Component
     * 
     * @param message The message to parse
     * @return The parsed Adventure Component
     */
    private @NotNull Component parseMessage(@NotNull String message) {
        Objects.requireNonNull(message, "Message cannot be null");
        
        if (message.isEmpty()) {
            return Component.empty();
        }
        
        // Replace legacy color codes with Adventure Components
        String parsed = message
            .replace("&0", "<black>")
            .replace("&1", "<dark_blue>")
            .replace("&2", "<dark_green>")
            .replace("&3", "<dark_aqua>")
            .replace("&4", "<dark_red>")
            .replace("&5", "<dark_purple>")
            .replace("&6", "<gold>")
            .replace("&7", "<gray>")
            .replace("&8", "<dark_gray>")
            .replace("&9", "<blue>")
            .replace("&a", "<green>")
            .replace("&b", "<aqua>")
            .replace("&c", "<red>")
            .replace("&d", "<light_purple>")
            .replace("&e", "<yellow>")
            .replace("&f", "<white>")
            .replace("&k", "<obfuscated>")
            .replace("&l", "<bold>")
            .replace("&m", "<strikethrough>")
            .replace("&n", "<underlined>")
            .replace("&o", "<italic>")
            .replace("&r", "<reset>");
        
        try {
            // Use MiniMessage format for modern Adventure Components
            return net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(parsed);
        } catch (net.kyori.adventure.text.minimessage.ParsingException e) {
            // Log parsing error and fallback to plain text
            LOGGER.warning("Failed to parse MiniMessage format: " + e.getMessage() + ", original: " + message);
            return Component.text(message, NamedTextColor.WHITE);
        } catch (IllegalArgumentException e) {
            // Handle invalid arguments in message parsing
            LOGGER.warning("Invalid message format: " + e.getMessage() + ", original: " + message);
            return Component.text(message, NamedTextColor.WHITE);
        } catch (Exception e) {
            // Unexpected error fallback
            LOGGER.severe("Unexpected error parsing message: " + e.getMessage() + ", original: " + message);
            return Component.text(message, NamedTextColor.WHITE);
        }
    }
}