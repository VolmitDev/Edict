package art.arcane.edict.message;

/**
 * A message string.
 *
 * @param message The message this contains.
 */
public record StringMessage(String message) implements Message {

    @Override
    public String toString() {
        return message;
    }
}
