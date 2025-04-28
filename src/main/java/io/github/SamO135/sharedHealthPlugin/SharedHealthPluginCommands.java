package io.github.SamO135.sharedHealthPlugin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SharedHealthPluginCommands {
    private final SharedHealthPlugin plugin;

    public SharedHealthPluginCommands(SharedHealthPlugin plugin) {
        this.plugin = plugin;
    }

    private LiteralArgumentBuilder<CommandSourceStack> commandTree = Commands.literal("sh")
            .then(Commands.literal("debug")
                    .requires(this::senderHasPermission)
                    .then(Commands.literal("showHealthLogs")
                            .then(Commands.argument("toggle", BoolArgumentType.bool())
                                    .executes(this::showHealthLogs)))
                    .then(Commands.literal("logPlayerHealth")
                            .executes(this::logPlayerHealth))
                    .then(Commands.literal("syncPlayerHealth")
                            .executes(this::syncPlayerHealth))
                    .then(Commands.literal("delete-custom-world")
                            .executes(this::deleteCustomWorld))
            )
            .then(Commands.literal("timer")
                    .then(Commands.literal("start")
                            .requires(this::senderHasPermission)
                            .executes(this::startTimer))
                    .then(Commands.literal("pause")
                            .requires(this::senderHasPermission)
                            .executes(this::pauseTimer))
                    .then(Commands.literal("reset")
                            .requires(this::senderHasPermission)
                            .executes(this::resetTimer))
                    .then(Commands.literal("show")
                            .executes(this::showTimer))
                    .then(Commands.literal("hide")
                            .executes(this::hideTimer))
            )
            .then(Commands.literal("attempts")
                    .then(Commands.literal("reset")
                            .requires(this::senderHasPermission)
                            .executes(this::resetAttempts))
                    .then(Commands.literal("show")
                            .executes(this::showAttempts))
                    .then(Commands.literal("hide")
                            .executes(this::hideAttempts))
            )
            .then(Commands.literal("run")
                    .requires(this::senderHasPermission)
                    .then(Commands.literal("start")
                            .executes(this::startRun))
                    .then(Commands.literal("end")
                            .executes(this::endRun))
            );

    private int showHealthLogs(CommandContext<CommandSourceStack> ctx) {
        plugin.loggingEnabled = ctx.getArgument("toggle", boolean.class);

        // send player message
        Component message = plugin.createMessageComponent("player health logs is now set to: " + plugin.loggingEnabled);
        ctx.getSource().getSender().sendMessage(message);

        return Command.SINGLE_SUCCESS;
    }

    private int logPlayerHealth(CommandContext<CommandSourceStack> ctx) {
        Component message;
        for (Player player : Bukkit.getOnlinePlayers()) {
            message = plugin.createMessageComponent(player.getName() + ": " + player.getHealth() + " health");
            ctx.getSource().getSender().sendMessage(message);
            plugin.getLogger().info(player.getName() + ": " + player.getHealth() + " health");
        }
        return Command.SINGLE_SUCCESS;
    }

    private int syncPlayerHealth(CommandContext<CommandSourceStack> ctx) {
        // send message to player
        Component message = plugin.createMessageComponent("All players' health synced to: " + plugin.syncPlayerHealth());
        ctx.getSource().getSender().sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }

    private int startTimer(CommandContext<CommandSourceStack> ctx) {
        // send player message
        Component message = plugin.createMessageComponent("Start timer");
        ctx.getSource().getSender().sendMessage(message);

        plugin.getTimer().resume();
        return Command.SINGLE_SUCCESS;
    }

    private int pauseTimer(CommandContext<CommandSourceStack> ctx) {
        // send player message
        Component message = plugin.createMessageComponent("Pause timer");
        ctx.getSource().getSender().sendMessage(message);

        plugin.getTimer().pause();
        return Command.SINGLE_SUCCESS;
    }

    private int resetTimer(CommandContext<CommandSourceStack> ctx) {
        // send player message
        Component message = plugin.createMessageComponent("Reset timer");
        ctx.getSource().getSender().sendMessage(message);

        plugin.getTimer().reset();
        return Command.SINGLE_SUCCESS;
    }

    private int showTimer(CommandContext<CommandSourceStack> ctx) {
        // send player message
        Component message = plugin.createMessageComponent("Show timer");
        ctx.getSource().getSender().sendMessage(message);

        CommandSender sender = ctx.getSource().getSender();
        if (sender instanceof Player player){
            plugin.showTimerFor(player);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int hideTimer(CommandContext<CommandSourceStack> ctx) {
        // send player message
        Component message = plugin.createMessageComponent("Hide timer");
        ctx.getSource().getSender().sendMessage(message);

        CommandSender sender = ctx.getSource().getSender();
        if (sender instanceof Player player){
            plugin.hideTimerFor(player);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int startRun(CommandContext<CommandSourceStack> ctx) {
        // send message to players
        Component message = plugin.createMessageComponent("Creating new world. You will be teleported shortly...");
        Bukkit.getServer().sendMessage(message);

        plugin.getDimensionResetHandler().startRun();
        plugin.resetPlayers();
        plugin.getAttemptTracker().incrementAttempt();
        plugin.getAttemptTracker().addAllOnlinePlayers();
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.showTimerFor(player);
            plugin.showAttemptsFor(player);
        }
        int countdownTime = 5;
        Bukkit.getScheduler().runTaskTimer(plugin, new Countdown(plugin, countdownTime), 1L, 30L);
        return Command.SINGLE_SUCCESS;
    }

    private int endRun(CommandContext<CommandSourceStack> ctx) {
        //send message to players
        Component message = plugin.createMessageComponent("Run ended. Time: " + plugin.getTimer().getTime());
        Bukkit.getServer().sendMessage(message);

        plugin.getDimensionResetHandler().endRun();
        plugin.getTimer().pause();
        return Command.SINGLE_SUCCESS;
    }

    private int deleteCustomWorld(CommandContext<CommandSourceStack> ctx) {
        boolean deleted = plugin.getDimensionResetHandler().deleteCustomWorld();
        Component message;

        // send message to player
        if (deleted) {
            message = plugin.createMessageComponent("Custom dimension has been deleted");
        } else {
            message = plugin.createMessageComponent("Custom dimension could not be deleted");
        }
        ctx.getSource().getSender().sendMessage(message);

        return Command.SINGLE_SUCCESS;
    }

    private int resetAttempts(CommandContext<CommandSourceStack> ctx) {
        // send player message
        Component message = plugin.createMessageComponent("Reset attempts");
        ctx.getSource().getSender().sendMessage(message);

        plugin.getAttemptTracker().reset();
        return Command.SINGLE_SUCCESS;
    }

    private int showAttempts(CommandContext<CommandSourceStack> ctx) {
        // send player message
        Component message = plugin.createMessageComponent("Show attempts");
        ctx.getSource().getSender().sendMessage(message);

        if (ctx.getSource().getSender() instanceof Player sender) {
            plugin.getAttemptTracker().addPlayer(sender);
            plugin.showAttemptsFor(sender);
        };
        return Command.SINGLE_SUCCESS;
    }

    private int hideAttempts(CommandContext<CommandSourceStack> ctx) {
        // send player message
        Component message = plugin.createMessageComponent("Hide attempts");
        ctx.getSource().getSender().sendMessage(message);

        if (ctx.getSource().getSender() instanceof Player sender) {
            plugin.getAttemptTracker().removePlayer(sender);
            plugin.hideAttemptsFor(sender);
        };
        return Command.SINGLE_SUCCESS;
    }

    public LiteralCommandNode<CommandSourceStack> getCommandNode(){
        return commandTree.build();
    }

    private boolean senderHasPermission(CommandSourceStack source) {
        if (!(source.getSender() instanceof Player playerSender)) return false;
        return playerSender.hasPermission("shared-health-plugin.global");
    }
}

