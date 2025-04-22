package com.darkangel.teammanager.gui;

import com.darkangel.teammanager.TeamManager;
import com.darkangel.teammanager.models.Team;
import com.darkangel.teammanager.utils.MaterialUtil;
import com.darkangel.teammanager.utils.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles GUI interactions for the team management system
 */
public class TeamGUIListener implements Listener {
    private final TeamManager plugin;
    private final TeamGUI gui;
    
    // Store players who are in the team creation process
    private final Map<UUID, String> teamCreators = new HashMap<>();
    
    public TeamGUIListener(TeamManager plugin, TeamGUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null || event.getCurrentItem() == null) {
            return;
        }
        
        String title = event.getView().getTitle();
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        
        // Prevent item moving in GUI
        if (title.contains("Team")) {
            event.setCancelled(true);
        }
        
        // Main GUI - No Team
        if (title.equals(ChatColor.DARK_AQUA + "Team Management")) {
            handleNoTeamMenu(player, clickedItem);
        }
        
        // Team Menu
        else if (title.contains("Team: ")) {
            handleTeamMenu(player, clickedItem);
        }
        
        // Team Invites
        else if (title.equals(ChatColor.GOLD + "Team Invites")) {
            handleInvitesMenu(player, clickedItem);
        }
        
        // Team List
        else if (title.equals(ChatColor.AQUA + "All Teams")) {
            handleTeamListMenu(player, clickedItem);
        }
        
        // Team Members
        else if (title.contains("Team Members: ")) {
            handleMembersMenu(player, clickedItem, event.getClick());
        }
        
