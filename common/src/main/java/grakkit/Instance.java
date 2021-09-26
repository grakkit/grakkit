package grakkit;

import org.graalvm.polyglot.Engine;

import java.util.ArrayList;
import java.util.LinkedList;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

public class Instance {

   /** The underlying context associated with this instance. */
   public Context context;

   /** The engine used for all instance contexts. */
   public static final Engine engine = Engine.newBuilder().build();

   /** All registered unload hooks tied to this instance. */
   public final Queue hooks = new Queue();

   /** All queued messages created by this instance. */
   public final LinkedList<Message> messages = new LinkedList<>();

   /** Metadata associated with this instance. */
   public String meta;

   /** The root directory of this instance. */
   public String root;

   /** All queued tasks linked to this instance. */
   public final Queue tasks = new Queue();

   /** Builds a new instance from the given paths. */
   public Instance (String root, String meta) {
      this.meta = meta;
      this.root = root;
   }

   /** Closes this instance's context. */
   public void close () {
      Context context = this.context;
      this.hooks.release();
      context.close();
   }

   /** Closes this instance and removes it from the instance registry. */
   public void destroy () {
      this.close();
      Grakkit.instances.remove(this);
   }

   /** Executes this instance by calling its entry point. */
   public void execute () throws Throwable {
      // do nothing
   }

   /** Opens this instance's context. */
   public void open () {
      this.context = Context.newBuilder("js")
         .engine(Instance.engine)
         .allowAllAccess(true)
         .allowExperimentalOptions(true)
         .option("js.nashorn-compat", "true")
         .option("js.commonjs-require", "true")
         .option("js.ecmascript-version", "2022")
         .option("js.commonjs-require-cwd", this.root)
         .build();
      this.context.getBindings("js").putMember("Grakkit", Value.asValue(new GrakkitAPI(this)));
      try {
         this.execute();
      } catch (Throwable error) {
         error.printStackTrace();
      }
   }

   /** Executes the tick loop for this instance. */
   public void tick () {
      this.tasks.release();
      new ArrayList<>(this.messages).forEach(message -> {
         this.messages.remove(message);
         if (Grakkit.channels.containsKey(message.channel)) {
            Grakkit.channels.get(message.channel).forEach(listener -> {
               try {
                  listener.executeVoid(message.content);
               } catch (Throwable error) {
                  // do nothing
               }
            });
         }
      });
   }
}
