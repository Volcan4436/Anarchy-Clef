package adris.altoclef.altomenu.config;

import adris.altoclef.AltoClef;
import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.managers.ModuleManager;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.altomenu.settings.Setting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import bleed.utils.Syracuse.JsonUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.Map;

public class Config {

    private final File configFile;
    private final File ROOT_DIR;
    private final File configFolder;
    private String name, description;

    /**
     * Used for loading Configs
     *
     * @param name        The Config's name
     * @param description A Description of the config
     * @param file        File to load the config from
     */
    public Config(String name, String description, File file) {
        this.name = name;
        this.description = description;
        this.configFile = file;

        // Main Folder
        ROOT_DIR = new File(FabricLoader.getInstance().getGameDirectory(), AltoClef.getName());
        if (!ROOT_DIR.exists()) ROOT_DIR.mkdir();

        // Configs folder
        configFolder = new File(ROOT_DIR, "Configs");
        if (!configFolder.exists()) configFolder.mkdir();
    }

    /**
     * Used for creating a Config
     *
     * @param name        The Config's name
     * @param description A Description of the config
     */
    public Config(String name, String description) {
        this.name = name;
        this.description = description;

        // Main Folder
        ROOT_DIR = new File(FabricLoader.getInstance().getGameDirectory(), AltoClef.getName());
        if (!ROOT_DIR.exists()) ROOT_DIR.mkdir();

        // Configs folder
        configFolder = new File(ROOT_DIR, "Configs");
        if (!configFolder.exists()) configFolder.mkdir();

        // Create new config file
        configFile = new File(configFolder, name + ".json");
    }


    public void save() throws IOException {
            JsonObject json = new JsonObject();
            json.addProperty("description", description);

            for (Mod module : ModuleManager.INSTANCE.getModules()) {
                JsonObject jsonMod = new JsonObject();

                jsonMod.addProperty("enabled", module.isEnabled());
                jsonMod.addProperty("key", module.getKey());

                json.add(module.getName(), jsonMod);
                for (Setting setting : module.getSettings()) {
                    if (setting instanceof ModeSetting s) {
                        jsonMod.addProperty(s.getName(), s.getMode());
                    }

                    if (setting instanceof BooleanSetting s) {
                        jsonMod.addProperty(s.getName(), s.isEnabled());
                    }

                    if (setting instanceof NumberSetting s) {
                        jsonMod.addProperty(s.getName(), s.getValue());
                    }
                }
            PrintWriter writer = new PrintWriter(configFile);
            writer.println(JsonUtils.prettyGson.toJson(json));
            writer.close();
        }
    }

    public void load() throws IOException {
        BufferedReader load = new BufferedReader(new FileReader(configFile));
        JsonObject json = (JsonObject) JsonUtils.jsonParser.parse(load);
        load.close();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            Mod module = ModuleManager.INSTANCE.getModuleByName(entry.getKey());

            if (module == null) continue;

            JsonObject jsonModule = (JsonObject) entry.getValue();
            module.setEnabled(jsonModule.get("enabled").getAsBoolean());

            int key = jsonModule.get("key").getAsInt();
            module.setKey(key);

            for (Setting setting : module.getSettings()) {
                if (setting instanceof ModeSetting s) {
                    String mode = jsonModule.get(s.getName()).getAsString();
                    s.setMode(mode);
                }

                if (setting instanceof BooleanSetting s) {
                    boolean bool = jsonModule.get(s.getName()).getAsBoolean();
                    s.setEnabled(bool);
                }

                if (setting instanceof NumberSetting s) {
                    double value = jsonModule.get(s.getName()).getAsDouble();
                    s.setValue(value);
                }
            }
        }
    }

    public void delete() {
        configloader.getConfigs().remove(this);
        configFile.delete();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (configFile != null) {
            File copy = new File(configFile.getAbsolutePath(), name);
            configFile.renameTo(copy);
        }
    }
}

