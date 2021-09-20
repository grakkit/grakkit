package grakkit;

import java.io.File;
import java.io.IOException;

import java.nio.file.Paths;

import org.graalvm.polyglot.Source;

public class FileInstance extends Instance {

   /** The main path of this instance, which ideally points to a JavaScript file. */
   public String main;
   
   /** Builds a new file-based instance from the given paths. */
   public FileInstance (String root, String main, String meta) {
      super(root, meta);
      this.main = main;
   }

   /** Executes this InstanceFile */
   @Override
   public void execute () throws IOException {
      File index = Paths.get(this.root).resolve(this.main).toFile();
      if (index.exists()) {
         this.context.eval(Source.newBuilder("js", index).mimeType("application/javascript+module").build());
      } else {
         index.createNewFile();
      }
   }
}
