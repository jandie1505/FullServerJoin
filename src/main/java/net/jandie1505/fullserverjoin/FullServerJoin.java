package net.jandie1505.fullserverjoin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class FullServerJoin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event) {

        if (event.getResult() != PlayerLoginEvent.Result.KICK_FULL) {
            return;
        }

        int playerPriority = getPlayerPriority(event.getPlayer());

        for (Player otherPlayer : this.getServer().getOnlinePlayers()) {

            int otherPlayerPriority = getPlayerPriority(otherPlayer);

            if (otherPlayerPriority < playerPriority) {
                // Kick other player to make room for the joining player
                otherPlayer.kickPlayer("§cYou have been kicked to make room for a player with higher priority.");
                event.allow(); // Allow the joining player to join
                return;
            }

        }

    }

    private int getPlayerPriority(Player player) {
        int defaultPriority = 0; // Default priority for players without the permission
        String permissionPrefix = "fullserverjoin.level.";

        if (player == null) {
            return 0;
        }

        if (player.hasPermission(permissionPrefix + "highest")) {
            return Integer.MAX_VALUE;
        }

        for (int i = 10; i >= 0; i--) {
            if (player.hasPermission(permissionPrefix + i)) {
                return i;
            }
        }

        return defaultPriority;
    }
}
