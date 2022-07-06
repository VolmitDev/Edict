package art.arcane.edict.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Registry for type handlers.
 */
public class HandlerRegistry {

     /**
      * The list of handlers registered
      */
     private final List<ParameterHandler<?>> handlers = new ArrayList<>();

     /**
      * Create a new handler registry.
      * @param handlers the handlers to register with
      */
     public HandlerRegistry(ParameterHandler<?>... handlers) {
          this.handlers.addAll(Arrays.asList(handlers));
     }

     /**
      * Register a new handler. Adding a new handler with the same type overwrites any existing handler.
      * @param handler the handler to register
      */
     public void register(ParameterHandler<?> handler) {
          handlers.add(handler);
     }

     /**
      * Get the handler for a certain type. No caching.
      * @param type the type to get the handler for
      * @return the requested handler or {@code null} if there is no associated handler registered
      */
     public ParameterHandler<?> getHandlerFor(Class<?> type) {
          for (ParameterHandler<?> handler : handlers) {
               if (handler.supports(type)) {
                    return handler;
               }
          }
          return null;
     }
}
