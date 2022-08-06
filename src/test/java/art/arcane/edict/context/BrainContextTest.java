package art.arcane.edict.context;

import art.arcane.edict.testconstruct.Brain;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class BrainContextTest {
    Context<Brain> SUT = new Context<Brain>() {

        private static final ConcurrentHashMap<@NotNull Thread, @Nullable Brain> map = new ConcurrentHashMap<>();

        @Override
        public ConcurrentHashMap<@NotNull Thread, @Nullable Brain> context() {
            return map;
        }
    };

    @Test
    public void testBrain() {
        Brain brain = new Brain(50, "male");
        Brain brain2 = new Brain(30, "female");
        SUT.post(brain);
        Thread thread = new Thread(() -> {
            SUT.post(brain2);
            assertEquals(brain2, SUT.get());
            SUT.clean();
        });
        thread.start();
        assertEquals(SUT.get(), SUT.get());
        SUT.clean();
        assertNull(SUT.context().get(thread));
        assertNotNull(SUT.get());
        SUT.delete();
        assertNull(SUT.get());
    }
}
