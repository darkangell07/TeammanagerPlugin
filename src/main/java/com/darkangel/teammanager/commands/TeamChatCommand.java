package com.darkangel.teammanager.commands;

import com.darkangel.teammanager.TeamManager;
import com.darkangel.teammanager.managers.TeamDataManager;
import com.darkangel.teammanager.models.Team;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the /tc command for team chat
 * This was actually pretty fun to implement
 */
public class TeamChatCommand implements CommandExecutor {
    private TeamManager plugin;
    private TeamDataManager teamData; // shorthand var name cuz I'm lazy
    
    // Prefix for team chat messages
    private static final String MSG_PREFIX = ChatColor.GRAY + "[" + ChatColor.GOLD + "Team" + ChatColor.GRAY + "] ";

    public TeamChatCommand(TeamManager plugin) {
        this.plugin = plugin;
        this.teamData = plugin.getTeamDataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Only players can use team chat obviously
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use team chat.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if player is in a team
        Team team = teamData.getPlayerTeam(player.getUniqueId());
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You're not in a team!");
            return true;
        }
        
        // Need args for a message...duh
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /tc <message>");
            return true;
        }
        
        // Build the message from args
        StringBuilder messageBuilder = new StringBuilder();
        for (String arg : args) {
            messageBuilder.append(arg).append(" ");
        }
        String message = messageBuilder.toString().trim();
        
        // Get the team chat color - defaults to yellow if not set
        // This was a pain to implement with the serialization but worth it
        ChatColor teamColor = team.getColor() != null ? team.getColor() : ChatColor.YELLOW;
        
        // Format: [Team] [PlayerName] Message
        String formattedMsg = MSG_PREFIX + 
                teamColor + player.getName() + ChatColor.WHITE + ": " + 
                message;
        
        // Send message to all online team members
        // Might add offline message storage later if I have time
        for (Player member : team.getOnlineMembers()) {
            member.sendMessage(formattedMsg);
        }
        
        // Log the message for admins - useful for moderation
        plugin.getLogger().info("[TeamChat:" + team.getName() + "] " + player.getName() + ": " + message);
        
        return true;
    }
    
    // TODO: implement offline message queue
    // probably need a database for this tho
    /*
    private void saveOfflineMessage(Team team, String message) {
        // code here when I figure it out
    }
    */
} 