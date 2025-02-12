package fr.kenda.freshagency.managers.bossbar;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.managers.IManager;
import fr.kenda.freshagency.utils.Config;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BossbarManager implements IManager {

    private BossBar agencyPub;
    private List<String> messages = new ArrayList<>();
    private int indexMessages = 0;


    @Override
    public void register() {
        agencyPub = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID);
        agencyPub.setProgress(1.0);
        messages = Config.getList("bossbar_info.agency.messages");
        Bukkit.getScheduler().runTaskTimer(FreshAgencyRunner.getInstance(), this::updateTitleBossbar, 20L, Config.getLong("bossbar_info.agency.interval_change") * 20L);
    }

    public void addPlayer(Player p) {
        agencyPub.addPlayer(p);
    }

    public void removePlayer(Player p) {
        agencyPub.removePlayer(p);
    }

    public void updateTitleBossbar() {
        int currentIndex = indexMessages % messages.size();
        agencyPub.setTitle(messages.get(currentIndex));
        indexMessages++;
    }
}