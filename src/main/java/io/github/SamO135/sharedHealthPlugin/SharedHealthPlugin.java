package io.github.SamO135.sharedHealthPlugin;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.WorldCreator;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SharedHealthPlugin extends JavaPlugin {
    public boolean loggingEnabled = false;
    private Timer timer;
    private AttemptTracker attemptTracker;
    private Set<UUID> showTimerPlayers = new HashSet<>();
    private Set<UUID> showAttemptsPlayers = new HashSet<>();
    private DimensionResetHandler dimensionResetHandler;

    @Override
    public void onEnable() {
        // register the player listener
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldTravelListener(), this);

        // register the commands
        SharedHealthPluginCommands commandTree = new SharedHealthPluginCommands(this);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(commandTree.getCommandNode());
        });

        // set up timer
        timer = new Timer(this);
        Bukkit.getScheduler().runTaskTimer(this, timer, 20L, 20L);

        // set up attempt tracker
        attemptTracker = new AttemptTracker();

        // set up dimension handler
        dimensionResetHandler = DimensionResetHandler.getInstance();
        dimensionResetHandler.setPlugin(this);

        // load custom dimension - needed so players can rejoin the server in this dimension
        new WorldCreator("world_run").createWorld();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getScheduler().cancelTasks(this);

        if (attemptTracker != null) {
            attemptTracker.deleteBossBar();
        }

        // Save data here
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

    public boolean isTimerShownFor(Player player) {
        return showTimerPlayers.contains(player.getUniqueId());
    }

    public void hideTimerFor(Player player) {
        showTimerPlayers.remove(player.getUniqueId());
    }

    public void showTimerFor(Player player) {
        showTimerPlayers.add(player.getUniqueId());
    }

    public boolean isAttemptsShownFor(Player player) {
        return showAttemptsPlayers.contains(player.getUniqueId());
    }

    public void showAttemptsFor(Player player) {
        showAttemptsPlayers.add(player.getUniqueId());
    }

    public void hideAttemptsFor(Player player) {
        showAttemptsPlayers.remove(player.getUniqueId());
    }

    public DimensionResetHandler getDimensionResetHandler() {
        return dimensionResetHandler;
    }

    public AttemptTracker getAttemptTracker() {
        return attemptTracker;
    }

    public void resetPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // clear inventory and potion effects
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

            // reset health and hunger
            double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getDefaultValue();
            player.setHealth(maxHealth);
            player.setFoodLevel(20);
            player.setSaturation(5f);

            // reset experience
            player.setLevel(0);
            player.setExp(0f);

            // extinguish if on fire
            player.setFireTicks(0);

            // reset velocity
            player.setVelocity(player.getVelocity().setX(0).setY(0).setZ(0));

            // reset fall damage
            player.setFallDistance(0f);

            // set gamemode to adventure
            player.setGameMode(GameMode.ADVENTURE);
        }
    }

    public void setPlayerGameMode(GameMode gameMode) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setGameMode(gameMode);
        }
    }
}
