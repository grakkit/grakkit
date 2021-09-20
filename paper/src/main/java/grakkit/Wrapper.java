package grakkit;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.graalvm.polyglot.Value;

public class Wrapper extends Command {

   /** The executor to use for this command. */
   public Value executor;

   /** The tab-completer to use for this command. */
   public Value tabCompleter;

   /** Creates a custom command with the given options. */
   public Wrapper (String name, String[] aliases) {
      super(name, "", "", Arrays.asList(aliases));
   }

   @Override
   public boolean execute (CommandSender sender, String label, String[] args) {
      try {
         this.executor.executeVoid(sender, label, args);
      } catch (Throwable error) {
         error.printStackTrace(System.err);
      }
      return true;
   }

   /** Sets this wrapper's command options. */
   public void options (String permission, String message, Value executor, Value tabCompleter) {
      this.executor = executor;
      this.tabCompleter = tabCompleter;
      this.setPermission(permission);
      this.setPermissionMessage(message);
   }

   @Override
   public ArrayList<String> tabComplete (CommandSender sender, String alias, String[] args) {
      ArrayList<String> output = new ArrayList<>();
      try {
         Value input = this.tabCompleter.execute(sender, alias, args);
         for (long index = 0; index < input.getArraySize(); index++) output.add(input.getArrayElement(index).toString());
      } catch (Throwable error) {
         error.printStackTrace(System.err);
      }
      return output;
   }
}
