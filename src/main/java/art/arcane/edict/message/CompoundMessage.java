package art.arcane.edict.message;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Message consisting of multiple messages.
 */
public class CompoundMessage implements Message {

    /**
     * The messages in this compound.
     */
    final List<Message> messages = new ArrayList<>();

    /**
     * Construct a new compound message.
     * @param messages the messages in this compound
     */
    public CompoundMessage(Message... messages) {
        for (Message message : messages) {
            if (message instanceof CompoundMessage compoundMessage) {
                this.messages.addAll(compoundMessage.getMessages());
            } else {
                this.messages.add(message);
            }
        }
    }

    /**
     * Get the messages in this compound message.
     * @return the messages
     */
    public List<Message> getMessages() {
        return messages;
    }

    /**
     * Add two compound messages.
     * @param message the other compound message
     * @return the new compound message
     */
    public @NotNull CompoundMessage add(@NotNull CompoundMessage message) {
        messages.addAll(message.getMessages());
        return this;
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
