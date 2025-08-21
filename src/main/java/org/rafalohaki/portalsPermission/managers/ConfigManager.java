package org.rafalohaki.portalsPermission.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Manager for handling plugin configuration with async operations
 * Zarządza konfiguracją pluginu z operacjami asynchronicznymi
 */
public class ConfigManager {
    
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private boolean debugMode;
    
    public ConfigManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Loads configuration asynchronously
     * Ładuje konfigurację asynchronicznie
     */
    public CompletableFuture<Void> loadConfigAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                plugin.saveDefaultConfig();
                plugin.reloadConfig();
                this.config = plugin.getConfig();
                this.debugMode = config.getBoolean("settings.debug", false);
                
                if (debugMode) {
                    plugin.getLogger().info("Configuration loaded successfully");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load configuration", e);
            }
        });
    }
    
    /**
     * Reloads configuration asynchronously
     * Przeładowuje konfigurację asynchronicznie
     */
    public CompletableFuture<Boolean> reloadConfigAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                plugin.reloadConfig();
                this.config = plugin.getConfig();
                this.debugMode = config.getBoolean("settings.debug", false);
                
                if (debugMode) {
                    plugin.getLogger().info("Configuration reloaded successfully");
                }
                return true;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to reload configuration", e);
                return false;
            }
        });
    }
    
    // Settings getters
    public boolean isEnabled() {
        return config != null && config.getBoolean("settings.enabled", true);
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    public @NotNull String getLanguage() {
        return config != null ? config.getString("settings.language", "en") : "en";
    }
    
    // Portal settings
    public boolean isNetherBlocked() {
        return config != null && config.getBoolean("portals.block_nether", true);
    }
    
    public boolean isEndBlocked() {
        return config != null && config.getBoolean("portals.block_end", true);
    }
    
    public boolean isCustomBlocked() {
        return config != null && config.getBoolean("portals.block_custom", false);
    }
    
    // Knockback settings
    public boolean isKnockbackEnabled() {
        return config != null && config.getBoolean("knockback.enabled", true);
    }
    
    public double getKnockbackStrength() {
        return config != null ? config.getDouble("knockback.strength", 1.5) : 1.5;
    }
    
    public double getKnockbackHeight() {
        return config != null ? config.getDouble("knockback.height", 0.8) : 0.8;
    }
    
    public boolean isKnockbackSoundEnabled() {
        return config != null && config.getBoolean("knockback.play_sound", true);
    }
    
    public @NotNull String getKnockbackSoundType() {
        return config != null ? config.getString("knockback.sound_type", "ENTITY_VILLAGER_NO") : "ENTITY_VILLAGER_NO";
    }
    
    public float getKnockbackSoundVolume() {
        return config != null ? (float) config.getDouble("knockback.sound_volume", 0.7) : 0.7f;
    }
    
    public float getKnockbackSoundPitch() {
        return config != null ? (float) config.getDouble("knockback.sound_pitch", 1.0) : 1.0f;
    }
    
    // Cooldown settings
    public boolean isCooldownEnabled() {
        return config != null && config.getBoolean("cooldown.enabled", true);
    }
    
    public int getCooldownTime() {
        return config != null ? config.getInt("cooldown.time_seconds", 5) : 5;
    }
    
    public boolean isCooldownMessageEnabled() {
        return config != null && config.getBoolean("cooldown.show_message", true);
    }
    
    // Messages
    public @NotNull String getMessage(@NotNull String key) {
        if (config == null) {
            return "&cConfiguration not loaded";
        }
        
        String language = getLanguage();
        String message = config.getString("messages." + language + "." + key);
        
        // Fallback to English if message not found in current language
        if (message == null && !"en".equals(language)) {
            message = config.getString("messages.en." + key);
        }
        
        // Final fallback
        if (message == null) {
            return "&cMessage not found: " + key;
        }
        
        return message;
    }
    
    public @NotNull String getMessage(@NotNull String key, @NotNull String placeholder, @NotNull String value) {
        return getMessage(key).replace("{" + placeholder + "}", value);
    }
    
    // Permissions
    public @NotNull String getPermission(@NotNull String key) {
        if (config == null) {
            return "portals." + key;
        }
        return config.getString("permissions." + key, "portals." + key);
    }
    
    /**
     * Gets configuration instance (can be null if not loaded)
     * Pobiera instancję konfiguracji (może być null jeśli nie załadowana)
     */
    public @Nullable FileConfiguration getConfig() {
        return config;
    }
    
    /**
     * Checks if configuration is loaded
     * Sprawdza czy konfiguracja jest załadowana
     */
    public boolean isLoaded() {
        return config != null;
    }
}