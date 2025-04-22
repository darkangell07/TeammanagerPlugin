package com.darkangel.teammanager.gui;

import com.darkangel.teammanager.TeamManager;
import com.darkangel.teammanager.models.Team;
import com.darkangel.teammanager.utils.MaterialUtil;
import com.darkangel.teammanager.utils.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * GUI for team management
 */
public class TeamGUI {
    private final TeamManager plugin;
    
    public TeamGUI(TeamManager plugin) {
        this.plugin = plugin;
        Bukkit.getLogger().info("[TeamManager] GUI initialized with Minecraft version: " + 
                Bukkit.getBukkitVersion() + " (Post-1.13: " + VersionUtil.isPost113() + ")");
    }
    
    /**
     * Open the team management menu for a player
     */
    public void openTeamMenu(Player player) {
        Team team = plugin.getTeamDataManager().getPlayerTeam(player.getUniqueId());
        
        if (team == null) {
            // Player doesn't have a team
            openNoTeamMenu(player);
        } else {
            // Player has a team
            openTeamMenuWithTeam(player, team);
        }
    }
    
    /**
     * Alias for openTeamMenu - used by command handler
     */
    public void openMainMenu(Player player) {
        openTeamMenu(player);
    }
    
    /**
     * Open menu for players without a team
     */
    private void openNoTeamMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.DARK_AQUA + "Team Management");
        
        // Create team option
        inventory.setItem(11, createItem(
                Material.EMERALD_BLOCK, 
                ChatColor.GREEN + "Create a Team", 
                Arrays.asList(
                    ChatColor.GRAY + "Click to create your own team",
                    ChatColor.GRAY + "and become a team owner!"
                )
        ));
        
        // View invites option
        inventory.setItem(13, createItem(
                MaterialUtil.getMaterial("PAPER", "PAPER"), 
                ChatColor.GOLD + "View Team Invites", 
                Arrays.asList(
                    ChatColor.GRAY + "Click to view your pending",
                    ChatColor.GRAY + "team invitations"
                )
        ));
        
        // View all teams option
        inventory.setItem(15, createItem(
                MaterialUtil.getMaterial("COMPASS", "COMPASS"), 
                ChatColor.AQUA + "Browse Teams", 
                Arrays.asList(
                    ChatColor.GRAY + "Click to browse all teams",
                    ChatColor.GRAY + "on the server"
                )
        ));
        
        player.openInventory(inventory);
    }
    
    /**
     * Open menu for players with a team
     */
    private void openTeamMenuWithTeam(Player player, Team team) {
        Inventory inventory = Bukkit.createInventory(null, 36, ChatColor.WHITE + "Team: " + team.getName());
        
        // Team info item
        inventory.setItem(4, createPlayerHead(
                player.getName(),
                ChatColor.AQUA + "Team: " + team.getColor() + team.getName(),
                Arrays.asList(
                    ChatColor.GRAY + "Level: " + ChatColor.YELLOW + team.getLevel(),
                    ChatColor.GRAY + "Members: " + ChatColor.YELLOW + team.getMemberCount() + "/" + team.getMaxMembers(),
                    ChatColor.GRAY + "Owner: " + ChatColor.YELLOW + Bukkit.getOfflinePlayer(team.getOwner()).getName(),
                    "",
                    ChatColor.GRAY + "PvP: " + (team.isPvpEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")
                )
        ));
        
        // Team Members
        inventory.setItem(10, createPlayerHead(
                team.getOwner().toString(),
                ChatColor.GOLD + "Team Members",
                Arrays.asList(
                    ChatColor.GRAY + "Click to view and manage",
                    ChatColor.GRAY + "your team members"
                )
        ));
        
        // Team Home
        Material bedMaterial = MaterialUtil.getMaterial("WHITE_BED", "BED");
        ItemStack homeItem = createItem(
                bedMaterial,
                ChatColor.GREEN + "Team Home",
                Arrays.asList(
                    team.getHomeLocation() != null 
                        ? ChatColor.GRAY + "Click to teleport to your team home"
                        : ChatColor.RED + "No team home set yet"
                )
        );
        inventory.setItem(12, homeItem);
        
        // Team Chat
        boolean inTeamChat = plugin.getTeamCommandHandler().isInTeamChat(player.getUniqueId());
        Material concreteType = inTeamChat 
                ? MaterialUtil.getMaterial("LIME_CONCRETE", "CONCRETE") 
                : MaterialUtil.getMaterial("RED_CONCRETE", "CONCRETE");
        inventory.setItem(14, createItem(
                concreteType,
                inTeamChat ? ChatColor.GREEN + "Team Chat: ON" : ChatColor.RED + "Team Chat: OFF",
                Arrays.asList(
                    ChatColor.GRAY + "Click to " + (inTeamChat ? "disable" : "enable"),
                    ChatColor.GRAY + "team chat mode"
                )
        ));
        
        // Set Home button (owner only)
        if (team.isOwner(player.getUniqueId())) {
            inventory.setItem(16, createItem(
                    MaterialUtil.getMaterial("COMPASS", "COMPASS"),
                    ChatColor.YELLOW + "Set Team Home",
                    Arrays.asList(
                        ChatColor.GRAY + "Click to set the team home",
                        ChatColor.GRAY + "at your current location"
                    )
            ));
        }
        
        // PvP toggle button (owner only)
        if (team.isOwner(player.getUniqueId())) {
            Material pvpMaterial = team.isPvpEnabled() 
                    ? Material.IRON_SWORD 
                    : Material.SHIELD;
            inventory.setItem(20, createItem(
                    pvpMaterial,
                    team.isPvpEnabled() ? ChatColor.RED + "Disable Team PvP" : ChatColor.GREEN + "Enable Team PvP",
                    Arrays.asList(
                        ChatColor.GRAY + "Click to " + (team.isPvpEnabled() ? "disable" : "enable"),
                        ChatColor.GRAY + "PvP between team members"
                    )
            ));
        }
        
        // Team Color (owner only)
        if (team.isOwner(player.getUniqueId())) {
            inventory.setItem(22, createItem(
                    MaterialUtil.getMaterial("LIME_DYE", "INK_SACK"),
                    ChatColor.GOLD + "Team Color",
                    Arrays.asList(
                        ChatColor.GRAY + "Click to change your",
                        ChatColor.GRAY + "team's color"
                    )
            ));
        }
        
        // Invite button (owner and recruiters)
        if (team.isOwner(player.getUniqueId()) || team.isRecruit(player.getUniqueId())) {
            inventory.setItem(24, createItem(
                    MaterialUtil.getMaterial("WRITABLE_BOOK", "BOOK_AND_QUILL"),
                    ChatColor.AQUA + "Invite Player",
                    Arrays.asList(
                        ChatColor.GRAY + "Click to invite a player",
                        ChatColor.GRAY + "to join your team"
                    )
            ));
        }
        
        // Leave/Disband button
        Material doorMaterial = MaterialUtil.getMaterial("OAK_DOOR", "WOOD_DOOR");
        String doorText = team.isOwner(player.getUniqueId()) ? "Disband Team" : "Leave Team";
        
        inventory.setItem(30, createItem(
                doorMaterial,
                ChatColor.RED + doorText,
                Arrays.asList(
                    ChatColor.GRAY + "Click to " + (team.isOwner(player.getUniqueId()) ? "disband" : "leave"),
                    ChatColor.GRAY + "your current team" + (team.isOwner(player.getUniqueId()) ? " (This cannot be undone!)" : "")
                )
        ));
        
        // Manage Members (owner only)
        if (team.isOwner(player.getUniqueId())) {
            inventory.setItem(32, createItem(
                    MaterialUtil.getMaterial("BARRIER", "BARRIER"),
                    ChatColor.GOLD + "Manage Members",
                    Arrays.asList(
                        ChatColor.GRAY + "Click to promote, demote",
                        ChatColor.GRAY + "or kick team members"
                    )
            ));
        }
        
        player.openInventory(inventory);
    }
    
    /**
     * Open the team invites menu
     */
    public void openInvitesMenu(Player player) {
        Set<String> invites = plugin.getTeamDataManager().getPlayerInviteNames(player.getUniqueId());
        
        int size = Math.min(54, ((invites.size() / 9) + 1) * 9);
        Inventory inventory = Bukkit.createInventory(null, size, ChatColor.GOLD + "Team Invites");
        
        if (invites.isEmpty()) {
            inventory.setItem(4, createItem(
                    Material.BARRIER,
                    ChatColor.RED + "No Invites",
                    Collections.singletonList(ChatColor.GRAY + "You have no pending team invites")
            ));
        } else {
            int slot = 0;
            for (String teamName : invites) {
                Team team = plugin.getTeamDataManager().getTeam(teamName);
                if (team != null) {
                    inventory.setItem(slot, createItem(
                            MaterialUtil.getMaterial("PAPER", "PAPER"),
                            ChatColor.GREEN + "Join Team: " + team.getColor() + team.getName(),
                            Arrays.asList(
                                ChatColor.GRAY + "Owner: " + ChatColor.YELLOW + Bukkit.getOfflinePlayer(team.getOwner()).getName(),
                                ChatColor.GRAY + "Members: " + ChatColor.YELLOW + team.getMemberCount() + "/" + team.getMaxMembers(),
                                "",
                                ChatColor.GRAY + "Click to join this team"
                            )
                    ));
                    slot++;
                }
            }
        }
        
        player.openInventory(inventory);
    }
    
    /**
     * Open the team list menu
     */
    public void openTeamListMenu(Player player) {
        Collection<Team> teams = plugin.getTeamDataManager().getAllTeams();
        
        int size = Math.min(54, ((teams.size() / 9) + 1) * 9);
        Inventory inventory = Bukkit.createInventory(null, size, ChatColor.AQUA + "All Teams");
        
        if (teams.isEmpty()) {
            inventory.setItem(4, createItem(
                    Material.BARRIER,
                    ChatColor.RED + "No Teams",
                    Collections.singletonList(ChatColor.GRAY + "There are no teams on the server yet")
            ));
        } else {
            int slot = 0;
            for (Team team : teams) {
                inventory.setItem(slot, createItem(
                        MaterialUtil.getMaterial("NAME_TAG", "NAME_TAG"),
                        ChatColor.GREEN + "Team: " + team.getColor() + team.getName(),
                        Arrays.asList(
                            ChatColor.GRAY + "Owner: " + ChatColor.YELLOW + Bukkit.getOfflinePlayer(team.getOwner()).getName(),
                            ChatColor.GRAY + "Members: " + ChatColor.YELLOW + team.getMemberCount() + "/" + team.getMaxMembers(),
                            ChatColor.GRAY + "Level: " + ChatColor.YELLOW + team.getLevel(),
                            "",
                            ChatColor.GRAY + "Click for more information"
                        )
                ));
                slot++;
            }
        }
        
        player.openInventory(inventory);
    }
    
    /**
     * Open the team members menu
     */
    public void openMembersMenu(Player player, Team team) {
        int size = 36;
        Inventory inventory = Bukkit.createInventory(null, size, ChatColor.GOLD + "Team Members: " + team.getName());
        
        // Owner item
        UUID ownerUUID = team.getOwner();
        inventory.setItem(4, createPlayerHead(
                ownerUUID.toString(),
                ChatColor.GOLD + "Owner: " + Bukkit.getOfflinePlayer(ownerUUID).getName(),
                Collections.singletonList(ChatColor.GRAY + "The team owner has full control over the team")
        ));
        
        // Recruiters
        int slot = 9;
        for (UUID recruiterUUID : team.getRecruiters()) {
            if (!recruiterUUID.equals(ownerUUID)) {
                String name = Bukkit.getOfflinePlayer(recruiterUUID).getName();
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Recruiters can invite new members");
                
                if (team.isOwner(player.getUniqueId())) {
                    lore.add("");
                    lore.add(ChatColor.YELLOW + "Left-click to demote to member");
                    lore.add(ChatColor.RED + "Right-click to kick from team");
                }
                
                inventory.setItem(slot, createPlayerHead(
                        recruiterUUID.toString(),
                        ChatColor.AQUA + "Recruiter: " + name,
                        lore
                ));
                slot++;
            }
        }
        
        // Regular members
        for (UUID memberUUID : team.getMembers()) {
            if (!team.isOwner(memberUUID) && !team.isRecruit(memberUUID)) {
                String name = Bukkit.getOfflinePlayer(memberUUID).getName();
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Regular team member");
                
                if (team.isOwner(player.getUniqueId())) {
                    lore.add("");
                    lore.add(ChatColor.GREEN + "Left-click to promote to recruiter");
                    lore.add(ChatColor.RED + "Right-click to kick from team");
                } else if (team.isRecruit(player.getUniqueId())) {
                    lore.add("");
                    lore.add(ChatColor.RED + "Right-click to kick from team");
                }
                
                inventory.setItem(slot, createPlayerHead(
                        memberUUID.toString(),
                        ChatColor.WHITE + "Member: " + name,
                        lore
                ));
                slot++;
            }
        }
        
        player.openInventory(inventory);
    }
    
    /**
     * Open the team color selection menu
     */
    public void openColorMenu(Player player, Team team) {
        if (!team.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the team owner can change the team color.");
            return;
        }
        
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.GREEN + "Team Color");
        
        addColorOption(inventory, 10, "WHITE_WOOL", "WOOL", ChatColor.WHITE, "White");
        addColorOption(inventory, 11, "ORANGE_WOOL", "WOOL", ChatColor.GOLD, "Gold");
        addColorOption(inventory, 12, "MAGENTA_WOOL", "WOOL", ChatColor.LIGHT_PURPLE, "Light Purple");
        addColorOption(inventory, 13, "LIGHT_BLUE_WOOL", "WOOL", ChatColor.AQUA, "Aqua");
        addColorOption(inventory, 14, "YELLOW_WOOL", "WOOL", ChatColor.YELLOW, "Yellow");
        addColorOption(inventory, 15, "LIME_WOOL", "WOOL", ChatColor.GREEN, "Green");
        addColorOption(inventory, 16, "PINK_WOOL", "WOOL", ChatColor.LIGHT_PURPLE, "Pink");
        
        addColorOption(inventory, 19, "GRAY_WOOL", "WOOL", ChatColor.DARK_GRAY, "Dark Gray");
        addColorOption(inventory, 20, "LIGHT_GRAY_WOOL", "WOOL", ChatColor.GRAY, "Gray");
        addColorOption(inventory, 21, "CYAN_WOOL", "WOOL", ChatColor.DARK_AQUA, "Dark Aqua");
        addColorOption(inventory, 22, "PURPLE_WOOL", "WOOL", ChatColor.DARK_PURPLE, "Dark Purple");
        addColorOption(inventory, 23, "BLUE_WOOL", "WOOL", ChatColor.BLUE, "Blue");
        addColorOption(inventory, 24, "BROWN_WOOL", "WOOL", ChatColor.GOLD, "Brown");
        addColorOption(inventory, 25, "RED_WOOL", "WOOL", ChatColor.RED, "Red");
        
        player.openInventory(inventory);
    }
    
    /**
     * Add a color option to the color selection menu
     */
    private void addColorOption(Inventory inventory, int slot, String modernMaterial, String legacyMaterial, 
                              ChatColor chatColor, String colorName) {
        Material material = MaterialUtil.getMaterial(modernMaterial, legacyMaterial);
        short durability = 0;
        
        // Handle data values for legacy versions
        if (!VersionUtil.isPost113() && legacyMaterial.equals("WOOL")) {
            durability = getWoolData(colorName);
        }
        
        ItemStack item = createItemWithDurability(
                material, 
                durability,
                chatColor + colorName,
                Collections.singletonList(ChatColor.GRAY + "Click to set your team's color to " + chatColor + colorName)
        );
        
        inventory.setItem(slot, item);
    }
    
    /**
     * Get the data value for wool in legacy versions
     */
    private short getWoolData(String color) {
        switch (color.toUpperCase()) {
            case "WHITE": return 0;
            case "ORANGE": return 1;
            case "MAGENTA": return 2;
            case "LIGHT BLUE": return 3;
            case "YELLOW": return 4;
            case "LIME": case "GREEN": return 5;
            case "PINK": return 6;
            case "GRAY": case "DARK GRAY": return 7;
            case "LIGHT GRAY": return 8;
            case "CYAN": case "DARK AQUA": return 9;
            case "PURPLE": case "DARK PURPLE": return 10;
            case "BLUE": return 11;
            case "BROWN": return 12;
            case "RED": return 14;
            case "BLACK": return 15;
            default: return 0;
        }
    }
    
    /**
     * Create an item for a GUI menu
     */
    @SuppressWarnings("deprecation")
    private ItemStack createItemWithDurability(Material material, short durability, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        
        // Set durability for legacy servers
        if (!VersionUtil.isPost113()) {
            try {
                item.setDurability(durability);
            } catch (Exception e) {
                Bukkit.getLogger().warning("[TeamManager] Error setting item durability: " + e.getMessage());
            }
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            
            // Hide attributes like durability
            try {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
            } catch (Exception e) {
                // Ignore if these flags aren't available in this version
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Create an item for a GUI menu
     */
    private ItemStack createItem(Material material, String name, List<String> lore) {
        return createItemWithDurability(material, (short) 0, name, lore);
    }
    
    /**
     * Create a player head item with the given player's skin
     */
    @SuppressWarnings("deprecation")
    private ItemStack createPlayerHead(String playerName, String name, List<String> lore) {
        ItemStack head;
        
        if (VersionUtil.isPost113()) {
            head = new ItemStack(Material.PLAYER_HEAD, 1);
        } else {
            // For pre-1.13
            head = new ItemStack(Material.getMaterial("SKULL_ITEM"), 1, (short) 3);
        }
        
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            // Try to set the skull owner
            try {
                if (playerName != null && !playerName.isEmpty()) {
                    if (playerName.length() > 16) {
                        // This is a UUID string, try to get the player name
                        try {
                            UUID uuid = UUID.fromString(playerName);
                            String actualName = Bukkit.getOfflinePlayer(uuid).getName();
                            if (actualName != null) {
                                meta.setOwner(actualName);
                            }
                        } catch (IllegalArgumentException e) {
                            // Not a valid UUID, just use as is
                            meta.setOwner(playerName);
                        }
                    } else {
                        meta.setOwner(playerName);
                    }
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("[TeamManager] Error setting skull owner: " + e.getMessage());
            }
            
            meta.setDisplayName(name);
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        
        return head;
    }
}