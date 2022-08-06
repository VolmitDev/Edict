package art.arcane.edict;

import art.arcane.edict.api.Command;
import art.arcane.edict.completables.CompletableCommandsRegistry;
import art.arcane.edict.context.SystemContext;
import art.arcane.edict.context.UserContext;
import art.arcane.edict.handler.ContextHandler;
import art.arcane.edict.handler.ContextHandlers;
import art.arcane.edict.handler.ParameterHandler;
import art.arcane.edict.handler.ParameterHandlers;
import art.arcane.edict.handler.handlers.*;
import art.arcane.edict.message.Message;
import art.arcane.edict.message.StringMessage;
import art.arcane.edict.permission.Permission;
import art.arcane.edict.user.SystemUser;
import art.arcane.edict.user.User;
import art.arcane.edict.util.BKTreeIndexer;
import art.arcane.edict.util.EDictionary;
import art.arcane.edict.parser.ParameterParser;
import art.arcane.edict.virtual.VClass;
import art.arcane.edict.virtual.VCommandable;
import art.arcane.edict.virtual.VMethod;
import lombok.Builder;
import lombok.Singular;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/*
    TODO: Command structure in README.
    Can also refer to classes in test.java.art.arcane.edict.testconstruct,
    but then they would need to be cleaned up somewhat, and more examples should be added.

    TODO: Colored text
    1. Make color construct
    2. Make gradient construct
    3. Define abstract color scheme for end-user configurability
    4. Apply colors in commands & debug
    5. Apply gradients in post, perhaps, since adding that manually everywhere is a pain

    TODO: Add auto-completions / suggestions
    If command ends without a space -> complete current term
    If command ends with a space -> give all options for next term
    Make a pipeline through this class. Generalize steps involved to prevent code duplication with #command.

    TODO: Test help
    1. Make a construct for user output which functions as an example and as a way to test
    2. Define a way to show clickable and hover-able content in there
    3. Try it out :)
*/
/**
 * <h1>Edict</h1>
 * <i>A Command System by Arcane Arts</i><br><br>
 * <h2>Constructing</h2>
 * Build using {@link #builder(Object, Object...)} (Lombok).
 * Specifying one or more @{@link Command} annotated classes as the root(s) of your command structure.
 * An example for this (and the settings below) can be found in the {@code README.md}.<br><br>
 * <h2>Configurable Options</h2>
 * <i>Not all options may be mentioned here</i><br>
 * <ul>
 *  <li>{@link EdictBuilder#settings(EDictionary)} the settings instance<br>
 *      By default, uses {@link EDictionary#EDictionary()}</li>
 *  <li>{@link EdictBuilder#syncRunner(Consumer)} how to run commands sync<br>
 *      By default, uses {@link Runnable#run()} (async)</li>
 *  <li>{@link EdictBuilder#systemUser(SystemUser)} system user to write debug/info/warnings to<br>
 *      By default, uses {@link SystemUser#SystemUser()} (System.out)</li>
 *  <li>{@link EdictBuilder#permissionFactory(BiFunction)} permission factory to create permissions<br>
 *      By default, uses {@link #defaultPermissionFactory}</li>
 *  <li>{@link EdictBuilder#parameterHandler(ParameterHandler)} / {@link EdictBuilder#parameterHandlers(ParameterHandlers)} handlers for custom parameter types<br>
 *      By default, features all parameter handlers in {@link #defaultParameterHandlers} (standard Java types)</li>
 *  <li>{@link EdictBuilder#contextHandler(ContextHandler)} / {@link EdictBuilder#contextHandlers(ContextHandlers)} handlers for custom context types<br>
 *      By default, there are no context handlers</li>
 * </ul>
 * <h2>Running</h2>
 * To parse commands through the system after initializing it, use {@link #command(String, User)}.
 */
@SuppressWarnings("unused")
@Builder(builderMethodName = "")
public class Edict {

    /**
     * The default parameter handlers. For most basic Java types.
     */
    private static final List<ParameterHandler<?>> defaultParameterHandlers = new ArrayList<>(List.of(
            new BooleanHandler(),
            new ByteHandler(),
            new DoubleHandler(),
            new FloatHandler(),
            new IntegerHandler(),
            new LongHandler(),
            new ShortHandler(),
            new StringHandler()
    ));

