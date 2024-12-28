package net.jandie1505.fullserverjoin.commands;

import net.chaossquad.mclib.ChatUtils;
import net.chaossquad.mclib.command.TabCompletingCommandExecutor;
import net.jandie1505.fullserverjoin.FullServerJoin;
import net.jandie1505.fullserverjoin.utilities.BypassStatus;
import net.jandie1505.fullserverjoin.utilities.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class JoinInfoCommand implements TabCompletingCommandExecutor {
    @NotNull private final FullServerJoin plugin;

    public JoinInfoCommand(@NotNull FullServerJoin plugin) {
        this.plugin = plugin;
    }

    /**
     * Join info command executor.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission(FullServerJoin.PERMISSION_COMMAND_GET_JOIN_LEVEL)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString(ConfigManager.CONFIG_MESSAGE_NO_PERMISSION, "&cNo permission")));
            return true;
        }

        Player player = null;

        if (args.length > 0) {
            player = ChatUtils.getPlayerFromString(args[0]);
        } else {
            if (sender instanceof Player p) player = p;
        }

        if (player == null) {
            sender.sendMessage("§cPlayer does not exist");
            return true;
        }

        int priority = this.plugin.getPlayerPriority(player);
        BypassStatus bypassStatus = this.plugin.getPlayerBypassStatus(player);

        sender.sendMessage("§7Join information about " + player.getName() + ":§r\n" +
                "§7- Priority: " + (priority == Integer.MAX_VALUE ? "max (" + priority + ")" : priority) + "§r\n" +
                "§7- Bypass: " + bypassStatus + " (" + bypassStatus.isBypass() + ")" + "§r"
        );

        return true;
    }

    /**
     * Join info command tab completer.
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(FullServerJoin.PERMISSION_COMMAND_GET_JOIN_LEVEL)) return List.of();
        if (args.length == 1) return List.copyOf(this.plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        return List.of();
    }

    public @NotNull FullServerJoin getPlugin() {
        return plugin;
    }

}
