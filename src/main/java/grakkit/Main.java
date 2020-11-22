package grakkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import org.graalvm.polyglot.Value;

public final class Main extends JavaPlugin {

   public static CommandMap registry;
   public static Map<String, Custom> commands = new HashMap<>();

   public void reload () {
      getServer().getPluginManager().disablePlugin(this);
      getServer().getPluginManager().enablePlugin(this);
   }

   public void register (String namespace, String name, List<String> aliases, String permission, String message, Value executor, Value tabCompleter) {

      // define key
      String key = namespace + ":" + name;

      // check if command already exists
      if (Main.commands.containsKey(key)) {

         // modify existing command
         Custom command = commands.get(key);
         command.setPermission(permission);
         command.setPermissionMessage(message);
         command.executor = executor;
         command.tabCompleter = tabCompleter;
      } else {

         // create new command
         Custom command = new Custom(name, aliases, permission, message, executor, tabCompleter);
         Main.registry.register(namespace, command);
         Main.commands.put(key, command);
      }
   }

   @Override
   public void onLoad() {

      // reflect the registry
      try {
         Field internal = getServer().getClass().getDeclaredField("commandMap");
         internal.setAccessible(true);
         Main.registry = (CommandMap) internal.get(getServer());
      } catch (Exception error) {

         // handle errors and exit
         error.printStackTrace(System.err);
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }

   @Override
   public void onEnable() {
      try {

         // de-reference executors and tab-completers for each command (doesnt work)
         Main.commands.values().forEach(command -> {
            command.executor = null;
            command.tabCompleter = null;
         });

         // create plugin folder
         this.getDataFolder().mkdir();

         // copy default config from resources
         this.getConfig().options().copyDefaults(true);

         // save config
         this.saveDefaultConfig();

         // load context
         Core.load(getDataFolder().getPath(), getConfig().getString("main", "index.js"));
      } catch (Exception error) {

         // handle errors and exit
         error.printStackTrace(System.err);
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }

   @Override
   public void onDisable() {
   }

   private static URL locate (Class<?> clazz) {
      try {
         URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
         if (location instanceof URL) return location;
      } catch (SecurityException | NullPointerException error) {
         // try other method instead of throwing error
      }
      URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
      if (resource instanceof URL) {
         String link = resource.toString();
         String suffix = clazz.getCanonicalName().replace('.', '/') + ".class";
         if (link.endsWith(suffix)) {
            String base = link.substring(0, link.length() - suffix.length()), path = base;
            if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);
            try {
               return new URL(path);
            } catch (Exception error) {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static {
      try {
         Class.forName("org.graalvm.polyglot.Value");
      } catch (Exception error1) {
         try {
            URLClassLoader loader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
            Class<URLClassLoader> clazz = URLClassLoader.class;
            Method method = clazz.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(loader, Main.locate(Main.class));
         } catch (Exception error2) {
            throw new RuntimeException("Failed to add plugin to class path!", error2);
         }
      }
   }
}