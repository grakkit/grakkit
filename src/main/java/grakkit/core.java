package grakkit;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public final class core {

   public static Context context;

   public static Set<Value> queue;

   public static Map<String, Value> methods = new HashMap<>();

   static {
      methods.put("queue", Value.asValue((Consumer<Value>) (script) -> {

         // add script to queue
         queue.add(script);
      }));
      /*
      methods.put("execute", Value.asValue((Consumer<Value>) (script) -> {
         
         // create new thread
         queue.add(Value.asValue((Runnable) () -> new Thread(() -> script.execute()).run()));
      }));
      */
   }

   public static void tick () {
      queue.forEach(value -> {
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
            core.context.getBindings("js").putMember("Core", Value.asValue(methods));
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