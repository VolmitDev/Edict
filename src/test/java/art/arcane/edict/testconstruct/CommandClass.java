package art.arcane.edict.testconstruct;

import art.arcane.edict.command.Command;
import art.arcane.edict.command.Param;
import art.arcane.edict.message.StringMessage;
import art.arcane.edict.user.User;

@Command(name = "test", description = "a test class", aliases = "alias")
public class CommandClass {

    @Command(name = "command", description = "a test command", aliases = "verify")
    public void command(@Param User user) {
        user.send(new StringMessage("command ran"));
    }

    @Command(description = "test coommand 2")
    public void methodName() {

    }
}
