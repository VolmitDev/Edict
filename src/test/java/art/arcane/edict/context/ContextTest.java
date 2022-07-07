package art.arcane.edict.context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class ContextTest {

    final Context<Integer> SUT = new Context<>() {

        private static final ConcurrentHashMap<@NotNull Thread, @Nullable Integer> context = new ConcurrentHashMap<>();

        @Override
        public ConcurrentHashMap<@NotNull Thread, @Nullable Integer> context() {
            return context;
        }
    };

    @Test
    void post() {
        SUT.post(1);
    }

    @Test
    void clean() throws InterruptedException {
        Thread r = new Thread(() -> SUT.post(5));
        r.start();
        while (r.isAlive()) {Thread.sleep(1);}
        assertEquals(5, SUT.context().get(r));
        SUT.clean();
        assertNull(SUT.context().get(r));
    }

    @Test
    void getAndPost() throws InterruptedException {
        SUT.post(5);
        Thread r = new Thread(() -> SUT.post(6));
        r.start();
        while (r.isAlive()) {Thread.sleep(1);}
        assertEquals(5, SUT.get());
        assertEquals(6, SUT.context().get(r));
        SUT.post(1);
        assertEquals(1, SUT.get());
    }

    @Test
    void delete() {
        SUT.post(5);
        SUT.delete();
        assertNull(SUT.get());
    }
}