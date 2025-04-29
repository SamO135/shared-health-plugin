package io.github.SamO135.sharedHealthPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerJoinEvent;

class PlayerListener implements Listener {
    private final SharedHealthPlugin plugin;
    private Player lastSatiatedHealPlayer;
    private long lastSatiatedHealTime = 0L;

    public PlayerListener(SharedHealthPlugin plugin) {
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
        Component message = plugin.createMessageComponent(damagedPlayer.getName() + "<grey> has taken <color:#f61c21>" + damage + " ❤<grey> damage.");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
            if (player != damagedPlayer) {
                // scuffed but works (I think)
                double newHealth = Math.max(0.0, player.getHealth() - damage);
                player.damage(damage); // apply damage normally for the on-hit effects (sound, knockback etc.)
                player.setHealth(newHealth); // set health manually to bypass damage modifiers (armour etc.)
            }
        }
    }

    @EventHandler
    public void onPlayerRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player healedPlayer)) return;
        if (event.getRegainReason() == RegainReason.CUSTOM) return;

        // logging
        if (plugin.loggingEnabled) {
            plugin.getLogger().info(healedPlayer.getName() + " healed " + event.getAmount() + " from " + event.getRegainReason() + " with " + healedPlayer.getSaturation() + " saturation");
        }

        double healAmount = event.getAmount();

        if (event.getRegainReason() == RegainReason.SATIATED && lastSatiatedHealPlayer != healedPlayer) {
            long timeNow = System.currentTimeMillis();
            long satiatedHealCooldownMS = 500;
            if (timeNow - lastSatiatedHealTime < satiatedHealCooldownMS) {
                event.setCancelled(true);
                return;
            } else {
                lastSatiatedHealTime = timeNow;
                lastSatiatedHealPlayer = healedPlayer;
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != healedPlayer) {
                player.heal(healAmount, RegainReason.CUSTOM);
            }
        }
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // When a player leaves and rejoins the server, by default the boss bar will not show for them regardless
        // of their preferences, so I have to manually add them back to the bossbar when rejoining.
        if (plugin.isAttemptsShownFor(event.getPlayer())) {
            this.plugin.getAttemptTracker().addPlayer(event.getPlayer());
        }
    }
}