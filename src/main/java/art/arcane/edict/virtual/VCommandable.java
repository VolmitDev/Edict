package art.arcane.edict.virtual;

import art.arcane.edict.message.CompoundMessage;
import art.arcane.edict.permission.Permission;
import art.arcane.edict.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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
    @NotNull String @NotNull [] aliases();

    /**
     * List of aliases, filtered for empty entries.
     * @return list of aliases for this commandable
     */
    default @NotNull List<String> getAliases() {
        return Stream.of(aliases()).filter(a -> !a.isEmpty()).toList();
    };

    /**
     * Parent commandable.
     * @return the parent commandable
     */
    @NotNull VCommandable parent();

    /**
     * All names (including aliases) of the commandable.
     * @return all names of the commandable
     */
    default @NotNull List<String> allNames() {
        List<String> names = new ArrayList<>(Collections.singletonList(name()));
        names.addAll(getAliases());
        return names;
    }

    /**
     * The permission node of the commandable.
     * @return the permission node of the commandable
     */
    @NotNull Permission permission();

    /**
     * Send help to a user.
     * @param user the user
     */
    @NotNull CompoundMessage getHelpFor(@NotNull User user);

    /**
     * Run this commandable. It is assumed that this is in fact the right commandable, and that the user has permission.
     * @param input the remaining input string to parse with
     * @param user the user that ran the command
     * @return true if a command leaf successfully ran,
     * or one of the branches sent help for a command (because the command ended there).
     */
    boolean run(@NotNull List<String> input, @NotNull User user);

    /**
     * Get suggestions from this commandable. It is assumed that this is in fact the right commandable, and that the user has permission.
     *
     * @param input the remaining input string to parse with
     * @param user  the user that wants suggestions
     * @return a list of strings representing suggestions
     */
    @NotNull List<String> suggest(@NotNull List<String> input, @NotNull User user);

    /**
     * Append the details of this commandable to the network representation string buider.
     * @param builder the builder
     * @param indent the indentation to add when going a layer deeper
     * @param currentIndent the current indentation
     */
    void networkString(@NotNull StringBuilder builder, @NotNull String indent, @NotNull String currentIndent);

    /**
     * Appends the name(s) of this commandable to the network string.
     * @param builder the string builder building the network
     */
    default void appendNamesNetworkString(@NotNull StringBuilder builder) {
        builder.append(name()).append(" ");
        if (!getAliases().isEmpty()) {
            builder.append("[").append(String.join(", ", getAliases())).append("] ");
        }
    }
}
