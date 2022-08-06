package art.arcane.edict.util;

import art.arcane.edict.api.Edicted;
import art.arcane.edict.api.Command;
import art.arcane.edict.api.Param;
import art.arcane.edict.message.StringMessage;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Settings class.
 */
@Command(name = "edictSettings", description = "Settings for the Edict command system this is using. Changes are hot-loaded", permission = "edict")
public class EDictionary implements Edicted {

    public EDictionary() {}

    /**
     * Threshold required to match an input string successfully with a command.
     */
    public double matchThreshold = 0.6;

    /**
     * The number of attempts a user gets to pick the correct input for an ambiguous parameter input.
     */
    public int optionPickAttempts = 3;

    /**
     * Whether to always pick the first option or not, if multiple are possible when running a command.
     */
    public boolean alwaysPickFirstOption = false;

    /**
     * Timeout for multiple-option picking (multiplied by {@link #optionPickAttempts} if all tries timeout).
     */
    public int optionPickTimeout;
}
