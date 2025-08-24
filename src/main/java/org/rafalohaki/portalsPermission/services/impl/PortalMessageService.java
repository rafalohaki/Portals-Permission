package org.rafalohaki.portalsPermission.services.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rafalohaki.portalsPermission.managers.ConfigManager;
import org.rafalohaki.portalsPermission.services.IPortalMessageService;

/**
 * Implementation of portal message service using Adventure Components
 * Implementacja serwisu wiadomości portali używając Adventure Components
 */
public class PortalMessageService implements IPortalMessageService {
    
    private final ConfigManager configManager;
    
    /**
     * Constructor for PortalMessageService
     * Konstruktor dla PortalMessageService
     * 
     * @param configManager The configuration manager
     */
    public PortalMessageService(@NotNull ConfigManager configManager) {
        this.configManager = configManager;
    }
    
    @Override
    public void sendMessage(@NotNull Player player, @NotNull String message) {
        if (message.isEmpty()) {
            return;
        }
        
        // Parse color codes and create Adventure Component
        Component component = parseMessage(message);
        player.sendMessage(component);
    }
    
    @Override
    public void sendCooldownMessage(@NotNull Player player, int remainingTime) {
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
    
    @Override
    public void sendPermissionDeniedMessage(@NotNull Player player, @NotNull String messageKey) {
        String message = configManager.getMessage(messageKey);
        
        Component component = Component.text()
            .append(Component.text("❌ ", NamedTextColor.RED))
            .append(parseMessage(message))
            .color(NamedTextColor.RED)
            .decorate(TextDecoration.BOLD)
            .build();
            
        player.sendMessage(component);
    }
    
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
        } catch (Exception e) {
            // Fallback to plain text if parsing fails
            return Component.text(message, NamedTextColor.WHITE);
        }
    }
}