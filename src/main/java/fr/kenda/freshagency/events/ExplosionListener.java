package fr.kenda.freshagency.events;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.blocks.BlockSnap;
import fr.kenda.freshagency.data.PlayerData;
import fr.kenda.freshagency.managers.game.GamePlayerManager;
import fr.kenda.freshagency.managers.game.GameWorldManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;

public class ExplosionListener implements Listener {

    private static GamePlayerManager gamePlayerManager = null;
    private final GameWorldManager gameWorldManager;

    public ExplosionListener() {
        gameWorldManager = FreshAgencyRunner.getInstance().getManagers().getManager(GameWorldManager.class);
        gamePlayerManager = FreshAgencyRunner.getInstance().getManagers().getManager(GamePlayerManager.class);
    }

   /* @EventHandler
    public void onExplode(EntityExplodeEvent e) {

        e.blockList().removeIf(block -> {
            if (block.getType() == Material.AIR) {
                return true;
            }
            if (block.getType() == Config.getMaterial("pressure_plate")) {
                Block b = block.getLocation().clone().add(new Vector(0, -1, 0)).getBlock();
                e.blockList().remove(b);
            }

            BlockSnap blockSnap = new BlockSnap(block);
            if (gameWorldManager.isBlockContainsInProtectedArea(blockSnap))
                return true;

            if (gameWorldManager.isBlockContainsInOriginalMap(blockSnap)) {
                gameWorldManager.addBlockToRestore(blockSnap);
            }

            block.setType(Material.AIR);
            gamePlayerManager.getDataGameFromPlayer(e.getEntity().getWorld().getPlayers().get(0)).updatePercent();

            return true;
        });
    }*/

    public static void handle(EntityDamageByEntityEvent event, Player entity) {
        // Calculer les dégâts restants après l'application des dommages
        double remainingHealth = entity.getHealth() - 1;

        // Vérifier si les dégâts sont inférieurs à 0.5
        if (remainingHealth < 0.5) {
            PlayerData pd = gamePlayerManager.getDataGameFromPlayer(entity);
            //gamePlayerManager.dropPlayerItems(entity);
            gamePlayerManager.resetPlayer(entity, pd.isHasCheckpoint());
            remainingHealth = entity.getMaxHealth(); // Réinitialiser la vie du joueur
        }
        event.setDamage(0);
        entity.setHealth(remainingHealth);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        List<Block> blocksToRemove = new ArrayList<>();
        for (Block block : e.blockList()) {
            if (block.getType() == Material.AIR) {
                blocksToRemove.add(block);
                continue;
            }

            // Vérifie si le bloc est une plaque de pression
            if (block.getType() == Material.STONE_PRESSURE_PLATE ||
                    block.getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE ||
                    block.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {

                // Effectue une action spécifique pour les plaques de pression
                blocksToRemove.add(block); // Exemple : les ajouter à une liste pour les supprimer
                continue;
            }

            BlockSnap blockSnap = new BlockSnap(block);
            if (gameWorldManager.isBlockContainsInProtectedArea(blockSnap)) {
                blocksToRemove.add(block);
                continue;
            }

            if (gameWorldManager.isBlockContainsInOriginalMap(blockSnap)) {
                gameWorldManager.addBlockToRestore(blockSnap);
            }

            block.setType(Material.AIR);
            gamePlayerManager.getDataGameFromPlayer(e.getEntity().getWorld().getPlayers().get(0)).updatePercent();
            blocksToRemove.add(block);
        }
        e.blockList().removeAll(blocksToRemove);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Cas où le "damager" est un Slime
        if (event.getDamager() instanceof Slime slime && event.getEntity() instanceof Player entity) {
            // Vérifie la taille du Slime (taille 2 pour les mini-slimes)
            if (slime.getSize() == 2) {
                Location location = slime.getLocation();
                slime.remove(); // Supprime le slime après l'explosion
                slime.getWorld().createExplosion(location, 1.0F); // Crée une petite explosion (1.0F)

                // Appelle une méthode de traitement (assurez-vous qu'elle gère correctement les entités)
                handle(event, entity);
            }
        }

        // Cas où le "damager" est une TNT (TNTPrimed)
        if (event.getDamager() instanceof TNTPrimed && event.getEntity() instanceof Player entity) {
            // Appelle la même méthode de traitement
            handle(event, entity);
        }
    }

}