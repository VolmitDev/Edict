package art.arcane.edict;

import art.arcane.edict.api.Command;
import art.arcane.edict.completables.CompletableCommandsRegistry;
import art.arcane.edict.context.SystemContext;
import art.arcane.edict.context.UserContext;
import art.arcane.edict.handler.ContextHandler;
import art.arcane.edict.handler.ContextHandlerRegistry;
import art.arcane.edict.handler.ParameterHandlerRegistry;
import art.arcane.edict.handler.ParameterHandler;
import art.arcane.edict.handler.handlers.*;
import art.arcane.edict.message.Message;
import art.arcane.edict.message.StringMessage;
import art.arcane.edict.permission.Permission;
import art.arcane.edict.user.SystemUser;
import art.arcane.edict.user.User;
import art.arcane.edict.util.BKTreeIndexer;
import art.arcane.edict.util.EDictionary;
import art.arcane.edict.util.ParameterParser;
import art.arcane.edict.virtual.VCommandable;
import art.arcane.edict.virtual.VClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * The main System.
 * TODO: Colored text
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
     * Default system user.
     */
    private static final SystemUser defaultSystemUser = new SystemUser();

    /**
     * Root commands
     */
    private final List<VCommandable> rootCommands = new ArrayList<>();

    /**
     * System indexer.
     */
    private final BKTreeIndexer indexer = new BKTreeIndexer();

    /**
     * Permission factory
     */
    private final BiFunction<@Nullable Permission, @NotNull String, @NotNull Permission> permissionFactory;

    /**
     * Handler registry.
     */
    private final ParameterHandlerRegistry parameterHandlerRegistry;

    /**
     * Context handler registry.
     */
    private final ContextHandlerRegistry contextHandlerRegistry;

    /**
     * Completable commands registry.
     */
    private final CompletableCommandsRegistry completableCommandsRegistry = new CompletableCommandsRegistry();

    /**
     * System user.
     */
    private final SystemUser systemUser;

    /**
     * Sync runner.
     */
    private final Consumer<Runnable> syncRunner;

    /**
     * Create a new command system.<br>
     * Use {@link #command(String, User)} to run commands with the system.
     * Uses default values:<br>
     *  - PermissionFactory: {@link #defaultPermissionFactory}<br>
     *  - EDictionary: settings, uses defaults of that class (by {@link EDictionary})<br>
     *  - SystemUser: {@link SystemUser} (Using System.out.)<br>
     *  - ParameterHandlers: {@link #defaultHandlers}<br>
     *  - ContextHandlers: {@code None}<br>
     *  - SyncRunner: {@link Runnable#run()}
     * @throws NullPointerException <br>
     * - If the {@link ParameterHandler} for any of the types of any parameter of any methods of the {@code commandRoots} or any of its children is not registered.<br>
     * - If the {@link ContextHandler} for any contextual parameter of any methods of the {@code commandRoots} or any of its children is not registered.<br>
     * > If this is the case, use {@link #Edict(List, BiFunction, EDictionary, SystemUser, ParameterHandler[], ContextHandler[], Consumer)} to register more handlers, or replace the parameter type.
     */
    public Edict(@NotNull Object... commandRoots) throws NullPointerException {
        this(List.of(commandRoots), defaultPermissionFactory, new EDictionary(), defaultSystemUser, defaultHandlers, new ContextHandler<?>[]{}, Runnable::run);
    }

    /**
     * Create a new command system.<br>
     * Use {@link #command(String, User)} to run commands with the system.
     * @param commandRoots the roots of the commands
     * @param permissionFactory factory to create permissions
     * @param settings settings
     * @param systemUser the user to output system messages to
     * @param parameterHandlers the handlers you wish to register
     * @param contextHandlers the context handlers you wish to register
     * @param syncRunner consumer of runnable, called for {@link art.arcane.edict.api.Command}s that have {@link Command#sync()} true
     * @throws NullPointerException if the {@link ParameterHandler} for any of the parameters of any methods of the {@code commandRoots} or any of its children is not registered
     * or if the {@link ContextHandler} for any of the contextual parameter of any methods of the {@code commandRoots} or any of its children is not registered
     */
    public Edict(@NotNull List<Object> commandRoots, @NotNull BiFunction<@Nullable Permission, @NotNull String, @NotNull Permission> permissionFactory, @NotNull EDictionary settings, @NotNull SystemUser systemUser, @NotNull ParameterHandler<?>[] parameterHandlers, @NotNull ContextHandler<?>[] contextHandlers, @NotNull Consumer<Runnable> syncRunner) throws NullPointerException {

        // System
        this.systemUser = systemUser;

        // Permission factory
        this.permissionFactory = permissionFactory;

        // Sync runner
        this.syncRunner = syncRunner;

        // Settings
        EDictionary.set(settings);

        // System settings root
        if (EDictionary.get().settingsAsCommands) {
            VClass vRoot = VClass.fromInstance(EDictionary.get(), null, this);
            if (vRoot == null) {
                w(new StringMessage("Could not register settings commands!"));
            } else {
                rootCommands.add(vRoot);
            }
        }

        // Command Roots
        for (Object root : commandRoots) {
            VClass vRoot = VClass.fromInstance(root, null, this);
            if (vRoot == null) {
                w(new StringMessage("Could not register root " + root.getClass().getSimpleName() + "!"));
                continue;
            }
            rootCommands.add(vRoot);
        }

        // Indexer
        indexer.addAll(rootCommands);

        // Handlers
        this.parameterHandlerRegistry = new ParameterHandlerRegistry(defaultHandlers);
        if (parameterHandlers != null) {
            for (ParameterHandler<?> handler : parameterHandlers) {
                parameterHandlerRegistry.register(handler);
                d(new StringMessage("Registered handler: " + handler.getClass().getSimpleName()));
            }
        }

        // Context handlers
        this.contextHandlerRegistry = new ContextHandlerRegistry();
        if (contextHandlers != null) {
            for (ContextHandler<?> contextHandler : contextHandlers) {
                contextHandlerRegistry.register(contextHandler);
                d(new StringMessage("Registered context handler: " + contextHandler.getClass().getSimpleName()));
            }
        }
    }

    /**
     * Run a command through the system.
     * @param command the command to run
     * @param user the user that ran the command
     */
    public void command(@NotNull String command, @NotNull User user) {
        command(command, user, false);
    }

    /**
     * Run a command through the system.
     * @param command the command to run
     * @param user the user that ran the command
     * @param forceSync force the execution of this command in sync
     */
    public void command(@NotNull String command, @NotNull User user, boolean forceSync) {
        Runnable r = () -> {
            final String fCommand = ParameterParser.cleanCommand(command);

            i(new StringMessage(user.name() + " sent command: " + fCommand));

            List<String> input = List.of(fCommand.split(" "));

            // Blank check
            if (input.isEmpty()) {
                // TODO: Send root command help
                user.send(new StringMessage("This is an empty command wtf do you want"));
                return;
            }

            d(new StringMessage("Running command: " + fCommand));

            // Loop over roots
            new UserContext().post(user);
            new SystemContext().post(this);

            for (VCommandable root : indexer.search(input.get(0), settings().matchThreshold, (vCommandable -> user.hasPermission(vCommandable.permission())), this)) {
                d(new StringMessage("Running root: " + root.getClass().getSimpleName()));
                if (root.run(input.subList(1, input.size()), user)) {
                    return;
                }
            }

            d(new StringMessage("Could not find suitable command for input: " + fCommand));
            user.send(new StringMessage("Failed to run any commands for your input. Please try (one of): " + String.join(", ", rootCommands.stream().map(VCommandable::name).toList())));

        };

        if (forceSync) {
            d(new StringMessage("Running command in forced sync. Likely for testing purposes."));
            r.run();
        } else {
            new Thread(r).start();
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

    /**
     * Get system settings.
     * @return the system settings
     */
    public EDictionary settings() {
        return EDictionary.get();
    }

    /**
     * Get the {@link ParameterHandlerRegistry}.
     * @return the {@link ParameterHandlerRegistry}
     */
    public ParameterHandlerRegistry getParameterHandlerRegistry() {
        return parameterHandlerRegistry;
    }

    /**
     * Get the {@link ContextHandlerRegistry}.
     * @return the {@link ContextHandlerRegistry}
     */
    public ContextHandlerRegistry getContextHandlerRegistry() {
        return contextHandlerRegistry;
    }

    /**
     * Get the {@link CompletableCommandsRegistry}.
     * @return the {@link CompletableCommandsRegistry}
     */
    public CompletableCommandsRegistry getCompletableCommandsRegistry() {
        return completableCommandsRegistry;
    }

    /**
     * Run a runnable in sync, using the {@link #syncRunner}.
     * @param runnable the runnable to run
     */
    public void runSync(Runnable runnable) {
        syncRunner.accept(runnable);
    }
}
