package art.arcane.edict.handlers.handlers;


import art.arcane.edict.exceptions.ParsingException;
import art.arcane.edict.handlers.ParameterHandler;
import art.arcane.edict.util.Randoms;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LongHandler implements ParameterHandler<Long> {
    @Override
    public List<Long> getPossibilities() {
        return null;
    }

    @Override
    public @NotNull Long parse(String in, boolean force, String parameterName) throws ParsingException {
        try {
            AtomicReference<String> r = new AtomicReference<>(in);
            double m = getMultiplier(r);
            return (long) (Long.valueOf(r.get()).doubleValue() * m);
        } catch (Throwable e) {
            throw new ParsingException(Long.class, parameterName, in, e);
        }
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(Long.class) || type.equals(long.class);
    }

    @Override
    public String toString(Long f) {
        return f.toString();
    }

    @Override
    public String getRandomDefault() {
        return String.valueOf(Randoms.irand(0, 99));
    }
}
