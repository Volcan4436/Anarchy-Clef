package adris.altoclef.altomenu.config;

import adris.altoclef.AltoClef;
import com.google.gson.JsonObject;
import bleed.utils.Syracuse.JsonUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class configloader {

    private static final List<Config> configs = new ArrayList<>();

    public static configloader INSTANCE = new configloader();
    public static Config lastAddedConfig;

    public static void loadConfigs() throws IOException {
            configs.clear();
            @SuppressWarnings("all")
            File ROOT_DIR = new File(FabricLoader.getInstance().getGameDirectory(), AltoClef.getName());
            if (!ROOT_DIR.exists()) ROOT_DIR.mkdir();

            File configFolder = new File(ROOT_DIR, "Configs");
            if (!configFolder.exists()) configFolder.mkdir();

            if (configFolder.listFiles().length <= 0) {
                Config defaultConfig = new Config("default", "Default configuration");
                defaultConfig.save();
                configs.add(defaultConfig);
                AltoClef.selectedConfig = defaultConfig;
                return;
            }

            for (File file : configFolder.listFiles()) {
                load(file);
            }

        AltoClef.selectedConfig = getDefaultConfig();
    }

    public static void addConfig(Config config) {
            configs.add(config);
            lastAddedConfig = config;
    }

    public static void saveNewConfig(String configName) throws IOException {
        // Create a new Config object with the current settings
        Config newConfig = new Config(configName, "New Configuration");

        // Save the new config to a .json file
        File ROOT_DIR = new File(FabricLoader.getInstance().getGameDirectory(), AltoClef.getName());
        if (!ROOT_DIR.exists()) ROOT_DIR.mkdir();

        File configFolder = new File(ROOT_DIR, "Configs");
        if (!configFolder.exists()) configFolder.mkdir();

        File configFile = new File(configFolder, configName + ".json");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception appropriately
            }
        }

        try {
            newConfig.save(); // Save the new config object to the new file
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }

        // Add the new config to the list of configs
        configs.add(newConfig);
        lastAddedConfig = newConfig;
    }

    public static void load(File file) throws IOException {

            BufferedReader load = new BufferedReader(new FileReader(file));
            JsonObject json = (JsonObject) JsonUtils.jsonParser.parse(load);
            load.close();

            configs.add(new Config(file.getName().replace(".json", ""), json.get("description").getAsString(), file));
    }

    public static void loadConfig(Config config) throws IOException {

            config.load();
    }

    public static Config getDefaultConfig() {

            for (Config config : configs) {
                if (config.getName().equalsIgnoreCase("default")) return config;
            }

            Config defaultConfig = new Config("default", "Default configuration");
            try {
                defaultConfig.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
            configs.add(defaultConfig);
            return defaultConfig;
    }

    public Config getConfigByName(String name) {
        for (Config config : configs) {
            if (config.getName().equals(name)) {
                return config;
            }
        }
        return null;
    }

    public static List<Config> getConfigs() {
            return configs;
    }
}
