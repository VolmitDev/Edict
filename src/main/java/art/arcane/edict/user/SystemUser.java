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

    @Override
    public void send(@NotNull Message message) {
        System.out.println(message);
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