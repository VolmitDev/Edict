package art.arcane.edict.context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Context handler. Stores global state for threads to be able to find some context {@link Object}.
 * It is indexed by Thread, so after the {@link #post(Object)} has been called, only this thread can access it in the context.
 * @param <T> the type of the context
 */
public interface Context<T> {

    /**
     * The context map. Don't use this for anything else. Use {@link #post(Object)} to add objects.
     * @return the concurrent hash map for this context
     */
    ConcurrentHashMap<@NotNull Thread, @Nullable T> context();

    /**
     * Post a new element to this global context map.
     * @param element the element to post to this context
     */
    default void post(@NotNull T element) {
        clean();
        context().put(Thread.currentThread(), element);
    }

    /**
     * Remove dead threads from the context map.
     */
    default void clean() {
        context().keys().asIterator().forEachRemaining(thread -> {
            if (!thread.isAlive()) {
                context().remove(thread);
            }
        });
    }

    /**
     * Get the element from the context.
     */
    default @Nullable T get() {
        return context().get(Thread.currentThread());
    }

    /**
     * Delete the current context.
     */
    default void delete() {
        context().remove(Thread.currentThread());
    }
}
