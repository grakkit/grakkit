package xyz.grakkit;

import command.AbstractCommand;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public final class Grakkit extends JavaPlugin {

   // initialize variables
   JS command;
   Value binding;
   Context context;

   // critial error script
   private void exit(String error) {
      System.err.println(error);
      this.getServer().getPluginManager().disablePlugin(this);
   }

   // code evaluator
   public Object eval(String code) {
      return context.eval("js", code);
   }

   // command creator
   public AbstractCommand command(String name, String usage, String desc, String error, List<String> aliases,
         String executor, String tabCompleter) {

      // create command
      AbstractCommand custom = new ScriptCommand(this, name, usage, desc, error, aliases, executor, tabCompleter);

      // return command
      return custom;
   }

   // plugin code
   @Override
   public void onEnable() {

      // initialize engine
      this.context = Context.newBuilder("js").allowAllAccess(true).build();
      this.binding = this.context.getBindings("js");

      // find root folder
      String root = System.getProperty("user.dir");

      // find config folder
      File config = Paths.get(root + "/plugins/grakkit").toAbsolutePath().toFile();

      // create config folder (if applicable)
      if (!config.exists() && !config.mkdir()) {
         this.exit("An error occurred attempting to create the config folder!");
         return;
      }

      // find index file
      File index = Paths.get(root + "/plugins/grakkit/index.mjs").toAbsolutePath().toFile();

      // export index file (if applicable)
      if (!index.exists()) {

         // initialize variables
         int bytes = 0;
         byte[] buffer = new byte[4096];
         InputStream input = JS.class.getResourceAsStream("/xyz/grakkit/basic.mjs");

         // catch verbose errors
         try {

            // create output stream
            FileOutputStream output = new FileOutputStream(index);

            // write to output stream
            while ((bytes = input.read(buffer)) > 0)
               output.write(buffer, 0, bytes);

            // close streams
            input.close();
            output.close();
         } catch (Exception error) {
            this.exit("An error occurred attempting to extract the index file!");
            return;
         }
      }

      // catch verbose errors
      try {

         // build index file
         Source source = Source.newBuilder("js", index).build();

         // evaluate index file
         context.eval(source);
      } catch (Exception error) {
         error.printStackTrace(System.err);
      }

      // register JS command
      this.command = new JS(this);
      this.getCommand("js").setExecutor(this.command);
      this.getCommand("js").setTabCompleter(this.command);
   }
}