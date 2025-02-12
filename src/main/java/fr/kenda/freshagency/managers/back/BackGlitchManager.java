package fr.kenda.freshagency.managers.back;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.managers.game.GameWorldManager;
import fr.kenda.freshagency.utils.Config;
import fr.kenda.freshagency.utils.Messages;
import fr.kenda.freshagency.utils.TitleActionBar;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

public class BackGlitchManager extends AbstractBackManager {

    private GameWorldManager gwm;

    public BackGlitchManager(FreshAgencyRunner instance) {
        super(instance);
    }

    @Override
    public void register() {
        super.register();
        gwm = instance.getManagers().getManager(GameWorldManager.class);
    }

    @Override
    protected void teleportPlayer(Player player) {
        Location newLocation = player.getLocation().clone().add(-1, 0, 0);
        newLocation.setY(Config.getInt("back_glitch.height"));
        Random random = new Random();
        int randomZ = getRandomBetween();
        Vector glitchVector = new Vector(0, random.nextInt(Config.getInt("back_glitch.space_effect_y")), randomZ);

        if (random.nextInt(5) == 0)
            glitchVector.add(new Vector(random.nextInt(Config.getInt("back_glitch.space_effect_x")), 0, 0));

        newLocation.add(glitchVector);
        Location newLocationHead = player.getLocation().clone().add(0, 1, 0);

        if (!newLocation.getBlock().getType().isAir() || !newLocationHead.getBlock().getType().isAir()) {
            newLocation.setY(player.getWorld().getHighestBlockAt(newLocation).getY());
        }

        if (gwm.isInArea(newLocation)) {
            player.teleport(newLocation);
        } else {
            int z;
            if (newLocation.getX() > 15)
                z = newLocation.getBlockZ() > gwm.getMaxZ() ? (int) gwm.getMaxZ() - 1 : newLocation.getBlockZ() < gwm.getMinZ() ? (int) gwm.getMinZ() + 1 : newLocation.getBlockZ();
            else
                z = newLocation.getBlockZ() > gwm.getMaxZSpawn() ? (int) gwm.getMaxZSpawn() - 1 : newLocation.getBlockZ() < gwm.getMinZSpawn() ? (int) gwm.getMinZSpawn() + 1 : newLocation.getBlockZ();

            Location centerLocation = new Location(player.getWorld(), newLocation.getX(), newLocation.getY(), z, -90, 0);
            player.teleport(centerLocation);
        }

        String errorMessage = Messages.transformColor("&4&l&kERROR 404");
        TitleActionBar.sendActionBar(player, errorMessage);
    }

    private int getRandomBetween() {
        Random random = new Random();
        final int maxValue = Config.getInt("back_glitch.space_effect_z");
        final int minValue = -Config.getInt("back_glitch.space_effect_z");
        return random.nextInt(maxValue - minValue) + minValue;
    }
}
