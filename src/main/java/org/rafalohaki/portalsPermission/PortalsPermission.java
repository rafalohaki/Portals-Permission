package org.rafalohaki.portalsPermission;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.rafalohaki.portalsPermission.commands.PortalsCommand;
import org.rafalohaki.portalsPermission.events.PortalAccessListener;
import org.rafalohaki.portalsPermission.managers.ConfigManager;
import org.rafalohaki.portalsPermission.managers.CooldownManager;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Main plugin class for Portals Permission
 * Główna klasa pluginu Portals Permission
 */
public final class PortalsPermission extends JavaPlugin {
    
    private ConfigManager configManager;
    private CooldownManager cooldownManager;
    private PortalAccessListener portalListener;
    private PortalsCommand portalsCommand;
    
    @Override
    public void onEnable() {
        try {
            getLogger().info("Starting Portals Permission v" + getDescription().getVersion());
            
            // Initialize managers
            initializeManagers();
            
            // Load configuration asynchronously
            loadConfigurationAsync().thenRun(() -> {
                // Register components on main thread
                getServer().getScheduler().runTask(this, this::registerComponents);
            }).exceptionally(throwable -> {
                getLogger().log(Level.SEVERE, "Failed to load configuration during startup", throwable);
                getServer().getPluginManager().disablePlugin(this);
                return null;
            });
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable plugin", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        try {
            getLogger().info("Shutting down Portals Permission...");
            
            // Shutdown managers
            shutdownManagers();
            
            getLogger().info("Portals Permission disabled successfully");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error during plugin shutdown", e);
        }
    }
    
    /**
     * Initializes all managers
     * Inicjalizuje wszystkie menedżery
     */
    private void initializeManagers() {
        this.configManager = new ConfigManager(this);
        this.cooldownManager = new CooldownManager(this, configManager);
        
        getLogger().info("Managers initialized successfully");
    }
    
    /**
     * Loads configuration asynchronously
     * Ładuje konfigurację asynchronicznie
     */
    private @NotNull CompletableFuture<Void> loadConfigurationAsync() {
        return configManager.loadConfigAsync().thenRun(() -> {
            getLogger().info("Configuration loaded successfully");
            
            if (configManager.isDebugMode()) {
                getLogger().info("Debug mode is enabled");
            }
        });
    }
    
    /**
     * Registers all components (listeners, commands, etc.)
     * Rejestruje wszystkie komponenty (listenery, komendy, itp.)
     */
    private void registerComponents() {
        try {
            // Register event listeners
            registerEventListeners();
            
            // Register commands
            registerCommands();
            
            getLogger().info("All components registered successfully");
            getLogger().info("Portals Permission enabled successfully!");
            
            // Log configuration status
            logConfigurationStatus();
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register components", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    /**
     * Registers event listeners
     * Rejestruje listenery wydarzeń
     */
    private void registerEventListeners() {
        this.portalListener = new PortalAccessListener(this, configManager, cooldownManager);
        getServer().getPluginManager().registerEvents(portalListener, this);
        
        getLogger().info("Event listeners registered");
    }
    
    /**
     * Registers commands
     * Rejestruje komendy
     */
    private void registerCommands() {
        this.portalsCommand = new PortalsCommand(this, configManager, cooldownManager);
        
        var command = getCommand("portals");
        if (command != null) {
            command.setExecutor(portalsCommand);
            command.setTabCompleter(portalsCommand);
            getLogger().info("Commands registered");
        } else {
            getLogger().warning("Failed to register 'portals' command - command not found in plugin.yml");
        }
    }
    
    /**
     * Logs current configuration status
     * Loguje aktualny status konfiguracji
     */
    private void logConfigurationStatus() {
        if (configManager.isDebugMode()) {
            getLogger().info("=== Configuration Status ===");
            getLogger().info("Plugin Enabled: " + configManager.isEnabled());
            getLogger().info("Language: " + configManager.getLanguage());
            getLogger().info("Nether Blocked: " + configManager.isNetherBlocked());
            getLogger().info("End Blocked: " + configManager.isEndBlocked());
            getLogger().info("Custom Blocked: " + configManager.isCustomBlocked());
            getLogger().info("Knockback Enabled: " + configManager.isKnockbackEnabled());
            getLogger().info("Cooldown Enabled: " + configManager.isCooldownEnabled());
            getLogger().info("=============================");
        }
    }
    
    /**
     * Shuts down all managers
     * Wyłącza wszystkie menedżery
     */
    private void shutdownManagers() {
        if (cooldownManager != null) {
            cooldownManager.shutdown();
        }
        
        // Clear references
        this.configManager = null;
        this.cooldownManager = null;
        this.portalListener = null;
        this.portalsCommand = null;
    }
    
    /**
     * Gets the configuration manager
     * Pobiera menedżer konfiguracji
     */
    public @NotNull ConfigManager getConfigManager() {
        if (configManager == null) {
            throw new IllegalStateException("ConfigManager not initialized");
        }
        return configManager;
    }
    
    /**
     * Gets the cooldown manager
     * Pobiera menedżer cooldown
     */
    public @NotNull CooldownManager getCooldownManager() {
        if (cooldownManager == null) {
            throw new IllegalStateException("CooldownManager not initialized");
        }
        return cooldownManager;
    }
    
    /**
     * Checks if plugin is properly initialized
     * Sprawdza czy plugin jest poprawnie zainicjalizowany
     */
    public boolean isInitialized() {
        return configManager != null && 
               cooldownManager != null && 
               configManager.isLoaded();
    }
}
