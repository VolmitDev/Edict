package art.arcane.edict.handlers;

import art.arcane.edict.user.User;

/**
 * Context handler.
 * @param <T> the type this context handler can handle
 */
public interface ContextHandler<T> {

    /**
     * The type this context handler handles
     * @return the type
     */
    boolean supports(Class<?> type);

    /**
     * The handler for this context. Can use any data found in the user object for context derivation.
     * @param user The user whose data may be used
     * @return The value in the assigned type
     */
    T handle(User user);

    /**
     * Use #handle to return the current string name
     * @return The string name (or whatever) for this context's return
     */
    default String handleToString(User user) {
        return handle(user).toString();
    }
}
