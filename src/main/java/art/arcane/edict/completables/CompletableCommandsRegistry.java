package art.arcane.edict.completables;

import art.arcane.edict.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for commands that need to be completed.
 * TODO: Feed back data into here
 */
public class CompletableCommandsRegistry {

    /**
     * The list of completable commands registered.
     */
    private final ConcurrentHashMap<User, CompletableFuture<String>> commands = new ConcurrentHashMap<>();

    /**
     * Register a new completable command. Adding a new completable command for the same user overwrites any existing completable command.
     * @param command the completable command to register
     */
    public void register(@NotNull User user, @NotNull CompletableFuture<String> command) {
        commands.put(user, command);
    }

    /**
     * Get the completable command for a certain user.
     * @param user the user to get the completable command for
     * @return the requested completable command or {@code null} if there is no completable command registered for the specified {@code user}
     */
    public CompletableFuture<?> getHandlerFor(User user) throws NullPointerException {
        return commands.get(user);
    }
}
