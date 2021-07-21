package grakkit;

import java.util.LinkedList;
import java.util.List;

import org.graalvm.polyglot.Value;

public class Superset {

   /* the internal list */
   public List<Value> list = new LinkedList<>();

   /** execute and remove all scripts */
   public void release () {
      new LinkedList<>(this.list).forEach(value -> {
         try {
            if (value.canExecute()) value.execute();
         } catch (Throwable error) {
            // do nothing
         }
         this.list.remove(value);
      });
   }
}
