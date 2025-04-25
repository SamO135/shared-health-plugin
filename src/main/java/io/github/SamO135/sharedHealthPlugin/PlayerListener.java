package io.github.SamO135.sharedHealthPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

class PlayerDamageListener implements Listener {
    private final Logger logger;
    private Player lastSatiatedHealPlayer;
    private long lastSatiatedHealTime = 0L;

    public PlayerDamageListener(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
    }

    @EventHandler
    public void onPlayerTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player damagedPlayer)) return;
        if (event.getCause() == DamageCause.CUSTOM) return;
        logger.info(damagedPlayer.getName() + " took " + event.getFinalDamage() + " damage from: " + event.getCause());

        double damage = event.getFinalDamage();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != damagedPlayer){
                player.damage(damage);
            }
        }
//        syncPlayerHealth();
    }

    @EventHandler
    public void onPlayerRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player healedPlayer)) return;
        if (event.getRegainReason() == RegainReason.CUSTOM) return;
        logger.info(healedPlayer.getName() + " healed " + event.getAmount() + " from " + event.getRegainReason() + " with " + healedPlayer.getSaturation() + " saturation");

        double healAmount = event.getAmount();

        if (event.getRegainReason() == RegainReason.SATIATED && lastSatiatedHealPlayer != healedPlayer){
            long timeNow = System.currentTimeMillis();
            long satiatedHealCooldownMS = 500;
            if (timeNow - lastSatiatedHealTime < satiatedHealCooldownMS){
                event.setCancelled(true);
            }
            else {
                lastSatiatedHealTime = timeNow;
                lastSatiatedHealPlayer = healedPlayer;
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != healedPlayer) {
                player.heal(healAmount, RegainReason.CUSTOM);
            }
        }
//        syncPlayerHealth();
    }

    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player deadPlayer)) return;
        logger.info(deadPlayer.getName() + " died");

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != deadPlayer && !player.isDead()) {
                player.setHealth(0.0);
            }
        }
    }

    private void syncPlayerHealth() {
        double sharedHealth = Bukkit.getOnlinePlayers().stream().mapToDouble(Player::getHealth).min().orElse(20d);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setHealth(sharedHealth);
        }
    }
}