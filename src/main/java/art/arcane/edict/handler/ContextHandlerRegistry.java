package art.arcane.edict.handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry for type context handlers.
 */
public class ContextHandlerRegistry {

    /**
     * The list of handlers registered
     */
    private final List<ContextHandler<?>> handlers = new ArrayList<>();

    /**
     * Register a new handler. Adding a new handler with the same type overwrites any existing handler.
     * @param handler the handler to register
     */
    public void register(ContextHandler<?> handler) {
        handlers.add(handler);
    }

    /**
     * Get the handler for a certain type. No caching.
     * @param type the type to get the handler for
     * @return the requested handler
     * @throws NullPointerException if no {@link ContextHandler} is registered for the {@code type}
     */
    public ContextHandler<?> getHandlerFor(Class<?> type) throws NullPointerException {
        for (ContextHandler<?> handler : handlers) {
            if (handler.supports(type)) {
                return handler;
            }
        }
        throw new NullPointerException("ContextHandler does not exist for type: " + type.getSimpleName());
    }
}
