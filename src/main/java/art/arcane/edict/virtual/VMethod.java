package art.arcane.edict.virtual;

import art.arcane.edict.Edict;
import art.arcane.edict.api.Command;
import art.arcane.edict.message.StringMessage;
import art.arcane.edict.permission.Permission;
import art.arcane.edict.user.User;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
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
public record VMethod(@NotNull Command command, @NotNull VClass parent, @NotNull Method method, @NotNull List<VParam> params, @NotNull Permission permission, @NotNull Edict system) implements VCommandable {

    @Override
    public @NotNull String name() {
        return command.name().isBlank() ? method.getName() : command.name();
    }

    @Override
    public String[] aliases() {
        return command.aliases();
    }

    @Override
    public boolean run(@NotNull List<String> input, @NotNull User user) {
        if (input.size() < params.size()) {
            // TODO: Send help
            user.send(new StringMessage("Send more parameters bitch"));
            return true;
        }
        user.send(new StringMessage("Running command " + name() + " with input: " + String.join(", ", input)));
        try {
            // TODO: Proper implementation that actually supports parameters. smh.
            method.invoke(parent.instance());
            return true;
        } catch (IllegalAccessException | InvocationTargetException e) {
            long l = System.currentTimeMillis();
            user.send(new StringMessage("WARNING: System error, please contact your admin. Code: " + l));
            system.w(new StringMessage("(Code: " + l + ") Failed to invoke " + method.getName() + " on " + parent.getClass().getSimpleName() + " due to " + e));
            system.w(new StringMessage(Arrays.toString(e.getStackTrace())));
            system.w(new StringMessage("This is MOST likely an issue with Edict. Please contact us with the method (and class) and command that was ran."));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return command.hashCode() + method.hashCode() + params.hashCode() + permission.hashCode() + system.hashCode();
    }
}
