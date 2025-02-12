package fr.kenda.freshagency.commands;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.data.PlayerData;
import fr.kenda.freshagency.managers.game.GamePlayerManager;
import fr.kenda.freshagency.utils.Messages;
import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveUnknownHostException;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class StartLiveCommand extends BaseCommand {

    private final GamePlayerManager gamePlayerManager;
    private final FreshAgencyRunner instance;

    public StartLiveCommand() {
        instance = FreshAgencyRunner.getInstance();
        gamePlayerManager = instance.getManagers().getManager(GamePlayerManager.class);
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messages.transformColor(prefix + Messages.getMessage("need_be_player")));
            return false;
        }

        if (!isInGameWorld(player, player) || !isArgsSufficient(player, args, 2, "&cCommande: &7/startlive <on/off> <name_account> (si off, mettre un nom aléatoire)")) {
            return false;
        }

        String action = args[0].toLowerCase();
        PlayerData playerData = gamePlayerManager.getDataGameFromPlayer(player);

        switch (action) {
            case "off":
                gamePlayerManager.startGame(player, false);
                return true;

            case "on":
                if (playerData.hasStartStream()) {
                    player.sendMessage(Messages.transformColor(prefix + Messages.getMessage("stream_already_start")));
                    return false;
                }

                String account = args[1];
                player.sendMessage(Messages.transformColor(prefix + Messages.getMessage("connect_account", "{host}", account)));

                try {
                    TikTokLive.newClient(account)
                            .onConnected((liveClient, event) -> connect(player, liveClient, playerData))
                            .onGift((liveClient, giftEvent) -> gift(giftEvent, playerData))
                            .buildAndConnectAsync();
                    playerData.setUpdateStats();
                    return true;
                } catch (TikTokLiveUnknownHostException e) {
                    player.sendMessage(Messages.transformColor(Messages.getPrefix() + Messages.getMessage("account_not_found")));
                }
                return false;

            default:
                player.sendMessage(Messages.transformColor("&cCommande: &7/startlive <on/off> (si off, mettre un nom aléatoire)"));
                return false;
        }
    }


    private void connect(Player player, LiveClient liveClient, PlayerData playerData) {
        new BukkitRunnable() {
            @Override
            public void run() {
                playerData.setLiveClient(liveClient);
                player.sendMessage(Messages.transformColor(prefix + Messages.getMessage("connect_account_success")));
                // Exécuter la téléportation de manière synchrone
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        gamePlayerManager.startGame(player, true);
                        instance.getDatabases().getDatabase().updateGame(player.getName(), 2);
                    }
                }.runTask(instance);
            }
        }.runTaskAsynchronously(instance);
    }

    private void gift(TikTokGiftEvent tikTokGiftEvent, PlayerData playerData) {
        if (gamePlayerManager.getDataGameFromPlayer(playerData.getPlayer()).isInStartGame()) return;
        int diamondCost = tikTokGiftEvent.getGift().getDiamondCost();
        int amount = tikTokGiftEvent.getCombo();
        String donatorName = tikTokGiftEvent.getUser().getProfileName();

        playerData.updateDonator(playerData.getPlayer(), donatorName, amount * diamondCost);
    }
}
