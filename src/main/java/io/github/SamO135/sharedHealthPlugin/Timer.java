package io.github.SamO135.sharedHealthPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;

public class Timer implements Runnable{
    private SharedHealthPlugin plugin;
    private int seconds = 0;
    private Duration duration = Duration.ofSeconds(0);
    private boolean paused = true;

    public Timer(SharedHealthPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!paused) {
            // increment timer
            seconds++;
            duration = Duration.ofSeconds(seconds);
        }

        // create action bar message
        MiniMessage mm = MiniMessage.miniMessage();
        Component message = mm.deserialize(String.format("%02d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart()));
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!this.plugin.isTimerHiddenFor(player)) {
                player.sendActionBar(message);
            }
        }
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public void reset() {
        seconds = 0;
    }
}
