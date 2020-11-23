package grakkit;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public class Core {

   static {

      // check if graalvm is installed
      try {

         // test for polyglot
         Class.forName("org.graalvm.polyglot.Value");
      } catch (Exception $) {

         // handle errors
         try {

            // expose class loader (reflection)
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);

            // load classes
            method.invoke((URLClassLoader) Thread.currentThread().getContextClassLoader(), Core.locate(Core.class));
         } catch (Exception error) {

            // throw error
            throw new RuntimeException("Failed to add plugin to class path!", error);
         }
      }
   }

   private static URL locate (Class<?> clazz) {

      // handle errors
      try {

         // search via protection domain
         URL resource = clazz.getProtectionDomain().getCodeSource().getLocation();
         
         // check for valid resource
         if (resource instanceof URL) {

            // return class location
            return resource;
         }
      } catch (SecurityException | NullPointerException error) {
         // try other method instead of throwing error
      }

      // search via class name
      URL resource = clazz.getResource(clazz.getSimpleName() + ".class");

      // check for valid resouce
      if (resource instanceof URL) {

         // get stringified link
         String link = resource.toString();

         // get valid suffix
         String suffix = clazz.getCanonicalName().replace('.', '/') + ".class";

         // check link for valid suffix
         if (link.endsWith(suffix)) {

            // remove suffix
            String path = link.substring(0, link.length() - suffix.length());

            // handle jar protocol
            if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);

            // handle errors
            try {

               // return class location
               return new URL(path);
            } catch (Exception error) {

               // failed to find class location
               return null;
            }
         } else {

            // failed to find class location
            return null;
         }
      } else {

         // failed to find class location
         return null;
      }
   }

   public static String root;
   public static Context context;
   public static List<Value> hooks = new LinkedList<>();
   public static List<Value> queue = new LinkedList<>();

   public static void loop () {

      // execute all scripts in queue
      new LinkedList<Value>(Core.queue).forEach(value -> {

         // handle errors
         try {

            // execute script
            value.execute();
         } catch (Exception error) {

            // log error
            error.printStackTrace(System.err);
         }

         // remove script
         Core.queue.remove(value);
      });
   }

   public static void open (String path, String main) throws Exception {

      // declare root path
      Core.root = path;

      // get index file from config
      File index = Paths.get(path, main).toFile();

      // check if index exists
      if (index.exists()) {
   
         // create context
         Core.context = Context.newBuilder("js")
            .allowAllAccess(true)
            .allowExperimentalOptions(true)
            .option("js.nashorn-compat", "true")
            .option("js.commonjs-require", "true")
            .option("js.commonjs-require-cwd", path)
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

   public static void close () {

      // remove all scripts in queue
      Core.queue.clear();
   
      // trigger all closure hooks
      new LinkedList<Value>(Core.hooks).forEach(value -> {

         // handle errors
         try {

            // trigger hook
            value.execute();
         } catch (Exception error) {

            // log error
            error.printStackTrace(System.err);
         }

         // remove hook
         Core.hooks.remove(value);
      });
      
      // close context
      Core.context.close();
   }

   public void hook (Value script) {

      // add script to hooks
      if (script.canExecute()) hooks.add(script);
   }

   public void push (Value script) {

      // add script to queue
      if (script.canExecute()) queue.add(script);
   }

   public void sync (Value script) {

      // add thread to queue
      if (script.canExecute()) queue.add(Value.asValue(new Thread(script::execute)));
   }

   public String getRoot () {

      // provide root path
      return Core.root;
   }

   public void setRoot (String path) {

      // declare root path
      Core.root = path;
   }
}