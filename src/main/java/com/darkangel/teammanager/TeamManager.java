package com.darkangel.teammanager;

import com.darkangel.teammanager.commands.*;
import com.darkangel.teammanager.listeners.ChatListener;
import com.darkangel.teammanager.listeners.PvPListener;
import com.darkangel.teammanager.managers.TeamDataManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for TeamManager
 * Started working on this Aug 2023, finally getting somewhere!
 */
public class TeamManager extends JavaPlugin {
    private TeamDataManager teamDataManager;
    
    // might add config manager later if I have time
    // private ConfigManager configManager;

    @Override
    public void onEnable() {
        // First let's save the default config
        saveDefaultConfig();
        
        // Init the team data manager - need this first
        teamDataManager = new TeamDataManager(this);
        teamDataManager.loadTeams();
        
        // Register commands - pretty simple stuff
        getCommand("team").setExecutor(new TeamCommand(this));
        getCommand("tc").setExecutor(new TeamChatCommand(this));
        
        // TODO: Add more commands later
        // getCommand("ta").setExecutor(new TeamAdminCommand(this));
        
        // Register listeners - had some issues with these before, finally working
        getServer().getPluginManager().registerEvents(new PvPListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        
        getLogger().info("TeamManager has been enabled! Woohoo!");
    }

    @Override
    public void onDisable() {
        // Make sure to save all team data when plugin shuts down
        // Almost forgot this once and lost all my test data lol
        if (teamDataManager != null) {
            teamDataManager.saveTeams();
        }
        
        getLogger().info("TeamManager has been disabled! Bye!");
    }
    
    /**
     * Gets the team data manager - used by commands and listeners
     */
    public TeamDataManager getTeamDataManager() {
        return teamDataManager;
    }
    
    // might add these methods later if needed
    /*
    private void setupMetrics() {
        // add bStats metrics?
    }
    
    private void checkForUpdates() {
        // maybe add update checker
    }
    */
} 