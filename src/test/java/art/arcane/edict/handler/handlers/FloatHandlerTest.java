package art.arcane.edict.handler.handlers;

import art.arcane.edict.exception.ParsingException;
import art.arcane.edict.handler.ParameterHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FloatHandlerTest {

    private final ParameterHandler<Float> SUT = new FloatHandler();

    @Test
    void getPossibilities() {
        assertNull(SUT.getPossibilities());
        assertNull(SUT.getPossibilities("5.1"));
    }

    @Test
    void parse() {
        try {
            assertEquals(1.5f, SUT.parse("1.5", ""));
            assertEquals(Float.MIN_VALUE, SUT.parse(String.valueOf(Float.MIN_VALUE), ""));
            assertEquals(Float.MAX_VALUE, SUT.parse(String.valueOf(Float.MAX_VALUE), ""));
            assertEquals(-1.5125f, SUT.parse("-1.5125", ""));
            assertThrowsExactly(ParsingException.class, () -> SUT.parse("-1,125.5", ""));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void supports() {
        assertTrue(SUT.supports(Float.class));
    }

    @Test
    void testToString() {
        assertEquals("-1.5", SUT.toString(-1.5f));
        assertEquals(String.valueOf(Float.MIN_VALUE), SUT.toString(Float.MIN_VALUE));
    }

    @Test
    void getRandomDefault() {
        try {
            float def = SUT.parse(SUT.getRandomDefault(), "");
            assertTrue(def < Float.MAX_VALUE && def > Float.MIN_VALUE);
        } catch (Exception e) {
            fail(e);
        }
    }
}