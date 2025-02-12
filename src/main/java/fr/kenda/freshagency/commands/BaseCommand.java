package fr.kenda.freshagency.commands;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.managers.game.GamePlayerManager;
import fr.kenda.freshagency.managers.game.GameWorldManager;
import fr.kenda.freshagency.utils.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class BaseCommand implements CommandExecutor {

    protected final GameWorldManager gwm;
    protected final GamePlayerManager gpm;
    protected final String prefix;
    protected final FreshAgencyRunner instance;

    public BaseCommand() {
        instance = FreshAgencyRunner.getInstance();
        gwm = instance.getManagers().getManager(GameWorldManager.class);
        gpm = instance.getManagers().getManager(GamePlayerManager.class);
        prefix = Messages.getPrefix();
    }

    protected boolean hasPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(Messages.transformColor(prefix + Messages.getMessage("no_permission")));
            return false;
        }
        return true;
    }

    protected Player getPlayer(CommandSender sender, String[] args) {
        final String token = args[args.length - 1];
        Player player = gpm.getPlayerFromToken(token);
        if (player == null)
            sender.sendMessage(Messages.transformColor(prefix + Messages.getMessage("player_invalid", "{token}", token)));
        return player;
    }

    protected boolean isInGameWorld(CommandSender sender, Player player) {
        if (!gwm.isWorldContainsInGame(player.getWorld())) {
            sender.sendMessage(Messages.transformColor(prefix + Messages.getMessage("need_in_game_world")));
            return false;
        }
        return true;
    }

    protected boolean isArgsSufficient(CommandSender commandSender, String[] args, int count, String msgDeny) {
        if (args.length != count) {
            commandSender.sendMessage(Messages.transformColor(prefix + msgDeny));
            return false;
        }
        return true;
    }

    public void printCommand(String cmd, String[] args) {
        String arguments = String.join(" ", args);
        instance.getServer().getConsoleSender().sendMessage("[Command] /" + cmd + " " + arguments);
    }

    @Override
    public abstract boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args);
}
