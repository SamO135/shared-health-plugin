package io.github.SamO135.sharedHealthPlugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SharedHealthPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
