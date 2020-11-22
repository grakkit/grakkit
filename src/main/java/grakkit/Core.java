package grakkit;

import java.io.File;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public final class Core {

   public static Context context;
   public static List<Value> queue = new LinkedList<>();

   static {
      new Timer().schedule((TimerTask) (Runnable) () -> {
         new LinkedList<Value>(queue).forEach(value -> {
            value.execute();
            queue.remove(value);
         });
      }, 0, 1);
   }

   public static void load (String path, String... more) throws Exception {
      
      // close context to prepare for new one
      if (Core.context instanceof Context) Core.context.close();

      // get index file from config
      File index = Paths.get(path, more).toFile();

      // check if index exists
      if (index.exists()) {
   
         // create context
         Core.context = Context.newBuilder("js")
            .allowAllAccess(true)
            .allowExperimentalOptions(true)
            .option("js.nashorn-compat", "true")
            .option("js.commonjs-require", "true")
            .option("js.commonjs-require-cwd", "./plugins/grakkit")
            .build();

         // evaluate index
         try {
            Core.context.getBindings("js").putMember("Core", Value.asValue(new Core()));
            Core.context.eval(Source.newBuilder("js", index).mimeType("application/javascript+module").build());
         } catch (Exception error) {

            // handle script errors
            error.printStackTrace(System.err);
         }
      } else {

         // handle failed init
         throw new Exception("The entry point \"" + index.getPath().replace('\\', '/') + "\" could not be found!");
      }
   }

   public void queue (Value script) {
      if (script.canExecute()) queue.add(script);
   }

   public void execute (Value script) {
      if (script.canExecute()) queue.add(Value.asValue((Runnable) () -> new Thread(() -> script.execute()).run()));
   }
}