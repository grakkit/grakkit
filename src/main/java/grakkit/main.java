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
            // change this URL to a wiki page later on
            this.getServer().getLogger().severe("The file \"plugins/grakkit/index.js\" could not be found. This file is likely missing because core is not installed. To install core, head on over to https://github.com/grakkit/grakkit and follow the instructions there.");
            this.getServer().getPluginManager().disablePlugin(this);
         } else {
            main.context = Context.newBuilder("js").allowAllAccess(true).allowExperimentalOptions(true).build();
            try {
               main.context.eval(Source.newBuilder("js", local).cached(false).build());
            } catch (Exception error) {
               error.printStackTrace(System.err);
            }
         }
      } catch (Exception error) {
         error.printStackTrace(System.err);
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }

   @Override
   public void onDisable() {
   }
}