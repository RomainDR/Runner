package fr.kenda.freshagency.events;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.data.PlayerData;
import fr.kenda.freshagency.managers.game.GamePlayerManager;
import fr.kenda.freshagency.utils.Config;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class DisabledEvent implements Listener {

    @EventHandler
    public void onWeather(WeatherChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onFall(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player)
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL)
                e.setCancelled(true);
    }

    @EventHandler
    public void mobSpawn(CreatureSpawnEvent e) {
        if (e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL))
            e.setCancelled(true);
    }

    @EventHandler
    public void onRaidTrigger(RaidTriggerEvent event) {
        event.setCancelled(true);
    }


    @EventHandler
    public void onEntityDamageByLightning(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
            event.setDamage(0);
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity && event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByFire(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || event.getCause() == EntityDamageEvent.DamageCause.LAVA || event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION || event.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            event.setCancelled(true);

            if (event.getEntity() instanceof Player entity) {
                GamePlayerManager gamePlayerManager = FreshAgencyRunner.getInstance().getManagers().getManager(GamePlayerManager.class);

                // Calculer les dégâts restants après l'application des dommages
                double remainingHealth = entity.getHealth() - event.getDamage();

                // Vérifier si les dégâts sont supérieurs à la vie restante du joueur
                if (remainingHealth <= 0) {
                    PlayerData pd = gamePlayerManager.getDataGameFromPlayer(entity);
                    //gamePlayerManager.dropPlayerItems(entity);
                    gamePlayerManager.resetPlayer(entity, pd.isHasCheckpoint());
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            event.setDamage(event.getDamage() / Config.getInt("damage_reduce"));
            ExplosionListener.handle(event, player);
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
        event.setFoodLevel(20);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        event.getDrops().clear();
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        GamePlayerManager gamePlayerManager = FreshAgencyRunner.getInstance().getManagers().getManager(GamePlayerManager.class);
        PlayerData pd = gamePlayerManager.getDataGameFromPlayer(event.getPlayer());
        gamePlayerManager.resetPlayer(event.getPlayer(), pd.isHasCheckpoint());
    }
}
