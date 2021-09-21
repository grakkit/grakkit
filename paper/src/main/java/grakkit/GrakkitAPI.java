package grakkit;

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.LinkedList;
import java.util.UUID;

import org.graalvm.polyglot.Value;

public class GrakkitAPI {

   /** The underlying instance to which this API is linked. */
   private Instance instance;

   /** Builds a new Grakkit API object around the given instance. */
   public GrakkitAPI (Instance instance) {
      this.instance = instance;
   }

   /** Destroys the current instance. */
   public void destroy () throws Exception {
      if (this.instance == Grakkit.driver) {
         throw new Exception("The primary instance cannot be destroyed!");
      } else {
         this.instance.destroy();
      }
   }

   /** Sends a message into the global event framework. Listeners will fire on next tick. */
   public void emit (String channel, String message) {
      this.instance.messages.add(new Message(channel, message));
   }

   /** Creates a new file instance with the given index path. */
   public FileInstance fileInstance (String main) {
      return this.fileInstance(main, UUID.randomUUID().toString());
   }

   /** Creates a new file instance with the given index path and metadata tag. */
   public FileInstance fileInstance (String main, String meta) {
      FileInstance instance = new FileInstance(this.instance.root, main, meta);
      Grakkit.instances.add(instance);
      return instance;
   }

   /** Gets the "meta" value of the current instance. */
   public String getMeta () {
      return this.instance.meta;
   }

   /** Returns the "root" of the current instance. */
   public String getRoot () {
      return this.instance.root;
   }

   /** Adds an unload hook to be executed just before the instance is closed. */
   public void hook (Value script) {
      this.instance.hooks.list.add(script);
   }

   /** Loads the given class from the given source, usually a JAR library. */
   public Class<?> load (File source, String name) throws ClassNotFoundException, MalformedURLException {
      URL link = source.toURI().toURL();
      String path = source.toPath().normalize().toString();
      return Class.forName(name, true, Grakkit.loaders.computeIfAbsent(path, (key) -> new URLClassLoader(
         new URL[] { link },
         Grakkit.class.getClassLoader()
      )));
   }

   /** Unregisters an event listener from the channel registry. */
   public boolean off (String channel, Value listener) {
      if (Grakkit.channels.containsKey(channel)) {
         return Grakkit.channels.get(channel).remove(listener); 
      } else {
         return false;
      }
   }
   
   /** Registers an event listener to the channel registry. */
   public void on (String channel, Value listener) {
      Grakkit.channels.computeIfAbsent(channel, key -> new LinkedList<>()).add(listener);
   }

   /** Pushes a script into the tick loop to be fired upon next tick. */
   public void push (Value script) {
      this.instance.tasks.list.add(script);
   }

   /** Creates a new script instance with the given source code. */
   public ScriptInstance scriptInstance (String code) {
      return this.scriptInstance(code, UUID.randomUUID().toString());
   }

   /** Creates a new script instance with the given source code and metadata tag. */
   public ScriptInstance scriptInstance (String code, String meta) {
      ScriptInstance instance = new ScriptInstance(this.instance.root, code, meta);
      Grakkit.instances.add(instance);
      return instance;
   }

   /** Closes and re-opens the current instance. Works best when pushed into the tick loop. */
   public void swap () {
      this.hook(Value.asValue((Runnable) () -> this.instance.open()));
      this.instance.close();
   }
   
   /** An alias for the push method, deprecated. */
   @Deprecated
   public void sync (Value script) {
      this.push(script);
   }
}
