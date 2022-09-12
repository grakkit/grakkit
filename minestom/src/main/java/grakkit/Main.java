package grakkit;

import net.minestom.server.MinecraftServer;

import net.minestom.server.extensions.Extension;

import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.TaskSchedule;

public class Main extends Extension {
   private SchedulerManager schedulerManager = MinecraftServer.getSchedulerManager();
   @Override

   public void initialize() {
      Grakkit.patch(new Loader(this.getClass().getClassLoader())); // CORE - patch class loader with GraalJS
      Grakkit.init(this.getDataDirectory().toString());
      schedulerManager.submitTask(() -> {
          Grakkit.tick();
          return TaskSchedule.seconds(1);
      });
      this.getLogger().info("[Grakkit] Initialized!");
   }
   @Override
   public void terminate() {
      Grakkit.close(); // CORE - close before exit
   }
}
