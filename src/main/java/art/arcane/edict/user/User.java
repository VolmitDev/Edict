package art.arcane.edict.user;

import art.arcane.edict.message.Message;
import art.arcane.edict.permission.Permission;

/**
 * A user of the command system.
 */
public interface User {

    /**
     * The name of the user.
     * @return the name of the user
     */
    default String name() {
        return getClass().getSimpleName();
    }

    /**
     * Whether this user can use context when using commands.
     * Context is environment derived data that can be used by the system to autofill contextual (optional) parameters.
     * An example is a game you made, where there are multiple worlds; then the context can autofill the current world of the player.
     * @return whether the user can use context
     * TODO: Context (+ tests)
     */
    boolean canUseContext();

    /**
     * Send the user a message.
     * @param message the message to send
     */
    void send(Message message);

    /**
     * Whether the user has permission for a specific permission node.
     * @param permission the permission node
     * @return true if permission is granted
     */
    default boolean hasPermission(Permission permission) {
        return true;
    }
}
