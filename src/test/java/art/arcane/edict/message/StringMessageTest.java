package art.arcane.edict.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringMessageTest {

    private final StringMessage SUT = new StringMessage("Test");

    @Test
    public void constructorTest() {
        assertEquals("Test", SUT.string());
    }

    @Test
    public void toStringTest() {
        assertEquals("Test", String.valueOf(SUT));
    }
}