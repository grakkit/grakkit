package grakkit;

import java.io.File;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

public final class core {

   public static Context context;

   public static boolean init (File index) {
      
      // close context to prepare for new one
      if (core.context instanceof Context) core.context.close();

      // create context
      core.context = Context.newBuilder("js")
         .allowAllAccess(true)
         .allowExperimentalOptions(true)
         .option("js.nashorn-compat", "true")
         .option("js.commonjs-require", "true")
         .option("js.commonjs-require-cwd", "./plugins/grakkit")
         .build();

      // check if index exists
      if (index.exists()) {

         // evaluate index.js
         try {
            core.context.eval(Source.newBuilder("js", index).mimeType("application/javascript+module").cached(false).build());
         } catch (Exception error) {

            // handle script errors
            error.printStackTrace(System.err);
         }

         // return status
         return true;
      } else {
            
         // return status
         return false;
      }
   }
}