package fr.kenda.freshagency.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class LocationTransform {

    /**
     * Deserializes a string to create a Location
     *
     * @param world              String
     * @param serializedLocation String
     * @return Location
     */
    public static Location deserializeCoordinate(String world, String serializedLocation) {
        return deserializeCoordinate(Bukkit.getWorld(world), serializedLocation);
    }

    /**
     * Deserializes a string to create a Location in the player's world
     *
     * @param player             Player
     * @param serializedLocation String
     * @return Location
     */
    public static Location deserializeCoordinate(Player player, String serializedLocation) {
        return deserializeCoordinate(player.getWorld(), serializedLocation);
    }

    /**
     * Deserializes a string to create a Location
     *
     * @param world              World
     * @param serializedLocation String
     * @return Location
     */
    private static Location deserializeCoordinate(World world, String serializedLocation) {
        String[] parts = serializedLocation.split(";");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].replace(',', '.'); // Replace ',' with '.'
        }

        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        double z = Double.parseDouble(parts[2]);

        return new Location(world, x, y, z);
    }
}
