package fr.kenda.freshagency.commands;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.managers.game.GamePlayerManager;
import fr.kenda.freshagency.utils.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WinCommand extends BaseCommand {

    private final GamePlayerManager gpm;

    public WinCommand() {
        super();
        gpm = FreshAgencyRunner.getInstance().getManagers().getManager(GamePlayerManager.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        printCommand(s, args);

        //if (!hasPermission(commandSender, Permission.PERMISSION_ADMIN)) return false;

        if (!isArgsSufficient(commandSender, args, 4, prefix + "&cCommande: §7/win <add|remove> <count> <giftCount> <token>"))
            return false;

        final Player player = getPlayer(commandSender, args);
        if (player == null) return false;

        if (!isInGameWorld(commandSender, player)) return false;

        String subCmd = args[0];
        try {
            int number = Integer.parseInt(args[1]);
            int giftCount = Integer.parseInt(args[2]);

            final int total = number * giftCount;

            switch (subCmd) {
                case "add" -> gpm.getDataGameFromPlayer(player).addWin(total);
                case "remove" -> gpm.getDataGameFromPlayer(player).removeWin(total);
                default ->
                        commandSender.sendMessage(Messages.transformColor(prefix + Messages.getMessage("invalid_subcommand")));
            }
        } catch (NumberFormatException e) {
            commandSender.sendMessage(Messages.transformColor(prefix + Messages.getMessage("number_invalid")));
        }
        return true;
    }


}