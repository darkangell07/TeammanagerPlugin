package com.darkangel.teammanager.commands;

import com.darkangel.teammanager.TeamManager;
import com.darkangel.teammanager.managers.TeamDataManager;
import com.darkangel.teammanager.models.Team;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamChatCommand implements CommandExecutor {
    private final TeamManager plugin;
    private final TeamDataManager teamDataManager;

    public TeamChatCommand(TeamManager plugin) {
        this.plugin = plugin;
        this.teamDataManager = plugin.getTeamDataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("teammanager.team.chat")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use team chat.");
            return true;
        }
        
        // Get player's team
        Team team = teamDataManager.getPlayerTeam(player.getUniqueId());
        
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You're not in a team.");
            return true;
        }
        
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /tc <message>");
            return true;
        }
        
        // Build message
        StringBuilder messageBuilder = new StringBuilder();
        for (String arg : args) {
            messageBuilder.append(arg).append(" ");
        }
        String message = messageBuilder.toString().trim();
        
        // Format chat message
        String formattedMessage = ChatColor.AQUA + "[Team Chat] " + 
                ChatColor.YELLOW + player.getName() + ChatColor.WHITE + ": " + message;
        
        // Send to all online team members
        for (Player member : team.getOnlineMembers()) {
            member.sendMessage(formattedMessage);
        }
        
        return true;
    }
} 