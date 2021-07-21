package grakkit;

/*
import com.caoccao.javet.interop.V8Host;
import com.caoccao.javet.interop.V8Runtime;
*/

import org.bukkit.plugin.java.JavaPlugin;

import org.graalvm.polyglot.Value;

public class Main extends JavaPlugin {

   @Override
   public void onLoad() {

      /*
      try {
         V8Runtime v8Runtime = V8Host.getNodeInstance().createV8Runtime();
         System.out.println(v8Runtime.getExecutor("'test'").executeString());
      } catch (Exception error) {
         // do nothing
      }
      */

      Grakkit.patch(new Loader(this.getClassLoader())); // CORE - patch class loader with GraalJS
      Wrapper.init(this.getServer());
   }

   @Override
   public void onEnable() {
      this.getConfig().options().copyDefaults(true);
      this.saveDefaultConfig();
      this.getServer().getScheduler().runTaskTimer(this, Grakkit::loop, 0, 1); // CORE - run task loop
      Grakkit.init(this.getDataFolder().getPath(), this.getConfig().getString("main", "index.js")); // CORE - initialize
   }

   @Override
   public void onDisable() {
      Grakkit.close(); // CORE - close before exit
      Wrapper.close();
   }

   public void register (String namespace, String name, String[] aliases, String permission, String message, Value executor, Value tabCompleter) {
      Wrapper.register(namespace, name, aliases, permission, message, executor, tabCompleter);
   }
}
