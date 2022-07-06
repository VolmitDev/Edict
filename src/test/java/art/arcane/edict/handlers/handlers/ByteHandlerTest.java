package art.arcane.edict.handlers.handlers;

import art.arcane.edict.exceptions.ParsingException;
import art.arcane.edict.handlers.ParameterHandler;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ByteHandlerTest {

    private final ParameterHandler<Byte> SUT = new ByteHandler();

    @Test
    void getPossibilities() {
        assertNull(SUT.getPossibilities());
        assertNull(SUT.getPossibilities("1"));
    }

    @Test
    void testToString() {
        assertEquals("127", SUT.toString(Byte.MAX_VALUE));
        assertEquals("-128", SUT.toString(Byte.MIN_VALUE));
    }

    @Test
    void parse() {
        try {
            assertEquals(Byte.MAX_VALUE, SUT.parse("127", ""));
            assertEquals(Byte.MIN_VALUE, SUT.parse("-128", ""));
            assertThrowsExactly(ParsingException.class, () -> SUT.parse("125.3", ""));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void supports() {
        assertTrue(SUT.supports(Byte.class));
    }

    @Test
    void getRandomDefault() {
        try {
            byte val = SUT.parse(SUT.getRandomDefault(), "");
            assertTrue(val < Byte.MAX_VALUE && val > Byte.MIN_VALUE);
        } catch (Exception e) {
            fail(e);
        }
    }
}