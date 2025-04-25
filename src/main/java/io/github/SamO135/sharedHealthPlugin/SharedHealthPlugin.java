package io.github.SamO135.sharedHealthPlugin;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SharedHealthPlugin extends JavaPlugin {
    public boolean loggingEnabled = false;
    private Timer timer;
    private Set<UUID> hiddenTimerPlayers = new HashSet<>();
    private DimensionResetHandler dimensionResetHandler;

    @Override
    public void onEnable() {
        // register the player listener
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // register the commands
        SharedHealthPluginCommands commandTree = new SharedHealthPluginCommands(this);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(commandTree.getCommandNode());
        });

        // set up timer
        timer = new Timer(this);
        Bukkit.getScheduler().runTaskTimer(this, timer, 20L, 20L);

        // set up dimension handler
        dimensionResetHandler = DimensionResetHandler.getInstance();
        dimensionResetHandler.setPlugin(this);

        // load custom dimension - needed so players can rejoin the server in this dimension
        new WorldCreator("world_run").createWorld();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    protected double syncPlayerHealth() {
        double sharedHealth = Bukkit.getOnlinePlayers().stream().mapToDouble(Player::getHealth).min().orElse(20d);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setHealth(sharedHealth);
        }
        return sharedHealth;
    }

    public Timer getTimer() {
        return timer;
    }

    public boolean isTimerHiddenFor(Player player) {
        return hiddenTimerPlayers.contains(player.getUniqueId());
    }

    public void hideTimerFor(Player player) {
        hiddenTimerPlayers.add(player.getUniqueId());
    }

    public void showTimerFor(Player player) {
        hiddenTimerPlayers.remove(player.getUniqueId());
    }

    public DimensionResetHandler getDimensionResetHandler() {
        return dimensionResetHandler;
    }
}
