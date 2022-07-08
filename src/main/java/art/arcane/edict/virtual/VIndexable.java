package art.arcane.edict.virtual;

import org.jetbrains.annotations.NotNull;

/**
 * Indexable virtual construct for fast searching.
 */
public interface VIndexable {

    /**
     * Name of the commandable.
     * @return the name of the commandable
     */
    @NotNull String name();

    /**
     * List of aliases for this commandable.
     * @return list of aliases for this commandable
     */
    @NotNull String[] aliases();

}
