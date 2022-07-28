package art.arcane.edict.context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

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
    void clean() {
        Thread rr = Thread.currentThread();
        AtomicReference<AssertionFailedError> e = new AtomicReference<>(null);
        Thread t = new Thread(() -> {
            try {
                SUT.post(5);
                assertEquals(5, SUT.context().get(Thread.currentThread()));
                SUT.clean();
                assertNotNull(SUT.context().get(rr));
                assertNotNull(SUT.context().get(Thread.currentThread()));
            } catch (AssertionFailedError ex) {
                e.set(ex);
                throw ex;
            }
        });
        t.start();
        if (e.get() != null) {
            throw e.get();
        }
        t.interrupt();
        SUT.clean();
        assertNull(SUT.context().get(t));
        assertNotNull(SUT.context().get(Thread.currentThread()));
    }

    @Test
    void getAndPost() throws InterruptedException {
        ReentrantLock l = new ReentrantLock();
        SUT.post(5);
        Thread r = new Thread(() -> {
            l.lock();
            SUT.post(6);
            l.unlock();
        });
        r.start();
        Thread.sleep(2);
        l.lock();
        l.unlock();
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