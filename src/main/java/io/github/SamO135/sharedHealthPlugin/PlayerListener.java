package io.github.SamO135.sharedHealthPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

class PlayerDamageListener implements Listener {
    private SharedHealthPlugin plugin;
    private Player lastSatiatedHealPlayer;
    private long lastSatiatedHealTime = 0L;

    public PlayerDamageListener(SharedHealthPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player damagedPlayer)) return;
        if (event.getCause() == DamageCause.CUSTOM) return;

        double damage = event.getFinalDamage();
        DamageCause damageCause = event.getCause();

        // logging
        if (plugin.loggingEnabled) {
            plugin.getLogger().info(damagedPlayer.getName() + " took " + damage + " damage from: " + damageCause);
        }

        // create chat message
        MiniMessage mm = MiniMessage.miniMessage();
        Component message = mm.deserialize(damagedPlayer.getName() + "<grey> has taken <color:#f61c21>" + damage + " ❤<grey> damage.");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
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

        // logging
        if (plugin.loggingEnabled){
            plugin.getLogger().info(healedPlayer.getName() + " healed " + event.getAmount() + " from " + event.getRegainReason() + " with " + healedPlayer.getSaturation() + " saturation");
        }

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

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != deadPlayer && !player.isDead()) {
                player.setHealth(0.0);
            }
        }
    }
}