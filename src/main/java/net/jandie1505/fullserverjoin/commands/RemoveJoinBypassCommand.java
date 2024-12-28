package net.jandie1505.fullserverjoin.commands;

import net.chaossquad.mclib.ChatUtils;
import net.chaossquad.mclib.command.TabCompletingCommandExecutor;
import net.jandie1505.fullserverjoin.FullServerJoin;
import net.jandie1505.fullserverjoin.utilities.TempBypassData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RemoveJoinBypassCommand implements TabCompletingCommandExecutor {
    @NotNull private final FullServerJoin plugin;

    public RemoveJoinBypassCommand(@NotNull FullServerJoin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (args.length < 1) {
            sender.sendMessage("§cYou need to specify a player name or uuid");
            return true;
        }

        UUID playerId = ChatUtils.getPlayerUUIDFromString(args[0]);
        if (playerId == null) {
            sender.sendMessage("§cPlayer not found");
            return true;
        }

        TempBypassData bypassData = this.plugin.getTemporaryBypassPlayers().remove(playerId);

        if (bypassData == null) {
            sender.sendMessage("§cThis player already is not temporarily bypassing the player limit");
            return true;
        }

        sender.sendMessage(Component.text("The cannot bypass the player limit anymore ")
                .color(NamedTextColor.GREEN)
                .append(
                        Component.text("[?]")
                                .color(NamedTextColor.GRAY)
                                .hoverEvent(HoverEvent.showText(
                                        Component.text()
                                                .append(Component.text("- If the player hasn't joined, the player can't bypass the player limit anymore.\n").color(NamedTextColor.GRAY))
                                                .append(Component.text("- If the player has joined, the player can now be kicked to make room for other players.\n").color(NamedTextColor.GRAY))
                                                .build()
                                ))
                )
        );

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        for (UUID playerId : this.plugin.getTemporaryBypassPlayers().keySet()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);

            if (player != null) {
                completions.add(player.getName());
            } else {
                completions.add(playerId.toString());
            }

        }

        return completions;
    }

    public @NotNull FullServerJoin getPlugin() {
        return plugin;
    }

}
