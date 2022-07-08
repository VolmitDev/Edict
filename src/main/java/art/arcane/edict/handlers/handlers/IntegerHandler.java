package art.arcane.edict.handlers.handlers;


import art.arcane.edict.exception.ParsingException;
import art.arcane.edict.handlers.ParameterHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class IntegerHandler implements ParameterHandler<Integer> {
    @Override
    public List<Integer> getPossibilities() {
        return null;
    }

    @Override
    public @NotNull Integer parse(String in, boolean force, String parameterName) throws ParsingException {
        try {
            AtomicReference<String> r = new AtomicReference<>(in);
            double m = getMultiplier(r);
            return (int) (Integer.valueOf(r.get()).doubleValue() * m);
        } catch (Throwable e) {
            throw new ParsingException(Integer.class, parameterName, in, e);
        }
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(Integer.class) || type.equals(int.class);
    }

    @Override
    public String toString(Integer f) {
        return f.toString();
    }

    @Override
    public String getRandomDefault() {
        return String.valueOf(randomInt(0, 99));
    }
}
