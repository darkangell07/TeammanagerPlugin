package com.darkangel.teammanager.listeners;

import com.darkangel.teammanager.TeamManager;
import com.darkangel.teammanager.managers.TeamDataManager;
import com.darkangel.teammanager.models.Team;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class PvPListener implements Listener {
    private final TeamManager plugin;
    private final TeamDataManager teamDataManager;

    public PvPListener(TeamManager plugin) {
        this.plugin = plugin;
        this.teamDataManager = plugin.getTeamDataManager();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Check if both entities are players
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player victim = (Player) event.getEntity();
        Player attacker = null;
        
        // Get the attacker, handling different damage sources
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            ProjectileSource shooter = projectile.getShooter();
            
            if (shooter instanceof Player) {
                attacker = (Player) shooter;
            }
        }
        
        // If attacker is not a player, return
        if (attacker == null) {
            return;
        }
        
        // Check if both players are in the same team
        Team victimTeam = teamDataManager.getPlayerTeam(victim.getUniqueId());
        Team attackerTeam = teamDataManager.getPlayerTeam(attacker.getUniqueId());
        
        if (victimTeam != null && attackerTeam != null && victimTeam.equals(attackerTeam)) {
            // If they're in the same team and PvP is disabled, cancel the event
            if (!victimTeam.isPvpEnabled()) {
                event.setCancelled(true);
            }
        }
    }
} 