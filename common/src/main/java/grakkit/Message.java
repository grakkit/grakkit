package grakkit;

public class Message {

   /** The channel to send this message to. */
   public String channel;

   /** The content of the message. */
   public String content;

   /** Create a message for deployment within the cross-context communication system. */
   public Message (String channel, String content) {
      this.channel = channel;
      this.content = content;
   }
}
