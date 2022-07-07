package art.arcane.edict.virtual;

import art.arcane.edict.Edict;
import art.arcane.edict.command.Command;
import art.arcane.edict.message.StringMessage;
import art.arcane.edict.permission.Permission;
import art.arcane.edict.user.User;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Record of a command, representing the final node in the tree of commands.
 * I.e. like a leaf in a tree data-structure.
 * @param command the command annotation
 * @param parent parent branches
 * @param method the method for this command
 * @param permission the permission node of this command
 * @param params the parameters of this method {@link VParam}s
 * @param system the command system
 */
public record VCommand(@NotNull Command command, @NotNull VCommands parent, @NotNull Method method, @NotNull List<VParam> params, @NotNull Permission permission, @NotNull Edict system) implements VCommandable {

    @Override
    public @NotNull String name() {
        return command.name().isBlank() ? method.getName() : command.name();
    }

    @Override
    public String[] aliases() {
        return command.aliases();
    }
}
