package grakkit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.graalvm.polyglot.Value;

public final class Custom extends Command {
   
   public Value executor;
   public Value tabCompleter;

   public Custom (String name, List<String> aliases, String permission, String message, Value executor, Value tabCompleter) {
      super(name, "", "", aliases);
      this.executor = executor;
      this.tabCompleter = tabCompleter;
      if (permission.length() > 0) {
         this.setPermission(permission);
         this.setPermissionMessage(message);
      }
   }

   @Override
   public boolean execute (CommandSender sender, String label, String[] args) {
      try {
         this.executor.execute(sender, label, args);
      } catch (Exception error) {
         error.printStackTrace(System.err);
      }
      return true;
   }

   @Override
   public List<String> tabComplete (CommandSender sender, String alias, String[] args) {
      List<String> output = new ArrayList<String>();
      try {
         Value input = this.tabCompleter.execute(sender, alias, args);
         for (int index = 0; index < input.getArraySize(); index++) output.add(input.getArrayElement(index).asString());
      } catch (Exception error) {
         error.printStackTrace(System.err);
      }
      return output;
   }
}
