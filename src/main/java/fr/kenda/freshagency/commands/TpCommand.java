package fr.kenda.freshagency.commands;

import fr.kenda.freshagency.utils.Config;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpCommand extends BaseCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        printCommand(s, args);

        //if (!hasPermission(commandSender, Permission.PERMISSION_ADMIN)) return false;

        if (!isArgsSufficient(commandSender, args, 2, prefix + "&cCommande: §7/tppercent <percent> <token>"))
            return false;

        final Player player = getPlayer(commandSender, args);
        if (player == null) return false;

        if (!isInGameWorld(commandSender, player)) return false;
        try {
            int number = (Integer.parseInt(args[0]) * Config.getInt("map_size")) / 100;
            Location playerLocation = player.getLocation();
            World playerWorld = player.getWorld();
            Location loc = new Location(playerWorld, number, playerLocation.getY(), playerLocation.getZ());
            loc.setY(playerWorld.getHighestBlockYAt(loc));
            if (loc.getY() < Config.getInt("maximum_void_y"))
                loc.setY(playerLocation.getY());
            loc.setYaw(playerLocation.getYaw());
            loc.setPitch(playerLocation.getPitch());
            player.teleport(loc);
        } catch (NumberFormatException e) {
            player.sendMessage("Veuillez entrer un nombre valide.");
        }


        return false;
    }
}
