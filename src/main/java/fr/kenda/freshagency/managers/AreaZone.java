package fr.kenda.freshagency.managers;

import org.bukkit.Location;

public class AreaZone {

    private final int x1;
    private final int y1;
    private final int z1;
    private final int x2;
    private final int y2;
    private final int z2;

    public AreaZone(Location loc1, Location loc2) {
        this.x1 = Math.min(loc1.getBlockX(), loc2.getBlockX());
        this.y1 = Math.min(loc1.getBlockY(), loc2.getBlockY());
        this.z1 = Math.min(loc1.getBlockZ(), loc2.getBlockZ());

        this.x2 = Math.max(loc1.getBlockX(), loc2.getBlockX());
        this.y2 = Math.max(loc1.getBlockY(), loc2.getBlockY());
        this.z2 = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
    }

    public boolean isContainsInArea(Location loc) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return (x >= x1 && x <= x2) &&
                (y >= y1 && y <= y2) &&
                (z >= z1 && z <= z2);
    }
}

