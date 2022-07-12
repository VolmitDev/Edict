package art.arcane.edict.virtual;

import art.arcane.edict.Edict;
import art.arcane.edict.api.Param;
import art.arcane.edict.exception.ContextMissingException;
import art.arcane.edict.handler.ContextHandler;
import art.arcane.edict.handler.ParameterHandler;
import art.arcane.edict.message.CompoundMessage;
import art.arcane.edict.message.HoverableClickableMessage;
import art.arcane.edict.message.HoverableMessage;
import art.arcane.edict.permission.Permission;
import art.arcane.edict.user.User;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

/**
 * Record for a virtual parameter.
 * @param param the parameter annotation
 * @param parameter the parameter itself
 */
public record VParam(@NotNull Param param, @NotNull Parameter parameter, @NotNull VMethod parent, @NotNull ParameterHandler<?> parameterHandler, @Nullable ContextHandler<?> contextHandler, @NotNull Edict system) implements VCommandable {

    /**
     * Create a list of parameters from a method.
     * @param method the method
     * @param system the command system
     * @return a list of parameters. Can be empty if there are none
     * @throws MissingResourceException if any of the parameters is not annotated by @Param (making it invalid)
     * @throws NullPointerException if the {@link ParameterHandler} for any of the parameters of any methods of this class or any of its children is not registered
     * or if the {@link ContextHandler} for any of the contextual parameter of any methods of the {@code commandRoots} or any of its children is not registered
     */
    public static @NotNull List<VParam> paramsFromMethod(@NotNull VMethod parent, @NotNull Method method, @NotNull Edict system) throws MissingResourceException, NullPointerException {
        List<VParam> params = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            if (!parameter.isAnnotationPresent(Param.class)) {
                throw new MissingResourceException("@Param annotation missing on provided parameter", parameter.getClass().getSimpleName(), "@Param");
            }
            Param annotation = parameter.getDeclaredAnnotation(Param.class);
            params.add(new VParam(
                    annotation,
                    parameter,
                    parent,
                    system.getParameterHandlers().getHandlerFor(parameter.getType()),
                    annotation.contextual() ? system.getContextHandlers().getHandlerFor(parameter.getType()) : null,
                    system
            ));
        }
        params.sort((o1, o2) -> {
            int result = 0;
            if (!o1.param.defaultValue().isBlank()) {
                result -= 2;
            }
            if (!o2.param.defaultValue().isBlank()) {
                result += 2;
            }
            if (o1.param.contextual()) {
                result -= 1;
            }
            if (o2.param.contextual()) {
                result += 1;
            }
            return result;
        });
        return params;
    }

    /**
     * Name of the commandable.
     *
     * @return the name of the commandable
     */
    @Override
    public @NotNull String name() {
        return param.name().isBlank() ? parameter.getName() : param.name();
    }

    /**
     * List of aliases for this commandable.
     *
     * @return list of aliases for this commandable
     */
    @Override
    public @NotNull String[] aliases() {
        return param.aliases();
    }

    /**
     * The permission node of the commandable.
     *
     * @return the permission node of the commandable
     */
    @Override
    public @NotNull Permission permission() {
        throw new NotImplementedException();
    }

    /**
     * Whether this parameter is required for a certain user.
     * @param user the user
     * @return true if it is required for the user
     */
    public boolean isRequiredFor(User user) {
        return !param.defaultValue().isBlank() || (user.canUseContext() && param.contextual());
    }

    /**
     * Send help to a user.
     *
     * @param user the user
     */
    @Override
    public @NotNull CompoundMessage getHelpFor(@NotNull User user) {
        // For all params {P.Name} + brackets for type + hoverable description + clickable with best approximation

        String mainText = name();
        String hoverText = name();
        List<String> onRunCommand = buildCommand(user);

        if (aliases().length != 0) {
            hoverText += " (" + String.join(", ", aliases()) + ")\n";
        }

        if (!param().description().isBlank()) {
            hoverText += param().description() + "\n";
        }

        if (!param().defaultValue().isBlank()) {
            hoverText += "Uses default value: " + param().defaultValue();
        } else if (param().contextual() && user.canUseContext()) {
            try {
                assert contextHandler != null;
                String contextValue = parameterHandler.toStringForce(contextHandler.handle(user));
                hoverText += "Uses your context value: " + contextValue;
            } catch (ContextMissingException ignored) {
                hoverText += "Required parameter";
            }
        } else {
            hoverText += "Required parameter";
        }

        if (user.canUseClickable()) {
            hoverText += "\nClick to run:\n" + String.join(" ", onRunCommand);
            Runnable onRun = () -> user.suggestCommand(String.join(" ", onRunCommand));
            return new CompoundMessage(new HoverableClickableMessage(
                    mainText,
                    hoverText,
                    onRun
            ));
        } else {
            hoverText += "\nTo run with this parameter, enter:\n" + String.join(" ", onRunCommand);
            return new CompoundMessage(new HoverableMessage(
                    mainText,
                    hoverText
            ));
        }
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

        List<VParam> otherParams = new ArrayList<>(parent().params());
        otherParams.remove(this);

        for (VParam otherParam : otherParams) {
            if (isRequiredFor(user)) {
                command.add(otherParam.name() + "= ");
            }
        }

        command.add(name() + "= ");

        return command;
    }

    /**
     * Run this commandable. It is assumed that this is in fact the right commandable, and that the user has permission.
     *
     * @param input the remaining input string to parse with
     * @param user  the user that ran the command
     * @return true if a command leaf successfully ran,
     * or one of the branches sent help for a command (because the command ended there).
     */
    @Override
    public boolean run(@NotNull List<String> input, @NotNull User user) {
        throw new NotImplementedException();
    }
    @Override
    public int hashCode() {
        return param.hashCode() + parameter.hashCode() + parameterHandler.hashCode() + (contextHandler != null ? contextHandler.hashCode() : 0) + system.hashCode();
    }
}
