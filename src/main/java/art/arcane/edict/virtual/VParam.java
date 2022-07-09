package art.arcane.edict.virtual;

import art.arcane.edict.Edict;
import art.arcane.edict.api.Param;
import art.arcane.edict.handler.ContextHandler;
import art.arcane.edict.handler.ParameterHandler;
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
public record VParam(@NotNull Param param, @NotNull Parameter parameter, @NotNull ParameterHandler<?> handler, @Nullable ContextHandler<?> contextHandler) implements VCommandable {

    /**
     * Create a list of parameters from a method.
     * @param method the method
     * @param system the command system
     * @return a list of parameters. Can be empty if there are none
     * @throws MissingResourceException if any of the parameters is not annotated by @Param (making it invalid)
     * @throws NullPointerException if the {@link ParameterHandler} for any of the parameters of any methods of this class or any of its children is not registered
     * or if the {@link ContextHandler} for any of the contextual parameter of any methods of the {@code commandRoots} or any of its children is not registered
     */
    public static @NotNull List<VParam> paramsFromMethod(@NotNull Method method, @NotNull Edict system) throws MissingResourceException, NullPointerException {
        List<VParam> params = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            if (!parameter.isAnnotationPresent(Param.class)) {
                throw new MissingResourceException("@Param annotation missing on provided parameter", parameter.getClass().getSimpleName(), "@Param");
            }
            Param annotation = parameter.getDeclaredAnnotation(Param.class);
            params.add(new VParam(
                    annotation,
                    parameter,
                    system.getParameterHandlerRegistry().getHandlerFor(parameter.getType()),
                    annotation.contextual() ? system.getContextHandlerRegistry().getHandlerFor(parameter.getType()) : null));
        }
        return params;
    }

    /**
     * Name of the commandable.
     *
     * @return the name of the commandable
     */
    @Override
    public @NotNull String name() {
        return param.name();
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
}
