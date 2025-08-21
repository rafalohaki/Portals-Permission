package org.rafalohaki.portalsPermission.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rafalohaki.portalsPermission.managers.ConfigManager;
import org.rafalohaki.portalsPermission.managers.CooldownManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Main command executor for portal management
 * Główny executor komend dla zarządzania portalami
 */
public class PortalsCommand implements CommandExecutor, TabCompleter {
    
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final CooldownManager cooldownManager;
    private final MiniMessage miniMessage;
    
    public PortalsCommand(@NotNull JavaPlugin plugin, @NotNull ConfigManager configManager, @NotNull CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.cooldownManager = cooldownManager;
        this.miniMessage = MiniMessage.miniMessage();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Check basic admin permission
        if (!sender.hasPermission(configManager.getPermission("admin"))) {
            sendMessage(sender, configManager.getMessage("no_command_permission"));
            return true;
        }
        
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload" -> handleReloadCommand(sender);
            case "info" -> handleInfoCommand(sender);
            case "cooldown" -> handleCooldownCommand(sender, args);
            case "help" -> sendHelpMessage(sender);
            default -> {
                String usage = "/portals <reload|info|cooldown|help>";
                sendMessage(sender, configManager.getMessage("invalid_usage", "usage", usage));
            }
        }
        
