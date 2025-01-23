package fr.utt.simpleItemStorage;

import java.io.File;

/**
 * The ConfigManipulator class is responsible for managing the configuration settings
 * of the SimpleItemStorage plugin. It ensures that the configuration file is created
 * and provides methods to access configuration values.
 */
public class ConfigManipulator {
    private static ConfigManipulator instance = null;

    /**
     * Private constructor to prevent instantiation from outside the class.
     * Initializes the configuration by creating necessary directories and saving
     * the default configuration file.
     */
    private ConfigManipulator() {
        instance = this;

        // Create the data folder if it does not exist
        new File(SimpleItemStorage.getInstance().getDataFolder().getPath()).mkdirs();
        SimpleItemStorage plugin = SimpleItemStorage.getInstance();
        plugin.saveDefaultConfig();
    }

    /**
     * Returns the singleton instance of the ConfigManipulator class.
     * If the instance does not exist, it creates a new one.
     *
     * @return the singleton instance of ConfigManipulator
     */
    public static ConfigManipulator getInstance() {
        if (instance == null) {
            new ConfigManipulator();
        }
        return instance;
    }

    /**
     * Checks if the server is in online mode.
     *
     * @return true if the server is in online mode, false otherwise
     */
    public static boolean isOnlineMode() {
        return SimpleItemStorage.getInstance().getServer().getOnlineMode();
    }

    /**
     * Reloads the configuration settings. Currently, this method does not perform any actions.
     */
    public void reload() {
    }
}