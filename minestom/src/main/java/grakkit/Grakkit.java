package grakkit;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.HashMap;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public class Grakkit {

   /** the base path of the environment */
   private static String base;

   /** the entry point path, relative to the base */
   private static String main;

   /** the polyglot context */
   private static Context context;

   /** a list of unload hooks */
   private static final Superset hooks = new Superset();

   /** a list of currently scheduled tasks */
   private static final Superset tasks = new Superset();

   /** a cached map of path names to classloaders */
   private static final HashMap<String, URLClassLoader> loaders = new HashMap<String, URLClassLoader>();

   /** inject polyglot into the classpath if not available at runtime */
   static void patch (Loader loader) {
      try {
         loader.addURL(Grakkit.locate(Grakkit.class));
         Thread.currentThread().setContextClassLoader((ClassLoader) loader);
      } catch (Throwable error) {
         throw new RuntimeException("Failed to load classes!", error);
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

   /** initialize base and entry point paths, then open core */ 
   public static void init (String base, String main) {
      Grakkit.base = base;
      Grakkit.main = main;
      Grakkit.open();
   }

   /** locate the entry point and run it in a new context */ 
   public static void open ()  {
      File index = Paths.get(Grakkit.base, Grakkit.main).toFile();
      try {
         Grakkit.context = Context.newBuilder("js")
            .allowAllAccess(true)
            .allowExperimentalOptions(true)
            .option("js.nashorn-compat", "true")
            .option("js.commonjs-require", "true")
            .option("js.commonjs-require-cwd", Grakkit.base)
            .build();
         Grakkit.context.getBindings("js").putMember("Grakkit", Value.asValue(new Grakkit()));
         if (index.exists()) {
            Grakkit.context.eval(Source.newBuilder("js", index).mimeType("application/javascript+module").build());
         } else {
            index.createNewFile();
         }
      } catch (Throwable error) {
         error.printStackTrace(System.err);
      }
   }

   /** release all scheduled tasks */ 
   public static void loop () {
      Grakkit.tasks.release();
   }

   /** release all unload hooks, clear all tasks, and close the context */ 
   public static void close () {
      Grakkit.hooks.release();
      Grakkit.tasks.list.clear();
      Grakkit.context.close();
   }

   /** add an unload hook */
   public void hook (Value script) {
      Grakkit.hooks.list.add(script);
   }

   /** schedule a task in this thread */
   public void push (Value script) {
      Grakkit.tasks.list.add(script);
   }

   /** schedule a task in a new thread */
   public void sync (Value script) {
      Grakkit.tasks.list.add(Value.asValue(new Thread(script::execute)));
   }

   /** close and re-open the environment */
   public void swap () {
      Grakkit.close(); Grakkit.open();
   }

   /** return the current base path */
   public String getRoot () {
      return Grakkit.base;
   }

   /** load classes from external files */
   public Class<?> load (File source, String name) throws ClassNotFoundException, MalformedURLException {
      URL link = source.toURI().toURL();
      String path = source.toPath().normalize().toString();
      return Class.forName(name, true, Grakkit.loaders.computeIfAbsent(path, (key) -> {
         return new URLClassLoader(
            new URL[] { link },
            Grakkit.class.getClassLoader()
         );
      }));
   }
}
