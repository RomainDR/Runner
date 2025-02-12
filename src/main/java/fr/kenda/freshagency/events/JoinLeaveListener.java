package fr.kenda.freshagency.events;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.data.PlayerData;
import fr.kenda.freshagency.database.Database;
import fr.kenda.freshagency.managers.Managers;
import fr.kenda.freshagency.managers.bossbar.BossbarManager;
import fr.kenda.freshagency.managers.game.GamePlayerManager;
import fr.kenda.freshagency.managers.game.GameWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import spark.Spark;

public class JoinLeaveListener implements Listener {

    private final GamePlayerManager gamePlayerManager;
    private final GameWorldManager gameWorldManager;
    private final BossbarManager bossbarManager;
    private final FreshAgencyRunner instance;
    private final Database database;

    public JoinLeaveListener() {
        instance = FreshAgencyRunner.getInstance();
        Managers managers = instance.getManagers();
        this.gamePlayerManager = managers.getManager(GamePlayerManager.class);
        this.gameWorldManager = managers.getManager(GameWorldManager.class);
        this.bossbarManager = managers.getManager(BossbarManager.class);
        this.database = FreshAgencyRunner.getInstance().getDatabases().getDatabase();

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage("");
        Player player = e.getPlayer();
        gamePlayerManager.addPlayerToGame(player);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setLevel(0);
        player.setExp(0);
        bossbarManager.addPlayer(player);
        player.getInventory().clear();
        World emptyWorld = gameWorldManager.getEmptyWorld();
        if (emptyWorld != null) {
            Location loc = emptyWorld.getSpawnLocation();
            loc.setYaw(-90);
            player.teleport(loc);
            database.addGame(player.getWorld(), player.getName(), 1);
        } else player.kickPlayer("Une erreur s'est produite lors du chargement d'un monde disponible.");
    }


    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        e.setQuitMessage("");
        Player player = e.getPlayer();
        World world = player.getWorld();


        PlayerData pd = gamePlayerManager.getDataGameFromPlayer(player);
        if (pd != null) {
            gamePlayerManager.getDataGameFromPlayer(player).disconnectLive(player);
            if (pd.isCanUpdateStat())
                database.updateStats(pd);
            gamePlayerManager.removePlayerToGame(player);
            bossbarManager.removePlayer(player);
        }

        String playerName = player.getName().toLowerCase();
        Spark.unmap("/" + playerName + "/percent");
        Spark.unmap("/" + playerName + "/win");

        if (world.getPlayers().size() - 1 != 0)
            return;

        if (gameWorldManager.isWorldContainsInGame(world)) {
            Bukkit.getScheduler().runTaskLater(instance, () -> {
                        gameWorldManager.recreate(world);
                        database.deleteGame(player);
                    }
                    , 20L);
        }
    }
}