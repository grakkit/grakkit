package grakkit;

import java.io.File;

import java.lang.reflect.Method;

import java.net.URL;
import java.net.URLClassLoader;

import java.nio.file.Paths;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public class Core {

   /** the base path of the environment */
   private static String base;

   /** the entry point path, relative to the base */
   private static String main;

   /** the polyglot context */
   private static Context context;

   /** a list of unload hooks */
   private static Superset hooks = new Superset();

   /** a list of currently scheduled tasks */
   private static Superset tasks = new Superset();
   
   /** inject polyglot into the classpath if not available at runtime */
   static void patch () {
      try {
         Value.asValue(new Object());
      } catch (Throwable none) {
         try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke((URLClassLoader) ClassLoader.getSystemClassLoader(), Core.locate(Core.class));
         } catch (Throwable error) {
            throw new RuntimeException("Failed to add plugin to class path!", error);
         }
      }
   }
   
   /** locate classes for injection */ 
   private static URL locate (Class<?> clazz) {
      try {
         URL resource = clazz.getProtectionDomain().getCodeSource().getLocation();
         if (resource instanceof URL) return resource;
      } catch (SecurityException | NullPointerException error) {
         // do nothing
      }
      URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
      if (resource instanceof URL) {
         String link = resource.toString();
         String suffix = clazz.getCanonicalName().replace('.', '/') + ".class";
         if (link.endsWith(suffix)) {
            String path = link.substring(0, link.length() - suffix.length());
            if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);
            try {
               return new URL(path);
            } catch (Throwable error) {
               // do nothing
            }
         }
      }
      return null;
   }

   /**initialize base and entry point paths, then open core */ 
   public static void init (String base, String main) {
      Core.base = base;
      Core.main = main;
      Core.open();
   }

   /** locate the entry point and run it in a new context */ 
   public static void open ()  {
      File index = Paths.get(Core.base, Core.main).toFile();
      try {
         Core.context = Context.newBuilder("js")
            .allowAllAccess(true)
            .allowExperimentalOptions(true)
            .option("js.nashorn-compat", "true")
            .option("js.commonjs-require", "true")
            .option("js.commonjs-require-cwd", Core.base)
            .build();
         // Core.context.getBindings("js").putMember("Core", Value.asValue(new Core()));
         Core.context.eval(Source.newBuilder("js", index).mimeType("application/javascript+module").build());
      } catch (Throwable error) {
         error.printStackTrace(System.err);
      }
   }

   /** release all scheduled tasks */ 
   public static void loop () {
      Core.tasks.release();
   }

   /** release all unload hooks, clear all tasks, and close the context */ 
   public static void close () {
      Core.hooks.release();
      Core.tasks.list.clear();
      Core.context.close();
   }

   /** add an unload hook */
   public void hook (Value script) {
      Core.hooks.list.add(script);
   }

   /** schedule a task in this thread */
   public void push (Value script) {
      Core.tasks.list.add(script);
   }
   
   /** schedule a task in a new thread */
   public void sync (Value script) {
      Core.tasks.list.add(Value.asValue(new Thread(script::execute)));
   }

   /** close and re-open the environment */
   public void swap () {
      Core.close(); Core.open();
   }
}