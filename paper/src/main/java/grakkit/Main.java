package grakkit;

import java.lang.reflect.Field;
import java.sql.DriverManager;

import java.util.HashMap;
import java.util.function.Consumer;

import org.bukkit.command.CommandMap;

import org.bukkit.plugin.java.JavaPlugin;

import org.graalvm.polyglot.Value;

public class Main extends JavaPlugin {

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
      Grakkit.patch(new Loader(this.getClassLoader())); // CORE - patch class loader with GraalJS
      try {
         Field internal = this.getServer().getClass().getDeclaredField("commandMap");
         internal.setAccessible(true);
         Main.registry = (CommandMap) internal.get(this.getServer());
      } catch (Throwable error) {
         error.printStackTrace();
      }
   }

   @Override
   public void onEnable() {
      try {
         this.getServer().getScheduler().runTaskTimer(this, Grakkit::tick, 0, 1); // CORE - run task loop
      } catch (Throwable error) {
         // none
      }
      Grakkit.init(this.getDataFolder().getPath()); // CORE - initialize
   }

   @Override
   public void onDisable() {
      try {
         if (Main.onDisableCallback != null) {
            Main.onDisableCallback.accept(null);
         }
      } catch (Throwable error) {
         // none
      }

      Grakkit.close(); // CORE - close before exit
      Main.commands.values().forEach(command -> {
         command.executor = Value.asValue((Runnable) () -> {});
         command.tabCompleter = Value.asValue((Runnable) () -> {});
      });
   }

   /** Registers a custom command to the server with the given options. */
   public void register (String namespace, String name, String[] aliases, String permission, String message, Value executor, Value tabCompleter) {
      String key = namespace + ":" + name;
      Wrapper command;
      if (Main.commands.containsKey(key)) {
         command = Main.commands.get(key);
      } else {
         command = new Wrapper(name, aliases);
         Main.registry.register(namespace, command);
         Main.commands.put(key, command);
      }
      command.options(permission, message, executor, tabCompleter);
   }

   /**
    * Allow developers to pass in a callback to the `onDisable` function.
    * @param fn
    */
   public void registerOnDisable(Consumer<Void> fn) {
      Main.onDisableCallback = fn;
   }
}
