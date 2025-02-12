package fr.kenda.freshagency.managers.back;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.utils.Config;
import fr.kenda.freshagency.utils.Messages;
import fr.kenda.freshagency.utils.TitleActionBar;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BackManager extends AbstractBackManager {


    public BackManager(FreshAgencyRunner instance) {
        super(instance);
    }

    @Override
    protected void teleportPlayer(Player player) {
        Location current = player.getLocation();
        Location newLocation = current.clone().add(new Vector(-1, 0, 0));
        Location newLocationHead = newLocation.clone().add(new Vector(0, 1, 0));
        Location newLocationBelow = newLocation.clone().add(new Vector(0, -1, 0));

        // Chercher le point le plus haut de l'axe Y où il n'y a pas de bloc sur deux de hauteur
        while (!newLocation.getBlock().getType().isAir() || !newLocationHead.getBlock().getType().isAir()) {
            newLocation.add(0, 1, 0);
            newLocationHead.add(0, 1, 0);
        }

        // Descendre jusqu'à ce que le bloc en dessous ne soit plus de l'air
        while (newLocationBelow.getBlock().getType().isAir()) {
            newLocation.add(0, -1, 0);
            newLocationBelow.add(0, -1, 0);
            if (newLocation.getY() <= -25) {
                newLocation.setY(current.getY());
                break;
            }
        }

        // Arrondir la coordonnée Y au supérieur
        newLocation.setY(Math.ceil(newLocation.getY()));
        // Orienter vers l'est (EAST)
        newLocation.setYaw(-90);
        newLocation.setPitch(0);

        player.teleport(newLocation);


        int currentX = (int) player.getLocation().getX();
        int departureX = Config.getInt("x_coordinate_start");
        int distance = currentX - departureX;

        String distanceMessage = Config.getString("distance_message", "{distance}", String.valueOf(distance));
        String backMessage = Config.getString("back.back_message", "{count}", String.valueOf(playerNeedBacks.get(player) - playerBacks.get(player).intValue() + 1), "{max_back}", String.valueOf(playerNeedBacks.get(player)));
        TitleActionBar.sendActionBar(player, backMessage + Messages.transformColor(" &8&l|| ") + distanceMessage);
    }


}
