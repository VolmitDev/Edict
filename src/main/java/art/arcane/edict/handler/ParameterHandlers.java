package art.arcane.edict.handler;

import java.util.ArrayList;

/**
 * Registry for type handlers.
 */
public class ParameterHandlers extends ArrayList<ParameterHandler<?>> {

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
          throw new NullPointerException();
     }
}
