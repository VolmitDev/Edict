package art.arcane.edict;

import art.arcane.edict.handlers.ContextHandler;
import art.arcane.edict.handlers.ContextHandlerRegistry;
import art.arcane.edict.handlers.HandlerRegistry;
import art.arcane.edict.handlers.ParameterHandler;
import art.arcane.edict.handlers.handlers.*;
import art.arcane.edict.message.Message;
import art.arcane.edict.message.StringMessage;
import art.arcane.edict.permission.Permission;
import art.arcane.edict.user.SystemUser;
import art.arcane.edict.user.User;
import art.arcane.edict.virtual.VCommandable;
import art.arcane.edict.virtual.VCommands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

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
     * Default permission factory.
     */
    private static final BiFunction<@Nullable Permission, @NotNull String, @NotNull Permission> defaultPermissionFactory = (parent, s) -> new Permission() {
        @Override
        public Permission getParent() {
            return parent;
        }

        @Override
        public String toString() {
            return s;
        }
    };

    /**
     * Root commands
     */
    private final List<VCommandable> rootCommands = new ArrayList<>();

    /**
     * Permission factory
     */
    private final BiFunction<@Nullable Permission, @NotNull String, @NotNull Permission> permissionFactory;

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
    public Edict(@NotNull Class<?>... commandRoots) {
        this(new ArrayList<>(List.of(commandRoots)), null, null, null, null);
    }

    /**
     * Create a new command system.
     * @param commandRoots the roots of the commands.
     * @param permissionFactory factory to create permissions. By default {@link #defaultPermissionFactory} is used.
     * @param systemUser the user to output system messages to. By default, uses {@link SystemUser} (Using System.out.)
     * @param handlers the handlers you wish to register. By default, {@link #defaultHandlers} are already registered.
     * @param contextHandlers the context handlers you wish to register. By default, there are no context handlers.
     */
    public Edict(@NotNull List<Class<?>> commandRoots, @Nullable BiFunction<@Nullable Permission, @NotNull String, @NotNull Permission> permissionFactory, @Nullable SystemUser systemUser, @Nullable ParameterHandler<?>[] handlers, @Nullable ContextHandler<?>[] contextHandlers) {

        // Command Roots
        for (Class<?> root : commandRoots) {
            VCommands vRoot = VCommands.fromClass(root, null, this);
            if (vRoot == null) {
                w(new StringMessage("Could not register root " + root.getSimpleName() + "!"));
                continue;
            }
            rootCommands.add(vRoot);
        }

        // Permission factory
        this.permissionFactory = permissionFactory == null ? defaultPermissionFactory : permissionFactory;

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

        // Clean command
        command = command.strip();
        while (command.contains("  ")) {
            command = command.replace("  ", " ");
        }
        command = command.replace(" =", "=");
        List<String> splitInput = List.of(command.split(" "));

        // Loop over roots
        for (VCommandable root : rootCommands.stream().sorted(Comparator.comparingInt(o -> o.match(splitInput.get(0), user))).toList()) {
            root.run(splitInput, user);
        }
    }

    /**
     * Make a {@link Permission} node.
     * @param input the input to make the node
     */
    public @NotNull Permission makePermission(@Nullable Permission parent, @NotNull String input) {
        return permissionFactory.apply(parent, input);
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
