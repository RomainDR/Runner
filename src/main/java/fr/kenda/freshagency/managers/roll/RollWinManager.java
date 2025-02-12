package fr.kenda.freshagency.managers.roll;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.managers.IManager;
import fr.kenda.freshagency.utils.Config;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class RollWinManager implements IManager {

    private final FreshAgencyRunner instance;
    private Map<String, Win> wins;

    public RollWinManager(FreshAgencyRunner instance) {
        this.instance = instance;
    }

    private void loadWins() {
        wins = loadItems("roll.wins", Win::new);
    }

    public Win getRandomWin() {
        return getRandomItem(wins.values());
    }

    @Override
    public void register() {
        loadWins();
    }

    private <T> Map<String, T> loadItems(String path, ItemFactory<T> factory) {
        FileConfiguration config = instance.getConfig();
        return Optional.ofNullable(config.getConfigurationSection(path))
                .map(section -> section.getKeys(false).stream()
                        .collect(Collectors.toMap(
                                key -> key,
                                key -> factory.create(
                                        key,
                                        Config.getString(path + "." + key + ".title"),
                                        Config.getMaterial(path + "." + key + ".material"),
                                        Config.getInt(path + "." + key + ".percentage"),
                                        Config.getList(path + "." + key + ".commands")
                                )
                        ))
                ).orElse(Collections.emptyMap());
    }

    private <T> T getRandomItem(Collection<T> items) {
        int totalPercentage = items.stream().mapToInt(item -> ((Win) item).percentage()).sum();
        int randomValue = new Random().nextInt(totalPercentage);

        int cumulativePercentage = 0;
        for (T item : items) {
            cumulativePercentage += ((Win) item).percentage();
            if (randomValue < cumulativePercentage) {
                return item;
            }
        }
        return null;
    }

    @FunctionalInterface
    private interface ItemFactory<T> {
        T create(String name, String title, Material type, int percentage, List<String> commands);
    }

    public record Win(String name, String title, Material type, int percentage, List<String> commands) {
    }
}
