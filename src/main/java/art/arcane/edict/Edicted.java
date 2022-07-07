package art.arcane.edict;

import art.arcane.edict.context.SystemContext;
import art.arcane.edict.context.UserContext;
import art.arcane.edict.user.User;

/**
 * Interface for classes in the Edict system. Classes implementing this interface must still implement {@link art.arcane.edict.command.Command}.
 */
public interface Edicted {

    /**
     * Get the user that sent the command.
     * @return the user
     */
    default User user() {
        return new UserContext().get();
    }

    /**
     * Get the system in which the command was run.
     * @return the system
     */
    default Edict system() {
        return new SystemContext().get();
    }

}
