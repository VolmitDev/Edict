package art.arcane.edict.handlers.handlers;

import art.arcane.edict.exception.ParsingException;
import art.arcane.edict.handlers.ParameterHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DoubleHandler implements ParameterHandler<Double> {
    @Override
    public List<Double> getPossibilities() {
        return null;
    }

    @Override
    public @NotNull Double parse(String in, boolean force, String parameterName) throws ParsingException {
        try {
            AtomicReference<String> r = new AtomicReference<>(in);
            double m = getMultiplier(r);
            return Double.parseDouble(r.get()) * m;
        } catch (Throwable e) {
            throw new ParsingException(Double.class, parameterName, in, e);
        }
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(Double.class) || type.equals(double.class);
    }

    @Override
    public String toString(Double f) {
        return f.toString();
    }

    @Override
    public String getRandomDefault() {
        return String.valueOf(randomDouble(0, 99.99));
    }
}
