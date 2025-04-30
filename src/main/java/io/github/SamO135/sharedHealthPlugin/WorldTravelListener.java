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
        if (fromWorld == null) return;

        World targetWorld = Bukkit.getWorld("world_run");
        if (targetWorld == null) return;

        // check if player is exiting the nether or end
        if (fromWorld.getEnvironment() == World.Environment.THE_END) {
            // find player's spawn location
            Location playerSpawn = event.getPlayer().getRespawnLocation();
            Location worldSpawn = targetWorld.getSpawnLocation();
            Location targetLocation = playerSpawn != null ? playerSpawn : worldSpawn;

            event.setTo(targetLocation);
        } else if (fromWorld.getEnvironment() == World.Environment.NETHER) {
            Location from = event.getFrom();
            Location targetLocation = new Location(
                    targetWorld, from.getX() * 8.0,
                    from.getY(), from.getZ() * 8.0,
                    from.getYaw(),
                    from.getPitch()
            );
            event.setTo(targetLocation);
        }
    }
}
