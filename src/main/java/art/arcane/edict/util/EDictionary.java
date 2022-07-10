package art.arcane.edict.util;

import art.arcane.edict.api.Edicted;
import art.arcane.edict.api.Command;
import art.arcane.edict.api.Param;
import art.arcane.edict.message.StringMessage;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Settings class.
 */
@Command(name = "edictSettings", description = "Settings for the Edict command system this is using. Changes are hot-loaded", permission = "edict")
public class EDictionary implements Edicted {

    /**
     * Construct a new settings instance
     */
    public EDictionary() {

    }

    /**
     * Threshold required to match an input string successfully with a command.
     */
    public double matchThreshold = 0.6;

    @Command(description = "Set the matching threshold")
    public void setMatchThreshold(@Param(description = "The threshold") Double matchThreshold) {
        update("setMatchThreshold", this.matchThreshold, matchThreshold, () -> this.matchThreshold = matchThreshold);
    }

    /**
     * Whether these settings can be changed as commands.
     */
    public boolean settingsAsCommands = false;

    @Command(description = "Set whether settings can be used as commands. Does not hot-load.")
    public void setSettingsAsCommands(Boolean settingsAsCommands) {
        update("settingsAsCommands", this.settingsAsCommands, settingsAsCommands, () -> this.settingsAsCommands = settingsAsCommands);
    }

    /**
     * The number of attempts a user gets to pick the correct input for an ambiguous parameter input.
     */
    public int optionPickAttempts = 3;

    @Command(description = "Set the number of attempts a user gets to pick the correct input for an ambiguous parameter input.")
    public void setOptionPickAttempts(int optionPickAttempts) {
        update("optionPickAttempts", this.optionPickAttempts, optionPickAttempts, () -> this.optionPickAttempts = optionPickAttempts);
    }

    /**
     * Whether to always pick the first option or not, if multiple are possible when running a command.
     */
    public boolean alwaysPickFirstOption;

    @Command(description = "Set whether to always pick the first option or not, if multiple are possible when running a command.")
    public void setAlwaysPickFirstOption(boolean alwaysPickFirstOption) {
        update("alwaysPickFirstOption", this.alwaysPickFirstOption, alwaysPickFirstOption, () -> this.alwaysPickFirstOption = alwaysPickFirstOption);
    }

    /**
     * Send an update to the user and system.
     * @param setting the name of the setting
     * @param oldValue the old value of the setting
     * @param newValue the new value of the setting
     * @param runUpdate to update the setting (with locking)
     */
    private void update(@NotNull String setting, @NotNull Object oldValue, @NotNull Object newValue, @NotNull Runnable runUpdate) {
        lock.lock();
        user().send(new StringMessage("Set " + setting + " from " + oldValue + " to " + newValue));
        system().i(new StringMessage(user().name() + " set " + setting + " from " + oldValue + " to " + newValue));
        runUpdate.run();
        lock.unlock();
    }

    /**
     * Lock for changing settings.
     */
    private final ReentrantLock lock = new ReentrantLock();
}
