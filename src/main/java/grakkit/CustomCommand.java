package grakkit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.graalvm.polyglot.Value;

public final class CustomCommand extends Command {
   
   public Value executor;
   public Value tabCompleter;

   public CustomCommand (String name, String description, String usage, List<String> aliases, String permission, String message, String fallback, Value executor, Value tabCompleter) {
      super(name, description, usage, aliases);
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
      } catch (UnsupportedOperationException error) {
         // if the executor is not executable, do not report the error
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
      } catch (UnsupportedOperationException error) {
         // if the tab-completer is not executable, do not report the error
      } catch (Exception error) {
         error.printStackTrace(System.err);
      }
      return output;
   }
}
