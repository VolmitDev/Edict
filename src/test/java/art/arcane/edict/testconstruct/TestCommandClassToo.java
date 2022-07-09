package art.arcane.edict.testconstruct;

import art.arcane.edict.api.Command;
import art.arcane.edict.api.Edicted;
import art.arcane.edict.message.StringMessage;

@Command(description = "a subcategory", name = "subcategory")
public class TestCommandClassToo implements Edicted {

    @Command(description = "a command named 'method'")
    public void method() {
        system().d(new StringMessage("Wow, method ran!"));
    }

}
