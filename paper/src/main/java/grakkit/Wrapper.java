package grakkit;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Server;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandMap;

import org.graalvm.polyglot.Value;

public class Wrapper extends Command {

   public Wrapper (String name, String[] aliases, String permission, String message, Value executor, Value tabCompleter) {
      super(name, "", "", Arrays.asList(aliases));
      this.executor = executor;
      this.tabCompleter = tabCompleter;
      this.setPermission(permission);
      this.setPermissionMessage(message);
   }
   
   /** the current executor script */
   public Value executor;

   /** the current tab-completer script */
   public Value tabCompleter;

   /** the server command map */
   public static CommandMap registry;

   /** the current list of registered commands */
   public static Map<String, Wrapper> commands = new HashMap<>();

   /** exposes the server command map with reflection */
   public static void init (Server server) {
      try {
         Field internal = server.getClass().getDeclaredField("commandMap");
         internal.setAccessible(true);
         Wrapper.registry = (CommandMap) internal.get(server);
      } catch (Throwable error) {
         error.printStackTrace(System.err);
      }
   }

   /** registers a new command or updates an existing one */
   public static void register (String namespace, String name, String[] aliases, String permission, String message, Value executor, Value tabCompleter) {
      String key = namespace + ":" + name;
      if (Wrapper.commands.containsKey(key)) {
         Wrapper command = Wrapper.commands.get(key);
         command.setPermission(permission);
         command.setPermissionMessage(message);
         command.executor = executor;
         command.tabCompleter = tabCompleter;
      } else {
         Wrapper command = new Wrapper(name, aliases, permission, message, executor, tabCompleter);
         Wrapper.registry.register(namespace, command);
         Wrapper.commands.put(key, command);
      }
   }

   /** unlinks all executors and tab-completers */
   public static void close () {
      Wrapper.commands.values().forEach(command -> {
         command.executor = Value.asValue((Runnable) () -> {});
         command.tabCompleter = Value.asValue((Runnable) () -> {});
      });
   }

   /** runs the current executor script */
   @Override
   public boolean execute (CommandSender sender, String label, String[] args) {
      try {
         this.executor.execute(sender, label, args);
      } catch (Throwable error) {
         error.printStackTrace(System.err);
      }
      return true;
   }

   /** runs and returns the results from the current tab-completer script */
   @Override
   public List<String> tabComplete (CommandSender sender, String alias, String[] args) {
      List<String> output = new ArrayList<>();
      try {
         Value input = this.tabCompleter.execute(sender, alias, args);
         for (long index = 0; index < input.getArraySize(); index++) output.add(input.getArrayElement(index).toString());
      } catch (Throwable error) {
         error.printStackTrace(System.err);
      }
      return output;
   }
}
