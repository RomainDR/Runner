package fr.kenda.freshagency.commands;

import fr.kenda.freshagency.utils.Config;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.jetbrains.annotations.NotNull;

public class SpawnMonsterCommand extends BaseCommand {

    private final int forward;

    public SpawnMonsterCommand() {
        super();
        forward = Config.getInt("mount.location_forward");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        printCommand(s, args);

        //if (!hasPermission(commandSender, Permission.PERMISSION_ADMIN)) return false;

        if (!isArgsSufficient(commandSender, args, 1, prefix + "&cCommande: §7/spawnmonster <token>")) return false;

        final Player player = getPlayer(commandSender, args);
        if (player == null || !isInGameWorld(commandSender, player)) return false;

        Location location = player.getLocation().add(forward, 0, 0);
        location.setY(player.getWorld().getHighestBlockYAt(location) + 1);

        Slime slime = player.getWorld().spawn(location, Slime.class);
        slime.setSize(2);

        return false;
    }
}
