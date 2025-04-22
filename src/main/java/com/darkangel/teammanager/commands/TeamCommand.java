package com.darkangel.teammanager.commands;

import com.darkangel.teammanager.TeamManager;
import com.darkangel.teammanager.managers.TeamDataManager;
import com.darkangel.teammanager.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class TeamCommand implements CommandExecutor, TabCompleter {
    private final TeamManager plugin;
    private final TeamDataManager teamDataManager;
    
    // Add a HashMap to track confirmation for team disband
    private final Map<UUID, Long> disbandConfirmation = new HashMap<>();
    private static final long CONFIRMATION_TIMEOUT_MS = 30000; // 30 seconds timeout
    
    // Add a HashSet to track players who have team chat toggled on
    private final Set<UUID> teamChatToggled = new HashSet<>();
    
    // Add a Map to track team alliances
    private final Map<String, Set<String>> teamAlliances = new HashMap<>();

    private final List<String> subCommands = Arrays.asList(
            "create", "disband", "invite", "join", "leave", 
            "kick", "pvp", "info", "list", "help", "promote", "confirm",
            "sethome", "home", "tctoggle", "color", "ally", "setlevel", "demote",
            "desc", "stats"
    );
    
    private final List<String> pvpOptions = Arrays.asList("on", "off");
    private final List<String> colorOptions = Arrays.asList(
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", 
            "dark_purple", "gold", "gray", "dark_gray", "blue", "green",
            "aqua", "red", "light_purple", "yellow", "white"
    );

    public TeamCommand(TeamManager plugin) {
        this.plugin = plugin;
        this.teamDataManager = plugin.getTeamDataManager();
        
        // Load team homes, colors, levels, and alliances from config
        loadTeamData();
    }
    
    /**
     * Loads additional team data from config files
     */
    private void loadTeamData() {
        FileConfiguration config = plugin.getConfig();
        
        // Load team alliances (example format: "team1:team2,team3;team4:team5")
        String alliancesStr = config.getString("team_alliances", "");
        if (!alliancesStr.isEmpty()) {
            String[] alliancePairs = alliancesStr.split(";");
            for (String pair : alliancePairs) {
                String[] parts = pair.split(":");
                if (parts.length == 2) {
                    String team = parts[0];
                    String[] allies = parts[1].split(",");
                    Set<String> allySet = new HashSet<>(Arrays.asList(allies));
                    teamAlliances.put(team, allySet);
                }
            }
        }
        
        // Additional data is loaded in the Team class
    }
    
    /**
     * Saves additional team data to config
     */
    private void saveTeamData() {
        FileConfiguration config = plugin.getConfig();
        
        // Save team alliances
        StringBuilder alliancesStr = new StringBuilder();
        for (Map.Entry<String, Set<String>> entry : teamAlliances.entrySet()) {
            if (alliancesStr.length() > 0) {
                alliancesStr.append(";");
            }
            alliancesStr.append(entry.getKey()).append(":");
            alliancesStr.append(String.join(",", entry.getValue()));
        }
        config.set("team_alliances", alliancesStr.toString());
        
        // Save config
        plugin.saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Open the team GUI instead of showing help
            plugin.getTeamGUI().openMainMenu(player);
            return true;
        }
        
        if (args[0].equalsIgnoreCase("help")) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                handleCreate(player, args);
                break;
            case "disband":
                handleDisband(player);
                break;
            case "invite":
                handleInvite(player, args);
                break;
            case "join":
                handleJoin(player, args);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "kick":
                handleKick(player, args);
                break;
            case "pvp":
                handlePvP(player, args);
                break;
            case "info":
                handleInfo(player, args);
                break;
            case "list":
                handleList(player);
                break;
            case "promote":
                handlePromote(player, args);
                break;
            case "demote":
                handleDemote(player, args);
                break;
            case "confirm":
                handleConfirm(player);
                break;
            case "sethome":
                handleSetHome(player);
                break;
            case "home":
                handleHome(player);
                break;
            case "tctoggle":
                handleTeamChatToggle(player);
                break;
            case "color":
                handleTeamColor(player, args);
                break;
            case "ally":
                handleTeamAlly(player, args);
                break;
            case "setlevel":
                handleSetLevel(player, args);
                break;
            case "desc":
                handleSetDescription(player, args);
                break;
            case "stats":
                handleShowStats(player, args);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown sub-command. Type /team help for help.");
                break;
        }
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("join")) {
                // Tab complete team names for join
                return teamDataManager.getAllTeams().stream()
                        .map(Team::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("invite") || subCommand.equals("kick") || subCommand.equals("promote")) {
                // Tab complete online player names for invite, kick, and promote
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("pvp")) {
                // Tab complete on/off for pvp toggle
                return pvpOptions.stream()
                        .filter(option -> option.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("color")) {
                // Tab complete colors
                return colorOptions.stream()
                        .filter(color -> color.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("ally")) {
                // Tab complete team names for ally
                return teamDataManager.getAllTeams().stream()
                        .map(Team::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("setlevel")) {
                // Tab complete levels 1-10
                List<String> levels = new ArrayList<>();
                for (int i = 1; i <= 10; i++) {
                    levels.add(String.valueOf(i));
                }
                return levels.stream()
                        .filter(level -> level.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }

    // ------------- Command Handlers -------------

    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GREEN + "===== Team Commands =====");
        player.sendMessage(ChatColor.YELLOW + "/team create <name>" + ChatColor.WHITE + " - Create a new team");
        player.sendMessage(ChatColor.YELLOW + "/team disband" + ChatColor.WHITE + " - Disband your team (Owner only)");
        player.sendMessage(ChatColor.YELLOW + "/team invite <player>" + ChatColor.WHITE + " - Invite a player to your team");
        player.sendMessage(ChatColor.YELLOW + "/team join <name>" + ChatColor.WHITE + " - Join a team you've been invited to");
        player.sendMessage(ChatColor.YELLOW + "/team leave" + ChatColor.WHITE + " - Leave your current team");
        player.sendMessage(ChatColor.YELLOW + "/team kick <player>" + ChatColor.WHITE + " - Kick a player from your team");
        player.sendMessage(ChatColor.YELLOW + "/team promote <player>" + ChatColor.WHITE + " - Promote a member to recruiter");
        player.sendMessage(ChatColor.YELLOW + "/team demote <player>" + ChatColor.WHITE + " - Demote a recruiter to member");
        player.sendMessage(ChatColor.YELLOW + "/team pvp [on|off]" + ChatColor.WHITE + " - Toggle team PvP (Owner only)");
        player.sendMessage(ChatColor.YELLOW + "/team info [name]" + ChatColor.WHITE + " - Show team information");
        player.sendMessage(ChatColor.YELLOW + "/team list" + ChatColor.WHITE + " - List all teams");
        player.sendMessage(ChatColor.YELLOW + "/team sethome" + ChatColor.WHITE + " - Set team home location");
        player.sendMessage(ChatColor.YELLOW + "/team home" + ChatColor.WHITE + " - Teleport to team home");
        player.sendMessage(ChatColor.YELLOW + "/team tctoggle" + ChatColor.WHITE + " - Toggle team chat mode");
        player.sendMessage(ChatColor.YELLOW + "/team color <color>" + ChatColor.WHITE + " - Set team color");
        player.sendMessage(ChatColor.YELLOW + "/team ally <team>" + ChatColor.WHITE + " - Request alliance with another team");
        player.sendMessage(ChatColor.YELLOW + "/team desc <text>" + ChatColor.WHITE + " - Set team description");
        player.sendMessage(ChatColor.YELLOW + "/team stats [name]" + ChatColor.WHITE + " - View team statistics");
        player.sendMessage(ChatColor.YELLOW + "/team setlevel <1-10>" + ChatColor.WHITE + " - Set team level (Admin only)");
        player.sendMessage(ChatColor.YELLOW + "/team help" + ChatColor.WHITE + " - Show this help message");
    }

    private void handleCreate(Player player, String[] args) {
        if (!player.hasPermission("teammanager.team.create")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to create teams.");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /team create <name>");
            return;
        }
        
        String teamName = args[1];
        
        // Check if player is already in a team
        if (teamDataManager.getPlayerTeam(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "You're already in a team. Leave it first to create a new one.");
            return;
        }
        
        // Create the team
        Team team = teamDataManager.createTeam(teamName, player.getUniqueId());
        
        if (team == null) {
            player.sendMessage(ChatColor.RED + "A team with that name already exists.");
            return;
        }
        
        // Add player to scoreboard team
        team.addPlayerToScoreboardTeam(player);
        
        player.sendMessage(ChatColor.GREEN + "Team '" + teamName + "' created successfully! You are the owner.");
    }

    private void handleDisband(Player player) {
        if (!player.hasPermission("teammanager.team.disband")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to disband teams.");
            return;
        }
        
        // Get player's team
        Team team = teamDataManager.getPlayerTeam(player.getUniqueId());
        
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You're not in a team.");
            return;
        }
        
        // Check if player is the owner
        if (!team.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the team owner can disband the team.");
            return;
        }
        
        // Check if player has already requested confirmation
        if (disbandConfirmation.containsKey(player.getUniqueId())) {
            long confirmTime = disbandConfirmation.get(player.getUniqueId());
            if (System.currentTimeMillis() - confirmTime < CONFIRMATION_TIMEOUT_MS) {
                // Player has already requested confirmation and it hasn't expired
                // Instead, redirect them to the confirm command
                player.sendMessage(ChatColor.YELLOW + "Please type " + ChatColor.RED + "/team confirm" + 
                        ChatColor.YELLOW + " to disband your team or wait for the request to expire.");
                return;
            }
        }
        
        // Request confirmation
        disbandConfirmation.put(player.getUniqueId(), System.currentTimeMillis());
        
        player.sendMessage(ChatColor.YELLOW + "===== DISBAND CONFIRMATION =====");
        player.sendMessage(ChatColor.RED + "WARNING: You are about to disband team '" + team.getName() + "'.");
        player.sendMessage(ChatColor.RED + "This action CANNOT be undone.");
        player.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.RED + "/team confirm" + 
                ChatColor.YELLOW + " within 30 seconds to disband your team.");
        player.sendMessage(ChatColor.YELLOW + "Or type anything else to cancel this operation.");
    }

    private void handleConfirm(Player player) {
        // Check if player has a pending disband confirmation
        if (!disbandConfirmation.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You don't have any pending confirmation.");
            return;
        }
        
        // Check if confirmation has expired (30 seconds)
        long confirmTime = disbandConfirmation.get(player.getUniqueId());
        if (System.currentTimeMillis() - confirmTime > CONFIRMATION_TIMEOUT_MS) {
            disbandConfirmation.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "Your disband request has expired. Please try again.");
            return;
        }
        
        // Get player's team
        Team team = teamDataManager.getPlayerTeam(player.getUniqueId());
        
        if (team == null) {
            disbandConfirmation.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "You're not in a team.");
            return;
        }
        
        // Verify player is still the owner
        if (!team.isOwner(player.getUniqueId())) {
            disbandConfirmation.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "You are no longer the team owner.");
            return;
        }
        
        // Remove the confirmation entry
        disbandConfirmation.remove(player.getUniqueId());
        
        // Notify all online team members
        for (Player member : team.getOnlineMembers()) {
            member.sendMessage(ChatColor.YELLOW + "Your team '" + team.getName() + "' has been disbanded by the owner.");
        }
        
        // Remove the team
        teamDataManager.removeTeam(team.getName());
        
        player.sendMessage(ChatColor.GREEN + "Team '" + team.getName() + "' has been disbanded.");
    }

    private void handleInvite(Player player, String[] args) {
        if (!player.hasPermission("teammanager.team.invite")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to invite players.");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /team invite <player>");
            return;
        }
        
        // Get player's team
        Team team = teamDataManager.getPlayerTeam(player.getUniqueId());
        
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You're not in a team.");
            return;
        }
        
        // Check if player is the owner or a recruiter
        if (!team.isOwner(player.getUniqueId()) && !team.isRecruit(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the team owner and recruiters can invite players.");
            return;
        }
        
        // Get the target player
        Player targetPlayer = Bukkit.getPlayer(args[1]);
        
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Player '" + args[1] + "' is not online.");
            return;
        }
        
        UUID targetId = targetPlayer.getUniqueId();
        
        // Check if target player is already in the team
        if (team.isInTeam(targetId)) {
            player.sendMessage(ChatColor.RED + "Player '" + targetPlayer.getName() + "' is already in your team.");
            return;
        }
        
        // Check if target player is already in another team
        if (teamDataManager.getPlayerTeam(targetId) != null) {
            player.sendMessage(ChatColor.RED + "Player '" + targetPlayer.getName() + "' is already in another team.");
            return;
        }
        
        // Check if target player already has an invite from this team
        if (team.hasInvite(targetId)) {
            player.sendMessage(ChatColor.RED + "Player '" + targetPlayer.getName() + "' already has an invite to your team.");
            return;
        }
        
        // Send invite
        team.invitePlayer(targetId);
        teamDataManager.saveTeams();
        
        player.sendMessage(ChatColor.GREEN + "Invited " + targetPlayer.getName() + " to your team.");
        targetPlayer.sendMessage(ChatColor.YELLOW + "You've been invited to join team '" + team.getName() + "'.");
        targetPlayer.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.WHITE + "/team join " + team.getName() + ChatColor.YELLOW + " to accept.");
    }

    private void handleJoin(Player player, String[] args) {
        if (!player.hasPermission("teammanager.team.join")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to join teams.");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /team join <name>");
            return;
        }
        
        // Check if player is already in a team
        if (teamDataManager.getPlayerTeam(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "You're already in a team. Leave it first to join another one.");
            return;
        }
        
        // Get the team
        Team team = teamDataManager.getTeam(args[1]);
        
        if (team == null) {
            player.sendMessage(ChatColor.RED + "Team '" + args[1] + "' doesn't exist.");
            return;
        }
        
        // Check if player has an invite
        if (!team.hasInvite(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You don't have an invite to this team.");
            return;
        }
        
        // Add player to team
        team.addMember(player.getUniqueId());
        teamDataManager.saveTeams();
        
        // Notify team members
        for (Player member : team.getOnlineMembers()) {
            if (!member.equals(player)) {
                member.sendMessage(ChatColor.GREEN + player.getName() + " has joined your team!");
            }
        }
        
        player.sendMessage(ChatColor.GREEN + "You've joined team '" + team.getName() + "'!");
    }

    private void handleLeave(Player player) {
        if (!player.hasPermission("teammanager.team.leave")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to leave teams.");
            return;
        }
        
        // Get player's team
        Team team = teamDataManager.getPlayerTeam(player.getUniqueId());
        
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You're not in a team.");
            return;
        }
        
        // Check if player is the owner
        if (team.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "As the owner, you can't leave the team. Use '/team disband' or transfer ownership first.");
            return;
        }
        
        // Remove player from team
        team.removeMember(player.getUniqueId());
        teamDataManager.saveTeams();
        
        // Notify team members
        for (Player member : team.getOnlineMembers()) {
            member.sendMessage(ChatColor.YELLOW + player.getName() + " has left the team.");
        }
        
        player.sendMessage(ChatColor.GREEN + "You've left team '" + team.getName() + "'.");
    }

    private void handleKick(Player player, String[] args) {
        if (!player.hasPermission("teammanager.team.kick")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to kick players.");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /team kick <player>");
            return;
        }
        
        // Get player's team
        Team team = teamDataManager.getPlayerTeam(player.getUniqueId());
        
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You're not in a team.");
            return;
        }
        
        // Check if player is the owner or a recruiter
        if (!team.isOwner(player.getUniqueId()) && !team.isRecruit(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the team owner and recruiters can kick players.");
            return;
        }
        
        // Get the target player
        Player targetPlayer = Bukkit.getPlayer(args[1]);
        UUID targetId;
        String targetName;
        
        if (targetPlayer != null) {
            targetId = targetPlayer.getUniqueId();
            targetName = targetPlayer.getName();
        } else {
            // Try to find offline player by name
            targetId = null;
            targetName = args[1];
            
            for (UUID memberId : team.getAllMembers()) {
                String memberName = Bukkit.getOfflinePlayer(memberId).getName();
                
                if (memberName != null && memberName.equalsIgnoreCase(args[1])) {
                    targetId = memberId;
                    targetName = memberName;
                    break;
                }
            }
            
            if (targetId == null) {
                player.sendMessage(ChatColor.RED + "Player '" + args[1] + "' is not in your team.");
                return;
            }
        }
        
        // Check if target player is in the team
        if (!team.isInTeam(targetId)) {
            player.sendMessage(ChatColor.RED + "Player '" + targetName + "' is not in your team.");
            return;
        }
        
        // Check if target player is the owner
        if (team.isOwner(targetId)) {
            player.sendMessage(ChatColor.RED + "You can't kick the team owner.");
            return;
        }
        
        // Check if target is a recruiter and kicker is not the owner
        if (team.isRecruit(targetId) && !team.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the team owner can kick recruiters.");
            return;
        }
        
        // Remove player from team
        team.removeMember(targetId);
        teamDataManager.saveTeams();
        
        // Notify team members
        for (Player member : team.getOnlineMembers()) {
            member.sendMessage(ChatColor.YELLOW + targetName + " has been kicked from the team by " + player.getName() + ".");
        }
        
        // Notify kicked player if online
        if (targetPlayer != null) {
            targetPlayer.sendMessage(ChatColor.RED + "You've been kicked from team '" + team.getName() + "' by " + player.getName() + ".");
        }
        
        player.sendMessage(ChatColor.GREEN + "Kicked " + targetName + " from your team.");
    }

    private void handlePvP(Player player, String[] args) {
        if (!player.hasPermission("teammanager.team.pvp")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to toggle team PvP.");
            return;
        }
        
        // Get player's team
        Team team = teamDataManager.getPlayerTeam(player.getUniqueId());
        
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You're not in a team.");
            return;
        }
        
        // Check if player is the owner
        if (!team.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the team owner can toggle team PvP.");
            return;
        }
        
        boolean newPvPStatus;
        
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("on")) {
                newPvPStatus = true;
            } else if (args[1].equalsIgnoreCase("off")) {
                newPvPStatus = false;
            } else {
                player.sendMessage(ChatColor.RED + "Usage: /team pvp [on|off]");
                return;
            }
        } else {
            // Toggle current status
            newPvPStatus = !team.isPvpEnabled();
        }
        
        // Update PvP status
        team.setPvpEnabled(newPvPStatus);
        teamDataManager.saveTeams();
        
        String statusText = newPvPStatus ? "enabled" : "disabled";
        
        // Notify team members
        for (Player member : team.getOnlineMembers()) {
            member.sendMessage(ChatColor.YELLOW + "Team PvP has been " + statusText + " by " + player.getName() + ".");
        }
        
        player.sendMessage(ChatColor.GREEN + "Team PvP " + statusText + ".");
    }

    private void handleInfo(Player player, String[] args) {
        if (!player.hasPermission("teammanager.team.info")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to view team information.");
            return;
        }
        
        // Get the team
        Team team;
        
        if (args.length >= 2) {
            // Show info for specific team
            team = teamDataManager.getTeam(args[1]);
            
            if (team == null) {
                player.sendMessage(ChatColor.RED + "Team '" + args[1] + "' doesn't exist.");
                return;
            }
        } else {
            // Show info for player's team
            team = teamDataManager.getPlayerTeam(player.getUniqueId());
            
            if (team == null) {
                player.sendMessage(ChatColor.RED + "You're not in a team. Use '/team info <name>' to view info for other teams.");
                return;
            }
        }
        
        // Display team info
        player.sendMessage(ChatColor.GREEN + "===== Team: " + team.getName() + " =====");
        
        // Owner
        String ownerName = Bukkit.getOfflinePlayer(team.getOwner()).getName();
        player.sendMessage(ChatColor.YELLOW + "Owner: " + ChatColor.WHITE + (ownerName != null ? ownerName : "Unknown"));
        
        // Description
        player.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.WHITE + team.getDescription());
        
        // Age
        player.sendMessage(ChatColor.YELLOW + "Age: " + ChatColor.WHITE + team.getAgeInDays() + " days");
        
        // PvP status
        String pvpStatus = team.isPvpEnabled() ? "Enabled" : "Disabled";
        player.sendMessage(ChatColor.YELLOW + "PvP: " + ChatColor.WHITE + pvpStatus);
        
        // Recruiters
        if (!team.getRecruits().isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Recruiters:");
            for (UUID recruitId : team.getRecruits()) {
                String recruitName = Bukkit.getOfflinePlayer(recruitId).getName();
                player.sendMessage(ChatColor.WHITE + "- " + (recruitName != null ? recruitName : "Unknown"));
            }
        }
        
        // Members
        if (!team.getMembers().isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Members:");
            for (UUID memberId : team.getMembers()) {
                String memberName = Bukkit.getOfflinePlayer(memberId).getName();
                player.sendMessage(ChatColor.WHITE + "- " + (memberName != null ? memberName : "Unknown"));
            }
        }
        
        // Total members
        int totalMembers = 1 + team.getRecruits().size() + team.getMembers().size();
        player.sendMessage(ChatColor.YELLOW + "Total Members: " + ChatColor.WHITE + totalMembers);
    }

    private void handleList(Player player) {
        if (!player.hasPermission("teammanager.team.list")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to list teams.");
            return;
        }
        
        Collection<Team> teams = teamDataManager.getAllTeams();
        
        if (teams.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "There are no teams yet.");
            return;
        }
        
        player.sendMessage(ChatColor.GREEN + "===== Teams =====");
        
        for (Team team : teams) {
            String ownerName = Bukkit.getOfflinePlayer(team.getOwner()).getName();
            int memberCount = 1 + team.getRecruits().size() + team.getMembers().size();
            
            player.sendMessage(ChatColor.YELLOW + team.getName() + ChatColor.WHITE + 
                    " | Owner: " + (ownerName != null ? ownerName : "Unknown") + 
                    " | Members: " + memberCount);
        }
    }

    /**
     * Handles promoting a team member to recruiter
     */
    private void handlePromote(Player player, String[] args) {
        if (!player.hasPermission("teammanager.team.promote")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to promote team members.");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /team promote <player>");
            return;
        }
        
        // Get player's team
        Team team = teamDataManager.getPlayerTeam(player.getUniqueId());
        
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You're not in a team.");
            return;
        }
        
        // Check if player is the owner
        if (!team.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the team owner can promote members.");
            return;
        }
        
        // Get the target player
        String targetName = args[1];
        UUID targetId = null;
        
        // Try to find player by name in team members
        for (UUID memberId : team.getMembers()) {
            String memberName = Bukkit.getOfflinePlayer(memberId).getName();
            if (memberName != null && memberName.equalsIgnoreCase(targetName)) {
                targetId = memberId;
                targetName = memberName; // Use correct case
                break;
            }
        }
        
        if (targetId == null) {
            player.sendMessage(ChatColor.RED + "Player '" + targetName + "' is not a member of your team or is already a recruiter.");
            return;
        }
        
        // Promote member to recruiter
        if (team.promoteToRecruit(targetId)) {
            teamDataManager.saveTeams();
            
            player.sendMessage(ChatColor.GREEN + "You've promoted " + targetName + " to a team recruiter.");
            
            // Notify promoted player if online
            Player targetPlayer = Bukkit.getPlayer(targetId);
            if (targetPlayer != null && targetPlayer.isOnline()) {
                targetPlayer.sendMessage(ChatColor.GREEN + "You've been promoted to a recruiter in team '" + team.getName() + "'.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to promote " + targetName + ". They might not be a regular member.");
        }
    }

    /**
     * Handles setting a team home location
     */
    private void handleSetHome(Player player) {
        if (!player.hasPermission("teammanager.team.sethome")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to set team home.");
            return;
        }
        
        // Get player's team
        Team team = teamDataManager.getPlayerTeam(player.getUniqueId());
        
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You're not in a team.");
            return;
        }
        
        // Check if player is the owner
        if (!team.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the team owner can set the team home.");
            return;
        }
        
        // Set team home
        Location loc = player.getLocation();
        team.setHomeLocation(loc);
        teamDataManager.saveTeams();
        
        player.sendMessage(ChatColor.GREEN + "Team home set at your current location.");
    }
    
    /**
     * Handles teleporting to team home
     */
    private void handleHome(Player player) {
        if (!player.hasPermission("teammanager.team.home")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use team home.");
            return;
        }
        
        // Get player's team
        Team team = teamDataManager.getPlayerTeam(player.getUniqueId());
        
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You're not in a team.");
            return;
        }
        
        // Get team home
        Location home = team.getHomeLocation();
        
        if (home == null) {
            player.sendMessage(ChatColor.RED + "Your team doesn't have a home set yet.");
            return;
        }
        
        // Teleport player to team home
        player.teleport(home);
        player.sendMessage(ChatColor.GREEN + "Teleported to team home.");
    }
    
    /**
     * Handles toggling team chat mode
     */
    private void handleTeamChatToggle(Player player) {
        if (!player.hasPermission("teammanager.team.chat")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use team chat.");
            return;
        }
        
        // Get player's team
        Team team = teamDataManager.getPlayerTeam(player.getUniqueId());
        
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You're not in a team.");
            return;
        }
        
        UUID playerId = player.getUniqueId();
        
        // Toggle team chat mode
        if (teamChatToggled.contains(playerId)) {
            teamChatToggled.remove(playerId);
            player.sendMessage(ChatColor.GREEN + "Team chat mode: " + ChatColor.RED + "OFF");
        } else {
            teamChatToggled.add(playerId);
            player.sendMessage(ChatColor.GREEN + "Team chat mode: " + ChatColor.GREEN + "ON");
        }
    }
    
    /**
     * Checks if a player has team chat toggled on
     */
    public boolean hasTeamChatToggled(UUID playerId) {
        return teamChatToggled.contains(playerId);
    }
    
    /**
     * Handles setting team color
     */
    private void handleTeamColor(Player player, String[] args) {
        if (!player.hasPermission("teammanager.team.color")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to set team color.");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /team color <color>");
            player.sendMessage(ChatColor.RED + "Available colors: " + String.join(", ", colorOptions));
            return;
        }
        
        // Get player's team
        Team team = teamDataManager.getPlayerTeam(player.getUniqueId());
        
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You're not in a team.");
            return;
        }
        
        // Check if player is the owner
        if (!team.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the team owner can set the team color.");
            return;
        }
        
        String colorName = args[1].toLowerCase();
        
        // Validate color
        if (!colorOptions.contains(colorName)) {
            player.sendMessage(ChatColor.RED + "Invalid color. Available colors: " + String.join(", ", colorOptions));
            return;
        }
        
        // Set team color
        ChatColor color = ChatColor.valueOf(colorName.toUpperCase());
        team.setColor(color);
        teamDataManager.saveTeams();
        
        // Update scoreboard team
        team.updateScoreboardTeamColor();
        
        player.sendMessage(ChatColor.GREEN + "Team color set to " + color + colorName);
    }
    
    /**
     * Handles team alliance requests
     */
    private void handleTeamAlly(Player player, String[] args) {
        if (!player.hasPermission("teammanager.team.ally")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to manage team alliances.");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /team ally <team>");
            return;
        }
        
        // Get player's team
        Team team = teamDataManager.getPlayerTeam(player.getUniqueId());
        
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You're not in a team.");
            return;
        }
        
        // Check if player is the owner
        if (!team.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the team owner can manage team alliances.");
            return;
        }
        
        String targetTeamName = args[1];
        Team targetTeam = teamDataManager.getTeam(targetTeamName);
        
        if (targetTeam == null) {
            player.sendMessage(ChatColor.RED + "Team '" + targetTeamName + "' doesn't exist.");
            return;
        }
        
        // Check if teams are already allied
        if (areTeamsAllied(team.getName(), targetTeamName)) {
            // Remove alliance
            removeAlliance(team.getName(), targetTeamName);
            
            player.sendMessage(ChatColor.YELLOW + "Alliance with team '" + targetTeamName + "' has been broken.");
            
            // Notify other team if owner is online
            Player targetOwner = Bukkit.getPlayer(targetTeam.getOwner());
            if (targetOwner != null && targetOwner.isOnline()) {
                targetOwner.sendMessage(ChatColor.YELLOW + "Team '" + team.getName() + "' has broken their alliance with your team.");
            }
        } else {
            // Request alliance
            player.sendMessage(ChatColor.YELLOW + "Alliance request sent to team '" + targetTeamName + "'.");
            
            // Notify target team owner
            Player targetOwner = Bukkit.getPlayer(targetTeam.getOwner());
            if (targetOwner != null && targetOwner.isOnline()) {
                targetOwner.sendMessage(ChatColor.YELLOW + "Team '" + team.getName() + "' wants to form an alliance with your team.");
                targetOwner.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.WHITE + "/team ally " + team.getName() + ChatColor.YELLOW + " to accept.");
            }
            
            // If the target team has already requested an alliance, form it
            if (hasAllianceRequest(targetTeamName, team.getName())) {
                addAlliance(team.getName(), targetTeamName);
                
                // Notify both teams
                player.sendMessage(ChatColor.GREEN + "Alliance formed with team '" + targetTeamName + "'!");
                
                if (targetOwner != null && targetOwner.isOnline()) {
                    targetOwner.sendMessage(ChatColor.GREEN + "Alliance formed with team '" + team.getName() + "'!");
                }
            }
        }
        
        // Save alliance data
        saveTeamData();
    }
    
    /**
     * Handles setting team level
     */
    private void handleSetLevel(Player player, String[] args) {
        if (!player.hasPermission("teammanager.admin.setlevel")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to set team levels.");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /team setlevel <1-10>");
            return;
        }
        
        // Get player's team
        Team team = teamDataManager.getPlayerTeam(player.getUniqueId());
        
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You're not in a team.");
            return;
        }
        
        int level;
        try {
            level = Integer.parseInt(args[1]);
            if (level < 1 || level > 10) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Level must be a number between 1 and 10.");
            return;
        }
        
        // Set team level
        team.setLevel(level);
        teamDataManager.saveTeams();
        
        player.sendMessage(ChatColor.GREEN + "Team level set to " + level + ".");
        
        // Calculate max members based on level
        int maxMembers = 5 + (level - 1) * 2; // Level 1: 5 members, Level 10: 23 members
        player.sendMessage(ChatColor.YELLOW + "Your team can now have up to " + maxMembers + " members.");
        
        // Notify other online team members
        for (Player member : team.getOnlineMembers()) {
            if (!member.equals(player)) {
                member.sendMessage(ChatColor.YELLOW + "Your team has reached level " + level + "!");
                member.sendMessage(ChatColor.YELLOW + "Your team can now have up to " + maxMembers + " members.");
            }
        }
    }
    
    // ------------- Alliance Management -------------
    
    private boolean areTeamsAllied(String team1, String team2) {
        Set<String> allies = teamAlliances.get(team1);
        return allies != null && allies.contains(team2);
    }
    
    private boolean hasAllianceRequest(String team1, String team2) {
        Set<String> allies = teamAlliances.get(team1);
        return allies != null && allies.contains("request:" + team2);
    }
    
    private void addAlliance(String team1, String team2) {
        // Add alliance for team1
        teamAlliances.computeIfAbsent(team1, k -> new HashSet<>()).add(team2);
        
        // Add alliance for team2
        teamAlliances.computeIfAbsent(team2, k -> new HashSet<>()).add(team1);
        
        // Remove any pending requests
        teamAlliances.get(team1).remove("request:" + team2);
        teamAlliances.get(team2).remove("request:" + team1);
    }
    
    private void removeAlliance(String team1, String team2) {
        // Remove alliance for team1
        if (teamAlliances.containsKey(team1)) {
            teamAlliances.get(team1).remove(team2);
        }
        
        // Remove alliance for team2
        if (teamAlliances.containsKey(team2)) {
            teamAlliances.get(team2).remove(team1);
        }
    }

    /**
     * Handles demoting a team recruiter to member
     */
    private void handleDemote(Player player, String[] args) {
        if (!player.hasPermission("teammanager.team.promote")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to demote team members.");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /team demote <player>");
            return;
        }
        
        // Get player's team
        Team team = teamDataManager.getPlayerTeam(player.getUniqueId());
        
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You're not in a team.");
            return;
        }
        
        // Check if player is the owner
        if (!team.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the team owner can demote recruiters.");
            return;
        }
        
        // Get the target player
        String targetName = args[1];
        UUID targetId = null;
        
        // Try to find player by name in team recruiters
        for (UUID recruitId : team.getRecruits()) {
            String memberName = Bukkit.getOfflinePlayer(recruitId).getName();
            if (memberName != null && memberName.equalsIgnoreCase(targetName)) {
                targetId = recruitId;
                targetName = memberName; // Use correct case
                break;
            }
        }
        
        if (targetId == null) {
            player.sendMessage(ChatColor.RED + "Player '" + targetName + "' is not a recruiter in your team.");
            return;
        }
        
        // Demote recruiter to member
        if (team.demoteToMember(targetId)) {
            teamDataManager.saveTeams();
            
            player.sendMessage(ChatColor.GREEN + "You've demoted " + targetName + " to a regular team member.");
            
            // Notify demoted player if online
            Player targetPlayer = Bukkit.getPlayer(targetId);
            if (targetPlayer != null && targetPlayer.isOnline()) {
                targetPlayer.sendMessage(ChatColor.YELLOW + "You've been demoted to a regular member in team '" + team.getName() + "'.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Failed to demote " + targetName + ". They might not be a recruiter.");
        }
    }

    /**
     * Toggles team chat for a player
     */
    public void toggleTeamChat(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (teamChatToggled.contains(playerId)) {
            teamChatToggled.remove(playerId);
            player.sendMessage(ChatColor.RED + "Team chat mode " + ChatColor.BOLD + "disabled" + ChatColor.RED + ".");
        } else {
            Team team = teamDataManager.getPlayerTeam(playerId);
            if (team == null) {
                player.sendMessage(ChatColor.RED + "You need to be in a team to use team chat.");
                return;
            }
            
            teamChatToggled.add(playerId);
            player.sendMessage(ChatColor.GREEN + "Team chat mode " + ChatColor.BOLD + "enabled" + ChatColor.GREEN + ".");
        }
    }
    
    /**
     * Checks if a player has team chat enabled
     * @param playerId The UUID of the player to check
     * @return true if the player has team chat enabled, false otherwise
     */
    public boolean isInTeamChat(UUID playerId) {
        return teamChatToggled.contains(playerId);
    }

    /**
     * Handle setting team description
     */
    private void handleSetDescription(Player player, String[] args) {
        if (!player.hasPermission("teammanager.team.desc")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to set team description.");
            return;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /team desc <description>");
            return;
        }
        
        // Get player's team
        Team team = teamDataManager.getPlayerTeam(player.getUniqueId());
        
        if (team == null) {
            player.sendMessage(ChatColor.RED + "You're not in a team.");
            return;
        }
        
        // Check if player is the owner
        if (!team.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the team owner can set the team description.");
            return;
        }
        
        // Build the description from the arguments
        StringBuilder descBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) descBuilder.append(" ");
            descBuilder.append(args[i]);
        }
        String description = descBuilder.toString();
        
        // Check length
        if (description.length() > 100) {
            player.sendMessage(ChatColor.RED + "Description is too long. Maximum length is 100 characters.");
            return;
        }
        
        // Set the description
        team.setDescription(description);
        teamDataManager.saveTeams();
        
        player.sendMessage(ChatColor.GREEN + "Team description has been updated!");
        
        // Notify other online team members
        for (Player member : team.getOnlineMembers()) {
            if (!member.equals(player)) {
                member.sendMessage(ChatColor.YELLOW + "Your team's description has been updated to: " + 
                        ChatColor.WHITE + description);
            }
        }
    }
    
    /**
     * Handle showing team statistics
     */
    private void handleShowStats(Player player, String[] args) {
        if (!player.hasPermission("teammanager.team.stats")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to view team statistics.");
            return;
        }
        
        // Get the team
        Team team;
        
        if (args.length >= 2) {
            // Show stats for specific team
            team = teamDataManager.getTeam(args[1]);
            
            if (team == null) {
                player.sendMessage(ChatColor.RED + "Team '" + args[1] + "' doesn't exist.");
                return;
            }
        } else {
            // Show stats for player's team
            team = teamDataManager.getPlayerTeam(player.getUniqueId());
            
            if (team == null) {
                player.sendMessage(ChatColor.RED + "You're not in a team. Use '/team stats <name>' to view stats for other teams.");
                return;
            }
        }
        
        // Display team stats
        player.sendMessage(ChatColor.GREEN + "===== " + team.getColor() + team.getName() + ChatColor.GREEN + " Statistics =====");
        player.sendMessage(ChatColor.YELLOW + "Age: " + ChatColor.WHITE + team.getAgeInDays() + " days");
        player.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + team.getLevel());
        player.sendMessage(ChatColor.YELLOW + "Members: " + ChatColor.WHITE + team.getMemberCount() + "/" + team.getMaxMembers());
        player.sendMessage(ChatColor.YELLOW + "Kills: " + ChatColor.WHITE + team.getTotalKills());
        player.sendMessage(ChatColor.YELLOW + "Deaths: " + ChatColor.WHITE + team.getTotalDeaths());
        player.sendMessage(ChatColor.YELLOW + "K/D Ratio: " + ChatColor.WHITE + String.format("%.2f", team.getKDRatio()));
        
        // Calculate team power - this is just a sample formula, you can customize it
        int teamPower = (int)(team.getLevel() * 10 + team.getMemberCount() * 5 + team.getKDRatio() * 20);
        player.sendMessage(ChatColor.YELLOW + "Team Power: " + ChatColor.GOLD + teamPower);
    }
} 