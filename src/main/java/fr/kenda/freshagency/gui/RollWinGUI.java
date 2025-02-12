package fr.kenda.freshagency.gui;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.managers.roll.RollWinManager;
import fr.kenda.freshagency.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RollWinGUI extends RollGUI<RollWinManager.Win> {

    public RollWinGUI(String title, Player owner, int row) {
        super(title, owner, row);
    }

    @Override
    protected RollWinManager.Win getRandomItem() {
        return FreshAgencyRunner.getInstance().getManagers().getManager(RollWinManager.class).getRandomWin();
    }

    @Override
    protected ItemStack createItemStack(RollWinManager.Win win) {
        return new ItemBuilder(win.type()).setName(win.title()).toItemStack();
    }

    @Override
    protected String getTitle(RollWinManager.Win win) {
        return win.title();
    }

    @Override
    protected void performCommand(RollWinManager.Win win) {
        for (String cmd : win.commands()) {
            cmd = cmd.replace("%player%", owner.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
    }
}
