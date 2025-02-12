package fr.kenda.freshagency.managers.back;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.data.PlayerData;
import fr.kenda.freshagency.managers.IManager;
import fr.kenda.freshagency.managers.game.GamePlayerManager;
import fr.kenda.freshagency.utils.Messages;
import fr.kenda.freshagency.utils.TitleActionBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractBackManager implements IManager {

    protected final FreshAgencyRunner instance;
    protected final ConcurrentHashMap<Player, AtomicInteger> playerBacks = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<Player, Integer> playerNeedBacks = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<Player, Boolean> playerInBack = new ConcurrentHashMap<>(); // Variable pour suivre si un joueur est en back
    protected GamePlayerManager gamePlayerManager;

    public AbstractBackManager(FreshAgencyRunner instance) {
        this.instance = instance;
    }

    @Override
    public void register() {
        gamePlayerManager = instance.getManagers().getManager(GamePlayerManager.class);
    }

    public void launchBack(int number, Player player) {
        PlayerData pd = gamePlayerManager.getDataGameFromPlayer(player);

        if (pd.isHasStartTimerEnd()) {
            pd.stopTimerEnd();
            TitleActionBar.sendTitle(player, Messages.getMessage("timer_stop"), 1, 2, 1);
        }

        playerNeedBacks.merge(player, number, Integer::sum);

        pd.addBlockBack(number);

        playerBacks.putIfAbsent(player, new AtomicInteger(0));
        playerBacks.get(player).addAndGet(number);

        if (playerInBack.get(player) != null) return;

        playerInBack.put(player, true); // Marque le joueur comme étant en back


        startTeleportation(player);
    }

    private void startTeleportation(Player player) {
        // Démarrer la tâche de téléportation avec un délai de 1 tick (20 fois par seconde)
        new TeleportTask(player).runTaskTimer(instance, 0L, 1L);
    }

    protected abstract void teleportPlayer(Player player);

    public boolean isInBack(Player player) {
        return playerInBack.getOrDefault(player, false);
    }

    private class TeleportTask extends BukkitRunnable {
        private final Player player;

        public TeleportTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            AtomicInteger remainingBacks = playerBacks.get(player);
            if (remainingBacks != null && remainingBacks.get() > 0) {
                int blocksPerTick = calculateBlocksPerTick(remainingBacks.get());

                for (int i = 0; i < blocksPerTick; i++) {
                    teleportPlayer(player);
                    remainingBacks.decrementAndGet();

                    if (remainingBacks.get() <= 0) {
                        break;
                    }
                }

            } else {
                playerNeedBacks.remove(player);
                playerBacks.remove(player);
                playerInBack.remove(player); // Marque le joueur comme n'étant plus en back
                this.cancel(); // Annule la tâche spécifique
            }
        }

        private int calculateBlocksPerTick(int remainingBacks) {
            // Calculer combien de segments de 525 (350 * 1.5) sont présents
            int segments = remainingBacks / 525;

            // Retourner le nombre de blocs par tick, en s'assurant de ne pas dépasser 10
            return Math.min(1 + segments, 10);
        }

    }
}
