package art.arcane.edict.util;

import art.arcane.edict.api.Edicted;
import art.arcane.edict.api.Command;
import art.arcane.edict.api.Param;
import art.arcane.edict.message.StringMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Settings class.
 */
@Command(name = "edictSettings", description = "Settings for the Edict command system this is using. Changes are hot-loaded", permission = "edict")
public class EDictionary implements Edicted {

    /**
     * Default config file location.
     */
    public static final File defaultConfigFile = new File(Path.of("").toAbsolutePath() + "/edict/config.json");

    /**
     * Threshold required to match an input string successfully with a command.
     */
    public double matchThreshold = 0.6;

    @Command(description = "Set the matching threshold")
    public void setMatchThreshold(@Param(description = "The threshold") Double matchThreshold) {
        update("setMatchThreshold", this.matchThreshold, matchThreshold);
        this.matchThreshold = matchThreshold;
    }

    /**
     * Whether these settings can be changed as commands.
     */
    public boolean settingsAsCommands = false;

    @Command(description = "Set whether settings can be used as commands. Does not hot-load.")
    public void setSettingsAsCommands(Boolean settingsAsCommands) {
        update("settingsAsCommands", this.settingsAsCommands, settingsAsCommands);
        this.settingsAsCommands = settingsAsCommands;
    }







    /// Util method

    /**
     * Send an update to the user and system.
     * @param setting the name of the setting
     * @param oldValue the old value of the setting
     * @param newValue the new value of the setting
     */
    private void update(String setting, Object oldValue, Object newValue) {
        user().send(new StringMessage("Set " + setting + " from " + oldValue + " to " + newValue));
        system().i(new StringMessage(user().name() + " set " + setting + " from " + oldValue + " to " + newValue));
    }

    /// Static settings to make this baby work :)

    /**
     * GSON.
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Lock.
     */
    private static final ReentrantLock LOCK = new ReentrantLock();

    /**
     * File.
     */
    private static File configFile;

    /**
     * Singleton.
     */
    private static EDictionary settings;

    /**
     * File last changed.
     */
    private static Long fileLastModified;

    /**
     * Initial hash code.
     */
    private static Integer currentHash;

    /**
     * Set up the settings system.
     * @param edict the initial settings. If {@code null} uses fresh settings.
     * @param file the configuration file
     * @throws IOException see {@link FileWriter#FileWriter(File)}
     */
    public static void setup(@Nullable EDictionary edict, @NotNull File file) throws IOException {
        LOCK.lock();
        settings = edict == null ? new EDictionary() : edict;
        configFile = file;
        if (!configFile.exists()) {
            if (!file.getParentFile().mkdirs() || !configFile.createNewFile()) {
                System.out.println("Config file @ " + configFile + " did not exist but failed to be created...?");
            }
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));
        bw.write(GSON.toJson(settings));
        bw.flush();
        bw.close();
        currentHash = settings.hashCode();
        fileLastModified = file.lastModified();
        LOCK.unlock();
    }

    /**
     * Update and load the config file. Corrects for differences between file and class in both directions.
     * Make sure to run {@link #setup(EDictionary, File)} first.
     * @return the settings. If {@link #setup(EDictionary, File)} was not called yet, it returns a fresh set of settings.
     */
    public static @NotNull EDictionary get() {

        LOCK.lock();

        // Setup failed
        if (settings == null) {
            return new EDictionary();
        }

        try {

            // Missing config or settings changed
            if (!configFile.exists() || currentHash != settings.hashCode()) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));
                bw.write(GSON.toJson(settings));
                bw.flush();
                bw.close();
                fileLastModified = configFile.lastModified();
                currentHash = settings.hashCode();
            }

            // File changed
            if (configFile.lastModified() > EDictionary.fileLastModified) {
                settings = GSON.fromJson(new BufferedReader(new FileReader(configFile)), EDictionary.class);
                fileLastModified = configFile.lastModified();
                currentHash = settings.hashCode();
            }

        } catch (IOException e) {
            System.out.println("IOException during settings loading: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }

        final EDictionary s = settings;

        LOCK.unlock();

        return s;
    }

    @Override
    public int hashCode() {
        int code = 0;
        for (Field field : getClass().getFields()) {
            code += field.hashCode();
        }
        return code;
    }
}
