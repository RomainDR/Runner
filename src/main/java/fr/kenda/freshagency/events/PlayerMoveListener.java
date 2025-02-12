package fr.kenda.freshagency.events;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.data.PlayerData;
import fr.kenda.freshagency.managers.back.BackGlitchManager;
import fr.kenda.freshagency.managers.back.BackManager;
import fr.kenda.freshagency.managers.game.GamePlayerManager;
import fr.kenda.freshagency.managers.game.GameWorldManager;
import fr.kenda.freshagency.managers.jump.JumpManager;
import fr.kenda.freshagency.utils.Config;
import fr.kenda.freshagency.utils.TitleActionBar;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerMoveListener implements Listener {

    private final GameWorldManager gameWorldManager;
    private final BackManager backManager;
    private final BackGlitchManager backGlitchManager;
    private final GamePlayerManager gamePlayerManager;
    private final JumpManager jumpManager;
    private final Material pressurePlate;

    private final FreshAgencyRunner instance;
    private final int checkpointX;
    private final int maximumVoidY;
    private final String distanceMessageTemplate;
    private final String distanceDashMessageTemplate;
    private final int xCoordinateStart;

    public PlayerMoveListener() {
        instance = FreshAgencyRunner.getInstance();
        gameWorldManager = instance.getManagers().getManager(GameWorldManager.class);
        backManager = instance.getManagers().getManager(BackManager.class);
        backGlitchManager = instance.getManagers().getManager(BackGlitchManager.class);
        gamePlayerManager = instance.getManagers().getManager(GamePlayerManager.class);
        jumpManager = instance.getManagers().getManager(JumpManager.class);
        pressurePlate = Config.getMaterial("pressure_plate");

        checkpointX = Config.getInt("checkpoint.x");
        maximumVoidY = Config.getInt("maximum_void_y");
        distanceMessageTemplate = Config.getString("distance_message");
        distanceDashMessageTemplate = Config.getString("dash_message");
        xCoordinateStart = Config.getInt("x_coordinate_start");
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (!gameWorldManager.isWorldContainsInGame(player.getWorld())) {
            return;
        }

        PlayerData playerData = gamePlayerManager.getDataGameFromPlayer(player);

        if (playerData.isInStartGame()) {
            e.setCancelled(true);
            return;
        }

        // Vérifie si le joueur a changé de bloc en X ou Z
        if (hasChangedBlock(e) && playerData.isCanUpdateStat()) {
            playerData.addBlockWalk();
        }

        checkPressurePlate(player, e, playerData);
        checkIfWinArea(player, playerData);
        showDistance(player);
        teleportIfIsVoid(player, playerData);
        setIfCheckpoint(player, playerData);
    }

    private boolean hasChangedBlock(PlayerMoveEvent e) {
        // Vérifie si le joueur a changé de bloc en X
        int fromX = e.getFrom().getBlockX();
        int toX = e.getTo().getBlockX();

        if (toX - fromX < 0) {
            return false;
        }

        return fromX != toX;
    }


    private void checkIfWinArea(Player player, PlayerData playerData) {
        if (gameWorldManager.getWinArea().containsInCoordinate(player.getLocation())) {
            playerData.addWin(1);
            gamePlayerManager.resetPlayer(player, false);
            new BukkitRunnable() {
                @Override
                public void run() {
                    playFirework(player);
                }
            }.runTaskLater(instance, 10L);
        }
    }


    private void playFirework(Player player) {
        Location location = player.getLocation();
        Firework firework = location.getWorld().spawn(location.add(0, 1, 0), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect.Type type = FireworkEffect.Type.valueOf(Config.getString("firework.type"));

        List<Color> colors = new ArrayList<>();
        for (String color : Config.getList("firework.colors"))
            colors.add(getColorFromRGB(parseColor(color)));


        FireworkEffect.Builder effectBuild = FireworkEffect.builder()
                .withColor(colors)
                .with(type)
                .trail(Config.getBoolean("firework.trail"))
                .flicker(Config.getBoolean("firework.flicker"));

        if (Config.getBoolean("firework.fade.active")) {
            int[] rgbFade = parseColor(Config.getString("firework.fade.color"));
            effectBuild.withFade(Color.fromRGB(rgbFade[0], rgbFade[1], rgbFade[2]));
        }

        meta.addEffect(effectBuild.build());
        meta.setPower(Config.getInt("firework.power"));
        firework.setFireworkMeta(meta);
        firework.detonate();

    }

    private int[] parseColor(String colorString) {
        return Arrays.stream(colorString.split(";"))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    private Color getColorFromRGB(int[] rgb) {
        return Color.fromRGB(rgb[0], rgb[1], rgb[2]);
    }


    private double roundToHalf(double value) {
        return Math.round(value * 2) / 2.0;
    }

    private void checkPressurePlate(Player player, PlayerMoveEvent e, PlayerData playerData) {
        if (playerData.isHasStartTimerEnd()) {
            if (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getZ() != e.getTo().getZ()) {
                e.setCancelled(true);
            }
            return;
        }

        Block blockAtFeet = e.getTo().getBlock();

        if (blockAtFeet.getType() == pressurePlate) {
            // Téléportation du joueur avec coordonnées arrondies
            Location targetLocation = new Location(player.getWorld(),
                    roundToHalf(blockAtFeet.getX()),
                    player.getLocation().getY(),
                    roundToHalf(blockAtFeet.getZ()),
                    player.getLocation().getYaw(),
                    player.getLocation().getPitch()
            );

            player.teleport(targetLocation);
            gamePlayerManager.startTimerEnd(player);
        } else {
            gamePlayerManager.stopTimerEnd(player);
        }
    }

    private void showDistance(Player player) {
        int currentX = (int) player.getLocation().getX();
        int distance = currentX - xCoordinateStart;

        if (jumpManager.canJump(player)) {
            String dash = distanceDashMessageTemplate.replace("{count_dash}", String.valueOf(jumpManager.getJumpLeft(player)));
            dash = dash.replace("{distance_message}", distanceMessageTemplate.replace("{distance}", String.valueOf(distance)));
            TitleActionBar.sendActionBar(player, dash);
            return;
        }

        if (!backManager.isInBack(player) && !backGlitchManager.isInBack(player)) {
            TitleActionBar.sendActionBar(player, distanceMessageTemplate.replace("{distance}", String.valueOf(distance)));
        }


    }

    private void teleportIfIsVoid(Player player, PlayerData playerData) {
        if (player.getLocation().getY() <= maximumVoidY) {
            gamePlayerManager.resetPlayer(player, playerData.isHasCheckpoint());
        }
    }

    private void setIfCheckpoint(Player player, PlayerData playerData) {
        double playerX = player.getLocation().getX();
        boolean hasCheckpoint = playerData.isHasCheckpoint();

        if (playerX >= checkpointX && !hasCheckpoint) {
            playerData.setCheckpoint(true);
        } else if (playerX < checkpointX && hasCheckpoint) {
            playerData.setCheckpoint(false);
        }
    }
}
