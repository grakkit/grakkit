package grakkit;

import java.lang.reflect.Method;
import java.sql.DriverManager;

import net.fabricmc.api.ModInitializer;

public class Main implements ModInitializer {
   @Override
   public void onInitialize() {
      // Make getClassLoader accessible
      try {
         Method clsLdr = Main.class.getDeclaredMethod("getClassLoader");
         clsLdr.setAccessible(true);
      } catch (Throwable error) {
         error.printStackTrace();
      }

      // Black magic. This fixes a bug, as something is breaking SQL Integration for other plugins.
      // See https://github.com/grakkit/grakkit/issues/14.
      DriverManager.getDrivers();
      Grakkit.patch(new Loader(this.getClassLoader())); // CORE - patch class loader with GraalJS
      
   }
}
