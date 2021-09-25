package grakkit;

import java.lang.reflect.Field;

import java.util.HashMap;

import org.bukkit.command.CommandMap;

import org.bukkit.plugin.java.JavaPlugin;

import org.graalvm.polyglot.Value;

public class Main extends JavaPlugin {

   /** A list of all registered commands. */
   public static final HashMap<String, Wrapper> commands = new HashMap<>();

   /** The internal command map used to register commands. */
   public static CommandMap registry;

   @Override
   public void onLoad() {
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
}
