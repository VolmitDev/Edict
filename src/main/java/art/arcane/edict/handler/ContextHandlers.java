package art.arcane.edict.handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry for type context handlers.
 */
public class ContextHandlers extends ArrayList<ContextHandler<?>> {

    /**
     * Initialize context handler with some handlers.
     * @param handlers the handlers
     */
    public ContextHandlers(ContextHandler<?>... handlers) {
        addAll(List.of(handlers));
    }

    /**
     * Get the handler for a certain type. No caching.
     * @param type the type to get the handler for
     * @return the requested handler
     * @throws NullPointerException if no {@link ContextHandler} is registered for the {@code type}
     */
    public ContextHandler<?> getHandlerFor(Class<?> type) throws NullPointerException {
        for (ContextHandler<?> handler : this) {
            if (handler.supports(type)) {
                return handler;
            }
        }
        throw new NullPointerException("Cannot find ContextHandler for: " + type.getSimpleName());
    }
}
