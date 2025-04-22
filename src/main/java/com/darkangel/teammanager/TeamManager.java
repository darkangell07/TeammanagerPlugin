package com.darkangel.teammanager;

import com.darkangel.teammanager.commands.*;
import com.darkangel.teammanager.gui.TeamGUI;
import com.darkangel.teammanager.gui.TeamGUIListener;
import com.darkangel.teammanager.listeners.ChatListener;
import com.darkangel.teammanager.listeners.GUIChatListener;
import com.darkangel.teammanager.listeners.PvPListener;
import com.darkangel.teammanager.managers.TeamDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public class TeamManager extends JavaPlugin {
    private TeamDataManager dataManager;
    private TeamCommand cmdHandler;
    private TeamGUI guiManager;
    private TeamGUIListener guiListener;
    private boolean debugMode = false;

    @Override
    public void onEnable() {
        PluginDescriptionFile pdfFile = getDescription();
        
        // Display custom banner
        getLogger().info("  _____                    __  ___                                 ");
        getLogger().info(" |_   _|__ __ _ _ __  ___ |  \\/  |__ _ _ _  __ _ __ _ ___ _ _ ___ ");
        getLogger().info("   | |/ -_) _` | '  \\/ -_)| |\\/| / _` | ' \\/ _` / _` / -_) '_|_-<");
        getLogger().info("   |_|\\___\\__,_|_|_|_\\___||_|  |_\\__,_|_||_\\__,_\\__, \\___|_| /__/");
        getLogger().info("                                                |___/             ");
        getLogger().info("Version " + pdfFile.getVersion() + " - Created by DarkAngel");
        
        try {
            // Check if config directory exists, create if not
            if (!getDataFolder().exists()) {
                getLogger().info("Creating plugin directory...");
                getDataFolder().mkdirs();
            }
            
            // Save default configuration if it doesn't exist
            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                getLogger().info("Creating default configuration...");
                saveDefaultConfig();
            } else {
                getLogger().info("Loading configuration...");
            }
            
            // Check server version compatibility
            String version = Bukkit.getBukkitVersion();
            getLogger().info("Server version: " + version);
            
            // Load debug mode setting
            debugMode = getConfig().getBoolean("debug_mode", false);
            if (debugMode) {
                getLogger().info("Debug mode enabled - extra logging will be shown");
            }
            
            // Initialize team data manager
            getLogger().info("Initializing data manager...");
            dataManager = new TeamDataManager(this);
            dataManager.loadTeams();
            
            // Initialize team command handler
            getLogger().info("Registering commands...");
            cmdHandler = new TeamCommand(this);
            
            // Initialize GUI
            getLogger().info("Setting up GUI...");
            guiManager = new TeamGUI(this);
            guiListener = new TeamGUIListener(this, guiManager);
            
            // Register commands
            if (getCommand("team") != null) {
                getCommand("team").setExecutor(cmdHandler);
            } else {
                getLogger().severe("Failed to register 'team' command! Check plugin.yml");
            }
            
            if (getCommand("tc") != null) {
                getCommand("tc").setExecutor(new TeamChatCommand(this));
            } else {
                getLogger().warning("Failed to register 'tc' command! Check plugin.yml");
            }
            
            if (getCommand("teamgui") != null) {
                getCommand("teamgui").setExecutor(new TeamGuiCommand(this, guiManager));
            } else {
                getLogger().warning("Failed to register 'teamgui' command! Check plugin.yml");
            }
            
            // Register event listeners
            getLogger().info("Registering event listeners...");
            getServer().getPluginManager().registerEvents(new PvPListener(this), this);
            getServer().getPluginManager().registerEvents(new ChatListener(this), this);
            getServer().getPluginManager().registerEvents(guiListener, this);
            getServer().getPluginManager().registerEvents(new GUIChatListener(this, guiListener), this);
            
            // Final status message
            int playerCount = getServer().getOnlinePlayers().size();
            getLogger().info("==================================================");
            getLogger().info("TeamManager v" + pdfFile.getVersion() + " has been successfully enabled!");
            getLogger().info("Found " + playerCount + " online player(s)");
            getLogger().info("==================================================");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error during plugin startup!", e);
            getLogger().severe("Disabling plugin due to startup errors...");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            getLogger().info("Shutting down TeamManager...");
            
            // Save team data when server shuts down
            if (dataManager != null) {
                getLogger().info("Saving team data...");
                dataManager.saveTeams();
                getLogger().info("All team data has been saved successfully.");
            } else {
                getLogger().warning("No data manager found, no team data to save.");
            }
            
            // Final message
            getLogger().info("TeamManager has been disabled! Thanks for using my plugin :)");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error during plugin shutdown!", e);
        }
    }
    
    public TeamDataManager getTeamDataManager() {
        return dataManager;
    }
    
    public TeamCommand getTeamCommandHandler() {
        return cmdHandler;
    }
    
    public TeamGUI getTeamGUI() {
        return guiManager;
    }
    
    /**
     * Checks if debug mode is enabled
     * @return true if debug mode is enabled
     */
    public boolean isDebugMode() {
        return debugMode;
    }
    
    /**
     * Format a message with the plugin's prefix
     * @param message the message to format
     * @return the formatted message
     */
    public String formatMessage(String message) {
        return ChatColor.DARK_AQUA + "[Teams] " + ChatColor.RESET + message;
    }
} 