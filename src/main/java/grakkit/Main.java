package grakkit;

import org.bukkit.plugin.java.JavaPlugin;

import org.graalvm.polyglot.Value;

public class Main extends JavaPlugin {

   public void onLoad() {
      Core.patch(new Loader(this.getClassLoader())); // CORE - patch class loader with GraalJS
      Wrapper.init(this.getServer());
   }

   public void onEnable() {
      this.getConfig().options().copyDefaults(true);
      this.saveDefaultConfig();
      this.getServer().getScheduler().runTaskTimer(this, Core::loop, 0, 1); // CORE - run task loop
      Core.init(this.getDataFolder().getPath(), this.getConfig().getString("main", "index.js")); // CORE - initialize
   }

   public void onDisable() {
      Core.close(); // CORE - close before exit
      Wrapper.close();
   }

   public void register (String namespace, String name, String[] aliases, String permission, String message, Value executor, Value tabCompleter) {
      Wrapper.register(namespace, name, aliases, permission, message, executor, tabCompleter);
   }
}