        // Team Color Selection
        else if (title.equals(ChatColor.GREEN + "Team Color")) {
            handleColorMenu(player, clickedItem);
        }
    }
    
    /**
     * Handle clicks in the main menu for players without a team
     */
    private void handleNoTeamMenu(Player player, ItemStack clickedItem) {
        Material type = clickedItem.getType();
        
        if (type == Material.EMERALD_BLOCK) {
            // Create Team
            player.closeInventory();
            startTeamCreation(player);
        } 
        else if (materialMatches(type, "PAPER", "PAPER")) {
            // View Invites
            player.closeInventory();
            gui.openInvitesMenu(player);
        } 
        else if (materialMatches(type, "COMPASS", "COMPASS")) {
            // View All Teams
            player.closeInventory();
            gui.openTeamListMenu(player);
        }
    }
    
    /**
     * Handle clicks in the team menu
     */
    private void handleTeamMenu(Player player, ItemStack clickedItem) {
        Team team = plugin.getTeamDataManager().getPlayerTeam(player.getUniqueId());
        if (team == null) return;
        
        Material type = clickedItem.getType();
        
        if (isPlayerHead(type)) {
            // Team Members button
            player.closeInventory();
            gui.openMembersMenu(player, team);
        } 
        else if (isBedMaterial(type)) {
            // Team Home button
            player.closeInventory();
            if (team.getHomeLocation() != null) {
                player.teleport(team.getHomeLocation());
                player.sendMessage(ChatColor.GREEN + "Teleported to team home!");
            } else {
                player.sendMessage(ChatColor.RED + "Your team doesn't have a home set yet.");
            }
        } 
        else if (isConcreteMaterial(type)) {
            // Team Chat Toggle button
            player.closeInventory();
            plugin.getTeamCommandHandler().toggleTeamChat(player);
        } 
        else if (isDoorMaterial(type)) {
            // Leave/Disband Team button
            player.closeInventory();
            if (team.isOwner(player.getUniqueId())) {
                // Disband team (with confirmation)
                Bukkit.dispatchCommand(player, "team disband");
            } else {
                // Leave team
                Bukkit.dispatchCommand(player, "team leave");
            }
        } 
        else if (materialMatches(type, "COMPASS", "COMPASS")) {
            // Set Home button
            player.closeInventory();
            Bukkit.dispatchCommand(player, "team sethome");
        } 
        else if (isWeaponOrArmorMaterial(type)) {
            // Toggle PvP button
            player.closeInventory();
            if (team.isPvpEnabled()) {
                Bukkit.dispatchCommand(player, "team pvp off");
            } else {
                Bukkit.dispatchCommand(player, "team pvp on");
            }
        } 
        else if (isDyeMaterial(type)) {
            // Set Team Color button
            player.closeInventory();
            gui.openColorMenu(player, team);
        } 
        else if (isBookMaterial(type)) {
            // Invite Players button
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Type the name of the player you want to invite:");
            player.sendMessage(ChatColor.GRAY + "To cancel, type 'cancel'");
        } 
        else if (materialMatches(type, "BARRIER", "BARRIER") || type == Material.REDSTONE_BLOCK) {
            // Manage Members button
            player.closeInventory();
            gui.openMembersMenu(player, team);
        }
    }
    
    /**
     * Handle clicks in the team invites menu
     */
    private void handleInvitesMenu(Player player, ItemStack clickedItem) {
        if (materialMatches(clickedItem.getType(), "PAPER", "PAPER") && clickedItem.hasItemMeta()) {
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                if (displayName.contains("Join Team: ")) {
                    String teamName = ChatColor.stripColor(displayName.substring(displayName.indexOf(": ") + 2));
                    
                    player.closeInventory();
                    Bukkit.dispatchCommand(player, "team join " + teamName);
                }
            }
        }
    }
    
    /**
     * Handle clicks in the team list menu
     */
    private void handleTeamListMenu(Player player, ItemStack clickedItem) {
        if ((materialMatches(clickedItem.getType(), "NAME_TAG", "NAME_TAG") || clickedItem.getType() == Material.PAPER) 
                && clickedItem.hasItemMeta()) {
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                if (displayName.contains("Team: ")) {
                    String teamName = ChatColor.stripColor(displayName.substring(displayName.indexOf(": ") + 2));
                    
                    player.closeInventory();
                    Bukkit.dispatchCommand(player, "team info " + teamName);
                }
            }
        }
    }
    
    /**
     * Handle clicks in the team members menu
     */
    private void handleMembersMenu(Player player, ItemStack clickedItem, ClickType clickType) {
        Team team = plugin.getTeamDataManager().getPlayerTeam(player.getUniqueId());
        if (team == null) return;
        
        if (isPlayerHead(clickedItem.getType()) && clickedItem.hasItemMeta()) {
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = ChatColor.stripColor(meta.getDisplayName());
                String playerName;
                
                if (displayName.startsWith("Owner: ")) {
                    playerName = displayName.substring(7);
                } else if (displayName.startsWith("Recruiter: ")) {
                    playerName = displayName.substring(11);
                    
                    if (team.isOwner(player.getUniqueId())) {
                        player.closeInventory();
                        
                        if (clickType == ClickType.LEFT) {
                            // Demote recruiter to member
                            Bukkit.dispatchCommand(player, "team demote " + playerName);
                        } else if (clickType == ClickType.RIGHT) {
                            // Kick recruiter
                            Bukkit.dispatchCommand(player, "team kick " + playerName);
                        }
                    }
                } else if (displayName.startsWith("Member: ")) {
                    playerName = displayName.substring(8);
                    
                    if (team.isOwner(player.getUniqueId())) {
                        player.closeInventory();
                        
                        if (clickType == ClickType.LEFT) {
                            // Promote member to recruiter
                            Bukkit.dispatchCommand(player, "team promote " + playerName);
                        } else if (clickType == ClickType.RIGHT) {
                            // Kick member
                            Bukkit.dispatchCommand(player, "team kick " + playerName);
                        }
                    } else if (team.isRecruit(player.getUniqueId()) && clickType == ClickType.RIGHT) {
                        player.closeInventory();
                        // Recruiters can kick regular members
                        Bukkit.dispatchCommand(player, "team kick " + playerName);
                    }
                }
            }
        }
    }
    
    /**
     * Handle clicks in the team color menu
     */
    private void handleColorMenu(Player player, ItemStack clickedItem) {
        Team team = plugin.getTeamDataManager().getPlayerTeam(player.getUniqueId());
        if (team == null || !team.isOwner(player.getUniqueId())) return;
        
        if (clickedItem.hasItemMeta()) {
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.hasLore()) {
                String lore = meta.getLore().get(0);
                if (lore.contains("Click to set your team's color to ")) {
                    String colorName = ChatColor.stripColor(lore.substring(lore.lastIndexOf("to ") + 3));
                    
                    player.closeInventory();
                    Bukkit.dispatchCommand(player, "team color " + colorName.toUpperCase().replace(" ", "_"));
                }
            }
        }
    }
    
    /**
     * Checks if material is a player head in any version
     */
    private boolean isPlayerHead(Material material) {
        if (VersionUtil.isPost113()) {
            return material == Material.PLAYER_HEAD;
        } else {
            return material.name().equals("SKULL_ITEM") || material.name().equals("PLAYER_HEAD");
        }
    }
    
    /**
     * Checks if material is a bed in any version
     */
    private boolean isBedMaterial(Material material) {
        if (VersionUtil.isPost113()) {
            return material.name().endsWith("_BED");
        } else {
            return material.name().equals("BED") || material.name().endsWith("_BED");
        }
    }
    
    /**
     * Checks if material is concrete in any version
     */
    private boolean isConcreteMaterial(Material material) {
        if (VersionUtil.isPost113()) {
            return material.name().endsWith("_CONCRETE");
        } else {
            return material.name().equals("CONCRETE");
        }
    }
    
    /**
     * Checks if material is a door in any version
     */
    private boolean isDoorMaterial(Material material) {
        return materialMatches(material, "OAK_DOOR", "WOOD_DOOR") || 
               material.name().endsWith("_DOOR");
    }
    
    /**
     * Checks if material is a weapon or armor in any version
     */
    private boolean isWeaponOrArmorMaterial(Material material) {
        String name = material.name();
        return name.contains("SWORD") || name.contains("SHIELD") || 
               name.contains("CHESTPLATE") || name.contains("HELMET");
    }
    
    /**
     * Checks if material is a dye in any version
     */
    private boolean isDyeMaterial(Material material) {
        if (VersionUtil.isPost113()) {
            return material.name().endsWith("_DYE");
        } else {
            return material.name().equals("INK_SACK") || material.name().endsWith("_DYE");
        }
    }
    
    /**
     * Checks if material is a book in any version
     */
    private boolean isBookMaterial(Material material) {
        return materialMatches(material, "WRITABLE_BOOK", "BOOK_AND_QUILL") || 
               material.name().contains("BOOK");
    }
    
    /**
     * Checks if a material matches the modern or legacy name
     */
    private boolean materialMatches(Material material, String modernName, String legacyName) {
        String materialName = material.name();
        if (VersionUtil.isPost113()) {
            return materialName.equals(modernName);
        } else {
            return materialName.equals(legacyName);
        }
    }
    
    /**
     * Start the team creation process
     */
    private void startTeamCreation(Player player) {
        teamCreators.put(player.getUniqueId(), null);
        player.sendMessage(ChatColor.GREEN + "Please enter a name for your new team:");
        player.sendMessage(ChatColor.GRAY + "To cancel, type 'cancel'");
    }
    
    /**
     * Check if a player is in the team creation process
     */
    public boolean isCreatingTeam(UUID playerId) {
        return teamCreators.containsKey(playerId);
    }
    
    /**
     * Handle team creation chat input
     */
    public void handleTeamCreation(Player player, String message) {
        if (message.equalsIgnoreCase("cancel")) {
            teamCreators.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "Team creation cancelled.");
            return;
        }
        
        // Create the team
        Bukkit.dispatchCommand(player, "team create " + message);
        teamCreators.remove(player.getUniqueId());
    }
} 