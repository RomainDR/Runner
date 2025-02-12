package fr.kenda.freshagency.utils;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.managers.FileManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Messages {
    private static final FreshAgencyRunner instance = FreshAgencyRunner.getInstance();

    /**
     * Get prefix of plugin
     *
     * @return String
     */
    public static String getPrefix() {
        return transformColor(instance.getConfig().getString("prefix") + " ");
    }

    /**
     * get message from Message file
     *
     * @param path String
     * @param args String...
     * @return String
     */
    public static String getMessage(String path, String... args) {
        FileConfiguration config = instance.getManagers().getManager(FileManager.class).getConfigFrom("messages");
        if (config == null)
            return transformColor("&c[Messages] File messages doesn't exist. Relaunch or restore file before execute this command.");


        String message = config.getString(path);
        if (message == null) return "[Messages] Path '" + path + "' not found in messages.yml";

        int size = args.length - 1;
        for (int i = 0; i < size; i += 2)
            message = message.replace(args[i], args[i + 1]);

        return transformColor(message);

    }

    /**
     * Transform message with color
     *
     * @param message String
     * @return String
     */
    public static String transformColor(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Transform message with color to web
     *
     * @param message String
     * @return String
     */
    public static String transformColorWeb(String message) {
        message = message.replaceAll("§0", "<span style='color: #000000;'>")
                .replaceAll("§1", "<span style='color: #0000AA;'>")
                .replaceAll("§2", "<span style='color: #00AA00;'>")
                .replaceAll("§3", "<span style='color: #00AAAA;'>")
                .replaceAll("§4", "<span style='color: #AA0000;'>")
                .replaceAll("§5", "<span style='color: #AA00AA;'>")
                .replaceAll("§6", "<span style='color: #FFAA00;'>")
                .replaceAll("§7", "<span style='color: #AAAAAA;'>")
                .replaceAll("§8", "<span style='color: #555555;'>")
                .replaceAll("§9", "<span style='color: #5555FF;'>")
                .replaceAll("§a", "<span style='color: #55FF55;'>")
                .replaceAll("§b", "<span style='color: #55FFFF;'>")
                .replaceAll("§c", "<span style='color: #FF5555;'>")
                .replaceAll("§d", "<span style='color: #FF55FF;'>")
                .replaceAll("§e", "<span style='color: #FFFF55;'>")
                .replaceAll("§f", "<span style='color: #FFFFFF;'>");

        // Remplacement des styles de texte
        message = message.replaceAll("§l", "<span style='font-weight: bold;'>")
                .replaceAll("§n", "<span style='text-decoration: underline;'>")
                .replaceAll("§o", "<span style='font-style: italic;'>")
                .replaceAll("§m", "<span style='text-decoration: line-through;'>");

        // Réinitialisation (fermeture des balises)
        message = message.replaceAll("§r", "</span>");

        // S'assurer que toutes les balises sont fermées correctement
        return message + "</span>".repeat(message.split("<span").length - message.split("</span>").length);

    }


    public static List<String> getMessageList(String path) {
        FileConfiguration config = instance.getManagers().getManager(FileManager.class).getConfigFrom("messages");
        List<String> list = config.getStringList(path);
        List<String> listColored = new ArrayList<>();
        for (String str : list)
            listColored.add(transformColor(str));
        return listColored;
    }
}
