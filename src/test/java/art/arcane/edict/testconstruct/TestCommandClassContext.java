package art.arcane.edict.testconstruct;

import art.arcane.edict.api.Command;
import art.arcane.edict.api.Edicted;
import art.arcane.edict.api.Param;
import art.arcane.edict.message.StringMessage;

@Command(name = "context", description = "nothing")
public class TestCommandClassContext implements Edicted {
    @Command(name = "test", description = "test")
    public void testContextCommand(
            @Param(
                    description = "value",
                    contextual = true
            ) TestContextValue contextValue
    ) {
        user().send(new StringMessage(contextValue.getValue()));
    }
}
