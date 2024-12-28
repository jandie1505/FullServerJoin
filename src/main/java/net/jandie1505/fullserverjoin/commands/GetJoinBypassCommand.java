package net.jandie1505.fullserverjoin.commands;

import net.chaossquad.mclib.command.TabCompletingCommandExecutor;
import net.jandie1505.fullserverjoin.FullServerJoin;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class GetJoinBypassCommand implements TabCompletingCommandExecutor {
    @NotNull private final FullServerJoin plugin;

    public GetJoinBypassCommand(@NotNull FullServerJoin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (args.length < 1) {
            sender.sendMessage("§cYou need to specify a player name or uuid");
            return true;
        }

        String out = "§7Bypassing players: ";

        Iterator<UUID> i = this.plugin.getTemporaryBypassPlayers().keySet().iterator();
        while (i.hasNext()) {
            UUID playerId = i.next();

            String name;

            OfflinePlayer player = this.plugin.getServer().getOfflinePlayer(playerId);
            if (player != null) {
                name = player.getName();
            } else {
                name = playerId.toString();
            }

            out += name + (i.hasNext() ? "§r, " : "");
        }

        sender.sendMessage(out);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }

    public @NotNull FullServerJoin getPlugin() {
        return plugin;
    }

}
