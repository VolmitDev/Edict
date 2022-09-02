package art.arcane.edict;

import art.arcane.edict.context.UserContext;
import art.arcane.edict.testconstruct.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EdictTest {

    /*
        TODO: Complex command tests
        TODO: Ambiguous command tests (close calls)
     */

    public static final Edict SUT = Edict.builder(new TestCommandClass(), new TestCommandClassContext(), new TestCommandCategory())
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

    @Test
    void rootCommandTest() {
        TESTUSER.received.clear();
        SUT.command("rootcommand", TESTUSER, true);
        assertTrue(TESTUSER.received.size() > 0);
        assertEquals("ran root command", TESTUSER.received.get(TESTUSER.received.size() - 1).string());
    }

    @Test
    void testSuggestionsSimple() {
        suggestionCheck("te", "test");
    }

    @Test
    void test() {
        // TODO: Why are context and rootCommand not appearing in the suggestions?
        List<String> out = new ArrayList<>();
        SUT.suggest("", TESTUSER, out::addAll, true);
        System.out.println(out);
    }

    @Test
    void testSuggestionComplex() {
        // TODO: Fix this because the first one doesn't match anything because of the matching threshold requirement
        suggestionCheck("rootc", "rootcommand");
        suggestionCheck("con", "context");
    }

    private void suggestionCheck(String input, @NotNull String expected) {
        List<String> suggestions = new ArrayList<>();
        SUT.suggest(input, TESTUSER, suggestions::addAll, true);
        if (suggestions.isEmpty()) {
            fail("'" + input + "' did not lead to any suggestions");
        }
        if (!expected.equalsIgnoreCase(suggestions.get(0))) {
            fail("'" + input + "' did not lead to '" + expected + "' but did find " + suggestions);
        }
    }

    @Test
    void testSuggestionDeep() {
        // TODO: Fix this because the space at the end isn't registered
        suggestionCheck("method", "test subc ");
    }
}