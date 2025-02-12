package fr.kenda.freshagency.managers;

import fr.kenda.freshagency.FreshAgencyRunner;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;

public class FileManager implements IManager {

    private final FreshAgencyRunner instance = FreshAgencyRunner.getInstance();
    private final HashMap<String, FileConfiguration> files = new HashMap<>();

    public FileManager() {
        createFile("messages");
        createFile("database");
    }

    /**
     * Create file
     *
     * @param fileName String
     */
    public void createFile(String fileName) {
        final File file = new File(instance.getDataFolder(), fileName + ".yml");
        if (!file.exists()) {
            instance.saveResource(fileName + ".yml", false);
        }
        FileConfiguration configFile = YamlConfiguration.loadConfiguration(file);
        files.put(fileName, configFile);
    }


    /**
     * Get configuration from file
     *
     * @param fileName String
     * @return FileConfiguration
     */
    public FileConfiguration getConfigFrom(String fileName) {
        return files.get(fileName);
    }

    @Override
    public void register() {

    }
}
