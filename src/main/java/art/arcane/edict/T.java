package art.arcane.edict;

import art.arcane.edict.permission.Permission;
import art.arcane.edict.user.SystemUser;
import art.arcane.edict.util.EDictionary;

public class T {
    static {

        new Edict().toBuilder()

        new Edict(
                new SystemUser(),
                Runnable::run,
                new EDictionary(),
                (parent, value) -> new Permission() {

                    @Override
                    public Permission getParent() {
                        return parent;
                    }

                    @Override
                    public String toString() {
                        return value;
                    }
                }
        );
    }
}
