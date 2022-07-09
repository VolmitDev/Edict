package art.arcane.edict.handler.handlers;

import art.arcane.edict.exception.ParsingException;
import art.arcane.edict.handler.ParameterHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntegerHandlerTest {

    private final ParameterHandler<Integer> SUT = new IntegerHandler();

    @Test
    void getPossibilities() {
        assertNull(SUT.getPossibilities());
        assertNull(SUT.getPossibilities("1"));
    }

    @Test
    void parse() {
        try {
            assertEquals(15, SUT.parse("15", ""));
            assertEquals(Integer.MIN_VALUE, SUT.parse(String.valueOf(Integer.MIN_VALUE), ""));
            assertThrowsExactly(ParsingException.class, () -> SUT.parse("text", ""));
            assertThrowsExactly(ParsingException.class, () -> SUT.parse("1,9", ""));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void supports() {
        assertTrue(SUT.supports(Integer.class));
    }

    @Test
    void testToString() {
        assertEquals("12", SUT.toString(12));
        assertEquals("-10000", SUT.toString(-10000));
    }

    @Test
    void getRandomDefault() {
        try {
            int val = SUT.parse(SUT.getRandomDefault(), "");
            assertTrue(val < Integer.MAX_VALUE && val > Integer.MIN_VALUE);
        } catch (Exception e) {
            fail(e);
        }
    }
}