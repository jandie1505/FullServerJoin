package net.jandie1505.fullserverjoin.utilities;

import net.jandie1505.fullserverjoin.FullServerJoin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public final class ConfigManager {
    public static final String CONFIG_MAX_LEVEL = "max_level";
    public static final String CONFIG_MESSAGE_KICK = "kick_message";
    public static final String CONFIG_MESSAGE_NO_PERMISSION = "no_permission_message";

    @NotNull private final FullServerJoin plugin;
    @NotNull private YamlConfiguration config;

    public ConfigManager(@NotNull FullServerJoin plugin) {
        this.plugin = plugin;
        this.resetConfig();
    }

    /**
     * Resets the config to the default values.
     */
    public void resetConfig() {
        this.config = new YamlConfiguration();

        this.config.set(CONFIG_MAX_LEVEL, 10);
        this.config.setComments(CONFIG_MAX_LEVEL, List.of(
                "The maximum level for join permissions.",
                "Levels which are higher than this value will be ignored."
        ));

        this.config.set(CONFIG_MESSAGE_KICK, "&cYou have been kicked to make room for a player with higher priority");
        this.config.setComments(CONFIG_MESSAGE_KICK, List.of("Players will see this message when getting kicked to make room for a player with higher priority."));

        this.config.set(CONFIG_MESSAGE_NO_PERMISSION, "&cNo permission");
        this.config.setComments(CONFIG_MESSAGE_NO_PERMISSION, List.of("The message players see when they have no permission to use plugin commands."));
    }

    /**
     * Reloads the config file.
     */
    public void reloadConfig() {
        boolean success = this.loadConfig();
        if (!success) this.saveConfig();
    }

    public boolean loadConfig() {
        try {

            File configFile = this.getConfigFile();

            if (!configFile.exists()) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to load config file: File does not exist");
                return false;
            }

            this.config.load(configFile);

            this.plugin.getLogger().log(Level.INFO, "Config loaded successfully");
            return true;
        } catch (IOException | InvalidConfigurationException e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to load config file: Exception occurred", e);
            return false;
        }
    }

    public boolean saveConfig() {
        try {

            File configFile = this.getConfigFile();

            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            }

            this.config.save(configFile);

            this.plugin.getLogger().log(Level.INFO, "Config saved successfully");
            return true;
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to save config file: Exception occurred", e);
            return false;
        }
    }

    private File getConfigFile() {
        return new File(this.plugin.getDataFolder(), "config.yml");
    }

    public @NotNull FullServerJoin getPlugin() {
        return plugin;
    }

    public @NotNull YamlConfiguration getConfig() {
        return config;
    }

}
