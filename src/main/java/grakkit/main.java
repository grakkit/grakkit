package grakkit;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public final class main extends JavaPlugin {

   public static Context context;
   public static CommandMap registry;
   public static Map<String, CustomCommand> commands = new HashMap<String, CustomCommand>();

   public void reload () {
      this.getServer().getPluginManager().disablePlugin(this);
      this.getServer().getPluginManager().enablePlugin(this);
   }

   public void register (String key, String name, String description, String usage, List<String> aliases, String permission, String message, String fallback, Value executor, Value tabCompleter) {
      
      // check if command already exists
      if (commands.containsKey(key)) {

         // modify existing command
         CustomCommand command = commands.get(key);
         command.setUsage(usage);
         command.setDescription(description);
         command.setPermission(permission);
         command.setPermissionMessage(message);
         command.executor = executor;
         command.tabCompleter = tabCompleter;
      } else {

         // create new command
         CustomCommand command = new CustomCommand(name, description, usage, aliases, permission, message, fallback, executor, tabCompleter);
         registry.register(fallback, command);
         commands.put(key, command);
      }
   }

   public static URL locate (Class<?> clazz) {
      try {
         URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
         if (location != null) return location;
      } catch (SecurityException | NullPointerException error) {
         // ignore errors, need to try other methods
      }
      URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
      if (resource == null) return null;
      String link = resource.toString();
      String suffix = clazz.getCanonicalName().replace('.', '/') + ".class";
      if (!link.endsWith(suffix)) return null;
      String base = link.substring(0, link.length() - suffix.length()), path = base;
      if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);
      try {
         return new URL(path);
      } catch (MalformedURLException error) {
         return null;
      }
   }

   @Override
   public void onLoad() {
      try {

         // add grakkit to classpath
         URLClassLoader loader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
         Class<URLClassLoader> clazz = URLClassLoader.class;
         Method method = clazz.getDeclaredMethod("addURL", URL.class);
         method.setAccessible(true);
         method.invoke(loader, main.locate(main.class));

         // expose command map (reflection)
         Field internal = this.getServer().getClass().getDeclaredField("commandMap");
         internal.setAccessible(true);
         main.registry = (CommandMap) internal.get(this.getServer());
      } catch (Exception error) {

         // handle init errors and exit
         error.printStackTrace(System.err);
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }

   @Override
   public void onEnable() {

      // create context
      main.context = Context.newBuilder("js")
         .allowAllAccess(true)
         .allowExperimentalOptions(true)
         .option("js.nashorn-compat", "true")
         .option("js.commonjs-require", "true")
         .build();

      // create plugin folder & get index file
      this.getDataFolder().mkdir();
      File index = Paths.get(this.getDataFolder().getPath(), "index.js").toFile();

      // check if index exists
      if (index.exists()) {

         // evaluate index.js
         try {
            main.context.eval(Source.newBuilder("js", index).mimeType("application/javascript+module").cached(false).build());
         } catch (Exception error) {

            // handle script errors
            error.printStackTrace(System.err);
         }
      } else {
            
         // handle missing index and exit
         this.getServer().getLogger().severe("The file \"plugins/grakkit/index.js\" could not be found. [explain why file does/might not exist] [link to instructions]");
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }

   @Override
   public void onDisable() {

      // de-reference executors and tab-completers for each command
      commands.values().forEach(command -> {
         command.executor = Value.asValue(new Object());
         command.tabCompleter = Value.asValue(new Object());
      });
      
      // close context to prepare for new one on reload
      main.context.close();
   }
}