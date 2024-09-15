package net.jandie1505.fullserverjoin;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FullServerJoin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    public static final String PERMISSION_BYPASS_PLAYER_LIMIT = "fullserverjoin.bypass";
    public static final String PERMISSION_PREFIX_JOIN_LEVEL = "fullserverjoin.level.";
    public static final String PERMISSION_JOIN_LEVEL_HIGHEST = PERMISSION_PREFIX_JOIN_LEVEL + "highest";
    public static final String PERMISSION_COMMAND_GET_JOIN_LEVEL = "fullserverjoin.command.joininfo";

    private static final String CONFIG_MAX_LEVEL = "max_level";
    private static final String CONFIG_MESSAGE_KICK = "kick_message";
    private static final String CONFIG_MESSAGE_NO_PERMISSION = "no_permission_message";

    private YamlConfiguration config;

    @Override
    public void onEnable() {

        // config

        this.config = new YamlConfiguration();
        this.reloadConfig();

        // command

        PluginCommand command = this.getCommand("joininfo");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }

        // listener

        getServer().getPluginManager().registerEvents(this, this);

        // info

        this.getLogger().info("Successfully activated FULL SERVER JOIN (version " + this.getDescription().getVersion() + "), created by jandie1505.");

    }

    @Override
    public void onDisable() {
        this.config = null;
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
            playerToKick.kickPlayer(ChatColor.translateAlternateColorCodes('&', this.config.getString(CONFIG_MESSAGE_KICK, "&cYou have been kicked to make room for a player with higher priority.")));
            event.allow();
        }

    }

    // ----- COMMAND -----

    /**
     * Join info command executor.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission(PERMISSION_COMMAND_GET_JOIN_LEVEL)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString(CONFIG_MESSAGE_NO_PERMISSION, "&cNo permission")));
            return true;
        }

        Player player = null;

        if (args.length > 0) {
            player = this.getPlayerFromString(args[0]);
        } else {
            if (sender instanceof Player p) player = p;
        }

        if (player == null) {
            sender.sendMessage("§cPlayer does not exist");
            return true;
        }

        sender.sendMessage("§7Join information about " + player.getName() + ":§r\n" +
                "§7- Priority: " + this.getPlayerPriority(player) + "§r\n" +
                "§7- Bypass permission: " + player.hasPermission(PERMISSION_BYPASS_PLAYER_LIMIT) + "§r"
        );

        return true;
    }

    /**
     * Join info command tab completer.
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PERMISSION_COMMAND_GET_JOIN_LEVEL)) return List.of();
        if (args.length == 1) return List.copyOf(this.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        return List.of();
    }

    /**
     * Returns a Player from a string.
     * @param str player name/uuid string
     * @return player or null
     */
    @Nullable
    private Player getPlayerFromString(String str) {
        Player player = null;
        try {
            player = this.getServer().getPlayer(UUID.fromString(str));
        } catch (IllegalArgumentException e) {
            player = this.getServer().getPlayer(str);
        }
        return player;
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

        for (int i = this.config.getInt(CONFIG_MAX_LEVEL, 10); i >= 0; i--) {
            if (player.hasPermission(PERMISSION_PREFIX_JOIN_LEVEL + i)) {
                return i;
            }
        }

        return priority;
    }

    // ----- CONFIG -----

    @NotNull
    public YamlConfiguration getConfig() {
        return this.config != null ? this.config : new YamlConfiguration();
    }

    public void reloadConfig() {

        try {

            File configFile = new File(this.getDataFolder(), "config.yml");

            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();

                this.config.set(CONFIG_MAX_LEVEL, 10);
                this.config.setComments(CONFIG_MAX_LEVEL, List.of(
                        "The maximum level for join permissions.",
                        "Levels which are higher than this value will be ignored."
                ));

                this.config.set(CONFIG_MESSAGE_KICK, "&cYou have been kicked to make room for a player with higher priority");
                this.config.setComments(CONFIG_MESSAGE_KICK, List.of("Players will see this message when getting kicked to make room for a player with higher priority."));

                this.config.set(CONFIG_MESSAGE_NO_PERMISSION, "&cNo permission");
                this.config.setComments(CONFIG_MESSAGE_NO_PERMISSION, List.of("The message players see when they have no permission to use plugin commands."));

                this.config.save(new File(this.getDataFolder(), "config.yml"));
            }

            try {
                this.config.load(new File(this.getDataFolder(), "config.yml"));
            } catch (InvalidConfigurationException e) {
                this.getLogger().warning("Invalid configuration. Using defaults.");
            }

        } catch (IOException e) {
            this.getLogger().warning("Could not access config file. Using defaults.");
        }

    }

}
