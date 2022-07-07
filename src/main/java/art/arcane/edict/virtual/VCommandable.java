package art.arcane.edict.virtual;

import art.arcane.edict.Edict;
import art.arcane.edict.message.StringMessage;
import art.arcane.edict.permission.Permission;
import art.arcane.edict.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface VCommandable {

    /**
     * Name of the commandable.
     * @return the name of the commandable
     */
    @NotNull String name();

    /**
     * List of aliases for this commandable.
     * @return list of aliases for this commandable
     */
    @NotNull String[] aliases();

    /**
     * All names (including aliases) of the commandable.
     * @return all names of the commandable
     */
    default @NotNull List<String> allNames() {
        List<String> names = new ArrayList<>(Collections.singletonList(name()));
        names.addAll(List.of(aliases()));
        return names;
    }

    /**
     * The permission node of the commandable.
     * @return the permission node of the commandable
     */
    @NotNull Permission permission();

    /**
     * The parent of the commandable.
     * @return the parent of the commandable
     */
    @Nullable VCommands parent();

    /**
     * The system of the commandable.
     * @return the system of the commandable
     */
    @NotNull Edict system();

    /**
     * Whether the commandable matches a certain input string.
     * @param input the input string
     * @return true if the commandable matches the input string, false if not
     */
    default boolean matches(@NotNull String input) {
        // TODO: Implement logic. Maybe this processing part should be done in the parent considering the nature of the problem; finding the best match.
        // TODO: The alternative is strict matching.
        // TODO: Perhaps make this return a [0, 1] double to indicate the degree to which it matches.
        return true;
    };

    default void run(@NotNull List<String> input, @NotNull User user) {

        // Empty input list
        if (input.isEmpty()) {
            user.send(new StringMessage(name() + ": Cannot run empty command"));
            return;
        }

        // Match
        if (!matches(input.get(0))) {
            user.send(new StringMessage(name() + ": Input string " + input.get(0) + " does not match names or aliases " + String.join(", ", allNames())));
        }

        // Permission
        if (!user.hasPermission(permission())) {
            user.send(new StringMessage(name() + ": You do not have permission to run this category or command."));
        }
    }
}
