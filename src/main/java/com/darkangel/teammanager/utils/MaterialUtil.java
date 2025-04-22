package com.darkangel.teammanager.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Utility class for handling material name differences across Minecraft versions
 * Created by DarkAngel - 2023
 */
public class MaterialUtil {
    private static final Random RANDOM = new Random();
    private static final List<ChatColor> RAINBOW_COLORS = Arrays.asList(
            ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, 
            ChatColor.GREEN, ChatColor.AQUA, ChatColor.BLUE, 
            ChatColor.LIGHT_PURPLE
    );
    
    /**
     * Gets the appropriate bed material based on the server version
     * @param color desired bed color (for 1.13+)
     * @return the appropriate Material
     */
    public static Material getBedMaterial(String color) {
        // Before 1.13, there was only one BED material
        if (!VersionUtil.isPost113()) {
            try {
                return Material.valueOf("BED");
            } catch (IllegalArgumentException e) {
                // Fallback to modern materials if the old one doesn't exist
                return Material.WHITE_BED;
            }
        }
        
        // 1.13+ has colored beds
        try {
            return Material.valueOf(color + "_BED");
        } catch (IllegalArgumentException e) {
            return Material.WHITE_BED;
        }
    }
    
    /**
     * Gets the default bed material based on server version
     * @return the appropriate bed Material
     */
    public static Material getBedMaterial() {
        return getBedMaterial("WHITE");
    }
    
    /**
     * Gets the appropriate concrete material based on server version
     * @param color desired concrete color
     * @return the appropriate Material
     */
    public static Material getConcreteMaterial(String color) {
        if (!VersionUtil.isPost113()) {
            try {
                // In pre-1.13, concrete used a data value, but we'll use the base material
                return Material.valueOf("CONCRETE");
            } catch (IllegalArgumentException e) {
                // Fallback to modern materials
                return Material.WHITE_CONCRETE;
            }
        }
        
        try {
            return Material.valueOf(color + "_CONCRETE");
        } catch (IllegalArgumentException e) {
            return Material.WHITE_CONCRETE;
        }
    }
    
    /**
     * Gets the appropriate dye material based on server version
     * @param color desired dye color
     * @return the appropriate Material
     */
    public static Material getDyeMaterial(String color) {
        if (!VersionUtil.isPost113()) {
            try {
                // In pre-1.13, dye used a data value, but we'll use the base material
                return Material.valueOf("INK_SACK");
            } catch (IllegalArgumentException e) {
                return Material.LIME_DYE;
            }
        }
        
        try {
            return Material.valueOf(color + "_DYE");
        } catch (IllegalArgumentException e) {
            return Material.LIME_DYE;
        }
    }
    
    /**
     * Gets a safe material that exists in all supported versions
     * @param modernName modern (1.13+) material name
     * @param legacyName legacy (pre-1.13) material name
     * @param fallback guaranteed fallback material (e.g., STONE)
     * @return the appropriate Material
     */
    public static Material getMaterial(String modernName, String legacyName, Material fallback) {
        try {
            if (VersionUtil.isPost113()) {
                return Material.valueOf(modernName);
            } else {
                return Material.valueOf(legacyName);
            }
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
    
    /**
     * Gets a safe material that exists in all supported versions.
     * Uses STONE as a fallback if the material cannot be found.
     * @param modernName modern (1.13+) material name
     * @param legacyName legacy (pre-1.13) material name
     * @return the appropriate Material
     */
    public static Material getMaterial(String modernName, String legacyName) {
        return getMaterial(modernName, legacyName, Material.STONE);
    }
    
    /**
     * Creates a cool-looking item with custom name and lore
     * @param material the material to use
     * @param name the display name
     * @param lore the item lore
     * @return the created ItemStack
     */
    public static ItemStack createCustomItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.RESET + "" + ChatColor.BOLD + name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Creates a cool-looking item with rainbow text for the display name
     * @param material the material to use
     * @param name the display name (will be rainbow colored)
     * @param lore the item lore
     * @return the created ItemStack
     */
    public static ItemStack createRainbowItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Create rainbow text for the name
            StringBuilder rainbow = new StringBuilder();
            for (int i = 0; i < name.length(); i++) {
                ChatColor color = RAINBOW_COLORS.get(i % RAINBOW_COLORS.size());
                rainbow.append(color).append(name.charAt(i));
            }
            
            meta.setDisplayName(rainbow.toString());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Gets a random decorative material appropriate for the current version
     * (For GUI decorations)
     * @return a random decorative material
     */
    public static Material getRandomDecoration() {
        List<Material> decorations = new ArrayList<>();
        
        if (VersionUtil.isPost116()) {
            // 1.16+ decorations
            decorations.add(Material.SHROOMLIGHT);
            decorations.add(Material.CRIMSON_FUNGUS);
            decorations.add(Material.WARPED_FUNGUS);
            decorations.add(Material.CRYING_OBSIDIAN);
            decorations.add(Material.SOUL_LANTERN);
        } else if (VersionUtil.isPost113()) {
            // 1.13+ decorations
            decorations.add(Material.SEA_LANTERN);
            decorations.add(Material.END_ROD);
            decorations.add(Material.GLOWSTONE);
            decorations.add(Material.LANTERN);
            decorations.add(Material.BELL);
        } else {
            // Pre 1.13 decorations
            try {
                decorations.add(Material.valueOf("SEA_LANTERN"));
                decorations.add(Material.valueOf("END_ROD"));
                decorations.add(Material.valueOf("GLOWSTONE"));
                decorations.add(Material.valueOf("REDSTONE_LAMP_ON"));
            } catch (IllegalArgumentException e) {
                // Fallback
                return Material.GLASS;
            }
        }
        
        return decorations.get(RANDOM.nextInt(decorations.size()));
    }
    
    /**
     * My favorite wool color for each version!
     * Completely unnecessary but adds a personal touch.
     * @return my favorite wool type for the current version
     */
    public static Material getMyFavoriteWool() {
        if (VersionUtil.isPost113()) {
            return Material.MAGENTA_WOOL;
        } else {
            try {
                // Pre 1.13 used a single WOOL material with data values
                return Material.valueOf("WOOL");
            } catch (IllegalArgumentException e) {
                return Material.WHITE_WOOL;
            }
        }
    }
} 