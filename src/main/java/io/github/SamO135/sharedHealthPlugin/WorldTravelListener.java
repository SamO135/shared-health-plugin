package io.github.SamO135.sharedHealthPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class WorldTravelListener implements Listener {

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        World fromWorld = event.getFrom().getWorld();

        //check if player is exiting the nether or end
        if (fromWorld.getEnvironment() == World.Environment.NETHER ||
            fromWorld.getEnvironment() == World.Environment.THE_END) {
            World targetWorld = Bukkit.getWorld("world_run");

            if (targetWorld != null) {
                Location spawnLocation = targetWorld.getSpawnLocation();
                event.setTo(spawnLocation);
            }
        }
    }
}
