package art.arcane.edict.virtual;

import art.arcane.edict.Edict;
import art.arcane.edict.message.StringMessage;
import art.arcane.edict.permission.Permission;
import art.arcane.edict.user.User;
import org.jetbrains.annotations.NotNull;

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
     * The system of the commandable.
     * @return the system of the commandable
     */
    @NotNull Edict system();

    /**
     * Run this commandable. It is assumed that this is in fact the right commandable, and that the user has permission.
     * @param input the remaining input string to parse with
     * @param user the user that ran the command
     * @return true if a command leaf successfully ran,
     * or one of the branches sent help for a command (because the command ended there).
     */
    boolean run(@NotNull List<String> input, @NotNull User user);

    /**
     * The degree to which this commandable matches a certain input. Checks for permission.
     * @param input the input string
     * @param user the user to match against (for permission checking)
     * @return a [0, 100] interval integer indicating the degree to which this matches the input as a percentage.
     */
    default int match(@NotNull String input, @NotNull User user) {

        // Permission
        if (!user.hasPermission(permission())) {
            system().d(new StringMessage(name() + ": Failed user permission check against " + permission()));
            return 0;
        }

        // TODO: Implement this properly
        if (allNames().contains(input)) {
            return 100;
        } else {
            return 0;
        }
    }
}
