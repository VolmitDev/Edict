package art.arcane.edict.message;

/**
 * Message that has text that is displayed when hovering over it.
 */
public class HoverableMessage extends StringMessage {

    /**
     * The text to show when hovering over the text.
     */
    private final String hoverText;

    /**
     * A message string.
     *
     * @param message The message this contains.
     */
    public HoverableMessage(String message, String hoverText) {
        super(message);
        this.hoverText = hoverText;
    }

    /**
     * Get the text to show when hovering over.
     * @return the hover text
     */
    public String getHoverText() {
        return hoverText;
    }
}
