package net.jandie1505.fullserverjoin.commands;

import net.chaossquad.mclib.command.SubcommandCommand;
import net.chaossquad.mclib.command.SubcommandEntry;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class FSJCommand extends SubcommandCommand {

    public FSJCommand(@NotNull Plugin plugin) {
        super(plugin);
        this.addSubcommand("joininfo", SubcommandEntry.of(new FSJCommand(plugin)));
    }
    
}
