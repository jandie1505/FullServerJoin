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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FullServerJoin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    YamlConfiguration config;

    @Override
    public void onEnable() {

        // config

        this.config = new YamlConfiguration();

        try {

            File configFile = new File(this.getDataFolder(), "config.yml");

            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();

                this.config.set("maxLevel", 10);
                this.config.setComments("maxLevel", List.of("The maximum level for join permissions", "Higher levels will be ignored"));

                this.config.set("kickMessage", "&cYou have been kicked to make room for a player with higher priority");
                this.config.setComments("kickMessage", List.of("Players will see this message when getting kicked to make room for a player with higher priority"));

                this.config.set("noPermissionMessage", "&cNo permission");
                this.config.setComments("noPermissionMessage", List.of("The message players see when they have no permission to use plugin commands"));

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

        // command

        PluginCommand command = this.getCommand("getjoinpriority");

        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }

        // listener

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
                otherPlayer.kickPlayer(ChatColor.translateAlternateColorCodes('&', this.config.getString("kickMessage", "&cYou have been kicked to make room for a player with higher priority.")));
                event.allow(); // Allow the joining player to join
                return;
            }

        }

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("fullserverjoin.command.getjoinpriority")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString("noPermissionMessage", "&cNo permission")));
            return true;
        }

        Player player = null;

        if (args.length < 1) {

            if (sender instanceof Player) {
                player = (Player) sender;
            }

        } else {

            try {
                player = this.getServer().getPlayer(UUID.fromString(args[0]));
            } catch (IllegalArgumentException e) {
                player = this.getServer().getPlayer(args[0]);
            }

        }

        if (player == null) {
            sender.sendMessage("§cPlayer does not exist");
            return true;
        }

        sender.sendMessage("§7Join priority of " + player.getName() + ": " + this.getPlayerPriority(player));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 1) {
            return List.copyOf(this.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        }

        return List.of();
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

        for (int i = this.config.getInt("maxLevel", 10); i >= 0; i--) {
            if (player.hasPermission(permissionPrefix + i)) {
                return i;
            }
        }

        return defaultPriority;
    }
}
