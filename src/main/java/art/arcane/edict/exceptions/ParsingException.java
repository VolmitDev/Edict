package art.arcane.edict.exceptions;

/**
 * Thrown when a  parameter is parsed, but parsing fails.
 */
public class ParsingException extends Exception {

    /**
     * The name of the parameter.
     */
    private final String name;

    /**
     * The type of the failed parameter.
     */
    private final Class<?> type;

    /**
     * The input string given for the failed parameter.
     */
    private final String input;

    /**
     * The reason for the failed parameter.
     */
    private final String reason;

    /**
     * An exception thrown when a {@link art.arcane.edict.handlers.ParameterHandler} cannot parse the input.
     * @param type the parameter type
     * @param name the name of the parameter
     * @param input the input string
     * @param reason the reason why it failed
     */
    public ParsingException(Class<?> type, String name, String input, Throwable reason) {
        this(type, name, input, reason.getClass().getSimpleName() + " - " + reason.getMessage());
    }

    /**
     * An exception thrown when a {@link art.arcane.edict.handlers.ParameterHandler} cannot parse the input.
     * @param type the parameter type
     * @param name the name of the parameter
     * @param input the input string
     * @param reason the reason why it failed
     */
    public ParsingException(Class<?> type, String name, String input, String reason) {
        super("Could not parse " + name + " (" + type.getSimpleName() + ") because of: " + reason);
        this.name = name;
        this.type = type;
        this.input = input;
        this.reason = reason;
    }
}
