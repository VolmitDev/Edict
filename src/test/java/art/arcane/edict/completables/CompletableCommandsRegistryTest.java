package art.arcane.edict.completables;

import art.arcane.edict.testconstruct.TestUser;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class CompletableCommandsRegistryTest {

    CompletableCommandsRegistry SUT = new CompletableCommandsRegistry();

    @Test
    public void testSetCompleteGet() throws ExecutionException, InterruptedException, TimeoutException {
        TestUser user = new TestUser();
        CompletableFuture<String> command = new CompletableFuture<>();
        SUT.register(user, command);
        SUT.getCompletableFor(user).complete("test");
        assertEquals("test", command.get(1, TimeUnit.SECONDS));
    }

}