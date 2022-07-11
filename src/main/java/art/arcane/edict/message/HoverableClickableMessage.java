package art.arcane.edict.message;

/**
 * Message that is both hoverable and clickable.
 */
public class HoverableClickableMessage extends ClickableMessage {

    /**
     * The text to show when hovering over the text.
     */
    private final String hoverText;

    /**
     * Get the text to show when hovering over.
     * @return the hover text
     */
    public String getHoverText() {
        return hoverText;
    }

    /**
     * Converts this hoverable message to a string message.
     * @return a string message
     */
    public StringMessage toStringMessage() {
        return new StringMessage(super.string() + " (" + hoverText + ")");
    }

    /**
     * Construct a new string message.
     *
     * @param message    the message
     * @param runOnClick the runnable to run when clicked on
     */
    public HoverableClickableMessage(String message, String hoverText, Runnable runOnClick) {
        super(message, runOnClick);
        this.hoverText = hoverText;
    }
}
