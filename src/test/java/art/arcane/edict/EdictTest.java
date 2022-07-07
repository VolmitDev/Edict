package art.arcane.edict;

import art.arcane.edict.command.Command;
import art.arcane.edict.testconstruct.CommandClass;
import art.arcane.edict.testconstruct.TestUser;
import art.arcane.edict.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EdictTest {

    Edict SUT = new Edict(CommandClass.class);
    TestUser TESTUSER = new TestUser();

    @Test
    void testMethod() {
        new CommandClass().command(TESTUSER);
        assertEquals("command ran", TESTUSER.received.get(0).message());
        TESTUSER.received.clear();
    }

    @Test
    void command() {
        try {
            SUT.command(CommandClass.class.getDeclaredMethod("command", User.class).getDeclaredAnnotation(Command.class).name(), TESTUSER);
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