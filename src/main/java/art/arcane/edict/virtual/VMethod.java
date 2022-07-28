package art.arcane.edict.virtual;

import art.arcane.edict.Edict;
import art.arcane.edict.api.Command;
import art.arcane.edict.message.CompoundMessage;
import art.arcane.edict.message.HoverableClickableMessage;
import art.arcane.edict.message.HoverableMessage;
import art.arcane.edict.message.StringMessage;
import art.arcane.edict.permission.Permission;
import art.arcane.edict.user.User;
import art.arcane.edict.parser.ParameterParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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

    /**
     * Send help to a user.
     *
     * @param user the user
     */
    @Override
    public @NotNull CompoundMessage getHelpFor(@NotNull User user) {

        String mainText = name();
        String hoverText = name();
        List<String> onRunCommand = buildCommand(user);
        CompoundMessage result;

        if (aliases().length != 0) {
            hoverText += " (" + String.join(", ", aliases()) + ")\n";
        }

        if (!command.description().isBlank()) {
            hoverText += command().description();
        }

        if (user.canUseClickable()) {
            hoverText += "\nClick to run:\n" + String.join(" ", onRunCommand);
            Runnable onRun = () -> user.suggestCommand(String.join(" ", onRunCommand));
            result = new CompoundMessage(new HoverableClickableMessage(
                    mainText,
                    hoverText,
                    onRun
            ));
        } else {
            hoverText += "\nTo run with this parameter, enter:\n" + String.join(" ", onRunCommand);
            result = new CompoundMessage(new HoverableMessage(
                    mainText,
                    hoverText
            ));
        }

        for (VParam param : params) {
            result.add(param.getHelpFor(user));
        }

        return result;
    }

    /**
     * Get the number of required parameters for some user.
     * @param user the user
     * @return the number of required parameters
     */
    public int getInputRequirementFor(User user) {
        return (int) params.stream().filter(p -> p.isRequiredFor(user)).count();
    }

    /**
     * Build the required (full) command to get to this parameter.
     * @param user the user to build the command for
     * @return the command suggestion
     */
    private @NotNull List<String> buildCommand(@NotNull User user) {

        VCommandable parent = parent();
        List<String> command = new ArrayList<>();
        while (parent != null) {
            command.add(parent.name());
            parent = parent.parent();
        }

        for (VParam param : params) {
            if (param.isRequiredFor(user)) {
                command.add(param.name() + "= ");
            }
        }

        command.add(name() + "= ");

        return command;
    }

    @Override
    public boolean run(@NotNull List<String> input, @NotNull User user) {
        if (input.size() < params.stream().filter(p -> !(p.param().contextual() && user.canUseContext()) || !p.param().defaultValue().isBlank()).count()) {
            user.send(getHelpFor(user));
            return true;
        }
        user.send(new StringMessage("Running command '" + name() + "' with input: '" + String.join(", ", input) + "'"));
        ParameterParser parser = new ParameterParser(input, params, user, system);
        Object[] values = parser.parse();

        if (!parser.getBadArgsAndReasons().isEmpty()) {
            user.send(new StringMessage("Some of your inputs were bad & ignored:"));
            for (String argAndReason : parser.getBadArgsAndReasons()) {
                user.send(new StringMessage(" - " + argAndReason));
            }
        }

        if (values == null) {
            user.send(new StringMessage("Some parameters did not get a value:"));
            for (VParam param : parser.getMissingInputs()) {
                user.send(new StringMessage(" - " + param.name() + " (" + param.parameter().getType().getSimpleName() + ")"));
            }
            user.send(new StringMessage("Please try running the command again after fixing the parameters"));
            return true;
        }

        String reason = verifyParameters(values, method);
        if (reason != null) {
            long l = System.currentTimeMillis();
            user.send(new StringMessage("WARNING: System error, parameter value extraction failed. Please contact your admin with code: " + l));
            system.w(new StringMessage("(Code " + l + ") Parameter value extraction failed for " + parent().instance().getClass() + "#" + method.getName() + " with input '" + String.join(" ", input) + "' -> " + Arrays.toString(values) + "\n" +
                    "Because of: " + reason));
            return true;
        }

        AtomicBoolean success = new AtomicBoolean(true);

        Runnable executor = () -> {
            try {
                method.invoke(parent.instance(), values);
                success.set(true);
            } catch (IllegalAccessException | InvocationTargetException e) {
                long l = System.currentTimeMillis();
                user.send(new StringMessage("WARNING: System error, please contact your admin. Code: " + l));
                system.w(new StringMessage("(Code: " + l + ") Failed to invoke " + method.getName() + " on " + parent.getClass().getSimpleName() + " due to " + e));
                system.w(new StringMessage(Arrays.toString(e.getStackTrace())));
                system.w(new StringMessage("This is MOST likely an issue with Edict. Please contact us with the method (and class) and command that was ran."));
                success.set(false);
            }
        };

        if (command().sync()) {
            system.runSync(executor);
        } else {
            executor.run();
        }

        return success.get();
    }

    /**
     * Verify that the generated parameters for a method are correct.
     * @param parameterValues the generated parameter values
     * @param method the method
     * @return null if successful. Otherwise, a string message with the reason
     */
    private static @Nullable String verifyParameters(@NotNull Object @NotNull [] parameterValues, @NotNull Method method) {
        if (parameterValues.length != method.getParameters().length) {
            return parameterValues.length + " does not equal required parameter count of " + method.getParameters().length;
        }
        for (int i = 0; i < method.getParameters().length; i++) {
            if (parameterValues[i] == null) {
                continue;
            }

            if (parameterValues[i].getClass() != method.getParameters()[i].getType()) {
                return "Type of parameter " + i + " is " + parameterValues[i].getClass().getSimpleName() + " but should be " + method.getParameters()[i].getType().getSimpleName();
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return command.hashCode() + method.hashCode() + params.hashCode() + permission.hashCode() + system.hashCode();
    }
}
