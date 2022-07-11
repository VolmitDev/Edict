package art.arcane.edict.user;

import art.arcane.edict.message.Message;
import art.arcane.edict.permission.Permission;
import org.jetbrains.annotations.NotNull;

/**
 * A user of the command system.
 */
public interface User {

    /**
     * The name of the user.
     * @return the name of the user
     */
    default @NotNull String name() {
        return getClass().getSimpleName();
    }

    /**
     * Whether this user can use context when using commands.
     * Context is environment derived data that can be used by the system to autofill contextual (optional) parameters.
     * An example is a game you made, where there are multiple worlds; then the context can autofill the current world of the player.
     * @return whether the user can use context
     */
    boolean canUseContext();

    /**
     * Whether the user can use clickable messages.
     */
    boolean canUseClickable();

    /**
     * Send the user a message.
     * @param message the message to send
     */
    void send(@NotNull Message message);

    /**
     * Whether the user has permission for a specific permission node.
     * @param permission the permission node
     * @return true if permission is granted
     */
    default boolean hasPermission(@NotNull Permission permission) {
        return true;
    }

    /**
     * Play a sound effect to the user informing them their input had multiple options, and they should pick one.
     * Does nothing by default.
     */
    default void playPickNotification() {}
}
