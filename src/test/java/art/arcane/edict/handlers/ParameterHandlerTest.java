package art.arcane.edict.handlers;

import art.arcane.edict.handlers.handlers.IntegerHandler;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ParameterHandlerTest {

    ParameterHandler<?> SUT = new IntegerHandler();

    @Test
    public void getMultiplier() {
        assertEquals(1000, SUT.getMultiplier(new AtomicReference<>("1k")));
        assertEquals(1_000_000, SUT.getMultiplier(new AtomicReference<>("1m")));
        assertEquals(1_000_000, SUT.getMultiplier(new AtomicReference<>("1kk")));
        assertEquals(1_000_000_000, SUT.getMultiplier(new AtomicReference<>("1mk")));
        assertEquals(512, SUT.getMultiplier(new AtomicReference<>("1r")));
        assertEquals(512000, SUT.getMultiplier(new AtomicReference<>("1kr")));
        assertEquals(512000, SUT.getMultiplier(new AtomicReference<>("1rk")));
        assertEquals(100, SUT.getMultiplier(new AtomicReference<>("1h")));
        assertEquals(1600, SUT.getMultiplier(new AtomicReference<>("1ch")));
        assertEquals(1, SUT.getMultiplier(new AtomicReference<>("1")));
        assertNotEquals(100, SUT.getMultiplier(new AtomicReference<>("100")));
    }
}
