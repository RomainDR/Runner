package fr.kenda.freshagency;

import fr.kenda.freshagency.database.DatabaseManager;
import fr.kenda.freshagency.managers.Managers;
import fr.kenda.freshagency.managers.game.GameWorldManager;
import fr.kenda.freshagency.utils.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import spark.Spark;

public final class FreshAgencyRunner extends JavaPlugin {

    private static FreshAgencyRunner instance;
    private Managers manager;
    private DatabaseManager databases;


    public static FreshAgencyRunner getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        instance = this;
        manager = new Managers(this);
        databases = new DatabaseManager();
        manager.registerAll();

        Spark.port(Config.getInt("api_port"));
        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
            response.header("Content-Encoding", "UTF-8");
        });


    }

    public Managers getManagers() {
        return manager;
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers())
            player.kickPlayer("");


        manager.getManager(GameWorldManager.class).deleteWorldsGame();
        databases.disconnect();
    }

    public DatabaseManager getDatabases() {
        return databases;
    }
}
