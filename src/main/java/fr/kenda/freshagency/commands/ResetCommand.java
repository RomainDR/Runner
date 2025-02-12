package fr.kenda.freshagency.commands;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.managers.game.GamePlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ResetCommand extends BaseCommand {


    private final GamePlayerManager gpm;

    public ResetCommand() {
        super();
        gpm = FreshAgencyRunner.getInstance().getManagers().getManager(GamePlayerManager.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        printCommand(s, args);
        //if (!hasPermission(commandSender, Permission.PERMISSION_ADMIN)) return false;

        if (!isArgsSufficient(commandSender, args, 1, prefix + "&cCommande: §7/reset <token>")) return false;

        final Player player = getPlayer(commandSender, args);
        if (player == null) return false;

        if (!isInGameWorld(commandSender, player)) return false;


        gpm.spawnLightningTowardsPlayer(player, 0);

        return false;
    }
}
