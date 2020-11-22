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

      // handle errors
      try {

         // check if graal is installed
         Class.forName("org.graalvm.polyglot.Value");
      } catch (Exception $) {

         // handle errors
         try {

            // load classes from plugin
            URLClassLoader loader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
            Class<URLClassLoader> clazz = URLClassLoader.class;
            Method method = clazz.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(loader, Core.locate(Core.class));
         } catch (Exception error) {

            // throw error
            throw new RuntimeException("Failed to add plugin to class path!", error);
         }
      }
   }

   private static URL locate (Class<?> clazz) {
      try {
         URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
         if (location instanceof URL) return location;
      } catch (SecurityException | NullPointerException error) {
         // try other method instead of throwing error
      }
      URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
      if (resource instanceof URL) {
         String link = resource.toString();
         String suffix = clazz.getCanonicalName().replace('.', '/') + ".class";
         if (link.endsWith(suffix)) {
            String base = link.substring(0, link.length() - suffix.length()), path = base;
            if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);
            try {
               return new URL(path);
            } catch (Exception error) {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static String root;
   public static Context context;
   public static List<Value> hooks = new LinkedList<>();
   public static List<Value> queue = new LinkedList<>();

   public static void loop () {
      new LinkedList<Value>(Core.queue).forEach(value -> {
         value.execute();
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
   
      // trigger unload hooks
      new LinkedList<Value>(Core.hooks).forEach(value -> {
         value.execute();
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