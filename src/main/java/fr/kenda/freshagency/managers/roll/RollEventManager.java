package fr.kenda.freshagency.managers.roll;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.managers.IManager;
import fr.kenda.freshagency.utils.Config;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class RollEventManager implements IManager {

    private final FreshAgencyRunner instance;
    private Map<String, Event> events;

    public RollEventManager(FreshAgencyRunner instance) {
        this.instance = instance;
    }


    private void loadEvents() {
        events = loadItems("roll.events", Event::new);
    }

    public Event getRandomEvent() {
        return getRandomItem(events.values());
    }

    @Override
    public void register() {
        loadEvents();
    }

    private <T> Map<String, T> loadItems(String path, ItemFactory<T> factory) {
        FileConfiguration config = instance.getConfig();
        if (config.contains(path)) {
            return Objects.requireNonNull(config.getConfigurationSection(path)).getKeys(false).stream()
                    .collect(Collectors.toMap(
                            key -> key,
                            key -> factory.create(
                                    key,
                                    Config.getString(path + "." + key + ".title"),
                                    Config.getMaterial(path + "." + key + ".material"),
                                    Config.getInt(path + "." + key + ".percentage"),
                                    Config.getList(path + "." + key + ".commands")
                            )
                    ));
        }
        return new HashMap<>();
    }

    private <T> T getRandomItem(Collection<T> items) {
        int totalPercentage = items.stream().mapToInt(item -> ((Event) item).percentage()).sum();
        int randomValue = new Random().nextInt(totalPercentage);

        int cumulativePercentage = 0;
        for (T item : items) {
            cumulativePercentage += ((Event) item).percentage();
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

    public record Event(String name, String title, Material type, int percentage, List<String> commands) {
    }
}
