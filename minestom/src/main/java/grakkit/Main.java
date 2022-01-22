package grakkit;

import net.minestom.server.MinecraftServer;

import net.minestom.server.extensions.Extension;

import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.time.TimeUnit;

public class Main extends Extension {
   private SchedulerManager schedulerManager = MinecraftServer.getSchedulerManager();
   @Override

   public LoadStatus initialize() {
      Grakkit.patch(new Loader(this.getClass().getClassLoader())); // CORE - patch class loader with GraalJS
      schedulerManager.buildTask(() -> {
         schedulerManager.buildTask(() -> {
            Grakkit.tick();
         }).repeat(1, TimeUnit.TICK).schedule(); // CORE - run task loop
         Grakkit.init(this.getDataDirectory().toString()); // CORE - initialize
      }).delay(50, TimeUnit.MILLISECOND).schedule(); // delay 1/2 a second
      this.getLogger().info("[Grakkit] Initialized!");
      return LoadStatus.SUCCESS;
   }
   @Override
   public void terminate() {
      Grakkit.close(); // CORE - close before exit
   }
}