        return true;
    }
    
    /**
     * Handles reload command
     * Obsługuje komendę reload
     */
    private void handleReloadCommand(@NotNull CommandSender sender) {
        if (!sender.hasPermission(configManager.getPermission("reload"))) {
            sendMessage(sender, configManager.getMessage("no_command_permission"));
            return;
        }
        
        sendMessage(sender, "&eReloading configuration...");
        
        // Reload configuration asynchronously
        CompletableFuture<Boolean> reloadFuture = configManager.reloadConfigAsync();
        
        reloadFuture.thenAccept(success -> {
            // Send result message on main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (success) {
                    sendMessage(sender, configManager.getMessage("plugin_reloaded"));
                    
                    if (configManager.isDebugMode()) {
                        plugin.getLogger().info("Configuration reloaded by " + sender.getName());
                    }
                } else {
                    sendMessage(sender, "&cFailed to reload configuration. Check console for errors.");
                }
            });
        }).exceptionally(throwable -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                sendMessage(sender, "&cError occurred while reloading configuration.");
                plugin.getLogger().log(Level.SEVERE, "Error reloading configuration", throwable);
            });
            return null;
        });
    }
    
    /**
     * Handles info command
     * Obsługuje komendę info
     */
    private void handleInfoCommand(@NotNull CommandSender sender) {
        sendMessage(sender, "&6=== Portals Permission Info ===");
        sendMessage(sender, "&ePlugin Version: &f" + plugin.getDescription().getVersion());
        sendMessage(sender, "&eEnabled: &f" + (configManager.isEnabled() ? "&aYes" : "&cNo"));
        sendMessage(sender, "&eDebug Mode: &f" + (configManager.isDebugMode() ? "&aYes" : "&cNo"));
        sendMessage(sender, "&eLanguage: &f" + configManager.getLanguage());
        sendMessage(sender, "");
        sendMessage(sender, "&6Portal Settings:");
        sendMessage(sender, "&eNether Blocked: &f" + (configManager.isNetherBlocked() ? "&aYes" : "&cNo"));
        sendMessage(sender, "&eEnd Blocked: &f" + (configManager.isEndBlocked() ? "&aYes" : "&cNo"));
        sendMessage(sender, "&eCustom Blocked: &f" + (configManager.isCustomBlocked() ? "&aYes" : "&cNo"));
        sendMessage(sender, "");
        sendMessage(sender, "&6Knockback Settings:");
        sendMessage(sender, "&eKnockback Enabled: &f" + (configManager.isKnockbackEnabled() ? "&aYes" : "&cNo"));
        sendMessage(sender, "&eKnockback Strength: &f" + configManager.getKnockbackStrength());
        sendMessage(sender, "&eKnockback Height: &f" + configManager.getKnockbackHeight());
        sendMessage(sender, "");
        sendMessage(sender, "&6Cooldown Settings:");
        sendMessage(sender, "&eCooldown Enabled: &f" + (configManager.isCooldownEnabled() ? "&aYes" : "&cNo"));
        sendMessage(sender, "&eCooldown Time: &f" + configManager.getCooldownTime() + " seconds");
        sendMessage(sender, "&eActive Cooldowns: &f" + cooldownManager.getActiveCooldownCount());
    }
    
    /**
     * Handles cooldown command
     * Obsługuje komendę cooldown
     */
    private void handleCooldownCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            sendMessage(sender, "&cUsage: /portals cooldown <clear|check> [player]");
            return;
        }
        
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "clear" -> {
                if (args.length >= 3) {
                    // Clear specific player cooldown
                    String playerName = args[2];
                    Player target = plugin.getServer().getPlayer(playerName);
                    
                    if (target == null) {
                        sendMessage(sender, "&cPlayer not found: " + playerName);
                        return;
                    }
                    
                    cooldownManager.removeCooldown(target);
                    sendMessage(sender, "&aCleared cooldown for player " + target.getName());
                } else {
                    // Clear all cooldowns
                    cooldownManager.clearAllCooldowns();
                    sendMessage(sender, "&aCleared all cooldowns");
                }
            }
            case "check" -> {
                if (args.length < 3) {
                    sendMessage(sender, "&cUsage: /portals cooldown check <player>");
                    return;
                }
                
                String playerName = args[2];
                Player target = plugin.getServer().getPlayer(playerName);
                
                if (target == null) {
                    sendMessage(sender, "&cPlayer not found: " + playerName);
                    return;
                }
                
                if (cooldownManager.hasCooldown(target)) {
                    int remaining = cooldownManager.getRemainingCooldown(target);
                    sendMessage(sender, "&ePlayer " + target.getName() + " has " + remaining + " seconds remaining");
                } else {
                    sendMessage(sender, "&aPlayer " + target.getName() + " has no active cooldown");
                }
            }
            default -> sendMessage(sender, "&cUsage: /portals cooldown <clear|check> [player]");
        }
    }
    
    /**
     * Sends help message
     * Wysyła wiadomość pomocy
     */
    private void sendHelpMessage(@NotNull CommandSender sender) {
        sendMessage(sender, "&6=== Portals Permission Commands ===");
        sendMessage(sender, "&e/portals reload &7- Reload plugin configuration");
        sendMessage(sender, "&e/portals info &7- Show plugin information");
        sendMessage(sender, "&e/portals cooldown clear [player] &7- Clear cooldowns");
        sendMessage(sender, "&e/portals cooldown check <player> &7- Check player cooldown");
        sendMessage(sender, "&e/portals help &7- Show this help message");
    }
    
    /**
     * Sends message using Adventure Components
     * Wysyła wiadomość używając Adventure Components
     */
    private void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        try {
            Component component = miniMessage.deserialize(message);
            sender.sendMessage(component);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to send message to " + sender.getName() + ": " + message, e);
            // Fallback to plain text
            sender.sendMessage(message.replaceAll("&([0-9a-fk-or])", "§$1"));
        }
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission(configManager.getPermission("admin"))) {
            return completions;
        }
        
        if (args.length == 1) {
            // First argument - subcommands
            List<String> subCommands = Arrays.asList("reload", "info", "cooldown", "help");
            String input = args[0].toLowerCase();
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("cooldown")) {
            // Second argument for cooldown command
            List<String> cooldownActions = Arrays.asList("clear", "check");
            String input = args[1].toLowerCase();
            
            for (String action : cooldownActions) {
                if (action.startsWith(input)) {
                    completions.add(action);
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("cooldown")) {
            // Third argument for cooldown command - player names
            String input = args[2].toLowerCase();
            
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(input)) {
                    completions.add(player.getName());
                }
            }
        }
        
        return completions;
    }
}