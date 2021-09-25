package grakkit;

import net.minestom.server.MinecraftServer;

import net.minestom.server.extensions.Extension;

import net.minestom.server.utils.time.TimeUnit;

public class Main extends Extension {

   @Override
   public void initialize() {
      Grakkit.patch(new Loader(this.getClass().getClassLoader())); // CORE - patch class loader with GraalJS
   }

   @Override
   public void postInitialize() {
      MinecraftServer.getSchedulerManager().buildTask(() -> {
         Grakkit.tick();
      }).repeat(1, TimeUnit.TICK).schedule(); // CORE - run task loop
      Grakkit.init(this.getDataDirectory().toString(), MainInstance::new); // CORE - initialize
   }

   @Override
   public void terminate() {
      Grakkit.close(); // CORE - close before exit
   }
}
