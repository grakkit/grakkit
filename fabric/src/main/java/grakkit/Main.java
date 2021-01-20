package grakkit;

import net.fabricmc.api.ModInitializer;

import org.graalvm.polyglot.Value;

public class Main implements ModInitializer {

   public void onInitialize () {
   }

   // TODO - run this code on client start
   public void onLoad() {
      Core.patch(new Loader(this.getClassLoader())); // CORE - patch class loader with GraalJS
   }

   // TODO - run this code when entering game world
   public void onEnable() {

      // TODO - create default config if not already created
      this.getConfig().options().copyDefaults(true);
      this.saveDefaultConfig();

      // TODO - run Core::loop on every game tick
      this.getServer().getScheduler().runTaskTimer(this, Core::loop, 0, 1); // CORE - run task loop

      // TODO - parse "main" field from YML config file
      Core.init(this.getDataFolder().getPath(), this.getConfig().getString("main", "index.js")); // CORE - initialize
   }

   // TODO - run this code when exiting game world
   public void onDisable() {
      Core.close(); // CORE - close before exit
   }
}
