package io.github.SamO135.sharedHealthPlugin;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SharedHealthPlugin extends JavaPlugin {
    public boolean loggingEnabled = false;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);

        SharedHealthPluginCommands commandTree = new SharedHealthPluginCommands(this);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(commandTree.getCommandNode());
        });
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
}
