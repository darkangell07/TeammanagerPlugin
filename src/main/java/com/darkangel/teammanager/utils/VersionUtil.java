package com.darkangel.teammanager.utils;

import org.bukkit.Bukkit;

/**
 * Utility class for handling Minecraft version differences
 */
public class VersionUtil {
    private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static final int MAJOR_VERSION = Integer.parseInt(VERSION.split("_")[1]);
    private static final int MINOR_VERSION = Integer.parseInt(VERSION.split("_")[2].replaceAll("[^0-9]", ""));
    
    /**
     * Gets the server's major version (e.g., 8, 12, 16, etc.)
     * @return the major version number
     */
    public static int getMajorVersion() {
        return MAJOR_VERSION;
    }
    
    /**
     * Gets the server's minor version
     * @return the minor version number
     */
    public static int getMinorVersion() {
        return MINOR_VERSION;
    }
    
    /**
     * Checks if the server version is at least the specified version
     * @param major major version to check against
     * @param minor minor version to check against
     * @return true if the server is running at least the specified version
     */
    public static boolean isVersionAtLeast(int major, int minor) {
        return MAJOR_VERSION > major || (MAJOR_VERSION == major && MINOR_VERSION >= minor);
    }
    
    /**
     * Checks if the server is running 1.13 or higher (the "flattening" update)
     * @return true if server is 1.13+
     */
    public static boolean isPost113() {
        return isVersionAtLeast(13, 0);
    }
    
    /**
     * Checks if the server is running 1.16 or higher
     * @return true if server is 1.16+
     */
    public static boolean isPost116() {
        return isVersionAtLeast(16, 0);
    }
    
    /**
     * Checks if the server is running 1.19 or higher
     * @return true if server is 1.19+
     */
    public static boolean isPost119() {
        return isVersionAtLeast(19, 0);
    }
    
    /**
     * Gets the full version string (e.g., v1_16_R3)
     * @return the version string
     */
    public static String getVersionString() {
        return VERSION;
    }
} 