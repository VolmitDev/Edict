package art.arcane.edict.exception;

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
     * List of options that the handler could not decide between.
     */
    private final @NotNull List<?> options;

    /**
     * An exception thrown when the handler has to decide between multiple options, but they are equally good.
     * @param type the type of parameter to choose for
     * @param input the input string that lead to this issue
     * @param options the options the handler could not decide between
     */
    @SuppressWarnings("unused")
    public WhichException(@NotNull Class<?> type, @NotNull String input, @NotNull List<?> options) {
        super("Cannot parse \"" + input + "\" into type " + type.getSimpleName() + " because of multiple options");
        this.input = input;
        this.options = options;
    }

    /**
     * Get the options the handler could not choose between.
     * @return the options
     */
    public @NotNull List<?> getOptions() {
        return options;
    }

    /**
     * Get the input for which the WhichException occurred.
     * @return the input
     */
    public @NotNull String getInput() {
        return input;
    }
}
