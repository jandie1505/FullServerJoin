package net.jandie1505.fullserverjoin;

import net.chaossquad.mclib.command.SubcommandCommand;
import net.chaossquad.mclib.command.SubcommandEntry;
import net.jandie1505.fullserverjoin.commands.JoinInfoCommand;
import net.jandie1505.fullserverjoin.listeners.LoginHandler;
import net.jandie1505.fullserverjoin.utilities.BypassStatus;
import net.jandie1505.fullserverjoin.utilities.ConfigManager;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

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

        // events

        this.getServer().getPluginManager().registerEvents(new LoginHandler(this), this);

        // main command

        PluginCommand pluginCommand = this.getCommand("fullserverjoin");
        assert pluginCommand != null;
        SubcommandCommand command = new SubcommandCommand(this);
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);

        // other commands

        JoinInfoCommand joinInfoCommand = new JoinInfoCommand(this);
        command.addSubcommand("joininfo", SubcommandEntry.of(joinInfoCommand));

        // other commands standalone

        PluginCommand joinInfoPluginCommand = this.getCommand("joininfo");
        if (joinInfoPluginCommand != null) {
            joinInfoPluginCommand.setExecutor(joinInfoCommand);
            joinInfoPluginCommand.setTabCompleter(joinInfoCommand);
        }

        // info

        this.getLogger().info("Successfully activated FULL SERVER JOIN (version " + this.getDescription().getVersion() + "), created by jandie1505.");

    }

    // ----- PLAYER JOIN PRIORITY -----

    @NotNull
    public final BypassStatus getPlayerBypassStatus(@NotNull Player player) {

        if (player.hasPermission(PERMISSION_BYPASS_PLAYER_LIMIT) || this.getPlayerPriority(player) == Integer.MAX_VALUE) {
            return BypassStatus.PERMANENT;
        }

        return BypassStatus.NOT_AVAILABLE;
    }

    /**
     * Returns the join priority of the specified player.
     * @param player player
     * @return player priority
     */
    public int getPlayerPriority(@NotNull Player player) {

        if (player.hasPermission(PERMISSION_JOIN_LEVEL_HIGHEST)) return Integer.MAX_VALUE;

        int priority = 0; // Default priority for players without the permission

        if (player.hasPermission(PERMISSION_PREFIX_JOIN_LEVEL + "highest")) {
            return Integer.MAX_VALUE;
        }

        for (int i = this.getConfig().getInt(ConfigManager.CONFIG_MAX_LEVEL, 10); i >= 0; i--) {
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
