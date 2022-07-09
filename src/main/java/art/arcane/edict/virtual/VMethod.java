package art.arcane.edict.virtual;

import art.arcane.edict.Edict;
import art.arcane.edict.api.Command;
import art.arcane.edict.message.StringMessage;
import art.arcane.edict.permission.Permission;
import art.arcane.edict.user.User;
import art.arcane.edict.util.ParameterParser;
import org.jetbrains.annotations.Contract;
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
            // TODO: Send command help
            user.send(new StringMessage("Send more parameters bitch"));
            return true;
        }
        user.send(new StringMessage("Running command " + name() + " with input: " + String.join(", ", input)));
        try {
            Object[] values = ParameterParser.parse(input, params, user, system);

            if (!VMethod.verifyParameters(values, method)){
                long l = System.currentTimeMillis();
                user.send(new StringMessage("WARNING: System error, parameter value extraction failed. Please contact your admin with code: " + l));
                system.w(new StringMessage("(Code + " + l + ") Parameter value extraction failed for " + parent().getClass() + "#" + method.getName() + " with input '" + String.join(" ", input) + "' -> " + Arrays.toString(values)));
            }

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

    /**
     * Verify that the generated parameters for a method are correct.
     * @param parameterValues the generated parameter values
     * @param method the method
     * @return true if the parameter values are valid for the method, false if not
     */
    private static boolean verifyParameters(@NotNull Object[] parameterValues, @NotNull Method method) {
        for (int i = 0; i < method.getParameters().length; i++) {
            if (parameterValues[i] == null) {
                continue;
            }

            if (parameterValues[i].getClass() != method.getParameters()[i].getType()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return command.hashCode() + method.hashCode() + params.hashCode() + permission.hashCode() + system.hashCode();
    }
}
