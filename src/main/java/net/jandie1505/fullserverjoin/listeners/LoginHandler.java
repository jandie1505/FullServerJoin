package net.jandie1505.fullserverjoin.listeners;

import net.jandie1505.fullserverjoin.FullServerJoin;
import net.jandie1505.fullserverjoin.utilities.ConfigManager;
import net.jandie1505.fullserverjoin.utilities.TempBypassData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class LoginHandler implements Listener {
    @NotNull private final FullServerJoin plugin;

    public LoginHandler(@NotNull FullServerJoin plugin) {
        this.plugin = plugin;
    }

    // ----- LOGIN -----

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

        if (this.plugin.getPlayerBypassStatus(event.getPlayer()).isBypass()) {
            event.allow();
            this.plugin.getLogger().log(Level.INFO, "Player " + event.getPlayer().getName() + " (" + event.getPlayer().getUniqueId() + ") has bypassed the player limit (always bypass enabled).");
            this.handleTempBypass(event);
            return;
        }

        this.handleJoin(event);

    }

    private void handleJoin(PlayerLoginEvent event) {
        int priority = this.plugin.getPlayerPriority(event.getPlayer());

        Player playerToKick = this.findPlayerToKick(priority);

        if (playerToKick == null) {
            this.noPlayerToKick(event);
            return;
        }

        try {
            Component message = MiniMessage.miniMessage().deserialize(this.plugin.getConfig().getString(ConfigManager.CONFIG_MESSAGE_KICK, ""));
            playerToKick.kick(message);
        } catch (Exception e) {
            playerToKick.kick();
        }

        event.allow();
        this.plugin.getLogger().log(Level.INFO, "Player " + playerToKick.getName() + " (" + playerToKick.getUniqueId() + ") has been kicked to make room for " + event.getPlayer().getName() + " (" + event.getPlayer().getUniqueId() + ").");
    }

    private void noPlayerToKick(PlayerLoginEvent event) {
        if (!this.plugin.getPlayerBypassStatus(event.getPlayer()).isBypass()) return;

        event.allow();
        this.plugin.getLogger().log(Level.INFO, "Player " + event.getPlayer().getName() + " (" + event.getPlayer().getUniqueId() + ") has bypassed the player limit (server full).");
        this.handleTempBypass(event);
    }

    private void handleTempBypass(PlayerLoginEvent event) {

        TempBypassData bypassData = this.plugin.getTemporaryBypassPlayers().get(event.getPlayer().getUniqueId());
        if (bypassData != null) {
            bypassData.setUsed(true);
        }

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
            if (this.plugin.getPlayerBypassStatus(player).isBypass()) continue;
            int priority = this.plugin.getPlayerPriority(player);

            if (priority < lowestPriority) {
                lowestPriority = priority;
                lowestPriorityPlayer = player;
            }

        }

        return lowestPriorityPlayer;
    }

    // ----- LOGOUT -----

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.plugin.getTemporaryBypassPlayers().remove(event.getPlayer().getUniqueId());
    }

    public final @NotNull FullServerJoin getPlugin() {
        return plugin;
    }

}
