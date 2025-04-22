package com.darkangel.teammanager.listeners;

import com.darkangel.teammanager.TeamManager;
import com.darkangel.teammanager.models.Team;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Handles PvP interactions between team members
 */
public class PvPListener implements Listener {
    private final TeamManager plugin;
    
    public PvPListener(TeamManager plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Prevents PvP between team members if team PvP is disabled
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();
        
        // Get the teams
        Team victimTeam = plugin.getTeamDataManager().getPlayerTeam(victim.getUniqueId());
        Team attackerTeam = plugin.getTeamDataManager().getPlayerTeam(attacker.getUniqueId());
        
        // Check if both players are in the same team
        if (victimTeam != null && attackerTeam != null && victimTeam.getName().equals(attackerTeam.getName())) {
            // Check if team PvP is enabled
            if (!victimTeam.isPvpEnabled()) {
                // Cancel the damage and notify the attacker
                event.setCancelled(true);
                attacker.sendMessage(ChatColor.RED + "PvP is disabled in your team.");
            }
        }
    }
    
    /**
     * Track team kills and deaths for statistics
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        // Record death for victim's team
        Team victimTeam = plugin.getTeamDataManager().getPlayerTeam(victim.getUniqueId());
        if (victimTeam != null) {
            victimTeam.addDeath();
            
            // Log if debug mode is enabled
            if (plugin.isDebugMode()) {
                plugin.getLogger().info("Recorded death for team " + victimTeam.getName());
            }
        }
        
        // Record kill for killer's team if killer exists and is a player
        if (killer != null) {
            Team killerTeam = plugin.getTeamDataManager().getPlayerTeam(killer.getUniqueId());
            if (killerTeam != null) {
                killerTeam.addKill();
                
                // Don't count team kills in stats if PvP is enabled within the team
                if (victimTeam != null && victimTeam.equals(killerTeam) && killerTeam.isPvpEnabled()) {
                    // This is a team kill, subtract it
                    killerTeam.addDeath(); // Add an extra death to balance the kill
                    
                    // Log if debug mode is enabled
                    if (plugin.isDebugMode()) {
                        plugin.getLogger().info("Team kill detected, balancing stats for team " + killerTeam.getName());
                    }
                } else {
                    // Log if debug mode is enabled
                    if (plugin.isDebugMode()) {
                        plugin.getLogger().info("Recorded kill for team " + killerTeam.getName());
                    }
                }
            }
        }
        
        // Save team data
        plugin.getTeamDataManager().saveTeams();
    }
} 