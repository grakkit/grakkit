package grakkit;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.bukkit.plugin.java.JavaPlugin;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

public final class main extends JavaPlugin {

   public static Context context;

   @Override
   public void onEnable() {
      try {
         Paths.get("plugins/grakkit").toFile().mkdir();
         File local = Paths.get("plugins/grakkit/index.js").toFile();
         if (!local.exists()) {
            Files.createFile(local.toPath());
            URL remote = new URL("https://raw.githubusercontent.com/grakkit/core/master/index.min.js");
            remote.openStream().transferTo(new FileOutputStream(local));
         }
         main.context = Context.newBuilder("js").allowAllAccess(true).build();
         try {
            main.context.eval(Source.newBuilder("js", local).cached(false).build());
         } catch (Exception error) {
            error.printStackTrace(System.err);
         }
      } catch (Exception error) {
         error.printStackTrace(System.err);
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }
}