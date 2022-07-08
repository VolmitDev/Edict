package art.arcane.edict.testconstruct;

import art.arcane.edict.api.Edicted;
import art.arcane.edict.api.Command;
import art.arcane.edict.message.StringMessage;

@Command(name = "test", description = "a test class", aliases = "alias")
public class TestCommandClass implements Edicted {

    private final TestCommandClassToo x = new TestCommandClassToo();

    @Command(name = "command", description = "a test command", aliases = "verify")
    public void command() {
        user().send(new StringMessage("command ran"));
    }

    @Command(description = "test command 2")
    public void methodName() {
        user().send(new StringMessage("Ran methodName"));
    }
}
