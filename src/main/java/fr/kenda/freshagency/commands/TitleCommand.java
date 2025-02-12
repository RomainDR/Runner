package fr.kenda.freshagency.commands;

import fr.kenda.freshagency.utils.Messages;
import fr.kenda.freshagency.utils.TitleActionBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class TitleCommand extends BaseCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] args) {
        printCommand(s, args);

        //if (!hasPermission(commandSender, Permission.PERMISSION_ADMIN)) return false;
        if (args.length <= 2) {
            commandSender.sendMessage(Messages.transformColor(prefix + "&7/sendtitle <message;[subtitle]> <token>"));
            return false;
        }


        final Player player = getPlayer(commandSender, args);
        if (player == null || !isInGameWorld(commandSender, player)) return false;

        String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length - 1));
        String[] parts = message.split(";", 2);

        if (parts.length == 1) {
            TitleActionBar.sendTitle(player, parts[0].stripTrailing(), 1, 2, 1);
        } else {
            TitleActionBar.sendTitleAndSubtitle(player, parts[0].stripTrailing(), parts[1].stripLeading(), 1, 2, 1);
        }

        return true;
    }
}

