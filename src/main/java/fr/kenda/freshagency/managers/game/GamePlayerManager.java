package fr.kenda.freshagency.managers.game;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.data.PlayerData;
import fr.kenda.freshagency.managers.IManager;
import fr.kenda.freshagency.utils.*;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import spark.Response;
import spark.Spark;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GamePlayerManager implements IManager {

    private final Map<Player, PlayerData> playerDatas = new HashMap<>();
    private final FreshAgencyRunner instance;

    public GamePlayerManager(FreshAgencyRunner instance) {
        this.instance = instance;
    }

    @Override
    public void register() {
    }

    public void addPlayerToGame(Player player) {
        playerDatas.computeIfAbsent(player, PlayerData::new);
    }

    public PlayerData getDataGameFromPlayer(Player p) {
        return playerDatas.get(p);
    }

    public void startGame(Player player, boolean tiktokConnect) {
        resetPlayer(player, false);
        GameWorldManager gameWorldManager = instance.getManagers().getManager(GameWorldManager.class);
        gameWorldManager.regenerateMap(player.getWorld(), 100);


        PlayerData playerData = getDataGameFromPlayer(player);
        playerData.setUpdateStats();
        playerData.setStartTime();

        playerData.resetWin();

        new BukkitRunnable() {
            @Override
            public void run() {
                playerData.setIsInStart(true);
                new BukkitRunnable() {
                    int count = 3;

                    @Override
                    public void run() {
                        if (count == 0) {
                            this.cancel();
                            playerData.setIsInStart(false);
                            if (!tiktokConnect)
                                return;
                            startWebAPIToPlayer(player);
                            String host = instance.getDatabases().getDatabase().getHost();
                            int port = Config.getInt("api_port");
                            String linkPercent = "http://" + host + ":" + port + "/" + player.getName().toLowerCase() + "/percent";
                            String linkPWin = "http://" + host + ":" + port + "/" + player.getName().toLowerCase() + "/win";
                            TitleActionBar.sendTitle(player, Messages.getMessage("live_started"), 2, 1, 1);
                            ClickableText.makeTextOpenLink("&7Lien des pourcentages de la map: &n" + linkPercent, "&aOuvrir le lien", linkPercent, player);
                            ClickableText.makeTextOpenLink("&7Lien des win de la map: &n" + linkPWin, "&aOuvrir le lien", linkPWin, player);
                        } else {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
                            TitleActionBar.sendTitle(player, String.valueOf(count), 1, 2, 1);
                            count--;
                        }
                    }
                }.runTaskTimer(instance, 10L, 30L);
            }
        }.runTaskLater(instance, 10L);
    }

    private void startWebAPIToPlayer(Player player) {
        String playerName = player.getName().toLowerCase();
        Spark.get("/" + playerName + "/percent", (req, res) -> openURLPercent(res, player));
        Spark.get("/" + playerName + "/win", (req, res) -> {
            res.type("text/html; charset=UTF-8");
            PlayerData dataGame = getDataGameFromPlayer(player);
            if (dataGame == null) {
                return buildHtmlResponse("Data not found for player.");
            }
            String text = Messages.transformColorWeb(Messages.getMessage("player_win", "{win}", String.valueOf(dataGame.getWin()), "{max_win}", String.valueOf(Config.getInt("max_win"))));
            return buildHtmlResponse(text);
        });
    }

    private Object openURLPercent(Response res, Player player) {
        res.type("text/html; charset=UTF-8");
        PlayerData dataGame = getDataGameFromPlayer(player);
        if (dataGame == null) {
            return buildHtmlResponse("Data not found for player.");
        }
        String percent = dataGame.getPercentMap();
        return buildHtmlResponse(Messages.transformColorWeb(Messages.getMessage("map_percent", "{percent}", percent)));
    }

    private String buildHtmlResponse(String text) {
        String css = "@import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Roboto+Condensed:ital,wght@0,100..900;1,100..900&display=swap');" +
                "body { font-size: 2.5rem; background-color: transparent; color: black; font-weight: bold; font-family: 'Roboto Condensed', sans-serif; text-transform: uppercase; }";
        String script = "<script>setInterval(function(){ location.reload(); }, 1000);</script>";
        return "<html><head><title>Fresh Agency Runner</title><style>" + css + "</style>" + script + "</head><body>" + text + "</body></html>";
    }

    public void removePlayerToGame(Player player) {
        playerDatas.get(player).setLiveClient(null);
        playerDatas.remove(player);
    }

    public void spawnLightningTowardsPlayer(Player player, int number) {
        Location playerLocation = player.getLocation();
        double playerY = playerLocation.getY();
        int startX = playerLocation.getBlockX() + Config.getInt("wave.wave_lightning");
        World world = player.getWorld();
        Vector pos1 = LocationTransform.deserializeCoordinate(world.getName(), Config.getString("map.pos1")).toVector();
        Vector pos2 = LocationTransform.deserializeCoordinate(world.getName(), Config.getString("map.pos2")).toVector();
        int startZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int endZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        long waveSpeed = Math.max(1, (long) (Config.getFloat("wave.wave_delay") * 20L));

        new BukkitRunnable() {
            final Random random = new Random();
            int currentX = startX;

            @Override
            public void run() {
                if (!player.isOnline() || currentX <= player.getLocation().getBlockX()) {
                    cancel();
                    if (number != 0)
                        getDataGameFromPlayer(player).removeWin(number);
                    resetPlayer(player, false);
                    Location location = player.getLocation().add(1, 1, 0);
                    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1);
                    player.getWorld().spawnParticle(Particle.REDSTONE, location, 1, dustOptions);
                    return;
                }
                for (int z = startZ; z < endZ; z++) {
                    Location lightningLocation = new Location(world, currentX, playerY, z);
                    int highest = world.getHighestBlockYAt(currentX, z);
                    if (z % (random.nextInt(3) + 1) == 0) {
                        lightningLocation.setY(highest);
                        world.strikeLightning(lightningLocation);
                    }
                    if (random.nextInt(Config.getInt("wave.chance_explosion")) == 0) {
                        lightningLocation.setY(highest);
                        TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(lightningLocation, EntityType.PRIMED_TNT);
                        tnt.setFuseTicks(2);
                        tnt.setYield(Config.getFloat("wave.explosion_force"));
                    }
                }
                currentX -= 1;
            }
        }.runTaskTimer(instance, 0, waveSpeed);
    }

    public void resetPlayer(Player player, boolean hasCheckpoint) {
        Location targetLocation = !hasCheckpoint ? player.getWorld().getSpawnLocation() :
                new Location(player.getWorld(), Config.getInt("checkpoint.x"), Config.getInt("checkpoint.y"), Config.getInt("checkpoint.z"));
        targetLocation.setYaw(-90);
        player.teleport(targetLocation);
        player.setHealth(20);
        PlayerData playerData = getDataGameFromPlayer(player);
        playerData.stopTimerEnd();
    }

    public void startTimerEnd(Player player) {
        getDataGameFromPlayer(player).startTimerEnd();
    }

    public void stopTimerEnd(Player player) {
        PlayerData playerData = getDataGameFromPlayer(player);
        if (playerData.isHasStartTimerEnd()) {
            playerData.stopTimerEnd();
        }
    }

    public Player getPlayerFromToken(String token) {
        return playerDatas.values().stream()
                .filter(pd -> pd.getToken().equalsIgnoreCase(token))
                .map(PlayerData::getPlayer)
                .findFirst()
                .orElse(null);
    }
}
