package io.github.SamO135.sharedHealthPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public class Countdown implements Consumer<BukkitTask> {
    private SharedHealthPlugin plugin;
    private int remainingTime;

    public Countdown(SharedHealthPlugin plugin, int startTime) {
        this.plugin = plugin;
        this.remainingTime = startTime;
    }

    @Override
    public void accept(BukkitTask task) {
        String countdownText = Integer.toString(remainingTime);
        if (remainingTime == 0){
            countdownText = "GO";
        }

        // create title
        Component mainTitle = Component.text(countdownText);
        Component subtitle = Component.text("");
        Title title = Title.title(mainTitle, subtitle);
        Bukkit.getServer().showTitle(title);

        // create world border
        World world = Bukkit.getWorld("world_run");
        if (world != null) {
            WorldBorder border = world.getWorldBorder();
            border.setCenter(world.getSpawnLocation());
            border.setSize(10);

            if (remainingTime <= 0){
                //increase world border with an animation
                int borderAnimationDuration = 5;
                border.setSize(500, borderAnimationDuration);
                Bukkit.getScheduler().runTaskLater(plugin, border::reset, borderAnimationDuration * 20L);
            }
        }

        remainingTime--;
        if (remainingTime < 0) {
            plugin.setPlayerGameMode(GameMode.SURVIVAL);
            plugin.getTimer().reset();
            plugin.getTimer().resume();
            task.cancel();
        }
    }
}