    /**
     * The default permission factory. Simply sets the parent as the parent and the toString method as the input string.
     */
    private static BiFunction<Permission, String, Permission> defaultPermissionFactory = (parent, s) -> new Permission() {
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
     * Command root instances.
     */
    @Singular
    private List<Object> roots;

    /**
     * System user.
     */
    @Builder.Default
    private SystemUser systemUser = new SystemUser();

    /**
     * Sync runner.
     */
    @Builder.Default
    private Consumer<Runnable> syncRunner = Runnable::run;

    /**
     * Settings.
     */
    @Builder.Default
    private EDictionary settings = new EDictionary();

    /**
     * Permission factory
     */
    @Builder.Default
    private BiFunction<@Nullable Permission, @NotNull String, @NotNull Permission> permissionFactory = defaultPermissionFactory;

    /**
     * Handler registry.
     */
    @Builder.Default
    private ParameterHandlers parameterHandlers = new ParameterHandlers(defaultParameterHandlers);

    /**
     * Context handler registry.
     */
    @Builder.Default
    private ContextHandlers contextHandlers = new ContextHandlers();

    /**
     * Indent for {@link #networkString()}.
     */
    @Builder.Default
    private String networkStringIndent = "  ";

    /**
     * Root commands
     */
    private final List<VCommandable> rootCommands = new ArrayList<>();

    /**
     * System indexer.
     */
    private final BKTreeIndexer indexer = new BKTreeIndexer();

    /**
     * Completable commands' registry.
     */
    private final CompletableCommandsRegistry completableCommandsRegistry = new CompletableCommandsRegistry();

    /**
     * Build Edict.
     * @param mainRoot the main command root of the system. Can be {@code null}, in which case none are registered.
     * @param otherRoots other command roots
     * @return the builder
     */
    public static EdictBuilder builder(@Nullable Object mainRoot, @NotNull Object... otherRoots) {
        List<Object> r = new ArrayList<>();
        if (mainRoot != null) {
            r.add(mainRoot);
        }
        r.addAll(List.of(otherRoots));
        return new EdictBuilder().roots(r);
    }

    /**
     * Builder for Edict.
     */
    public static class EdictBuilder {
        /**
         * Register a new {@link ParameterHandler}. If none were registered before this, it also loads the {@link Edict#defaultParameterHandlers}.
         * @param handler the handler to register
         * @return this
         */
        public EdictBuilder parameterHandler(ParameterHandler<?> handler) {
            if (parameterHandlers$value == null) {
                parameterHandlers(new ParameterHandlers(defaultParameterHandlers));
            }
            parameterHandlers$value.add(handler);
            return this;
        }

        /**
         * Register a new {@link ContextHandler}.
         * @param handler the handler to register
         * @return this
         */
        public EdictBuilder contextHandler(ContextHandler<?> handler) {
            if (contextHandlers$value == null) {
                contextHandlers(new ContextHandlers());
            }
            contextHandlers$value.add(handler);
            return this;
        }
    }

    /**
     * Construct Edict. Recommend using {@link #builder(Object, Object...)} instead.
     * @param roots the root command classes
     * @param systemUser the user to send system messages to
     * @param syncRunner the consumer that takes runnable objects that must be run sync
     * @param settings the settings
     * @param permissionFactory factory to make permissions
     * @param parameterHandlers parameter handlers
     * @param contextHandlers context handlers
     * @throws NullPointerException if the {@link ParameterHandler} for any of the parameters of any methods of the {@link #roots} or any of its children is not registered
     * or if the {@link ContextHandler} for any of the contextual parameter of any methods of the {@link #roots} or any of its children is not registered
     */
    @Builder
    public Edict(
            @NotNull List<Object> roots,
            @NotNull SystemUser systemUser,
            @NotNull Consumer<Runnable> syncRunner,
            @NotNull EDictionary settings,
            @NotNull BiFunction<@Nullable Permission, @NotNull String, @NotNull Permission> permissionFactory,
            @NotNull ParameterHandlers parameterHandlers,
            @NotNull ContextHandlers contextHandlers,
            @NotNull String networkStringIndent
    ) throws NullPointerException {
        this.roots = roots;
        this.systemUser = systemUser;
        this.syncRunner = syncRunner;
        this.settings = settings;
        this.permissionFactory = permissionFactory;
        this.parameterHandlers = parameterHandlers;
        this.contextHandlers = contextHandlers;
        this.networkStringIndent = networkStringIndent;

        // Command Roots
        for (Object root : roots) {
            VCommandable vRoot = VClass.fromInstance(root, null, this);
            if (vRoot == null) {
                w(new StringMessage("Could not register root category: " + root.getClass().getSimpleName() + " due to circular reference!"));
                continue;
            }
            rootCommands.add(vRoot);
        }

        // Indexer
        indexer.addAll(rootCommands);

        // Print
        i(new StringMessage(networkString()));
    }

    /**
     * Run a command through the system.
     * @param command the command to run
     * @param user the user that ran the command
     */
    final public void command(@NotNull String command, @NotNull User user) {
        command(command, user, false);
    }

    /**
     * Run a command through the system.
     * @param command the command to run
     * @param user the user that ran the command
     * @param forceSync force the execution of this command in sync (testing)
     */
    final public void command(@NotNull String command, @NotNull User user, boolean forceSync) {
        i(new StringMessage(user.name() + " sent command: " +  command));
        Runnable r = () -> {

            final String fCommand = ParameterParser.cleanCommand(command.strip());

            i(new StringMessage(user.name() + " sent command: " + fCommand));

            List<String> input = List.of(fCommand.split(" "));

            // Blank check
            if (input.isEmpty()) {
                for (VCommandable root : rootCommands) {
                    user.send(root.getHelpFor(user));
                }
                return;
            }

            d(new StringMessage("Running command: " + fCommand));

            // Loop over roots
            new UserContext().post(user);
            new SystemContext().post(this);

            // Future
            CompletableFuture<String> future = completableCommandsRegistry.getCompletableFor(user);
            if (future != null) {
                d(new StringMessage(user.name() + " completed command with " + String.join(" ", input)));
                future.complete(command);
                return;
            }

            for (VCommandable root : indexer.search(input.get(0), getSettings().matchThreshold, (vCommandable -> user.hasPermission(vCommandable.permission())))) {
                d(new StringMessage("Running root: " + root.name()));
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
     * Get suggestions for a command.
     * @param command the input to get suggestions for
     * @param user the user that wants the suggestions
     * @param suggestionOutput the completable to send the output to when it is done
     * @param forceSync force the execution of suggestion retrieval in sync (testing)
     */
    final public void suggest(@NotNull String command, @NotNull User user, @NotNull CompletableFuture<List<String>> suggestionOutput, boolean forceSync) {
        i(new StringMessage(user.name() + " wants suggestions for command: " +  command));
        Runnable r = () -> {

            final String fCommand = ParameterParser.cleanCommand(command);

            List<String> input = List.of(fCommand.split(" "));

            // Blank check
            if (input.isEmpty()) {
                List<String> primarySuggestions = new ArrayList<>();
                List<String> secondarySuggestions = new ArrayList<>();
                for (VCommandable root : rootCommands) {
                    primarySuggestions.add(root.name());
                    secondarySuggestions.addAll(root.getAliases());
                }
                primarySuggestions.addAll(secondarySuggestions);
                suggestionOutput.complete(primarySuggestions);
                return;
            }

            d(new StringMessage("Suggesting for command: " + fCommand));

            // Loop over roots
            new UserContext().post(user);
            new SystemContext().post(this);

            List<String> suggestions = new ArrayList<>();
            for (VCommandable root : indexer.search(input.get(0), getSettings().matchThreshold, (vCommandable -> user.hasPermission(vCommandable.permission())))) {
                d(new StringMessage("Running root suggestions: " + ((VClass) root).instance().getClass().getSimpleName()));
                suggestions.addAll(root.suggest(input.subList(1, input.size()), user));
            }

            if (!suggestions.isEmpty()) {
                suggestionOutput.complete(suggestions);
                return;
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
    @NotNull
    final public Permission makePermission(@Nullable Permission parent, @NotNull String input) {
        return permissionFactory.apply(parent, input);
    }

    /**
     * Send an information message to the system.
     */
    final public void i(Message message) {
        systemUser.i(message);
    }

    /**
     * Send a warning message to the system.
     */
    final public void w(Message message) {
        systemUser.w(message);
    }

    /**
     * Send a debug message to the system.
     */
    final public void d(Message message) {
        systemUser.d(message);
    }

    /**
     * Get system settings.
     * @return the system settings
     */
    final public EDictionary getSettings() {
        return settings;
    }

    /**
     * Get the {@link ParameterHandlers}.
     * @return the {@link ParameterHandlers}
     */
    final public ParameterHandlers getParameterHandlers() {
        return parameterHandlers;
    }

    /**
     * Get the {@link ContextHandlers}.
     * @return the {@link ContextHandlers}
     */
    final public ContextHandlers getContextHandlers() {
        return contextHandlers;
    }

    /**
     * Get the {@link CompletableCommandsRegistry}.
     * @return the {@link CompletableCommandsRegistry}
     */
    final public CompletableCommandsRegistry getCompletableCommandsRegistry() {
        return completableCommandsRegistry;
    }

    /**
     * Run a runnable in sync, using the {@link #syncRunner}.
     * @param runnable the runnable to run
     */
    final public void runSync(Runnable runnable) {
        syncRunner.accept(runnable);
    }

    /**
     * String representation of the command network.
     * @return a string representing the command network.
     */
    final public @NotNull String networkString() {
        StringBuilder builder = new StringBuilder();
        int rootCats = 0;
        int rootComs = 0;
        for (VCommandable rootCommand : rootCommands) {
            if (rootCommand instanceof VClass) {
                rootCats++;
            } else {
                rootComs++;
            }
        }
        builder.append("Command Network\n")
                .append("Name [alias, alias, alias] ((sub)categories | commands)").append("\n")
                .append("<root> (").append(rootCats).append(" | ").append(rootComs).append(")");
        for (VCommandable rootCommand : rootCommands) {
            rootCommand.networkString(builder, networkStringIndent, networkStringIndent);
        }
        return builder.toString();
    }
}
