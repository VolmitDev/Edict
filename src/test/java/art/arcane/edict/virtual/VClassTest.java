package art.arcane.edict.virtual;

import art.arcane.edict.Edict;
import art.arcane.edict.testconstruct.TestCommandClass;
import art.arcane.edict.testconstruct.TestUser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VClassTest {

    final Edict SYSTEM = Edict.builder(null).build();
    final VClass SUT = VClass.fromInstance(new TestCommandClass(), null, SYSTEM);

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
        assert SUT != null;
        List<String> commandableBracket = SUT.indexer().search("[", SYSTEM.getSettings().matchThreshold, (vCommandable -> new TestUser().hasPermission(vCommandable.permission()))).stream().map(VCommandable::name).toList();
        List<String> commandableMatch = SUT.indexer().search("command", SYSTEM.getSettings().matchThreshold, (vCommandable -> new TestUser().hasPermission(vCommandable.permission()))).stream().map(VCommandable::name).toList();
        assertTrue(commandableBracket.isEmpty());
        assertEquals("command", commandableMatch.get(0));
        assertEquals(1, commandableMatch.size());
    }

    @Test
    void name() {
        assert SUT != null;
        assertEquals("test", SUT.name());
    }

    @Test
    void aliases() {
        assert SUT != null;
        assertTrue(List.of(SUT.aliases()).contains("alias"));
    }
}