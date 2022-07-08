package art.arcane.edict.exception;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown when a  parameter is parsed, but parsing fails.
 */
public class ParsingException extends Exception {

    /**
     * The name of the parameter.
     */
    private final @NotNull String name;

    /**
     * The type of the failed parameter.
     */
    private final @NotNull Class<?> type;

    /**
     * The input string given for the failed parameter.
     */
    private final @NotNull String input;

    /**
     * The reason for the failed parameter.
     */
    private final @NotNull String reason;

    /**
     * An exception thrown when a {@link art.arcane.edict.handlers.ParameterHandler} cannot parse the input.
     * @param type the parameter type
     * @param name the name of the parameter
     * @param input the input string
     * @param reason the reason why it failed
     */
    public ParsingException(@NotNull Class<?> type, @NotNull String name, @NotNull String input, @NotNull Throwable reason) {
        this(type, name, input, reason.getClass().getSimpleName() + " - " + reason.getMessage());
    }

    /**
     * An exception thrown when a {@link art.arcane.edict.handlers.ParameterHandler} cannot parse the input.
     * @param type the parameter type
     * @param name the name of the parameter
     * @param input the input string
     * @param reason the reason why it failed
     */
    public ParsingException(@NotNull Class<?> type, @NotNull String name, @NotNull String input, @NotNull String reason) {
        super("Could not parse " + name + " (" + type.getSimpleName() + ") because of: " + reason);
        this.name = name;
        this.type = type;
        this.input = input;
        this.reason = reason;
    }

    /**
     * Get teh the type for which the parsing exception occurred.
     * @return the type
     */
    public @NotNull Class<?> getType() {
        return type;
    }

    /**
     * Get the input string for which the parsing exception occurred.
     * @return the input string
     */
    public @NotNull String getInput() {
        return input;
    }

    /**
     * Get the name of the parameter for which the parsing exception occurred.
     * @return the name of the parameter
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * Get the reason explaining why the exception occurred.
     * @return the reason
     */
    public @NotNull String getReason() {
        return reason;
    }
}
