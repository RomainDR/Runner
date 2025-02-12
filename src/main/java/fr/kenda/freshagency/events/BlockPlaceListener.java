package fr.kenda.freshagency.events;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.blocks.BlockSnap;
import fr.kenda.freshagency.managers.game.GameWorldManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    private final GameWorldManager gameWorldManager;

    public BlockPlaceListener() {
        gameWorldManager = FreshAgencyRunner.getInstance().getManagers().getManager(GameWorldManager.class);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        BlockSnap bs = new BlockSnap(e.getBlock());
        if (gameWorldManager.isBlockContainsInProtectedArea(bs)) {
            e.setCancelled(true);
            return;
        }
        bs.setType(Material.AIR); //set de l'air car bloc de base
        gameWorldManager.addBlockToRestore(bs);

    }
}
