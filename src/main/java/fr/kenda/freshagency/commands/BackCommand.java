package fr.kenda.freshagency.commands;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.managers.back.BackGlitchManager;
import fr.kenda.freshagency.managers.back.BackManager;
import fr.kenda.freshagency.utils.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BackCommand extends BaseCommand {

    private final BackGlitchManager backGlitchManager;
    private final BackManager backManager;

    public BackCommand() {
        super();
        backManager = FreshAgencyRunner.getInstance().getManagers().getManager(BackManager.class);
        backGlitchManager = FreshAgencyRunner.getInstance().getManagers().getManager(BackGlitchManager.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        printCommand(s, args);

        //if (!hasPermission(commandSender, Permission.PERMISSION_ADMIN)) return false;

        if (!isArgsSufficient(commandSender, args, 3, prefix + "&cCommande: §7/back <number> <number_gift> <token>"))
            return false;

        final Player player = getPlayer(commandSender, args);
        if (player == null) return false;

        if (!isInGameWorld(commandSender, player)) return false;


        try {
            final int number = Integer.parseInt(args[0]);
            final int giftCount = Integer.parseInt(args[1]);

            final int total = number * giftCount;

            if (backGlitchManager.isInBack(player))
                backGlitchManager.launchBack(total, player);
            else
                backManager.launchBack(total, player);
        } catch (NumberFormatException e) {
            player.sendMessage(prefix + Messages.getMessage("number_invalid"));
        }
        return false;
    }
}
