package art.arcane.edict.handler.handlers;

import art.arcane.edict.exception.ParsingException;
import art.arcane.edict.handler.ParameterHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LongHandlerTest {

    private final ParameterHandler<Long> SUT = new LongHandler();

    @Test
    void getPossibilities() {
        assertNull(SUT.getPossibilities());
        assertNull(SUT.getPossibilities("1"));
    }

    @Test
    void parse() {
        try {
            assertEquals(1000L, SUT.parse("1000", ""));
            assertEquals(Long.MAX_VALUE, SUT.parse(String.valueOf(Long.MAX_VALUE), ""));
            assertEquals(Long.MIN_VALUE, SUT.parse(String.valueOf(Long.MIN_VALUE), ""));
            assertThrowsExactly(ParsingException.class, () -> SUT.parse("text", ""));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void supports() {
        assertTrue(SUT.supports(Long.class));
    }

    @Test
    void testToString() {
        assertEquals("100000", SUT.toString(100000L));
        assertEquals("-100000", SUT.toString(-100000L));
    }

    @Test
    void getRandomDefault() {
        try {
            long val = SUT.parse(SUT.getRandomDefault(), "");
            assertTrue(val < Long.MAX_VALUE && val > Long.MIN_VALUE);
        } catch (Exception e) {
            fail(e);
        }
    }
}