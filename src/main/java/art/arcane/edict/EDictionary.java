package art.arcane.edict;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Settings class.
 * TODO: Turn this into a commandable object
 */
public class EDictionary {

    /**
     * Default config file location.
     */
    private static final File defaultConfigFile = new File("edict/config.json");

    /**
     * Threshold required to match an input string successfully with a command.
     */
    public double matchThreshold = 0.6;








    /// Static settings to make this baby work :)

    /**
     * GSON.
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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
     * @param edict the initial settings
     * @param file the configuration file
     */
    public static void setup(EDictionary edict, File file) {
        settings = edict;
        configFile = file;
    }

    /**
     * Update and load the config file. Corrects for differences between file and class in both directions.
     * @return the settings
     */
    public static @NotNull EDictionary get() {

        // Setup failed
        if (settings == null) {
            setup(new EDictionary(), defaultConfigFile);
        }

        try {

            // Missing config
            if (!configFile.exists()) {
                saveToFile();
                fileLastModified = configFile.lastModified();
                currentHash = settings.hashCode();
            }

            // File changed
            if (configFile.lastModified() > EDictionary.fileLastModified) {
                settings = loadFromFile();
                fileLastModified = configFile.lastModified();
                currentHash = settings.hashCode();
            }

            // Settings changed
            if (currentHash != settings.hashCode()) {
                saveToFile();
                fileLastModified = configFile.lastModified();
                currentHash = settings.hashCode();
            }

        } catch (IOException e) {
            System.out.println("IOException during settings loading: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }

        return settings;
    }

    /**
     * Save config to file. Can be non-existent, will be created if so.
     * @throws IOException see {@link FileWriter#FileWriter(File)}
     */
    private static void saveToFile() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));
        bw.write(GSON.toJson(settings));
        bw.flush();
        bw.close();
    }

    /**
     * Load config from file.
     * @return the settings
     * @throws FileNotFoundException if the file does not exist
     */
    private static EDictionary loadFromFile() throws FileNotFoundException {
        return GSON.fromJson(new BufferedReader(new FileReader(configFile)), EDictionary.class);
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
