package io.github.SamO135.sharedHealthPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

public final class DimensionResetHandler {
    private SharedHealthPlugin plugin;
    private static DimensionResetHandler INSTANCE;

    private DimensionResetHandler() {
    }

    public static DimensionResetHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DimensionResetHandler();
        }

        return INSTANCE;
    }

    public void startRun() {
        // send message to players
        MiniMessage mm = MiniMessage.miniMessage();
        Component message = mm.deserialize("Creating new world. You will be teleported shortly...");
        Bukkit.getServer().sendMessage(message);

        // unload and delete dimensions
        unloadAndDeleteWorld("world_run");
        unloadAndDeleteWorld("world_nether");
        unloadAndDeleteWorld("world_the_end");

        // create new dimensions
        createNewWorld("world_run", World.Environment.NORMAL, WorldType.NORMAL);
        createNewWorld("world_nether", World.Environment.NETHER, WorldType.NORMAL);
        createNewWorld("world_the_end", World.Environment.THE_END, WorldType.NORMAL);

        teleportPlayersToWorld("world_run");
    }

    public void endRun() {
        // teleport players back to main/lobby world
        teleportPlayersToWorld("world");
    }

    private void teleportPlayersToWorld(String name) {
        World world = Bukkit.getWorld(name);
        if (world == null) {
            this.plugin.getLogger().info("Could not teleport players to '" + name + "'. That would could not be found.");
        }
        Location spawnLocation = world.getSpawnLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(spawnLocation);
        }
    }

    private void unloadAndDeleteWorld(String name) {
        World world = Bukkit.getWorld(name);
        if (world == null) {
            this.plugin.getLogger().info("Could not delete '" + name + "'. That world could not be found.");
            return;
        }

        // unload world
        boolean unloaded = Bukkit.unloadWorld(world, false);
        this.plugin.getLogger().info("Unloaded '" + world.getName() + "': " + unloaded);

        // delete world
        File deleteFolder = world.getWorldFolder();
        if (deleteFolder.exists()){
            try {
                FileUtils.deleteDirectory(deleteFolder);
            }
            catch (IOException e) {
                this.plugin.getLogger().info("World folder for '" + world.getName() + "' could not be found.");
            }
        }
    }

    private World createNewWorld(String name, World.Environment environment, WorldType worldType) {
        WorldCreator creator = new WorldCreator(name);
        creator.environment(environment);
        creator.type(worldType);
        return creator.createWorld();
    }

    public void setPlugin(SharedHealthPlugin plugin){
        this.plugin = plugin;
    }
}
