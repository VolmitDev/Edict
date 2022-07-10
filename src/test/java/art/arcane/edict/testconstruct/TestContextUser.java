package art.arcane.edict.testconstruct;

public class TestContextUser extends TestUser {

    private final TestContextValue value = new TestContextValue();

    /**
     * Whether this user can use context when using commands.
     * Context is environment derived data that can be used by the system to autofill contextual (optional) parameters.
     * An example is a game you made, where there are multiple worlds; then the context can autofill the current world of the player.
     *
     * @return whether the user can use context
     */
    @Override
    public boolean canUseContext() {
        return true;
    }

    public TestContextValue getValue() {
        return value;
    }
}
