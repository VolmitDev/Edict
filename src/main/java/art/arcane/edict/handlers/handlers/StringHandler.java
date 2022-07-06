package art.arcane.edict.handlers.handlers;


import art.arcane.edict.exceptions.ParsingException;
import art.arcane.edict.handlers.ParameterHandler;
import art.arcane.edict.util.Randoms;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Abstraction can sometimes breed stupidity
 */
public class StringHandler implements ParameterHandler<String> {
    @Override
    public List<String> getPossibilities() {
        return null;
    }

    @Override
    public String toString(String s) {
        return s;
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public @NotNull String parse(String in, boolean force, String parameterName) throws ParsingException {
        return in;
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(String.class);
    }

    @SuppressWarnings("SpellCheckingInspection")
    private final List<String> defaults = List.of(
            "text",
            "string",
            "blah",
            "derp",
            "yolo"
    );

    @Override
    public String getRandomDefault() {
        return defaults.get(Randoms.irand(0, defaults.size()));
    }
}
