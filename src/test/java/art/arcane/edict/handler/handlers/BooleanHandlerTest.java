package art.arcane.edict.handler.handlers;

import art.arcane.edict.handler.ParameterHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BooleanHandlerTest {

    private final ParameterHandler<Boolean> SUT = new BooleanHandler();

    @Test
    void getPossibilities() {
        assertTrue(SUT.getPossibilities().contains(true));
        assertTrue(SUT.getPossibilities().contains(false));
    }

    @Test
    void testToString() {
        assertEquals("true", SUT.toString(true));
    }

    @Test
    void parse() {
        try {
            assertEquals(true, SUT.parse("true", ""));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void supports() {
        assertTrue(SUT.supports(Boolean.class));
    }
}