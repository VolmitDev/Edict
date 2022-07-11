package art.arcane.edict.message;

import java.util.ArrayList;
import java.util.List;

/**
 * Message consisting of multiple messages.
 */
public class CompoundMessage implements Message {

    /**
     * The messages in this compound.
     */
    final List<Message> messages;

    /**
     * Construct a new compound message.
     * @param messages the messages in this compound
     */
    public CompoundMessage(Message... messages) {
        this.messages = List.of(messages);
    }

    /**
     * Get the messages in this compound message.
     * @return the messages
     */
    public List<Message> getMessages() {
        return messages;
    }

    /**
     * Turn the message into a string.
     *
     * @return the string representation of the message.
     */
    @Override
    public String string() {
        List<String> strings = new ArrayList<>();
        for (Message message : messages) {
            strings.add(message.string());
        }
        return String.join(", ", strings);
    }
}
