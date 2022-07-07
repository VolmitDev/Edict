package art.arcane.edict.virtual;

import art.arcane.edict.Edict;
import art.arcane.edict.message.Message;
import art.arcane.edict.message.StringMessage;
import art.arcane.edict.testconstruct.TestCommandClass;
import art.arcane.edict.testconstruct.TestUser;
import art.arcane.edict.user.User;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class VCommandsTest {

    VCommands SUT = VCommands.fromClass(TestCommandClass.class, null, new Edict());

    @Test
    void fromClass() {
        assertNotNull(SUT);
        assertEquals("test", SUT.name());
        List<String> childrenNames = SUT.children().stream().map(VCommandable::name).toList();
        assertTrue(childrenNames.contains("command"));
        assertTrue(childrenNames.contains("subcategory"));
        assertFalse(childrenNames.contains("empty"));
        assertFalse(childrenNames.contains("TestNotCommandClass"));
    }

    @Test
    void sortAndFilterChildren() {
        List<String> commandableBracket = VCommands.sortAndFilterChildren(SUT.children(), "[", new TestUser()).stream().map(VCommandable::name).collect(Collectors.toList());
        List<String> commandableMatch = VCommands.sortAndFilterChildren(SUT.children(), "command", new TestUser()).stream().map(VCommandable::name).collect(Collectors.toList());
        assertTrue(commandableBracket.isEmpty());
        assertEquals("command", commandableMatch.get(0));
    }

    @Test
    void name() {
        assertEquals("test", SUT.name());
    }

    @Test
    void aliases() {
        assertTrue(List.of(SUT.aliases()).contains("alias"));
    }

    @Test
    void run() {
        AtomicBoolean success = new AtomicBoolean(false);
        SUT.run(Collections.singletonList("command"), new User() {
            @Override
            public boolean canUseContext() {
                return false;
            }

            @Override
            public void send(Message message) {
                if (message.toString().equals("command ran")) {
                    success.set(true);
                }
            }
        });
        assertTrue(success.get());
    }
}