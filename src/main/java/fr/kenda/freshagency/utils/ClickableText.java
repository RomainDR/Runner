package fr.kenda.freshagency.utils;


import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClickableText {

    @SuppressWarnings("all")
    public static void makeTextOpenLink(String msg, String hoverMessage, String link, CommandSender sender) {
        TextComponent text = new TextComponent(Messages.transformColor(msg));
        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Messages.transformColor(hoverMessage)).create()));
        text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));

        if (sender instanceof Player)
            ((Player) sender).spigot().sendMessage(text);
        else
            sender.sendMessage(msg);
    }
}
