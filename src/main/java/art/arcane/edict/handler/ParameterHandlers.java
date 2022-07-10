package art.arcane.edict.handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry for type handlers.
 */
public class ParameterHandlers extends ArrayList<ParameterHandler<?>> {

     /**
      * Initialize parameter handler with some handlers.
      * @param handlers the handlers
      */
     public ParameterHandlers(List<ParameterHandler<?>> handlers) {
          addAll(handlers);
     }

     /**
      * Get the handler for a certain type. No caching.
      * @param type the type to get the handler for
      * @return the requested handler
      * @throws NullPointerException if no {@link ParameterHandler} is registered for the {@code type}
      */
     public ParameterHandler<?> getHandlerFor(Class<?> type) throws NullPointerException {
          for (ParameterHandler<?> handler : this) {
               if (handler.supports(type)) {
                    return handler;
               }
          }
          throw new NullPointerException("Cannot find ParameterHandler for: " + type.getSimpleName());
     }
}
