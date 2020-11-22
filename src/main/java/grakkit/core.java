package grakkit;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public final class core {

   public static Context context;

   public static List<Value> queue = new LinkedList<>();

   public static Map<String, Value> methods = new HashMap<>();

   public static void tick () {
      new LinkedList<Value> (queue).forEach(value -> {
         value.execute();
         queue.remove(value);
      });
   }

   public static boolean load (File index) {
      
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
            core.context.getBindings("js").putMember("Core", Value.asValue(new core()));
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

   public void queue (Value script) {
      queue.add(script);
   }

   public void execute (Value script) {
      queue.add(Value.asValue((Runnable) () -> new Thread(() -> script.execute()).run()));
   }
}