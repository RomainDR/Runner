package fr.kenda.freshagency.commands;

import fr.kenda.freshagency.utils.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RepairCommand extends BaseCommand {


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        printCommand(s, args);
        //if (!hasPermission(commandSender, Permission.PERMISSION_ADMIN)) return false;

        if (!isArgsSufficient(commandSender, args, 3, prefix + "&cCommande: §7/repair <percent> <giftCount> <token>"))
            return false;

        final Player player = getPlayer(commandSender, args);
        if (player == null) return false;

        if (!isInGameWorld(commandSender, player)) return false;

        try {
            int percent = Integer.parseInt(args[0]);
            final int giftCount = Integer.parseInt(args[1]);

            final int total = percent * giftCount;

            gwm.regenerateMap(player.getWorld(), total);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(Messages.transformColor(prefix + Messages.getMessage("number_invalid")));
        }
        return false;
    }
}