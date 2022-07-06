package art.arcane.edict.handlers.handlers;

import art.arcane.edict.exceptions.ParsingException;
import art.arcane.edict.exceptions.WhichException;
import art.arcane.edict.handlers.ParameterHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

class DoubleHandlerTest {

    ParameterHandler<Double> SUT = new DoubleHandler();

    @Test
    void getPossibilities() {
        assertNull(SUT.getPossibilities());
        assertNull(SUT.getPossibilities("5"));
    }

    @Test
    void parse() {
        try {
            assertEquals(5d, SUT.parse("5", ""));
            assertEquals(Double.MAX_VALUE, SUT.parse(String.valueOf(Double.MAX_VALUE), ""));
            assertEquals(Double.MIN_VALUE, SUT.parse(String.valueOf(Double.MIN_VALUE), ""));
            assertThrowsExactly(ParsingException.class, () -> SUT.parse("-12,5", ""));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void supports() {
        assertTrue(SUT.supports(Double.class));
    }

    @Test
    void testToString() {
        assertEquals("5.0", SUT.toString(5d));
        assertEquals("-121415.0", SUT.toString(-121415d));
        assertEquals("-12.5", SUT.toString(-12.5d));
    }

    @Test
    void getRandomDefault() {
        try {
            double val = SUT.parse(SUT.getRandomDefault(), "");
            assertTrue(val < Double.MAX_VALUE && val > Double.MIN_VALUE);
        } catch (Exception e) {
            fail(e);
        }
    }
}