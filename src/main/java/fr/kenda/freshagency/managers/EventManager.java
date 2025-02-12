package fr.kenda.freshagency.managers;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.events.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class EventManager implements IManager {

    private final FreshAgencyRunner instance;

    public EventManager(FreshAgencyRunner instance) {
        this.instance = instance;
    }

    @Override
    public void register() {

        final PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerMoveListener(), instance);
        pm.registerEvents(new PlayerDoubleJump(), instance);
        pm.registerEvents(new JoinLeaveListener(), instance);
        pm.registerEvents(new DisabledEvent(), instance);
        pm.registerEvents(new ExplosionListener(), instance);
        pm.registerEvents(new PlayerBreakBlock(), instance);
        pm.registerEvents(new BlockPlaceListener(), instance);
        pm.registerEvents(new BlockFallingListener(), instance);
    }
}
