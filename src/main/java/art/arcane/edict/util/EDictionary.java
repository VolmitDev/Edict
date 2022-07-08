package art.arcane.edict.util;

import art.arcane.edict.api.Edicted;
import art.arcane.edict.api.Command;
import art.arcane.edict.api.Param;
import art.arcane.edict.message.StringMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Settings class.
 */
@Command(name = "edictSettings", description = "Settings for the Edict command system this is using. Changes are hot-loaded", permission = "edict")
public class EDictionary implements Edicted {

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







    /// Util method

    /**
     * Send an update to the user and system.
     * @param setting the name of the setting
     * @param oldValue the old value of the setting
     * @param newValue the new value of the setting
     * @param runUpdate to update the setting (with locking)
     */
    private void update(@NotNull String setting, @NotNull Object oldValue, @NotNull Object newValue, @NotNull Runnable runUpdate) {
        if (!LOCKS.containsKey(setting)) {
            LOCKS_LOCK.lock();
            if (!LOCKS.containsKey(setting)) {
                LOCKS.put(setting, new ReentrantLock());
            }
            LOCKS_LOCK.unlock();
        }
        if (LOCKS.get(setting).isLocked()) {
            user().send(new StringMessage("You tried setting " + setting + " from " + oldValue + " to " + newValue + " but another user is updating it at the same time! Try again."));
        } else {
            LOCKS.get(setting).lock();
            user().send(new StringMessage("Set " + setting + " from " + oldValue + " to " + newValue));
            system().i(new StringMessage(user().name() + " set " + setting + " from " + oldValue + " to " + newValue));
            if (settings == null) {
                SETTINGS_LOCK.lock();
                if (settings == null) {
                    settings = new EDictionary();
                }
                SETTINGS_LOCK.unlock();
            }
            runUpdate.run();
            LOCKS.get(setting).unlock();
        }
    }

    /// Static settings to make this baby work :)

    /**
     * Setting update lock.
     */
    private static final ConcurrentHashMap<String, ReentrantLock> LOCKS = new ConcurrentHashMap<>();

    /**
     * LOCKS lock.
     */
    private static final ReentrantLock LOCKS_LOCK = new ReentrantLock();

    /**
     * Settings lock.
     */
    private static final ReentrantLock SETTINGS_LOCK = new ReentrantLock();

    /**
     * Singleton.
     */
    private static EDictionary settings;

    /**
     * Setup.
     * @param settings settings that should be used. If {@code null} uses default settings.
     */
    public static void set(@Nullable EDictionary settings) {
        SETTINGS_LOCK.lock();
        EDictionary.settings = settings == null ? new EDictionary() : settings;
        SETTINGS_LOCK.unlock();
    }

    /**
     * Corrects for differences because of multithreaded between file and class in both directions
     * @return the settings. If {@link #set(EDictionary)} was not called yet, it returns a fresh set of settings.
     */
    public static @NotNull EDictionary get() {

        // Default settings until setup
        if (settings == null) {
            return new EDictionary();
        }

        return settings;
    }
}
