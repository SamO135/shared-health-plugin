package io.github.SamO135.sharedHealthPlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DamageTrackerScoreboardObjective {
    private Scoreboard scoreboard;
    private Objective damageObjective;
    private Map<UUID, Double> playerTotalDamage = new HashMap<>();

    public DamageTrackerScoreboardObjective() {
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        damageObjective = scoreboard.getObjective("totalDamageTaken");
        if (damageObjective == null) {
            damageObjective = scoreboard.registerNewObjective("totalDamageTaken", "dummy", "Total Damage Taken");
            damageObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }
    }

    public void incrementDamage(Player player, double damage) {
        double newScore = incrementPlayerTotalDamage(player, damage);
        damageObjective.getScore(player.getName()).setScore((int)Math.round(newScore));
    }

    public void resetDamage(Player player) {
        resetPlayerDamage(player);
        damageObjective.getScore(player.getName()).setScore(0);
    }

    private double incrementPlayerTotalDamage(Player player, double damage) {
        UUID uuid = player.getUniqueId();
        double currentDamage = playerTotalDamage.getOrDefault(uuid, 0.0);
        playerTotalDamage.put(uuid, currentDamage + damage);
        return currentDamage + damage;
    }

    private void resetPlayerDamage(Player player) {
        playerTotalDamage.put(player.getUniqueId(), 0.0);
    }
}
