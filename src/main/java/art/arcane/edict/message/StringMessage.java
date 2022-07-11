package art.arcane.edict.message;

/**
 * A message string.
 */
public class StringMessage implements Message {

    /**
     * Message in the string message.
     */
    private final String message;

    /**
     * Construct a new string message.
     * @param message the message
     */
    public StringMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
