package io.github.SamO135.sharedHealthPlugin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SharedHealthPluginCommands {
    private final SharedHealthPlugin plugin;

    public SharedHealthPluginCommands(SharedHealthPlugin plugin) {
        this.plugin = plugin;
    }

    private LiteralArgumentBuilder<CommandSourceStack> commandTree = Commands.literal("sh")
            .then(Commands.literal("debug")
                    .then(Commands.literal("showLogs")
                            .then(Commands.argument("toggle", BoolArgumentType.bool())
                                    .executes(this::showLogs)))
                    .then(Commands.literal("logPlayerHealth")
                            .executes(this::logPlayerHealth))
                    .then(Commands.literal("syncPlayerHealth")
                            .executes(this::syncPlayerHealth))
            );

    private int showLogs(CommandContext<CommandSourceStack> ctx) {
        plugin.loggingEnabled = ctx.getArgument("toggle", boolean.class);
        return Command.SINGLE_SUCCESS;
    }

    private int logPlayerHealth(CommandContext<CommandSourceStack> ctx) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getLogger().info(player.getName() + ": " + player.getHealth() + " health");
        }
        return Command.SINGLE_SUCCESS;
    }

    private int syncPlayerHealth(CommandContext<CommandSourceStack> ctx) {
        double syncedHealth = plugin.syncPlayerHealth();
        plugin.getLogger().info("All player's health synced to: " + syncedHealth);
        return Command.SINGLE_SUCCESS;
    }


    public LiteralCommandNode<CommandSourceStack> getCommandNode(){
        return commandTree.build();
    }

}

