package art.arcane.edict.handlers.handlers;

import art.arcane.edict.exceptions.ParsingException;
import art.arcane.edict.handlers.ParameterHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShortHandlerTest {

    private final ParameterHandler<Short> SUT = new ShortHandler();

    @Test
    void getPossibilities() {
        assertNull(SUT.getPossibilities());
        assertNull(SUT.getPossibilities("1"));
    }

    @Test
    void parse() {
        try {
            assertEquals(Short.MAX_VALUE, SUT.parse(String.valueOf(Short.MAX_VALUE), ""));
            assertEquals(Short.MIN_VALUE, SUT.parse(String.valueOf(Short.MIN_VALUE), ""));
            assertThrowsExactly(ParsingException.class, () -> SUT.parse("nope", ""));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void supports() {
        assertTrue(SUT.supports(Short.class));
    }

    @Test
    void testToString() {
        assertEquals("15", SUT.toString((short) 15));
        assertEquals("-10", SUT.toString((short) -10));
        assertEquals(String.valueOf(Short.MAX_VALUE), SUT.toString(Short.MAX_VALUE));
        assertEquals(String.valueOf(Short.MIN_VALUE), SUT.toString(Short.MIN_VALUE));
    }

    @Test
    void getRandomDefault() {
        try {
            short val = SUT.parse(SUT.getRandomDefault(), "");
            assertTrue(val < Short.MAX_VALUE && val > Short.MIN_VALUE);
        } catch (Exception e) {
            fail(e);
        }
    }
}