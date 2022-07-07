package art.arcane.edict;

import art.arcane.edict.user.User;

import java.util.concurrent.ConcurrentHashMap;

public interface CommandInterface {
    ConcurrentHashMap<Thread, Edict> systemContext = new ConcurrentHashMap<>();
    ConcurrentHashMap<Thread, User> userContext = new ConcurrentHashMap<>();
}
