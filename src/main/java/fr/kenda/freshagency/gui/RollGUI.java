package fr.kenda.freshagency.gui;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.utils.Config;
import fr.kenda.freshagency.utils.TitleActionBar;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public abstract class RollGUI<T> extends Gui {
    private final FreshAgencyRunner instance = FreshAgencyRunner.getInstance();
    private final int rollReach = Config.getInt("roll.number_rolls");
    private final List<T> items;
    private int rollCount = 0;
    private double interval = Config.getFloat("roll.time_between_rolls");
    private T currentItem;

    public RollGUI(String title, Player owner, int row) {
        super(title, owner, row);
        items = new ArrayList<>();
        startAnimation();
    }

    @Override
    public ItemStack[] mainMenu() {
        ItemStack[] content = new ItemStack[size];

        for (int i = 0; i < size; i++) {
            T item = getRandomItem();
            items.add(item);
            content[i] = createItemStack(item);
        }

        if (size > 4) {
            currentItem = items.get(4);
        }

        return content;
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory() != inventory || e.getInventory() == owner.getInventory()) return;
        e.setCancelled(true);
    }

    private void startAnimation() {
        final float timeInterval = Config.getFloat("roll.time_between_rolls");
        new BukkitRunnable() {
            @Override
            public void run() {
                if (rollCount < rollReach) {
                    animateInventory(interval);
                    rollCount++;
                } else if (rollCount < rollReach + 3) {
                    interval += 0.6;
                    animateInventory(interval);
                    rollCount++;
                } else {
                    this.cancel();
                    final long wait = Config.getInt("roll.time_before_close");
                    Bukkit.getScheduler().runTaskLater(instance, () -> {
                        for (int i = 0; i < size; i++) {
                            if (i != 4)
                                inventory.setItem(i, new ItemStack(Config.getMaterial("roll.roll_decoration")));
                        }
                        updateContent(inventory.getContents());
                        owner.playSound(owner.getLocation(), Sound.valueOf(Config.getString("roll.sound_win")), 1.0f, 1.0f);
                    }, (long) ((interval + 0.6) * 20));
                    Bukkit.getScheduler().runTaskLater(instance, () -> {
                        close();
                        TitleActionBar.sendTitle(owner, getTitle(currentItem), 1, 2, 1);
                        performCommand(currentItem);
                    }, (long) (((interval + 0.6) * 20) + wait * 20));
                }
            }
        }.runTaskTimer(instance, 0, (long) (timeInterval * 20L));
    }

    private void animateInventory(double interval) {
        Player player = owner;

        Bukkit.getScheduler().runTaskLater(instance, () -> {
            ItemStack[] content = inventory.getContents();
            for (int i = 0; i < content.length - 1; i++) {
                content[i] = content[i + 1];
            }
            T item = getRandomItem();
            items.add(item);
            content[content.length - 1] = createItemStack(item);
            updateContent(content);

            player.playSound(player.getLocation(), Sound.valueOf(Config.getString("roll.sound_roll")), 1.0f, 1.0f);

            if (!items.isEmpty()) {
                items.remove(0);
            }

            if (content.length > 4) {
                currentItem = items.get(4);
            }
        }, (long) (interval * 20));
    }

    protected abstract T getRandomItem();

    protected abstract ItemStack createItemStack(T item);

    protected abstract String getTitle(T item);

    protected abstract void performCommand(T item);
}
