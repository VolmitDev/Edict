package art.arcane.edict.testconstruct;

import art.arcane.edict.exception.ParsingException;
import art.arcane.edict.exception.WhichException;
import art.arcane.edict.handler.ParameterHandler;

import java.util.List;

public class TestContextValueParameterHandler implements ParameterHandler<TestContextValue> {
    /**
     * Return a random value that may be entered
     *
     * @return a random default value
     */
    @Override
    public String getRandomDefault() {
        return null;
    }

    /**
     * Returns whether a certain type is supported by this handler<br>
     *
     * @param type the type to check
     * @return true if supported, false if not
     */
    @Override
    public boolean supports(Class<?> type) {
        return TestContextValue.class.equals(type);
    }

    /**
     * Converting the type back to a string (inverse of the {@link #parse(String, String) parse} method)
     *
     * @param testContextValue the input of the designated type to convert to a String
     * @return the resulting string
     */
    @Override
    public String toString(TestContextValue testContextValue) {
        return testContextValue.getValue();
    }

    /**
     * Should parse a String into the designated type
     *
     * @param in            the string to parse
     * @param force         force an option instead of throwing a {@link WhichException} if possible (can allow it throwing!)
     * @param parameterName the name of the parameter that is being parsed (only use this to create {@link ParsingException}s and {@link WhichException}s).
     * @return the value extracted from the string, of the designated type
     * @throws ParsingException thrown when the parsing fails (ex: "oop" translated to an integer throws this)
     * @throws WhichException   thrown when multiple results are possible
     */
    @Override
    public TestContextValue parse(String in, boolean force, String parameterName) throws ParsingException, WhichException {
        return new TestContextValue();
    }

    /**
     * Should return the possible values for this type
     *
     * @return possibilities for this type.
     */
    @Override
    public List<TestContextValue> getPossibilities() {
        return List.of(new TestContextValue());
    }
}
