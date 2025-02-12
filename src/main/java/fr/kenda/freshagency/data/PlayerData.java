package fr.kenda.freshagency.data;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.managers.game.GameWorldManager;
import fr.kenda.freshagency.timer.TimerEnd;
import fr.kenda.freshagency.utils.Config;
import fr.kenda.freshagency.utils.Messages;
import fr.kenda.freshagency.utils.TitleActionBar;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerData {

    private final BossBar topDonatorBossbar;
    private final Player player;
    private final String token;
    private LiveClient liveClient;
    private boolean hasCheckpoint;
    private boolean isInStartGame;
    private boolean hasStartTimerEnd;
    private boolean canUpdateStat = false;
    private Map<String, Integer> donator;
    private float percentMap;
    private int win;
    private int blockWalk;
    private int blockBack;
    private long startTime;

    public PlayerData(Player player) {
        this.player = player;
        hasCheckpoint = false;
        liveClient = null;
        isInStartGame = false;
        topDonatorBossbar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
        donator = new HashMap<>();
        percentMap = 100;
        startTime = 0;
        token = FreshAgencyRunner.getInstance().getDatabases().getDatabase().getTokenFromPlayer(player.getName());
    }

    public String getToken() {
        return token;
    }

    public Player getPlayer() {
        return player;
    }

    public void setIsInStart(boolean isInStart) {
        isInStartGame = isInStart;
    }

    public boolean isHasCheckpoint() {
        return hasCheckpoint;
    }

    public void setCheckpoint(boolean hasCheckpoint) {
        this.hasCheckpoint = hasCheckpoint;
    }

    public boolean isInStartGame() {
        return isInStartGame;
    }

    public void addBlockWalk() {
        blockWalk++;
    }

    public void addBlockBack(int number) {
        blockBack += number;
    }

    public void disconnectLive(Player player) {
        if (liveClient != null) {
            liveClient.disconnect();
            player.sendMessage(Messages.getPrefix() + Messages.getMessage("disconnect_account_success"));
        }
        liveClient = null;
    }

    public void addWin(int number) {
        win += number;
        TitleActionBar.sendTitle(player, Messages.getMessage("win_add", "%amount%", String.valueOf(number)), 1, 2, 1);
    }

    public void removeWin(int number) {
        win -= number;
        TitleActionBar.sendTitle(player, Messages.getMessage("win_remove", "%amount%", String.valueOf(number)), 1, 2, 1);
    }

    public int getWin() {
        return win;
    }

    public void addPlayerDonator(Player player) {
        if (!topDonatorBossbar.getPlayers().contains(player)) {
            topDonatorBossbar.addPlayer(player);
        }
    }

    public void updateDonator(Player player, String name, final int amount) {
        addPlayerDonator(player);
        donator.merge(name, amount, Integer::sum);
        sortDonators();
        final Map.Entry<String, Integer> topDonator = getTopDonator();
        topDonatorBossbar.setTitle(Config.getString("top_donator_message",
                "{donator}", topDonator.getKey(),
                "{total_diamond}", String.valueOf(topDonator.getValue())));
    }

    private void sortDonators() {
        donator = donator.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public Map.Entry<String, Integer> getTopDonator() {
        if(donator.isEmpty()) return null;
        return donator.entrySet().iterator().next();
    }

    public void startTimerEnd() {
        hasStartTimerEnd = true;
        new TimerEnd().start(this);
    }

    public boolean hasStartStream() {
        return liveClient != null;
    }

    public String getPercentMap() {
        return String.format("%.2f", percentMap);
    }

    public void setPercentMap(float percent) {
        percentMap = percent;
    }

    public void stopTimerEnd() {
        hasStartTimerEnd = false;
    }

    public boolean isHasStartTimerEnd() {
        return hasStartTimerEnd;
    }

    public void updatePercent() {
        setPercentMap(FreshAgencyRunner.getInstance().getManagers().getManager(GameWorldManager.class).getPercentOfMap(player.getWorld().getUID()));
    }

    public LiveClient getLiveClient() {
        return liveClient;
    }

    public void setLiveClient(LiveClient liveClient) {
        this.liveClient = liveClient;
    }

    public void resetWin() {
        win = 0;
    }

    public int getBlockBack() {
        return blockBack;
    }

    public int getBlockWalk() {
        return blockWalk;
    }

    public void setUpdateStats() {
        canUpdateStat = true;
    }

    public boolean isCanUpdateStat() {
        return canUpdateStat;
    }

    public void setStartTime() {
        startTime = System.currentTimeMillis();
    }

    public long getTimeLive() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }
}