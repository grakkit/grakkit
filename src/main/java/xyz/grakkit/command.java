package xyz.grakkit;

import command.AbstractCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.graalvm.polyglot.Value;
import java.util.List;
import java.util.ArrayList;

public class command extends AbstractCommand {

   // initialize variables
   Value executor;
   Value tabCompleter;

   // expose command constructor
   public command(String command, String usage, String description, String execute, String tabComplete) {
      super(command, usage, description);
      this.executor = (Value) core.context.eval("js", execute);
      this.tabCompleter = (Value) core.context.eval("js", tabComplete);
   }

   @Override
   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      try {

         // execute command code
         executor.execute(sender, String.join(" ", args));
         return true;
      } catch (Exception error) {

         // report command execution failure
         error.printStackTrace();
         return false;
      }
   }

   @Override
   public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

      // initialize output array
      List<String> output = new ArrayList<String>();

      try {

         // tab complete the command
         Value results = tabCompleter.execute(sender, String.join(" ", args));

         // push tab completions to output
         for (int i = 0; i < results.getArraySize(); i++) {
            output.add(results.getArrayElement(i).asString());
         }
      } catch (Exception error) {

         // report tab completion failure
         error.printStackTrace();
      }

      // return tab completion array
      return output;
   }
}