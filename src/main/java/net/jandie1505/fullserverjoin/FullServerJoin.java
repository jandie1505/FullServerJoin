package net.jandie1505.fullserverjoin;

import org.bukkit.ChatColor;
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

public class FullServerJoin extends JavaPlugin implements Listener {
    YamlConfiguration config;

    @Override
    public void onEnable() {

        YamlConfiguration defaultConfig = new YamlConfiguration();
        defaultConfig.set("maxLevel", 10);
        defaultConfig.setComments("maxLevel", List.of("The maximum level for join permissions"));

        defaultConfig.set("kickMessage", "&cYou have been kicked to make room for a player with higher priority");
        defaultConfig.setComments("kickMessage", List.of("Players will see this message when getting kicked to make room for a player with higher priority"));

        /*
        defaultConfig.set("alternativePermissionChecks", false);
        defaultConfig.setComments("alternativePermissionChecks", List.of(
                "When enabled, the plugin will iterate through all permissions of that player for getting the join level.",
                "When disabled, the plugin will iterate through all levels and will do a permission check for each level (starting with the highest level)",
                "If the players amount of permissions is less than the amount of join levels, enabling this could increase performance."
        ));
        */

        this.config = new YamlConfiguration();
        this.config.setDefaults(defaultConfig);

        try {

            File configFile = new File(this.getDataFolder(), "config.yml");

            if (!configFile.exists()) {
                configFile.mkdirs();
                configFile.createNewFile();
            }

            try {
                this.config.load(new File(this.getDataFolder(), "config.yml"));
            } catch (InvalidConfigurationException e) {
                this.getLogger().warning("Invalid configuration. Using defaults.");
            }

            this.config.save(new File(this.getDataFolder(), "config.yml"));

        } catch (IOException e) {
            this.getLogger().warning("Could not access config file. Using defaults.");
        }

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
