package fr.kenda.freshagency.events;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.blocks.BlockSnap;
import fr.kenda.freshagency.managers.game.GameWorldManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class BlockFallingListener implements Listener {

    private final GameWorldManager gwm;

    public BlockFallingListener() {
        gwm = FreshAgencyRunner.getInstance().getManagers().getManager(GameWorldManager.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFallBlock(final EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof FallingBlock fallingBlock)) {
            return;
        }

        if (event.getTo() == fallingBlock.getBlockData().getMaterial()) {
            Location loc = event.getEntity().getLocation();
            Block blockAtLocation = loc.getBlock();
            BlockSnap blockSnap = new BlockSnap(blockAtLocation);
            gwm.addBlockToRestore(blockSnap);
        }
    }
}
