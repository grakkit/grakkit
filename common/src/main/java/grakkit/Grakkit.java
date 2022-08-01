package grakkit;

import java.net.URL;
import java.net.URLClassLoader;

import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.graalvm.polyglot.Value;

public class Grakkit {

   /** All registered cross-context channels. */
   public static final HashMap<String, LinkedList<Value>> channels = new HashMap<>();

   /** The instance running on the main thread. */
   public static FileInstance driver;

   /** All instances created with the instance management system. */
   public static final LinkedList<Instance> instances = new LinkedList<>();

   /** All registered class loaders. */
   public static final HashMap<String, URLClassLoader> loaders = new HashMap<>();

   /** Closes all open instances. */
   public static void close () {
      Grakkit.driver.close();
      new ArrayList<>(Grakkit.instances).forEach(value -> value.destroy());
   }

   /** Initializes the Grakkit Environment. */
   public static void init (String root) {
      Paths.get(root).toFile().mkdir();
      String main = "index.js";
      Config[] configs = {
         new Config(Config.Format.JSON, root, ".grakkitrc", false),
         new Config(Config.Format.YAML, root, "config.yml", false),
         new Config(Config.Format.JSON, root, "grakkit.json", false),
         new Config(Config.Format.JSON, root, "package.json", true)
      };
      for (Config config : configs) {
         if (config.main != null) {
            main = config.main;
            break;
         }
      }
      Grakkit.driver = new FileInstance(root, main, "grakkit");
      try {
         Grakkit.driver.open();
      } catch (Throwable error) {
         error.printStackTrace();
      }
   }

   /** Locates the given class's true source location. */
   public static URL locate (Class<?> clazz) {
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

   /** Executes the task loop for all instances. */
   public static void tick () {
      Grakkit.driver.tick();
      Grakkit.instances.forEach(value -> value.tick());
   }

   /** Updates the current ClassLoader to one which supports the GraalJS engine. */
   public static void patch (Loader loader) {
      try {
         loader.addURL(Grakkit.locate(Grakkit.class));
         Thread.currentThread().setContextClassLoader((ClassLoader) loader);
      } catch (Throwable error) {
         throw new RuntimeException("Failed to load classes!", error);
      }
   }
}
