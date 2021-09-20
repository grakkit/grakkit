package grakkit;

public class ScriptInstance extends Instance {

   /** The source code of this instance, which ideally contains valid JavaScript. */
   public String code;
   
   /** Builds a new script-based instance from the given paths. */
   public ScriptInstance (String root, String code, String meta) {
      super(root, meta);
      this.code = code;
   }

   @Override
   public void execute () {
      this.context.eval("js", code);
   }
}
