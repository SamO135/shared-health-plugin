package io.github.SamO135.sharedHealthPlugin;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class AttemptTracker {
    private BossBar attemptBar;
    private int currentAttempt = 0;

    public AttemptTracker() {
        attemptBar = Bukkit.createBossBar("Attempt: " + currentAttempt, BarColor.WHITE, BarStyle.SOLID);
        attemptBar.setProgress(0.0);
        attemptBar.setVisible(true);
    }

    public void incrementAttempt() {
        currentAttempt++;
        attemptBar.setTitle("Attempt: " + currentAttempt);
    }

    public void reset() {
        currentAttempt = 0;
        attemptBar.setTitle("Attempt: " + currentAttempt);
    }

    /**
     * Shows the attempts tracker to the specified player
     * @param player the player to show the attempts tracker
     */
    public void addPlayer(Player player) {
        attemptBar.addPlayer(player);
    }

    /**
     * Hides the attempts tracker from the specified player
     * @param player the player to hide the attempts tracker from
     */
    public void removePlayer(Player player) {
        attemptBar.removePlayer(player);
    }

    /**
     * Shows the attempts tracker to all players that are currently online. When a player leaves, Minecraft's logic
     * will remove them from the boss bar.
     */
    public void addAllOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            attemptBar.addPlayer(player);
        }
    }

    public void deleteBossBar() {
        attemptBar.removeAll();
        attemptBar = null;
    }
}
