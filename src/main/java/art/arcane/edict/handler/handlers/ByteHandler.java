package art.arcane.edict.handler.handlers;

import art.arcane.edict.exception.ParsingException;
import art.arcane.edict.handler.ParameterHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ByteHandler implements ParameterHandler<Byte> {

    @Override
    public List<Byte> getPossibilities() {
        return null;
    }

    @Override
    public String toString(Byte aByte) {
        return aByte.toString();
    }

    @Override
    public @NotNull Byte parse(String in, boolean force, String parameterName) throws ParsingException {
        try {
            return Byte.parseByte(in);
        } catch (Throwable e) {
            throw new ParsingException(Byte.class, parameterName, in, e);
        }
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(Byte.class) || type.equals(byte.class);
    }

    @Override
    public String getRandomDefault() {
        return String.valueOf(randomInt(Byte.MIN_VALUE, Byte.MAX_VALUE));
    }
}
