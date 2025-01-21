package fr.utt.simpleItemStorage;

import java.io.File;

public class ConfigManipulator {
    private static ConfigManipulator instance = null;
    private ConfigManipulator() {
        instance = this;

        new File(SimpleItemStorage.getInstance().getDataFolder().getPath()).mkdirs();
        SimpleItemStorage plugin = SimpleItemStorage.getInstance();
        plugin.saveDefaultConfig();
    }

    public static ConfigManipulator getInstance() {
        if (instance == null) {
            new ConfigManipulator();
        }
        return instance;
    }

    public void reload() {
    }
}