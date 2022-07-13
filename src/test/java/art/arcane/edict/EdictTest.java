package art.arcane.edict;

import art.arcane.edict.context.UserContext;
import art.arcane.edict.testconstruct.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EdictTest {

    /*
        TODO: Complex command tests
        TODO: Ambiguous command tests (close calls)
        TODO: Complex context
     */

    final Edict SUT = Edict.builder(new TestCommandClass(), new TestCommandClassContext())
            .contextHandler(new TestContextValueContextHandler())
            .parameterHandler(new TestContextValueParameterHandler())
            .build();
    final TestUser TESTUSER = new TestContextUser();

    @Test
    void testMethod() {
        new UserContext().post(TESTUSER);
        new TestCommandClass().command();
        assertEquals("command ran", TESTUSER.received.get(0).string());
        TESTUSER.received.clear();
    }

    @Test
    void command() {
        SUT.command("test command", TESTUSER, true);
        assertTrue(TESTUSER.received.size() > 0);
        assertEquals("command ran", TESTUSER.received.get(TESTUSER.received.size() - 1).string());
    }

    @Test
    void context() {
        SUT.command("context test", TESTUSER, true);
        assertTrue(TESTUSER.received.size() > 0);
        assertEquals(TestContextValue.value, TESTUSER.received.get(TESTUSER.received.size() - 1).string());
    }
}