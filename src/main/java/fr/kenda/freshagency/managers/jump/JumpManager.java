package fr.kenda.freshagency.managers.jump;

import fr.kenda.freshagency.managers.IManager;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class JumpManager implements IManager {

    private HashMap<Player, Integer> jumpLeftByPlayer;

    @Override
    public void register() {
        jumpLeftByPlayer = new HashMap<>();
    }

    public void addJump(Player player, int number) {
        jumpLeftByPlayer.put(player, jumpLeftByPlayer.getOrDefault(player, 0) + number);
    }

    public void removeJump(Player player) {
        jumpLeftByPlayer.put(player, jumpLeftByPlayer.getOrDefault(player, 0) - 1);
    }

    /**
     * TODO
     * Add jump left to action bar
     */
    public final int getJumpLeft(Player player) {
        return jumpLeftByPlayer.getOrDefault(player, 0);
    }

    public final boolean canJump(Player player) {
        return jumpLeftByPlayer.getOrDefault(player, 0) > 0;
    }
}
