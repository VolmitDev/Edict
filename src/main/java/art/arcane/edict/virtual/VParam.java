package art.arcane.edict.virtual;

import art.arcane.edict.Edict;
import art.arcane.edict.command.Param;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

/**
 * Record for a virtual parameter.
 * @param param the parameter annotation
 * @param parameter the parameter itself
 * @param system the command system
 */
public record VParam(@NotNull Param param, @NotNull Parameter parameter, @NotNull Edict system) {

    /**
     * Create a list of parameters from a method.
     * @param method the method
     * @param system the command system
     * @return a list of parameters. Can be empty if there are none
     * @throws MissingResourceException if any of the parameters is not annotated by @Param (making it invalid)
     */
    public static @NotNull List<VParam> fromMethod(@NotNull Method method, @NotNull Edict system) throws MissingResourceException {
        List<VParam> params = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            if (!parameter.isAnnotationPresent(Param.class)) {
                throw new MissingResourceException("@Param annotation missing on provided parameter", parameter.getClass().getSimpleName(), "@Param");
            }
            params.add(new VParam(parameter.getDeclaredAnnotation(Param.class), parameter, system));
        }
        return params;
    }
}
