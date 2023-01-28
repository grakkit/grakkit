package io.github.grakkit.bootstrapper.spigot;

import java.lang.reflect.Field;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.function.Consumer;

import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.grakkit.Grakkit;

public class SpigotBootstrap extends JavaPlugin {
        /** A list of all registered commands. */
        public static final HashMap<String, Wrapper> commands = new HashMap<>();

        /** The internal command map used to register commands. */
        public static CommandMap registry;

        /** Internal consumer for onDisable */
        public static Consumer<Void> onDisableCallback; 

    @Override
    public void onLoad() {
        // Black magic. This fixes a bug, as something is breaking SQL Integration for other plugins. 
        // See https://github.com/grakkit/grakkit/issues/14.
        DriverManager.getDrivers();

        try {
            Field internal = this.getServer().getClass().getDeclaredField("commandMap");
            internal.setAccessible(true);
            SpigotBootstrap.registry = (CommandMap) internal.get(this.getServer());
        } catch (Throwable error) {
            error.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        Grakkit.platform = "spigot";
        Grakkit.dataDir = this.getDataFolder().getPath();
        Grakkit.initialiseCore();
    }
}