package adris.altoclef.altomenu.config;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.managers.ModuleManager;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.altomenu.settings.Setting;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.util.Map;

public class ConfigManager {

    private static final File CONFIG_FILE = new File(
            new File(FabricLoader.getInstance().getGameDirectory(), "AltoClef"), "config.json"
    );

    public static void saveConfig() {
        try {
            JsonObject root = new JsonObject();

            for (Mod module : ModuleManager.INSTANCE.getModules()) {
                JsonObject modJson = new JsonObject();
                modJson.addProperty("enabled", module.isEnabled());
                modJson.addProperty("key", module.getKey());

                // Save each setting
                for (Setting setting : module.getSettings()) {
                    if (setting instanceof BooleanSetting s) {
                        modJson.addProperty(s.getName(), s.isEnabled());
                    } else if (setting instanceof NumberSetting s) {
                        modJson.addProperty(s.getName(), s.getValue());
                    } else if (setting instanceof ModeSetting s) {
                        modJson.addProperty(s.getName(), s.getMode());
                    }
                }

                root.add(module.getName(), modJson);
            }

            CONFIG_FILE.getParentFile().mkdirs();
            try (Writer writer = new FileWriter(CONFIG_FILE)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
            }

            System.out.println("[Config] Saved successfully.");
        } catch (Exception e) {
            System.err.println("[Config] Failed to save:");
            e.printStackTrace();
        }
    }

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            System.out.println("[Config] No existing config found. Skipping load.");
            return;
        }

        try (Reader reader = new FileReader(CONFIG_FILE)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            // ✅ Ensure MinecraftClient is initialized before enabling modules
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.player == null) {
                System.out.println("[Config] Client not ready yet. Deferring module load.");
                return;
            }

            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                Mod module = ModuleManager.INSTANCE.getModuleByName(entry.getKey());
                if (module == null) continue;

                JsonObject data = entry.getValue().getAsJsonObject();

                // Safely load base properties
                if (data.has("enabled")) {
                    boolean enabled = data.get("enabled").getAsBoolean();
                    try {
                        module.setEnabled(enabled);
                    } catch (Exception ex) {
                        System.err.println("[Config] Could not enable module: " + module.getName());
                        ex.printStackTrace();
                    }
                }

                if (data.has("key")) module.setKey(data.get("key").getAsInt());

                // Load each setting with type checking
                for (Setting setting : module.getSettings()) {
                    try {
                        if (setting instanceof BooleanSetting s && data.has(s.getName())) {
                            s.setEnabled(data.get(s.getName()).getAsBoolean());
                        } else if (setting instanceof NumberSetting s && data.has(s.getName())) {
                            s.setValue(data.get(s.getName()).getAsDouble());
                        } else if (setting instanceof ModeSetting s && data.has(s.getName())) {
                            s.setMode(data.get(s.getName()).getAsString());
                        }
                    } catch (Exception ex) {
                        System.err.println("[Config] Failed to load setting '" + setting.getName() +
                                "' for module '" + module.getName() + "'");
                        ex.printStackTrace();
                    }
                }
            }

            System.out.println("[Config] Loaded successfully.");
        } catch (Exception e) {
            System.err.println("[Config] Failed to load config:");
            e.printStackTrace();
        }
    }
}
