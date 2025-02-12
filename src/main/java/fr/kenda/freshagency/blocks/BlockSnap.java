package fr.kenda.freshagency.blocks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Objects;
import java.util.UUID;

public class BlockSnap {
    private final UUID worldId;
    private final int x;
    private final short y;
    private final int z;
    Material type;

    public BlockSnap(Block b) {
        Location loc = b.getLocation();
        worldId = b.getWorld().getUID();
        x = (int) loc.getX();
        y = (short) loc.getY();
        z = (int) loc.getZ();
        type = b.getType();
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(worldId), x, y, z);
    }

    public Material getMaterial() {
        return type;
    }

    public UUID getWorldId() {
        return worldId;
    }

    public void setType(Material mat) {
        type = mat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockSnap blockSnap = (BlockSnap) o;
        return x == blockSnap.x && y == blockSnap.y && z == blockSnap.z && type == blockSnap.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, type);
    }

    public int getZ() {
        return z;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }
}