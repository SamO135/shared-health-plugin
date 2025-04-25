package io.github.SamO135.sharedHealthPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public class Countdown implements Consumer<BukkitTask> {
    private int remainingTime;

    public Countdown(int startTime) {
        this.remainingTime = startTime;
    }

    @Override
    public void accept(BukkitTask task) {
        String countdownText = Integer.toString(remainingTime);
        if (remainingTime == 0){
            countdownText = "GO";
        }

        Component mainTitle = Component.text(countdownText);
        Component subtitle = Component.text("");

        Title title = Title.title(mainTitle, subtitle);

        Bukkit.getServer().showTitle(title);

        remainingTime--;
        if (remainingTime < 0) {
            task.cancel();
        }
    }
}
