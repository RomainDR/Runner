package fr.kenda.freshagency.database;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.managers.FileManager;
import org.bukkit.configuration.file.FileConfiguration;


public class DatabaseManager {

    private final Database database;

    public DatabaseManager() {
        FreshAgencyRunner instance = FreshAgencyRunner.getInstance();
        final FileConfiguration config = instance.getManagers().getManager(FileManager.class).getConfigFrom("database");
        database = new Database(config.getString("host"), config.getInt("port"), config.getString("user"), config.getString("password"));

    }

    public void disconnect() {
        database.disconnect();
    }

    public Database getDatabase() {
        return database;
    }
}
