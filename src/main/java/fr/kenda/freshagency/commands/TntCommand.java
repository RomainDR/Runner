package fr.kenda.freshagency.commands;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.utils.Config;
import fr.kenda.freshagency.utils.Messages;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class TntCommand extends BaseCommand {

    private final Random random = new Random();
    private final int maxTntSpawn = Config.getInt("tnt.max_tnt_spawn_one_time");
    private final int groundDistanceX = Config.getInt("tnt.tnt_ground.rangeX");
    private final int groundDistanceZ = Config.getInt("tnt.tnt_ground.rangeZ");
    private final int rainDistanceX = Config.getInt("tnt.tnt_rain.rangeX");
    private final int rainDistanceZ = Config.getInt("tnt.tnt_rain.rangeZ");
    private final int tntSpawnHeightGround = Config.getInt("tnt.tnt_ground.tnt_spawn_height");
    private final int tntSpawnHeightRain = Config.getInt("tnt.tnt_rain.tnt_spawn_height");
    private final int delayExplodeGround = Config.getInt("tnt.tnt_ground.delay_explode") * 20;
    private final int delayExplodeRain = Config.getInt("tnt.tnt_rain.delay_explode") * 20;
    private final int delayBetweenRain = Config.getInt("tnt.tnt_rain.delay_between_rain") * 20;
    private final int numberToSpawn = Config.getInt("tnt.tnt_rain.number_to_spawn");
    private final int distance = Config.getInt("tnt.distance");

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        printCommand(s, args);

        //if (!hasPermission(commandSender, Permission.PERMISSION_ADMIN)) return false;

        if (!isArgsSufficient(commandSender, args, 3, prefix + "&cCommande: §7/tnt <count> <giftCount> <token>"))
            return false;

        final Player player = getPlayer(commandSender, args);
        if (player == null) return false;

        if (!isInGameWorld(commandSender, player)) return false;

        try {
            int number = Integer.parseInt(args[0]);
            int giftCount = Integer.parseInt(args[1]);

            final int total = number * giftCount;

            if (groundDistanceX <= 0 || groundDistanceZ <= 0) {
                commandSender.sendMessage(Messages.getPrefix() + " §cLes valeurs de tnt_ground.rangeX et tnt_ground.rangeZ doivent être positives.");
                return false;
            }

            Location playerLoc = player.getLocation().add(distance, 0, 0);

            if (total > maxTntSpawn) {
                spawnTntRain(player, total);
            } else {
                spawnTntGround(player, total, playerLoc);
            }

        } catch (NumberFormatException e) {
            commandSender.sendMessage(Messages.transformColor(prefix + Messages.getMessage("number_invalid")));
            return false;
        }
        return true;
    }

    private void spawnTntRain(Player player, int number) {
        new BukkitRunnable() {
            int spawned = 0;

            @Override
            public void run() {
                if (spawned >= number || !player.isOnline()) {
                    this.cancel();
                    return;
                }
                Location currentLoc = player.getLocation();
                for (int j = 0; j < numberToSpawn && spawned < number; j++) {
                    spawnTnt(player, rainDistanceX, rainDistanceZ, tntSpawnHeightRain, delayExplodeRain);
                    spawned++;
                }
            }
        }.runTaskTimer(FreshAgencyRunner.getInstance(), 0L, delayBetweenRain);
    }

    private void spawnTntGround(Player player, int number, Location playerLoc) {
        for (int i = 0; i < number; i++) {
            spawnTnt(player, groundDistanceX, groundDistanceZ, tntSpawnHeightGround, delayExplodeGround);
        }
    }

    private void spawnTnt(Player player, int rangeX, int rangeZ, int spawnHeight, int fuseTicks) {
        final Location loc = player.getLocation();

        double randomX = loc.getX() + (random.nextInt(rangeX * 2) - rangeX);
        double randomZ = loc.getZ() + (random.nextInt(rangeZ * 2) - rangeZ);
        int highestY = player.getWorld().getHighestBlockYAt((int) randomX, (int) randomZ);

        Location tntLoc = new Location(player.getWorld(), randomX, highestY + spawnHeight, randomZ);
        TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(tntLoc, EntityType.PRIMED_TNT);
        tnt.setFuseTicks(fuseTicks);
    }
}
