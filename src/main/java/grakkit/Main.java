package grakkit;

import org.bukkit.plugin.java.JavaPlugin;

import org.graalvm.polyglot.Value;

public class Main extends JavaPlugin {

   static {
      Core.patch(Main.class); // patch core on class load (core stage 1)
   }

   public void onLoad() {
      Wrapper.init(this.getServer());
   }

   public void onEnable() {
      this.getConfig().options().copyDefaults(true);
      this.saveDefaultConfig();
      this.getServer().getScheduler().runTaskTimer(this, Core::loop, 0, 1); // begin task loop (core stage 2)
      Core.init(this.getDataFolder().getPath(), this.getConfig().getString("main", "index.js")); // open core on load (core stage 3)
   }

   public void onDisable() {
      Core.close(); // close core on unload (core stage 4)
      Wrapper.close();
   }

   public void register (String namespace, String name, String[] aliases, String permission, String message, Value executor, Value tabCompleter) {
      Wrapper.register(namespace, name, aliases, permission, message, executor, tabCompleter);
   }
}