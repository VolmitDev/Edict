package art.arcane.edict.exception;

import art.arcane.edict.handlers.ParameterHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Thrown when more than one option is available for a singular mapping<br>
 * Like having a hashmap where one input maps to two outputs.
 */
public class WhichException extends Exception {

    /**
     * Input for which the WhichException occurred.
     */
    private final @NotNull String input;

    /**
     * The type of parameter for which this WhichException occurred.
     */
    private final @NotNull Class<?> type;

    /**
     * List of options that the handler could not decide between.
     */
    private final @NotNull List<?> options;

    /**
     * The handler of the parameter type from which this issue originated.
     */
    private final @NotNull ParameterHandler<?> handler;

    /**
     * An exception thrown when the handler has to decide between multiple options, but they are equally good.
     * @param type the type of parameter to choose for
     * @param input the input string that lead to this issue
     * @param options the options the handler could not decide between
     * @param handler the handler that raised this issue
     */
    public WhichException(@NotNull Class<?> type, @NotNull String input, @NotNull List<?> options, @NotNull ParameterHandler<?> handler) {
        super("Cannot parse \"" + input + "\" into type " + type.getSimpleName() + " because of multiple options");
        this.type = type;
        this.input = input;
        this.options = options;
        this.handler = handler;
    }

    /**
     * Get the options the handler could not choose between.
     * @return the options
     */
    public @NotNull List<?> getOptions() {
        return options;
    }

    /**
     * Get the handler that could not decide on an option.
     * @return the handler associated with this issue
     */
    public @NotNull ParameterHandler<?> getHandler() {
        return handler;
    }

    /**
     * Get the input for which the WhichException occurred.
     * @return the input
     */
    public @NotNull String getInput() {
        return input;
    }

    /**
     * Get the type for which the WhichException occurred.
     * @return the type
     */
    public @NotNull Class<?> getType() {
        return type;
    }
}
