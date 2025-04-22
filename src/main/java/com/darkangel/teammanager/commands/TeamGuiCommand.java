package com.darkangel.teammanager.commands;

import com.darkangel.teammanager.TeamManager;
import com.darkangel.teammanager.gui.TeamGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to open the team GUI
 */
public class TeamGuiCommand implements CommandExecutor {
    private final TeamManager plugin;
    private final TeamGUI gui;
    
    public TeamGuiCommand(TeamManager plugin, TeamGUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("teammanager.gui")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use the team GUI.");
            return true;
        }
        
        gui.openMainMenu(player);
        return true;
    }
} 