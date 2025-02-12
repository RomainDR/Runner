package fr.kenda.freshagency.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class TitleActionBar {


    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    public static void sendTitle(Player player, String title, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(Messages.transformColor(title), "", fadeIn * 20, stay * 20, fadeOut * 20);
    }

    public static void sendTitleAndSubtitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(Messages.transformColor(title), Messages.transformColor(subTitle), fadeIn * 20, stay * 20, fadeOut * 20);
    }

}
