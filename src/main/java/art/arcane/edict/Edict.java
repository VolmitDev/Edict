package art.arcane.edict;

import art.arcane.edict.handlers.ContextHandler;
import art.arcane.edict.handlers.ContextHandlerRegistry;
import art.arcane.edict.handlers.HandlerRegistry;
import art.arcane.edict.handlers.ParameterHandler;
import art.arcane.edict.handlers.handlers.*;
import art.arcane.edict.message.Message;
import art.arcane.edict.message.StringMessage;
import art.arcane.edict.user.SystemUser;
import art.arcane.edict.user.User;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

/**
 * The main System.
 */
public class Edict {

    /**
     * Default parameter handlers.
     */
    private static final ParameterHandler<?>[] defaultHandlers = new ParameterHandler[]{
            new BooleanHandler(),
            new ByteHandler(),
            new DoubleHandler(),
            new FloatHandler(),
            new IntegerHandler(),
            new LongHandler(),
            new ShortHandler(),
            new StringHandler()
    };

    /**
     * Handler registry.
     */
    private final HandlerRegistry handlerRegistry;

    /**
     * Context handler registry.
     */
    private final ContextHandlerRegistry contextHandlerRegistry;

    /**
     * System user.
     */
    private final SystemUser systemUser;

    /**
     * Create a new command system.
     */
    public Edict() {
        this(null, null, null);
    }

    /**
     * Create a new command system.
     * @param systemUser the user to output system messages to. By default, uses {@link SystemUser} (Using System.out.)
     * @param handlers the handlers you wish to register. By default, {@link #defaultHandlers} are already registered.
     * @param contextHandlers the context handlers you wish to register. By default, there are no context handlers.
     */
    public Edict(@Nullable SystemUser systemUser, @Nullable ParameterHandler<?>[] handlers, @Nullable ContextHandler<?>[] contextHandlers) {

        // System
        this.systemUser = systemUser;

        // Handlers
        this.handlerRegistry = new HandlerRegistry(defaultHandlers);
        if (handlers != null) {
            for (ParameterHandler<?> handler : handlers) {
                if (handler != null) {
                    handlerRegistry.register(handler);
                    d(new StringMessage("Registered handler: " + handler.getClass().getSimpleName()));
                }
            }
        }

        // Context handlers
        this.contextHandlerRegistry = new ContextHandlerRegistry();
        if (contextHandlers != null) {
            for (ContextHandler<?> contextHandler : contextHandlers) {
                if (contextHandler != null) {
                    contextHandlerRegistry.register(contextHandler);
                    d(new StringMessage("Registered context handler: " + contextHandler.getClass().getSimpleName()));
                }
            }
        }
    }

    /**
     * Run a command through the system.
     * @param command the command to run
     * @param user the user that ran the command
     */
    public void command(String command, User user) {
        throw new NotImplementedException();
    }

    /**
     * Send an information message to the system.
     */
    public void i(Message message) {
        systemUser.i(message);
    }

    /**
     * Send a warning message to the system.
     */
    public void w(Message message) {
        systemUser.w(message);
    }

    /**
     * Send a debug message to the system.
     */
    public void d(Message message) {
        systemUser.d(message);
    }
}
