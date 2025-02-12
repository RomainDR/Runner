package fr.kenda.freshagency.commands;

import fr.kenda.freshagency.FreshAgencyRunner;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class DebugCommand implements CommandExecutor {

    // Utilisation de short pour limiter la consommation de mémoire
    private final Map<LocalDateTime, Short> memoryMapMinutes = new HashMap<>();
    private final Map<LocalDateTime, Short> eachMemoryMap = new HashMap<>();
    private final String webhookUrl = "https://discord.com/api/webhooks/1298687744050204673/0X5ijETgu5qX4fwEh6TmJa027AklGz-74RIrhnGfQ8UzTdbGCocGYOgMFtw8pLE7ZyGe";

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length != 2) {
            commandSender.sendMessage("Usage: /startstat <duration_in_minutes> <eachTime_in_minutes>");
            return false;
        }

        int duration;
        int eachTime;
        try {
            duration = Integer.parseInt(args[0]);
            eachTime = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            commandSender.sendMessage("La durée et eachTime doivent être des nombres.");
            return false;
        }

        startMemoryTrackingAsync(duration, eachTime);
        commandSender.sendMessage("Suivi de la mémoire démarré pour " + duration + " minutes, envoi toutes les " + eachTime + " minutes.");
        return true;
    }

    private void startMemoryTrackingAsync(int duration, int eachTime) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        AtomicInteger secondsPassed = new AtomicInteger(0);

        scheduler.runTaskTimerAsynchronously(FreshAgencyRunner.getInstance(), () -> {

            if (secondsPassed.get() >= duration * 60) {
                return;
            }

            CompletableFuture.runAsync(() -> {
                short usedMemoryMb = (short) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
                LocalDateTime now = LocalDateTime.now();
                memoryMapMinutes.put(now, usedMemoryMb);

                if (secondsPassed.get() % 60 == 0 && !memoryMapMinutes.isEmpty()) {
                    // Trouver la valeur max dans la map
                    short maxMemoryMb = (short) memoryMapMinutes.values().stream().mapToInt(Short::shortValue).max().orElse((short) 0);
                    eachMemoryMap.put(now, maxMemoryMb);
                    memoryMapMinutes.clear();
                }

                if (secondsPassed.get() % (eachTime * 60) == 0 && !eachMemoryMap.isEmpty()) {
                    short maxEachMemoryMb = (short) eachMemoryMap.values().stream().mapToInt(Short::shortValue).max().orElse((short) 0);
                    sendToWebhookAsync(now, maxEachMemoryMb);
                    eachMemoryMap.clear();
                }

                secondsPassed.getAndIncrement();
            });

        }, 0L, 20L);
    }

    private void sendToWebhookAsync(LocalDateTime time, short memoryMb) {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH'h'mm");
                String formattedTime = time.format(formatter);

                String jsonPayload = "{\"content\":\"Timecode: " + formattedTime + "\\nHigh Memory: " + memoryMb + "Mb\"}";

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == 204) {
                    Bukkit.getLogger().info("Message envoyé avec succès au webhook.");
                } else {
                    Bukkit.getLogger().warning("Échec de l'envoi du message au webhook. Code de réponse: " + responseCode);
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("Erreur lors de l'envoi au webhook: " + e.getMessage());
            }
        });
    }
}
