package art.arcane.edict.context;

import art.arcane.edict.user.User;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Context for {@link User}s.
 */
public class UserContext implements Context<User> {

    /**
     * Context map.
     */
    private static final ConcurrentHashMap<Thread, User> context = new ConcurrentHashMap<>();

    @Override
    public ConcurrentHashMap<Thread, User> context() {
        return context;
    }
}
