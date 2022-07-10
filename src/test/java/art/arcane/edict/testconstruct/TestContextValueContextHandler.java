package art.arcane.edict.testconstruct;

import art.arcane.edict.exception.ContextMissingException;
import art.arcane.edict.handler.ContextHandler;
import art.arcane.edict.user.User;

public class TestContextValueContextHandler implements ContextHandler<TestContextValue> {
    /**
     * The type this context handler handles
     *
     * @param type
     * @return the type
     */
    @Override
    public boolean supports(Class<?> type) {
        return TestContextValue.class.equals(type);
    }

    /**
     * The handler for this context. Can use any data found in the user object for context derivation.
     *
     * @param user The user whose data may be used
     * @return The value in the assigned type
     * @throws ContextMissingException if the {@code user} specified does not have the values needed for this context
     */
    @Override
    public TestContextValue handle(User user) throws ContextMissingException {
        if (!(user instanceof TestContextUser tUser)) {
            throw new ContextMissingException();
        }
        return tUser.getValue();
    }
}
