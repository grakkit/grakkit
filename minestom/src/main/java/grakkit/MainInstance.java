package grakkit;

public class MainInstance extends FileInstance {

   /** Builds a new file-based main instance from the given paths. */
   public MainInstance (String root, String main) {
      super(root, main, "grakkit");
   }

   @Override
   public void close () {
      super.close();
   }
}
