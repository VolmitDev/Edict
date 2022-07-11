package art.arcane.edict.message;

/**
 * A message that can be clicked on.
 */
public class ClickableMessage extends StringMessage {

    /**
     * The runnable to run when clicked on.
     */
    private final Runnable runOnClick;

    /**
     * Construct a new string message.
     *
     * @param message    the message
     * @param runOnClick the runnable to run when clicked on
     */
    public ClickableMessage(String message, Runnable runOnClick) {
        super(message);
        this.runOnClick = runOnClick;
    }

    /**
     * Click the clickable.
     */
    public void click() {
        runOnClick.run();
    }
}