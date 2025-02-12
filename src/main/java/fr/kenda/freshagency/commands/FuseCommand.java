package fr.kenda.freshagency.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FuseCommand extends BaseCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        printCommand(s, args);
        //if (!hasPermission(commandSender, Permission.PERMISSION_ADMIN)) return false;

        if (!isArgsSufficient(commandSender, args, 1, prefix + "&cCommande: §7/fuse <token>")) return false;

        final Player player = getPlayer(commandSender, args);
        if (player == null || !isInGameWorld(commandSender, player)) return false;


        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();

        Bukkit.getServer().dispatchCommand(commandSender, String.format("particle minecraft:elder_guardian %s %s %s %s %s %s 1 1", x, y, z, x, y, z));
        Bukkit.getServer().dispatchCommand(commandSender, String.format("effect give %s minecraft:blindness 10 10", player.getName()));

        return false;
    }
}
