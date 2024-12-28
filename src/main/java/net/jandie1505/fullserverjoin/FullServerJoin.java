package net.jandie1505.fullserverjoin;

import net.chaossquad.mclib.command.SubcommandCommand;
import net.chaossquad.mclib.command.SubcommandEntry;
import net.jandie1505.fullserverjoin.commands.AllowJoinBypassCommand;
import net.jandie1505.fullserverjoin.commands.GetJoinBypassCommand;
import net.jandie1505.fullserverjoin.commands.JoinInfoCommand;
import net.jandie1505.fullserverjoin.commands.RemoveJoinBypassCommand;
import net.jandie1505.fullserverjoin.listeners.LoginHandler;
import net.jandie1505.fullserverjoin.utilities.BypassStatus;
import net.jandie1505.fullserverjoin.utilities.ConfigManager;
import net.jandie1505.fullserverjoin.utilities.TempBypassData;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FullServerJoin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    public static final String PERMISSION_BYPASS_PLAYER_LIMIT = "fullserverjoin.bypass";
    public static final String PERMISSION_PREFIX_JOIN_LEVEL = "fullserverjoin.level.";
    public static final String PERMISSION_JOIN_LEVEL_HIGHEST = PERMISSION_PREFIX_JOIN_LEVEL + "highest";
    public static final String PERMISSION_COMMAND_GET_JOIN_LEVEL = "fullserverjoin.command.joininfo";

    @NotNull private final ConfigManager configManager;
    @NotNull private final Map<UUID, TempBypassData> temporaryBypassPlayers;

    public FullServerJoin() {
        this.configManager = new ConfigManager(this);
        this.temporaryBypassPlayers = new HashMap<>();
    }

    @Override
    public void onEnable() {

        // config

        this.configManager.reloadConfig();

        // temp bypass set

        this.temporaryBypassPlayers.clear();

        // events

        this.getServer().getPluginManager().registerEvents(new LoginHandler(this), this);

        // main command

        PluginCommand pluginCommand = this.getCommand("joinmanager");
        assert pluginCommand != null;
        SubcommandCommand command = new SubcommandCommand(this);
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);

        // other commands

        JoinInfoCommand joinInfoCommand = new JoinInfoCommand(this);
        command.addSubcommand("joininfo", SubcommandEntry.of(joinInfoCommand));

        GetJoinBypassCommand getJoinBypassCommand = new GetJoinBypassCommand(this);
        command.addSubcommand("get-join-bypass", SubcommandEntry.of(getJoinBypassCommand));

        AllowJoinBypassCommand allowJoinBypassCommand = new AllowJoinBypassCommand(this);
        command.addSubcommand("allow-join-bypass", SubcommandEntry.of(allowJoinBypassCommand));

        RemoveJoinBypassCommand removeJoinBypassCommand = new RemoveJoinBypassCommand(this);
        command.addSubcommand("remove-join-bypass", SubcommandEntry.of(removeJoinBypassCommand));

        // other commands standalone

        PluginCommand joinInfoPluginCommand = this.getCommand("joininfo");
        if (joinInfoPluginCommand != null) {
            joinInfoPluginCommand.setExecutor(joinInfoCommand);
            joinInfoPluginCommand.setTabCompleter(joinInfoCommand);
        }

        PluginCommand getJoinBypassPluginCommand = this.getCommand("get-join-bypass");
        if (getJoinBypassPluginCommand != null) {
            getJoinBypassPluginCommand.setExecutor(getJoinBypassCommand);
            getJoinBypassPluginCommand.setTabCompleter(getJoinBypassCommand);
        }

        PluginCommand allowJoinBypassPluginCommand = this.getCommand("allow-join-bypass");
        if (allowJoinBypassPluginCommand != null) {
            allowJoinBypassPluginCommand.setExecutor(allowJoinBypassCommand);
            allowJoinBypassPluginCommand.setTabCompleter(allowJoinBypassCommand);
        }

        PluginCommand removeJoinBypassPluginCommand = this.getCommand("remove-join-bypass");
        if (removeJoinBypassPluginCommand != null) {
            removeJoinBypassPluginCommand.setExecutor(removeJoinBypassCommand);
            removeJoinBypassPluginCommand.setTabCompleter(removeJoinBypassCommand);
        }

        // temporary cleanup task

        new BukkitRunnable() {
            @Override
            public void run() {
                FullServerJoin.this.temporaryBypassCleanupTask();
            }
        }.runTaskTimer(this, 1, 30*20);

        // info

        this.getLogger().info("Successfully activated FULL SERVER JOIN (version " + this.getDescription().getVersion() + "), created by jandie1505.");

    }

    @Override
    public void onDisable() {
        this.temporaryBypassPlayers.clear();
    }

    // ----- PLAYER JOIN PRIORITY -----

    @NotNull
    public final BypassStatus getPlayerBypassStatus(@NotNull Player player) {

        if (player.hasPermission(PERMISSION_BYPASS_PLAYER_LIMIT) || this.getPlayerPriority(player) == Integer.MAX_VALUE) {
            return BypassStatus.PERMANENT;
        }

        if (this.temporaryBypassPlayers.containsKey(player.getUniqueId())) {
            return BypassStatus.TEMPORARY;
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

    // ----- TASKS -----

    private void temporaryBypassCleanupTask() {

        for (Map.Entry<UUID, TempBypassData> entry : Map.copyOf(this.temporaryBypassPlayers).entrySet()) {
            Player player = this.getServer().getPlayer(entry.getKey());

            if (entry.getValue().used()) {
                // If used, clean up if the player is not online anymore
                if (player != null) continue;
                this.temporaryBypassPlayers.remove(entry.getKey());
            } else {

                if (player != null) {
                    // If player is online and data is not set to used, set data to used
                    entry.getValue().setUsed(true);
                    continue;
                } else {
                    // If player is not online and data is not used, remove if time has expired
                    if (entry.getValue().removeAtEpoch() >= Instant.now().getEpochSecond()) continue;
                    this.temporaryBypassPlayers.remove(entry.getKey());
                }

            }

        }

    }

    // ----- OTHER -----

    @NotNull
    public YamlConfiguration getConfig() {
        return this.configManager.getConfig();
    }

    @NotNull
    public Map<UUID, TempBypassData> getTemporaryBypassPlayers() {
        return this.temporaryBypassPlayers;
    }

}
