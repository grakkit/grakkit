package xyz.grakkit;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.graalvm.polyglot.Value;

// add JS command
public class JS implements CommandExecutor, TabCompleter {

  // initialize variables
  Value tabCompleter;
  Grakkit master;

  // initialize constructor
  public JS(Grakkit master) {
    super();
    this.master = master;
    this.tabCompleter = (Value) this.master.eval("globalThis.tabCompleter || (() => [])");
  }

  // command logic
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    // handle failed evaluation
    try {
      // evaluate input
      String output = this.master.eval(String.join(" ", args)).toString();

      // send output to player (if applicable)
      if (sender instanceof Player)
        sender.sendMessage(output);

      // send output to console (if applicable)
      else
        System.out.println(output);
    } catch (Exception error) {
      // send error to player (if applicable)
      if (sender instanceof Player)
        sender.sendMessage("\u00a77" + error.getMessage());

      // send stack trace to console
      error.printStackTrace(System.err);
    }

    // report command success
    return true;
  }

  // tab completer
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