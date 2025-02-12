package fr.kenda.freshagency.events;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.blocks.BlockSnap;
import fr.kenda.freshagency.managers.game.GameWorldManager;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class PlayerBreakBlock implements Listener {

    private final GameWorldManager gameWorldManager;

    public PlayerBreakBlock() {
        gameWorldManager = FreshAgencyRunner.getInstance().getManagers().getManager(GameWorldManager.class);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();

        BlockSnap snap = new BlockSnap(block);
        if (gameWorldManager.isBlockContainsInProtectedArea(snap)) {
            e.setCancelled(true);
            return;
        }
        if (gameWorldManager.isBlockContainsInOriginalMap(snap)) {
            gameWorldManager.addBlockToRestore(snap);
        }
    }
}