package art.arcane.edict.exception;


import art.arcane.edict.handler.ContextHandler;
import art.arcane.edict.user.User;

/**
 * Exception thrown when a {@link ContextHandler} requires a {@link User} implementation but a different one is given.
 */
public class ContextMissingException extends Exception {
}
