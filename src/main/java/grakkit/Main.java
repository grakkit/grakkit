package grakkit;

import org.bukkit.plugin.java.JavaPlugin;

import org.graalvm.polyglot.Value;

public class Main extends JavaPlugin {

   static {
      Core.patch(new Loader());
   }

   public void onLoad() {
      Wrapper.init(this.getServer());
   }

   public void onEnable() {
      this.getConfig().options().copyDefaults(true);
      this.saveDefaultConfig();
      this.getServer().getScheduler().runTaskTimer(this, Core::loop, 0, 1);
      Core.init(this.getDataFolder().getPath(), this.getConfig().getString("main", "index.js"));
   }

   public void onDisable() {
      Core.close();
      Wrapper.close();
   }

   public void register (String namespace, String name, String[] aliases, String permission, String message, Value executor, Value tabCompleter) {
      Wrapper.register(namespace, name, aliases, permission, message, executor, tabCompleter);
   }
}
