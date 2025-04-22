package com.darkangel.teammanager.listeners;

import com.darkangel.teammanager.TeamManager;
import com.darkangel.teammanager.gui.TeamGUIListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Handles chat interactions for the GUI system
 */
public class GUIChatListener implements Listener {
    private final TeamManager plugin;
    private final TeamGUIListener guiListener;
    
    public GUIChatListener(TeamManager plugin, TeamGUIListener guiListener) {
        this.plugin = plugin;
        this.guiListener = guiListener;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is in team creation process
        if (guiListener.isCreatingTeam(player.getUniqueId())) {
            event.setCancelled(true);
            
            // Execute on the main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                guiListener.handleTeamCreation(player, event.getMessage());
            });
        }
    }
} 