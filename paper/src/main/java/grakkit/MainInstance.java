package grakkit;

import org.graalvm.polyglot.Value;

public class MainInstance extends FileInstance {

   /** Builds a new file-based main instance from the given paths. */
   public MainInstance (String root, String main) {
      super(root, main, "grakkit");
   }

   @Override
   public void close () {
      Main.commands.values().forEach(command -> {
         command.executor = Value.asValue((Runnable) () -> {});
         command.tabCompleter = Value.asValue((Runnable) () -> {});
      });
      super.close();
   }
}
