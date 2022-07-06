package art.arcane.edict.exceptions;

import art.arcane.edict.handlers.ParameterHandler;

import java.util.List;

/**
 * Thrown when more than one option is available for a singular mapping<br>
 * Like having a hashmap where one input maps to two outputs.
 */
public class WhichException extends Exception {

    /**
     * List of options that the handler could not decide between.
     */
    private final List<?> options;

    /**
     * The handler of the parameter type from which this issue originated.
     */
    private final ParameterHandler<?> handler;

    /**
     * An exception thrown when the handler has to decide between multiple options, but they are equally good.
     * @param type the type of parameter to choose for
     * @param input the input string that lead to this issue
     * @param options the options the handler could not decide between
     * @param handler the handler that raised this issue
     */
    public WhichException(Class<?> type, String input, List<?> options, ParameterHandler<?> handler) {
        super("Cannot parse \"" + input + "\" into type " + type.getSimpleName() + " because of multiple options");
        this.options = options;
        this.handler = handler;
    }

    /**
     * Get the options the handler could not choose between.
     * @return the options
     */
    public List<?> getOptions() {
        return options;
    }

    /**
     * Get the handler that could not decide on an option.
     * @return the handler associated with this issue
     */
    public ParameterHandler<?> getHandler() {
        return handler;
    }
}
