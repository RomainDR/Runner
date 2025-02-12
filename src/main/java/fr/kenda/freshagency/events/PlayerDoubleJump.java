package fr.kenda.freshagency.events;

import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.managers.jump.JumpManager;
import fr.kenda.freshagency.utils.Config;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class PlayerDoubleJump implements Listener {

    private final JumpManager jumpManager;
    private final float doubleJumpForce;

    public PlayerDoubleJump() {
        this.jumpManager = FreshAgencyRunner.getInstance().getManagers().getManager(JumpManager.class);
        this.doubleJumpForce = Config.getFloat("double_jump.double_jump_force");
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.SURVIVAL) {
            event.setCancelled(true);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setVelocity(player.getLocation().getDirection().multiply(doubleJumpForce).setY(1));
            jumpManager.removeJump(player);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.SURVIVAL && player.isOnGround() && jumpManager.canJump(player)) {
            player.setAllowFlight(true);
        }
    }
}
