package art.arcane.edict;

import art.arcane.edict.testconstruct.TestCommandClass;
import art.arcane.edict.testconstruct.TestUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EdictTest {

    Edict SUT = new Edict(TestCommandClass.class);
    TestUser TESTUSER = new TestUser();

    @Test
    void testMethod() {
        new TestCommandClass().command(TESTUSER);
        assertEquals("command ran", TESTUSER.received.get(0).message());
        TESTUSER.received.clear();
    }

    @Test
    void command() {
        try {
            SUT.command("test command", TESTUSER);
            assertEquals("command ran", TESTUSER.received.get(0).message());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void i() {
    }

    @Test
    void w() {
    }

    @Test
    void d() {
    }
}