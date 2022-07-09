package art.arcane.edict.virtual;

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
     * Run this commandable. It is assumed that this is in fact the right commandable, and that the user has permission.
     * @param input the remaining input string to parse with
     * @param user the user that ran the command
     * @return true if a command leaf successfully ran,
     * or one of the branches sent help for a command (because the command ended there).
     */
    boolean run(@NotNull List<String> input, @NotNull User user);
}
