package net.jandie1505.fullserverjoin;

import net.jandie1505.fullserverjoin.commands.JoinInfoCommand;
import net.jandie1505.fullserverjoin.utilities.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FullServerJoin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    public static final String PERMISSION_BYPASS_PLAYER_LIMIT = "fullserverjoin.bypass";
    public static final String PERMISSION_PREFIX_JOIN_LEVEL = "fullserverjoin.level.";
    public static final String PERMISSION_JOIN_LEVEL_HIGHEST = PERMISSION_PREFIX_JOIN_LEVEL + "highest";
    public static final String PERMISSION_COMMAND_GET_JOIN_LEVEL = "fullserverjoin.command.joininfo";

    @NotNull private final ConfigManager configManager;

    public FullServerJoin() {
        this.configManager = new ConfigManager(this);
    }

    @Override
    public void onEnable() {

        // config

        this.configManager.reloadConfig();

        // command

        PluginCommand command = this.getCommand("joininfo");
        if (command != null) {
            JoinInfoCommand cmd = new JoinInfoCommand(this);
            command.setExecutor(cmd);
            command.setTabCompleter(cmd);
        }

        // listener

        getServer().getPluginManager().registerEvents(this, this);

        // info

        this.getLogger().info("Successfully activated FULL SERVER JOIN (version " + this.getDescription().getVersion() + "), created by jandie1505.");

    }

    // ----- LOGIN EVENT -----

    /**
     * Login Event that manages if a player is allowed to join or not.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {

        // Allows players with bypassing permission to join the server under any circumstances
        if (event.getPlayer().hasPermission(PERMISSION_BYPASS_PLAYER_LIMIT)) {
            event.allow();
            return;
        }

        if (event.getResult() != PlayerLoginEvent.Result.KICK_FULL) return;

        int priority = this.getPlayerPriority(event.getPlayer());
        if (priority <= 0) return;

        Player playerToKick = this.findPlayerToKick(priority);

        if (playerToKick != null) {
            playerToKick.kickPlayer(ChatColor.translateAlternateColorCodes('&', this.config.getString(ConfigManager.CONFIG_MESSAGE_KICK, "&cYou have been kicked to make room for a player with higher priority.")));
            event.allow();
        }

    }

    // ----- FIND PLAYER TO KICK -----

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

        for (Player player : this.getServer().getOnlinePlayers()) {
            if (player.hasPermission(PERMISSION_BYPASS_PLAYER_LIMIT)) continue;
            int priority = this.getPlayerPriority(player);

            if (priority < lowestPriority) {
                lowestPriority = priority;
                lowestPriorityPlayer = player;
            }

        }

        return lowestPriorityPlayer;
    }

    // ----- PLAYER JOIN PRIORITY -----

    /**
     * Returns the join priority of the specified player.
     * @param player player
     * @return player priority
     */
    public int getPlayerPriority(@NotNull Player player) {

        if (player.hasPermission(PERMISSION_BYPASS_PLAYER_LIMIT)) return Integer.MAX_VALUE;
        if (player.hasPermission(PERMISSION_JOIN_LEVEL_HIGHEST)) return Integer.MAX_VALUE;

        int priority = 0; // Default priority for players without the permission

        if (player.hasPermission(PERMISSION_PREFIX_JOIN_LEVEL + "highest")) {
            return Integer.MAX_VALUE;
        }

        for (int i = this.config.getInt(ConfigManager.CONFIG_MAX_LEVEL, 10); i >= 0; i--) {
            if (player.hasPermission(PERMISSION_PREFIX_JOIN_LEVEL + i)) {
                return i;
            }
        }

        return priority;
    }

    // ----- CONFIG -----

    @NotNull
    public YamlConfiguration getConfig() {
        return this.configManager.getConfig();
    }

}
