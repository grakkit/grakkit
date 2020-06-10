package xyz.grakkit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.net.URL;

import org.bukkit.plugin.java.JavaPlugin;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import command.AbstractCommand;

public final class core extends JavaPlugin {

   // initialize variables
   public static Context context;

   // expose command registry
   public boolean register(String prefix, String command, String usage, String description, String execute,
         String tabComplete) {
      AbstractCommand custom = new command(command, usage, description, execute, tabComplete);
      return custom.register(prefix);
   }

   @Override
   public void onEnable() {

      // handle errors
      try {

         // build engine
         Context.Builder builder = Context.newBuilder("js");
         builder.allowAllAccess(true);
         builder.allowExperimentalOptions(true);
         builder.option("js.nashorn-compat", "true");
         Context context = builder.build();

         // expose context
         core.context = context;

         // find config folder
         File config = Paths.get("plugins/grakkit").toAbsolutePath().toFile();

         // create config folder (if applicable)
         config.mkdir();

         // find index file
         File index = Paths.get("plugins/grakkit/index.js").toAbsolutePath().toFile();

         // create index file (if applicable)
         if (!index.exists()) {

            // initialize variables
            int bytes = 0;
            byte[] buffer = new byte[4096];

            // create streams
            InputStream input = new URL("https://raw.githubusercontent.com/grakkit/core/master/index.min.js")
                  .openStream();
            FileOutputStream output = new FileOutputStream(index);

            // transfer data
            while ((bytes = input.read(buffer)) > 0)
               output.write(buffer, 0, bytes);

            // close streams
            input.close();
            output.close();
         }

         // handle evaluation errors
         try {

            // evaluate index file
            context.eval(Source.newBuilder("js", index).build());
         } catch (Exception error) {

            // print evaluation error
            error.printStackTrace(System.err);
         }
      } catch (Exception error) {

         // print error
         error.printStackTrace(System.err);

         // disable plugin
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }
}