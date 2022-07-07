package art.arcane.edict.context;

import art.arcane.edict.Edict;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Context for {@link Edict}s.
 */
public class SystemContext implements Context<Edict> {

    /**
     * Context map.
     */
    private static final ConcurrentHashMap<Thread, Edict> context = new ConcurrentHashMap<>();

    @Override
    public ConcurrentHashMap<Thread, Edict> context() {
        return context;
    }
}
