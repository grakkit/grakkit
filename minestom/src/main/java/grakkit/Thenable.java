package grakkit;

import org.graalvm.polyglot.Value;

public interface Thenable {

   /** The callback method for this thenable. */
   void then(Value resolve);
}
