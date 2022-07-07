package art.arcane.edict.permission;

/**
 * A permission node.
 */
public interface Permission {

    /**
     * Parent permission node.
     * @return the parent permission node
     */
    Permission getParent();

    /**
     * To string
     * @return String representation of the permission node
     */
    String toString();
}
