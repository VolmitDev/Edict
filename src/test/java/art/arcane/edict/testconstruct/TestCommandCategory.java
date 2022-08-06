package art.arcane.edict.testconstruct;

import art.arcane.edict.api.Command;
import art.arcane.edict.api.Edicted;
import art.arcane.edict.message.StringMessage;

@Command(name = "this-is-ignored", description = "nothing", singleCommandCategory = true)
public class TestCommandCategory implements Edicted {

    @Command(description = "empty")
    public void rootCommand() {
        user().send(new StringMessage("ran root command"));
    }

}
