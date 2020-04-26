package xyz.grakkit;

import command.AbstractCommand;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.graalvm.polyglot.Value;

public class ScriptCommand extends AbstractCommand {

   // initialize variables
   Value executor, tabCompleter;
   Grakkit master;

   // define constructor
   public ScriptCommand(Grakkit master, String name, String usage, String desc, String error, List<String> aliases,
         String executor, String tabCompleter) {
      super(name, usage, desc, error, aliases);
      this.master = master;
      this.executor = (Value) this.master.eval(executor);
      this.tabCompleter = (Value) this.master.eval(tabCompleter);
   }

   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      // call executor
      this.executor.execute(sender, String.join(" ", args));

      // report command success
      return true;
   }

   @Override
   public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
      // fetch tab completions
      Value input = this.tabCompleter.execute(args[args.length - 1]);

      // initialize output array
      List<String> output = new ArrayList<String>();

      // copy tab completions to output array
      for (int index = 0; index < input.getArraySize(); index++)
         output.add(input.getArrayElement(index).asString());

      // return tab completions
      return output;
   }
}