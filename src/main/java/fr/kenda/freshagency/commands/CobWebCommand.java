package fr.kenda.freshagency.commands;

import fr.kenda.freshagency.blocks.BlockSnap;
import fr.kenda.freshagency.utils.Config;
import fr.kenda.freshagency.utils.Messages;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CobWebCommand extends BaseCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        printCommand(s, args);

        //if (!hasPermission(commandSender, Permission.PERMISSION_ADMIN)) return false;

        if (!isArgsSufficient(commandSender, args, 1, prefix + "&cCommande: §7/cobweb <token>")) return false;

        final Player player = getPlayer(commandSender, args);
        if (player == null) return false;

        if (!isInGameWorld(commandSender, player)) return false;

        try {
            int radius = Config.getInt("cobweb.radius") / 2;
            Location playerLocation = player.getLocation();
            int baseX = playerLocation.getBlockX();
            int baseY = playerLocation.getBlockY() + 1;
            int baseZ = playerLocation.getBlockZ();

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Location loc = new Location(playerLocation.getWorld(), baseX + x, baseY + y, baseZ + z);
                        Block block = loc.getBlock();

                        BlockSnap bs = new BlockSnap(block);
                        if (gwm.isBlockContainsInOriginalMap(bs)) {
                            bs.setType(block.getType());
                            gwm.addBlockToRestore(bs);
                        }
                        block.setType(Material.COBWEB);
                    }
                }
            }
        } catch (NumberFormatException e) {
            commandSender.sendMessage(Messages.transformColor(prefix + Messages.getMessage("number_invalid")));
        }
        return false;
    }
}