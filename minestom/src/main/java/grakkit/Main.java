package grakkit;

import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.Extension;
import net.minestom.server.utils.time.TimeUnit;

public class Main extends Extension {
   @Override
   public void initialize() {
      Core.patch(new Loader(this.getClass().getClassLoader()));
   }
   @Override
   public void postInitialize() {
      MinecraftServer.getSchedulerManager().buildTask(() -> {
         Core.loop();
      }).repeat(1, TimeUnit.TICK).schedule();
      Core.init(this.getDataDirectory().toString(), "index.js");
   }

   @Override
   public void terminate() {
      Core.close();
      Wrapper.close();
   }
}