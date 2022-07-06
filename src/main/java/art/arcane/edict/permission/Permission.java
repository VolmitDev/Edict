package art.arcane.edict.permission;

/**
 * A permission node.
 */
public interface Permission {

    /**
     * Some permission node.
     * @return the permission node
     */
    String get();

    /**
     * Parent permission node.
     * @return the parent permission node
     */
    Permission getParent();

}
