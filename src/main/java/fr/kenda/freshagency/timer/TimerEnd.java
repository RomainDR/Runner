package fr.kenda.freshagency.timer;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.data.PlayerData;
import fr.kenda.freshagency.utils.Config;
import fr.kenda.freshagency.utils.LocationTransform;
import fr.kenda.freshagency.utils.Messages;
import fr.kenda.freshagency.utils.TitleActionBar;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TimerEnd {

    public void start(PlayerData playerData) {
        final FreshAgencyRunner instance = FreshAgencyRunner.getInstance();
        new CountdownTask(instance, playerData, 10).runTaskLater(instance, 0L);
    }

    private static class CountdownTask extends BukkitRunnable {
        final PlayerData playerData;
        final Player player;
        private final FreshAgencyRunner instance;
        private final int timer;

        public CountdownTask(FreshAgencyRunner instance, PlayerData playerData, int timer) {
            this.instance = instance;
            this.timer = timer;
            this.playerData = playerData;
            this.player = playerData.getPlayer();
        }

        @Override
        public void run() {
            if (!playerData.isHasStartTimerEnd()) {
                cancel();
                return;
            }

            if (timer > 0) {
                String color = timer > 3 ? "&a" : timer == 3 ? "&6" : timer == 2 ? "&c" : "&4";
                Sound sound = timer > 3 ? Sound.BLOCK_NOTE_BLOCK_BASS : Sound.ENTITY_ARROW_HIT_PLAYER;
                TitleActionBar.sendTitle(player, Messages.transformColor(color + timer), 0, 20, 0);
                player.playSound(player.getLocation(), sound, 1.0F, 1.0F);


                int nextTimer = timer - 1;

                // Créer une nouvelle instance de CountdownTask avec le timer décrémenté
                if (timer == 3) {
                    new CountdownTask(instance, playerData, nextTimer).runTaskLater(instance, 2 * 20L);
                } else if (timer == 2) {
                    new CountdownTask(instance, playerData, nextTimer).runTaskLater(instance, 3 * 20L);
                } else if (timer == 1) {
                    new CountdownTask(instance, playerData, nextTimer).runTaskLater(instance, 4 * 20L);
                } else {
                    new CountdownTask(instance, playerData, nextTimer).runTaskLater(instance, 20L);
                }
            } else {
                //teleport to X,Y,Z
                Location loc = LocationTransform.deserializeCoordinate(player, Config.getString("map.end_teleport"));
                loc.setYaw(-90);
                player.teleport(loc);
                playerData.stopTimerEnd();
                TitleActionBar.sendTitle(player, "", 1, 1, 1);

                this.cancel();
            }
        }
    }
}