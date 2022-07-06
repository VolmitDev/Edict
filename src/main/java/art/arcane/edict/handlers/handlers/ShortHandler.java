package art.arcane.edict.handlers.handlers;

import art.arcane.edict.exceptions.ParsingException;
import art.arcane.edict.handlers.ParameterHandler;
import art.arcane.edict.util.Randoms;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ShortHandler implements ParameterHandler<Short> {
    @Override
    public List<Short> getPossibilities() {
        return null;
    }

    @Override
    public @NotNull Short parse(String in, boolean force, String parameterName) throws ParsingException {
        try {
            AtomicReference<String> r = new AtomicReference<>(in);
            double m = getMultiplier(r);
            return (short) (Short.valueOf(r.get()).doubleValue() * m);
        } catch (Throwable e) {
            throw new ParsingException(Short.class, parameterName, in, e);
        }
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(Short.class) || type.equals(short.class);
    }

    @Override
    public String toString(Short f) {
        return f.toString();
    }

    @Override
    public String getRandomDefault() {
        return String.valueOf(Randoms.irand(0, 99));
    }
}
