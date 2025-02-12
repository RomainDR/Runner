package fr.kenda.freshagency.gui;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.managers.roll.RollEventManager;
import fr.kenda.freshagency.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RollEventGUI extends RollGUI<RollEventManager.Event> {

    public RollEventGUI(String title, Player owner, int row) {
        super(title, owner, row);
    }

    @Override
    protected RollEventManager.Event getRandomItem() {
        return FreshAgencyRunner.getInstance().getManagers().getManager(RollEventManager.class).getRandomEvent();
    }

    @Override
    protected ItemStack createItemStack(RollEventManager.Event event) {
        return new ItemBuilder(event.type()).setName(event.title()).toItemStack();
    }

    @Override
    protected String getTitle(RollEventManager.Event event) {
        return event.title();
    }

    @Override
    protected void performCommand(RollEventManager.Event event) {
        for (String cmd : event.commands()) {
            cmd = cmd.replace("%player%", owner.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
    }
}
