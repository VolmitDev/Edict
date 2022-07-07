package art.arcane.edict.testconstruct;

import art.arcane.edict.command.Command;

@Command(description = "a subcategory", name = "subcategory")
public class TestCommandClassToo {

    @Command(description = "a command named 'method'")
    public void method() {}

}
