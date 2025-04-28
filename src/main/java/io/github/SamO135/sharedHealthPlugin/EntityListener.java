package io.github.SamO135.sharedHealthPlugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.EnderDragon;

public class EntityListener implements Listener {
    private SharedHealthPlugin plugin;

    public EntityListener(SharedHealthPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void OnEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof EnderDragon) {
            plugin.getTimer().pause();
        }
    }
}
