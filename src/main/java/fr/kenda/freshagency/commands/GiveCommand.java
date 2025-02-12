package fr.kenda.freshagency.commands;

import fr.kenda.freshagency.utils.ItemBuilder;
import fr.kenda.freshagency.utils.Messages;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GiveCommand extends BaseCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        printCommand(s, args);
        //if (!hasPermission(commandSender, Permission.PERMISSION_ADMIN)) return false;

        if (!isArgsSufficient(commandSender, args, 3, prefix + "&cCommande: §7/give <item> <amount> <token>"))
            return false;
        final Player player = getPlayer(commandSender, args);
        if (player == null) return false;

        if (!isInGameWorld(commandSender, player)) return false;


        Material material = Material.getMaterial(args[0].toUpperCase());
        int amount = parseAmount(commandSender, args[1]);
        if (material == null || amount <= 0) {
            commandSender.sendMessage(prefix + Messages.getMessage("item_not_exist"));
            return false;
        }

        int sizeRemaining = calculateRemainingInventorySpace(player);
        player.getInventory().addItem(new ItemBuilder(material, Math.min(amount, sizeRemaining)).toItemStack());

        return false;
    }

    private int parseAmount(CommandSender commandSender, String amountStr) {
        try {
            return Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(prefix + "&cInvalid amount: " + amountStr);
            return -1;
        }
    }

    private int calculateRemainingInventorySpace(Player player) {
        ItemStack[] inv = player.getInventory().getContents();
        int sizeRemaining = 0;
        for (ItemStack itemStack : inv) {
            if (itemStack == null) {
                sizeRemaining += 64;
            } else {
                sizeRemaining += (64 - itemStack.getAmount());
            }
        }
        return sizeRemaining;
    }
}
