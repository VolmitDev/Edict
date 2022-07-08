package art.arcane.edict;

import art.arcane.edict.context.UserContext;
import art.arcane.edict.testconstruct.TestCommandClass;
import art.arcane.edict.testconstruct.TestUser;
import art.arcane.edict.util.EDictionary;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EdictTest {

    // TODO: Complex command tests

    final Edict SUT = new Edict(new TestCommandClass());
    final TestUser TESTUSER = new TestUser();

    @BeforeAll
    static void setup() {
        EDictionary.set(null);
    }

    @Test
    void testMethod() {
        new UserContext().post(TESTUSER);
        new TestCommandClass().command();
        assertEquals("command ran", TESTUSER.received.get(0).message());
        TESTUSER.received.clear();
    }

    @Test
    void command() {
        try {
            SUT.command("test command", TESTUSER, true);
            assertTrue(TESTUSER.received.size() > 0);
            assertEquals("command ran", TESTUSER.received.get(TESTUSER.received.size() - 1).message());
        } catch (Exception e) {
            fail(e);
        }
    }
}