package fr.kenda.freshagency.commands;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.data.PlayerData;
import fr.kenda.freshagency.managers.game.GamePlayerManager;
import fr.kenda.freshagency.utils.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DisconnectLiveCommand extends BaseCommand {

    private final GamePlayerManager gamePlayerManager;

    public DisconnectLiveCommand() {
        gamePlayerManager = FreshAgencyRunner.getInstance().getManagers().getManager(GamePlayerManager.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Messages.transformColor(prefix + Messages.getMessage("need_be_player")));
            return false;
        }

        if (!isInGameWorld(player, player)) return false;
        if (!isArgsSufficient(player, args, 0, "&cCommande: &7/disconnectlive")) return false;

        PlayerData playerData = gamePlayerManager.getDataGameFromPlayer(player);
        if (playerData != null && playerData.hasStartStream()) {
            player.sendMessage(prefix + Messages.getMessage("disconnect_account", "{host}", playerData.getLiveClient().getRoomInfo().getHostName()));
            playerData.disconnectLive(player);
        } else
            player.sendMessage(prefix + Messages.getMessage("no_account_connected"));
        return true;
    }
}