package com.darkangel.teammanager.models;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Team implements ConfigurationSerializable {
    private String name;
    private UUID owner;
    private Set<UUID> members;
    private Set<UUID> recruits;
    private Set<UUID> invites;
    private boolean pvpEnabled;
    private ChatColor color;
    private Location homeLocation;
    private int level;
    
    // Constructor
    public Team(String name, UUID owner) {
        this.name = name;
        this.owner = owner;
        this.members = new HashSet<>();
        this.recruits = new HashSet<>();
        this.invites = new HashSet<>();
        this.pvpEnabled = false;
        this.color = ChatColor.WHITE; // Default color
        this.level = 1; // Default level
    }
    
    // Constructor from serialized map
    @SuppressWarnings("unchecked")
    public Team(Map<String, Object> map) {
        this.name = (String) map.get("name");
        this.owner = UUID.fromString((String) map.get("owner"));
        
        // Convert member UUIDs from strings
        List<String> memberStrings = (List<String>) map.get("members");
        this.members = memberStrings.stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
        
        // Convert recruit UUIDs from strings
        List<String> recruitStrings = (List<String>) map.getOrDefault("recruits", new ArrayList<>());
        this.recruits = recruitStrings.stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
        
        // Convert invite UUIDs from strings
        List<String> inviteStrings = (List<String>) map.getOrDefault("invites", new ArrayList<>());
        this.invites = inviteStrings.stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
        
        this.pvpEnabled = map.containsKey("pvp") ? (Boolean) map.get("pvp") : false;
        
        // Load team color
        this.color = map.containsKey("color") ? 
                ChatColor.valueOf((String) map.get("color")) : ChatColor.WHITE;
        
        // Load team level
        this.level = map.containsKey("level") ? (Integer) map.get("level") : 1;
        
        // Load home location if it exists
        if (map.containsKey("home")) {
            Map<String, Object> homeMap = (Map<String, Object>) map.get("home");
            String worldName = (String) homeMap.get("world");
            World world = Bukkit.getWorld(worldName);
            
            if (world != null) {
                double x = (Double) homeMap.get("x");
                double y = (Double) homeMap.get("y");
                double z = (Double) homeMap.get("z");
                float yaw = ((Double) homeMap.getOrDefault("yaw", 0.0)).floatValue();
                float pitch = ((Double) homeMap.getOrDefault("pitch", 0.0)).floatValue();
                
                this.homeLocation = new Location(world, x, y, z, yaw, pitch);
            }
        }
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("owner", owner.toString());
        
        // Convert member UUIDs to strings
        List<String> memberStrings = members.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        map.put("members", memberStrings);
        
        // Convert recruit UUIDs to strings
        List<String> recruitStrings = recruits.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        map.put("recruits", recruitStrings);
        
        // Convert invite UUIDs to strings
        List<String> inviteStrings = invites.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        map.put("invites", inviteStrings);
        
        map.put("pvp", pvpEnabled);
        
        // Save team color
        map.put("color", color.name());
        
        // Save team level
        map.put("level", level);
        
        // Save home location
        if (homeLocation != null) {
            Map<String, Object> homeMap = new HashMap<>();
            homeMap.put("world", homeLocation.getWorld().getName());
            homeMap.put("x", homeLocation.getX());
            homeMap.put("y", homeLocation.getY());
            homeMap.put("z", homeLocation.getZ());
            homeMap.put("yaw", (double) homeLocation.getYaw());
            homeMap.put("pitch", (double) homeLocation.getPitch());
            
            map.put("home", homeMap);
        }
        
        return map;
    }
    
    /**
     * Adds the player to the scoreboard team for visual identification
     */
    public void addPlayerToScoreboardTeam(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        
        Scoreboard scoreboard = manager.getMainScoreboard();
        org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(name);
        
        if (scoreboardTeam == null) {
            // Create the scoreboard team if it doesn't exist
            scoreboardTeam = scoreboard.registerNewTeam(name);
            scoreboardTeam.setAllowFriendlyFire(pvpEnabled);
            scoreboardTeam.setColor(color);
            scoreboardTeam.setPrefix(color + "[" + name + "] ");
        }
        
        scoreboardTeam.addEntry(player.getName());
    }
    
    /**
     * Updates the scoreboard team color
     */
    public void updateScoreboardTeamColor() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        
        Scoreboard scoreboard = manager.getMainScoreboard();
        org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(name);
        
        if (scoreboardTeam != null) {
            scoreboardTeam.setColor(color);
            scoreboardTeam.setPrefix(color + "[" + name + "] ");
        }
    }
    
    // Getters and setters
    
    public String getName() {
        return name;
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public Set<UUID> getMembers() {
        return new HashSet<>(members);
    }
    
    public Set<UUID> getRecruits() {
        return new HashSet<>(recruits);
    }
    
    public Set<UUID> getAllMembers() {
        Set<UUID> allMembers = new HashSet<>();
        allMembers.add(owner);
        allMembers.addAll(members);
        allMembers.addAll(recruits);
        return allMembers;
    }
    
    public Collection<Player> getOnlineMembers() {
        return getAllMembers().stream()
                .map(Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline())
                .collect(Collectors.toList());
    }
    
    public boolean isPvpEnabled() {
        return pvpEnabled;
    }
    
    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
        
        // Update scoreboard team
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        
        Scoreboard scoreboard = manager.getMainScoreboard();
        org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(name);
        
        if (scoreboardTeam != null) {
            scoreboardTeam.setAllowFriendlyFire(pvpEnabled);
        }
    }
    
    public ChatColor getColor() {
        return color;
    }
    
    public void setColor(ChatColor color) {
        this.color = color;
    }
    
    public Location getHomeLocation() {
        return homeLocation;
    }
    
    public void setHomeLocation(Location homeLocation) {
        this.homeLocation = homeLocation;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        if (level < 1) level = 1;
        if (level > 10) level = 10;
        this.level = level;
    }
    
    public int getMaxMembers() {
        // Calculate max members based on level
        return 5 + (level - 1) * 2; // Level 1: 5 members, Level 10: 23 members
    }
    
    // Team membership methods
    
    public boolean isOwner(UUID playerId) {
        return owner.equals(playerId);
    }
    
    public boolean isInTeam(UUID playerId) {
        return isOwner(playerId) || members.contains(playerId) || recruits.contains(playerId);
    }
    
    public boolean isRecruit(UUID playerId) {
        return recruits.contains(playerId);
    }
    
    public void addMember(UUID playerId) {
        members.add(playerId);
        invites.remove(playerId);
        
        // Add player to scoreboard team if online
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            addPlayerToScoreboardTeam(player);
        }
    }
    
    public void removeMember(UUID playerId) {
        members.remove(playerId);
        recruits.remove(playerId);
        
        // Remove player from scoreboard team if online
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            if (manager != null) {
                Scoreboard scoreboard = manager.getMainScoreboard();
                org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(name);
                
                if (scoreboardTeam != null) {
                    scoreboardTeam.removeEntry(player.getName());
                }
            }
        }
    }
    
    public boolean promoteToRecruit(UUID playerId) {
        if (members.remove(playerId)) {
            recruits.add(playerId);
            return true;
        }
        return false;
    }
    
    public void invitePlayer(UUID playerId) {
        invites.add(playerId);
    }
    
    public boolean hasInvite(UUID playerId) {
        return invites.contains(playerId);
    }
    
    public void removeInvite(UUID playerId) {
        invites.remove(playerId);
    }
} 