package com.darkangel.teammanager.managers;

import com.darkangel.teammanager.TeamManager;
import com.darkangel.teammanager.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class TeamDataManager {
    private final TeamManager plugin;
    private final Map<String, Team> teams;
    private final File teamsFile;
    private FileConfiguration teamsConfig;

    public TeamDataManager(TeamManager plugin) {
        this.plugin = plugin;
        this.teams = new HashMap<>();
        this.teamsFile = new File(plugin.getDataFolder(), "teams.yml");
        
        // Create the teams.yml file if it doesn't exist
        if (!teamsFile.exists()) {
            plugin.saveResource("teams.yml", false);
        }
        
        this.teamsConfig = YamlConfiguration.loadConfiguration(teamsFile);
    }

    /**
     * Loads teams from teams.yml
     */
    public void loadTeams() {
        teams.clear();
        
        ConfigurationSection teamsSection = teamsConfig.getConfigurationSection("teams");
        if (teamsSection == null) return;
        
        for (String teamName : teamsSection.getKeys(false)) {
            ConfigurationSection teamSection = teamsSection.getConfigurationSection(teamName);
            if (teamSection == null) continue;
            
            try {
                // Load basic team data
                UUID ownerUUID = UUID.fromString(teamSection.getString("owner", ""));
                boolean pvpEnabled = teamSection.getBoolean("pvp_enabled", false);
                
                // Create team
                Team team = new Team(teamName, ownerUUID);
                team.setPvpEnabled(pvpEnabled);
                
                // Load members first (they might be promoted later)
                List<String> memberStrings = teamSection.getStringList("members");
                for (String memberString : memberStrings) {
                    UUID memberId = UUID.fromString(memberString);
                    team.addMember(memberId);
                }
                
                // Load recruits (needs to happen after members are loaded)
                List<String> recruitStrings = teamSection.getStringList("recruits");
                for (String recruitString : recruitStrings) {
                    UUID recruitId = UUID.fromString(recruitString);
                    // Need to add as member first, then promote
                    team.addMember(recruitId);
                    team.promoteToRecruit(recruitId);
                }
                
                // Load invites
                List<String> inviteStrings = teamSection.getStringList("invites");
                for (String inviteString : inviteStrings) {
                    UUID inviteId = UUID.fromString(inviteString);
                    team.invitePlayer(inviteId);
                }
                
                // Load home location if available
                if (teamSection.contains("home")) {
                    ConfigurationSection homeSection = teamSection.getConfigurationSection("home");
                    if (homeSection != null) {
                        String worldName = homeSection.getString("world");
                        double x = homeSection.getDouble("x");
                        double y = homeSection.getDouble("y");
                        double z = homeSection.getDouble("z");
                        float yaw = (float) homeSection.getDouble("yaw", 0);
                        float pitch = (float) homeSection.getDouble("pitch", 0);
                        
                        if (worldName != null && Bukkit.getWorld(worldName) != null) {
                            org.bukkit.Location loc = new org.bukkit.Location(
                                Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                            team.setHomeLocation(loc);
                        }
                    }
                }
                
                // Load team color if available
                if (teamSection.contains("color")) {
                    String colorName = teamSection.getString("color");
                    if (colorName != null) {
                        try {
                            org.bukkit.ChatColor color = org.bukkit.ChatColor.valueOf(colorName);
                            team.setColor(color);
                        } catch (IllegalArgumentException e) {
                            // Invalid color name, stick with default
                        }
                    }
                }
                
                // Load team level if available
                if (teamSection.contains("level")) {
                    int level = teamSection.getInt("level", 1);
                    team.setLevel(level);
                }
                
                // Add team to map
                teams.put(teamName.toLowerCase(), team);
                
                // Add online players to scoreboard team
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerId = player.getUniqueId();
                    if (team.isInTeam(playerId)) {
                        team.addPlayerToScoreboardTeam(player);
                    }
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING, "Error loading team " + teamName + ": " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Loaded " + teams.size() + " teams.");
    }

    /**
     * Saves teams to teams.yml
     */
    public void saveTeams() {
        // Clear existing teams section
        teamsConfig.set("teams", null);
        
        // Create new teams section
        ConfigurationSection teamsSection = teamsConfig.createSection("teams");
        
        // Save each team
        for (Team team : teams.values()) {
            ConfigurationSection teamSection = teamsSection.createSection(team.getName());
            
            // Save basic team data
            teamSection.set("owner", team.getOwner().toString());
            teamSection.set("pvp_enabled", team.isPvpEnabled());
            
            // Save recruits
            List<String> recruitStrings = new ArrayList<>();
            for (UUID recruitId : team.getRecruits()) {
                recruitStrings.add(recruitId.toString());
            }
            teamSection.set("recruits", recruitStrings);
            
            // Save members
            List<String> memberStrings = new ArrayList<>();
            for (UUID memberId : team.getMembers()) {
                memberStrings.add(memberId.toString());
            }
            teamSection.set("members", memberStrings);
            
            // Save invites - this needs special handling since we can't iterate through team.getInvites()
            List<String> inviteStrings = new ArrayList<>();
            // We'll save all invites directly using the keys in the config instead
            if (teamSection.contains("invites")) {
                inviteStrings = teamSection.getStringList("invites");
            }
            teamSection.set("invites", inviteStrings);
            
            // Save home location
            if (team.getHomeLocation() != null) {
                ConfigurationSection homeSection = teamSection.createSection("home");
                homeSection.set("world", team.getHomeLocation().getWorld().getName());
                homeSection.set("x", team.getHomeLocation().getX());
                homeSection.set("y", team.getHomeLocation().getY());
                homeSection.set("z", team.getHomeLocation().getZ());
                homeSection.set("yaw", team.getHomeLocation().getYaw());
                homeSection.set("pitch", team.getHomeLocation().getPitch());
            }
            
            // Save team color
            if (team.getColor() != null) {
                teamSection.set("color", team.getColor().name());
            }
            
            // Save team level
            teamSection.set("level", team.getLevel());
        }
        
        // Save to file
        try {
            teamsConfig.save(teamsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save teams to " + teamsFile, e);
        }
    }

    /**
     * Creates a new team
     * @param name Team name
     * @param owner Owner UUID
     * @return The created team, or null if a team with that name already exists
     */
    public Team createTeam(String name, UUID owner) {
        if (teams.containsKey(name.toLowerCase())) {
            return null;
        }
        
        Team team = new Team(name, owner);
        teams.put(name.toLowerCase(), team);
        saveTeams();
        return team;
    }

    /**
     * Gets a team by name
     * @param name Team name
     * @return The team, or null if not found
     */
    public Team getTeam(String name) {
        return teams.get(name.toLowerCase());
    }

    /**
     * Gets a player's team
     * @param playerId UUID of the player
     * @return The team the player is in, or null if not in a team
     */
    public Team getPlayerTeam(UUID playerId) {
        for (Team team : teams.values()) {
            if (team.isInTeam(playerId)) {
                return team;
            }
        }
        return null;
    }

    /**
     * Removes a team
     * @param name Team name
     */
    public void removeTeam(String name) {
        Team team = teams.remove(name.toLowerCase());
        if (team != null) {
            // Remove scoreboard team
            org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
            if (manager != null) {
                org.bukkit.scoreboard.Scoreboard scoreboard = manager.getMainScoreboard();
                org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(team.getName());
                if (scoreboardTeam != null) {
                    scoreboardTeam.unregister();
                }
            }
            saveTeams();
        }
    }

    /**
     * Gets all teams
     * @return Collection of all teams
     */
    public Collection<Team> getAllTeams() {
        return teams.values();
    }

    /**
     * Gets all teams a player is invited to
     * @param playerId UUID of the player
     * @return List of teams the player is invited to
     */
    public List<Team> getPlayerInvites(UUID playerId) {
        List<Team> invites = new ArrayList<>();
        for (Team team : teams.values()) {
            if (team.hasInvite(playerId)) {
                invites.add(team);
            }
        }
        return invites;
    }
    
    /**
     * Gets all team names a player is invited to
     * @param playerId UUID of the player
     * @return Set of team names the player is invited to
     */
    public Set<String> getPlayerInviteNames(UUID playerId) {
        Set<String> invites = new HashSet<>();
        for (Team team : teams.values()) {
            if (team.hasInvite(playerId)) {
                invites.add(team.getName());
            }
        }
        return invites;
    }
} 