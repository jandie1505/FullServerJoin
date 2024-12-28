package net.jandie1505.joinmanager.commands;

import net.chaossquad.mclib.ChatUtils;
import net.chaossquad.mclib.command.TabCompletingCommandExecutor;
import net.jandie1505.joinmanager.JoinManager;
import net.jandie1505.joinmanager.utilities.ConfigManager;
import net.jandie1505.joinmanager.utilities.TempBypassData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class AllowJoinBypassCommand implements TabCompletingCommandExecutor {
    @NotNull private final JoinManager plugin;

    public AllowJoinBypassCommand(@NotNull JoinManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (!this.plugin.getCommandPermission(sender).manage()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(this.plugin.getConfig().getString(ConfigManager.CONFIG_MESSAGE_NO_PERMISSION, "")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cYou need to specify a player name or uuid");
            return true;
        }

        UUID playerId = ChatUtils.getPlayerUUIDFromString(args[0]);
        if (playerId == null) {
            sender.sendMessage("§cPlayer not found");
            return true;
        }

        if (this.plugin.getTemporaryBypassPlayers().containsKey(playerId)) {
            sender.sendMessage("§cThis player can already bypass");
            return true;
        }

        this.plugin.getTemporaryBypassPlayers().put(playerId, new TempBypassData(Instant.now().plus(5, ChronoUnit.MINUTES).getEpochSecond()));

        sender.sendMessage(
                Component.text("The player can now bypass the player limit for 5 minutes ")
                        .color(NamedTextColor.GREEN)
                        .append(
                                Component.text("[?]")
                                        .color(NamedTextColor.GRAY)
                                        .hoverEvent(HoverEvent.showText(
                                                Component.text()
                                                        .append(Component.text("- If the player does not join within the 5 minutes, the temporary bypass expires.\n").color(NamedTextColor.GRAY))
                                                        .append(Component.text("- If the player does join within the 5 minutes, the player bypasses the player limit.\n").color(NamedTextColor.GRAY))
                                                        .append(Component.text("- As long as the player stays online, the player cannot be kicked because of a full server.\n").color(NamedTextColor.GRAY))
                                                        .append(Component.text("- To remove this privilege, you can always remove the temporary bypass.").color(NamedTextColor.GRAY))
                                                        .build()
                                        ))
                        )
        );

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }

    public @NotNull JoinManager getPlugin() {
        return plugin;
    }

}
