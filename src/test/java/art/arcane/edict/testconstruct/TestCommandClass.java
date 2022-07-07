package art.arcane.edict.testconstruct;

import art.arcane.edict.Edicted;
import art.arcane.edict.command.Command;
import art.arcane.edict.command.Param;
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

    }
}
