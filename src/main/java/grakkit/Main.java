package grakkit;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import org.graalvm.polyglot.Value;

class CustomCommand extends Command {
   
   public Value executor;
   public Value tabCompleter;

   public static Value empty = Value.asValue((Runnable) () -> {});
   public static CommandMap registry;
   public static Map<String, CustomCommand> commands = new HashMap<>();

   public CustomCommand (String name, String[] aliases, String permission, String message, Value executor, Value tabCompleter) {
      super(name, "", "", Arrays.asList(aliases));
      this.executor = executor;
      this.tabCompleter = tabCompleter;
      if (permission.length() > 0) {
         this.setPermission(permission);
         this.setPermissionMessage(message);
      }
   }

   @Override
   public boolean execute (CommandSender sender, String label, String[] args) {

      // handle errors
      try {

         // execute script
         this.executor.execute(sender, label, args);
      } catch (Exception error) {

         // log error
         error.printStackTrace(System.err);
      }

      // report command success to server
      return true;
   }

   @Override
   public List<String> tabComplete (CommandSender sender, String alias, String[] args) {

      // create list
      List<String> output = Arrays.asList();

      // handle errors
      try {

         // add tab completions to output
         Arrays.asList(this.tabCompleter.execute(sender, alias, args)).forEach(value -> output.add(value.toString()));
      } catch (Exception error) {

         // log error
         error.printStackTrace(System.err);
      }

      // send tab completions to server
      return output;
   }
}

public class Main extends JavaPlugin {

   public void reload () {

      // disable plugin
      getServer().getPluginManager().disablePlugin(this);

      // enable plugin
      getServer().getPluginManager().enablePlugin(this);
   }

   public static void register (String namespace, String name, String[] aliases, String permission, String message, Value executor, Value tabCompleter) {

      // define key
      String key = namespace + ":" + name;

      // check if command already exists
      if (CustomCommand.commands.containsKey(key)) {

         // modify existing command
         CustomCommand command = CustomCommand.commands.get(key);
         command.setPermission(permission);
         command.setPermissionMessage(message);
         command.executor = executor;
         command.tabCompleter = tabCompleter;
      } else {

         // create new command
         CustomCommand command = new CustomCommand(name, aliases, permission, message, executor, tabCompleter);
         CustomCommand.registry.register(namespace, command);
         CustomCommand.commands.put(key, command);
      }
   }

   @Override
   public void onLoad() {

      // handle errors
      try {

         // reflect the registry
         Field internal = getServer().getClass().getDeclaredField("commandMap");
         internal.setAccessible(true);
         CustomCommand.registry = (CommandMap) internal.get(getServer());
      } catch (Exception error) {

         // log error
         error.printStackTrace(System.err);

         // disable plugin
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }

   @Override
   public void onEnable() {

      // handle errors
      try {

         // create plugin folder
         this.getDataFolder().mkdir();

         // copy default configuration
         this.getConfig().options().copyDefaults(true);
         this.saveDefaultConfig();

         // load context
         Core.open(getDataFolder().getPath(), getConfig().getString("main", "index.js"));

         // begin tick loop
         this.getServer().getScheduler().runTaskTimer(this, Core::loop, 0, 1);
      } catch (Exception error) {

         // log error
         error.printStackTrace(System.err);

         // disable plugin
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }

   @Override
   public void onDisable() {

      // close context
      Core.close();

      // de-reference command scripts
      CustomCommand.commands.values().forEach(command -> {
         command.executor = CustomCommand.empty;
         command.tabCompleter = CustomCommand.empty;
      });
   }
}