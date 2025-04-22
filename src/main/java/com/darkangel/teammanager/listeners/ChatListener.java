package com.darkangel.teammanager.listeners;

import com.darkangel.teammanager.TeamManager;
import com.darkangel.teammanager.managers.TeamDataManager;
import com.darkangel.teammanager.models.Team;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final TeamManager plugin;
    private final TeamDataManager teamDataManager;

    public ChatListener(TeamManager plugin) {
        this.plugin = plugin;
        this.teamDataManager = plugin.getTeamDataManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Check if message starts with "/tc " (team chat command)
        if (message.startsWith("/tc ")) {
            event.setCancelled(true);
            
            // Extract the actual message (remove the "/tc " prefix)
            String teamChatMessage = message.substring(4).trim();
            
            // Check permissions
            if (!player.hasPermission("teammanager.team.chat")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to use team chat.");
                return;
            }
            
            // Get the player's team
            Team team = teamDataManager.getPlayerTeam(player.getUniqueId());
            
            if (team == null) {
                player.sendMessage(ChatColor.RED + "You're not in a team.");
                return;
            }
            
            // Check if the message is empty
            if (teamChatMessage.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Please provide a message. Usage: /tc <message>");
                return;
            }
            
            // Format the message
            String formattedMessage = ChatColor.AQUA + "[Team Chat] " + 
                    ChatColor.YELLOW + player.getName() + ChatColor.WHITE + ": " + teamChatMessage;
            
            // Send to all online team members
            for (Player member : team.getOnlineMembers()) {
                member.sendMessage(formattedMessage);
            }
        }
    }
} 