package net.jandie1505.fullserverjoin.listeners;

import net.jandie1505.fullserverjoin.FullServerJoin;
import net.jandie1505.fullserverjoin.utilities.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class LoginHandler implements Listener {
    @NotNull private final FullServerJoin plugin;

    public LoginHandler(@NotNull FullServerJoin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.KICK_FULL) return;

        if (this.plugin.getConfig().getBoolean(ConfigManager.ALWAYS_BYPASS, false)) {
            this.alwaysBypass(event);
        } else {
            this.handleJoin(event);
        }

    }

    private void alwaysBypass(PlayerLoginEvent event) {

        if (this.plugin.canPlayerBypass(event.getPlayer())) {
            event.allow();
            this.plugin.getLogger().log(Level.INFO, "Player " + event.getPlayer().getName() + " (" + event.getPlayer().getUniqueId() + ") has bypassed the player limit (always bypass enabled).");
            return;
        }

        this.handleJoin(event);

    }

    private void handleJoin(PlayerLoginEvent event) {
        int priority = this.plugin.getPlayerPriority(event.getPlayer());
        if (priority <= 0) return;

        Player playerToKick = this.findPlayerToKick(priority);

        if (playerToKick == null) {
            this.noPlayerToKick(event);
            return;
        }

        if (playerToKick.hasPermission(FullServerJoin.PERMISSION_BYPASS_PLAYER_LIMIT)) {
            try {
                Component message = MiniMessage.miniMessage().deserialize(this.plugin.getConfig().getString(ConfigManager.CONFIG_MESSAGE_KICK, ""));
                playerToKick.kick(message);
            } catch (Exception e) {
                playerToKick.kick();
            }

            this.plugin.getLogger().log(Level.INFO, "Player " + playerToKick.getName() + " (" + playerToKick.getUniqueId() + ") has been kicked to make room for " + event.getPlayer().getName() + " (" + event.getPlayer().getUniqueId() + ").");
        }

        event.allow();
    }

    private void noPlayerToKick(PlayerLoginEvent event) {
        if (!this.plugin.canPlayerBypass(event.getPlayer())) return;

        event.allow();
        this.plugin.getLogger().log(Level.INFO, "Player " + event.getPlayer().getName() + " (" + event.getPlayer().getUniqueId() + ") has bypassed the player limit (server full).");
    }

    /**
     * Returns a player with the lowest join priority available on the server.<br/>
     * Returns null if there is no player with a lower join priority than the specified join priority.
     * @param levelOfJoiningPlayer level of the player that tries to join
     * @return player with lower join priority
     */
    @Nullable
    private Player findPlayerToKick(int levelOfJoiningPlayer) {
        Player lowestPriorityPlayer = null;
        int lowestPriority = levelOfJoiningPlayer;

        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            int priority = this.plugin.getPlayerPriority(player);

            if (priority < lowestPriority) {
                lowestPriority = priority;
                lowestPriorityPlayer = player;
            }

        }

        return lowestPriorityPlayer;
    }

    public final @NotNull FullServerJoin getPlugin() {
        return plugin;
    }

}
