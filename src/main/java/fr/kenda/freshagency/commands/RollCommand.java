package fr.kenda.freshagency.commands;

import fr.kenda.freshagency.gui.RollEventGUI;
import fr.kenda.freshagency.gui.RollWinGUI;
import fr.kenda.freshagency.utils.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RollCommand extends BaseCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        //if (!hasPermission(commandSender, Permission.PERMISSION_ADMIN)) return false;

        if (!isArgsSufficient(commandSender, args, 2, prefix + "&cCommande: §7/roll <event/win> <token>")) return false;

        final Player player = getPlayer(commandSender, args);
        if (player == null) return false;

        if (!isInGameWorld(commandSender, player)) return false;

        String sub = args[0];
        switch (sub.toLowerCase()) {
            case "event" -> new RollEventGUI(Messages.transformColor("&6Roll Event"), player, 1).create();
            case "win" -> new RollWinGUI(Messages.transformColor("&6Roll Wins"), player, 1).create();
            default ->
                    commandSender.sendMessage(Messages.transformColor(prefix + Messages.getMessage("invalid_subcommand")));
        }

        return false;
    }
}
