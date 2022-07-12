package art.arcane.edict.user;


import art.arcane.edict.message.Message;
import org.jetbrains.annotations.NotNull;

/**
 * System user implementation. By default, sends to System.out.
 */
public class SystemUser implements User {

    /**
     * Create a new system user.
     */
    public SystemUser() {

    }

    @Override
    public boolean canUseContext() {
        return false;
    }

    /**
     * Whether the user can use clickable messages.
     */
    @Override
    public boolean canUseClickable() {
        return false;
    }

    @Override
    public void send(@NotNull Message message) {
        System.out.println(message.string());
    }

    /**
     * Suggest a command with missing inputs (like a form).
     *
     * @param command a string that can be used as input after adding values.
     */
    @Override
    public void suggestCommand(String command) {
        System.out.println("Command suggested: " + command);
    }

    /**
     * Send an information message to the system.
     */
    public void i(Message message) {
        send(message);
    }

    /**
     * Send a warning message to the system.
     */
    public void w(Message message) {
        send(message);
    }

    /**
     * Send a debug message to the system.
     */
    public void d(Message message) {
        send(message);
    }
}