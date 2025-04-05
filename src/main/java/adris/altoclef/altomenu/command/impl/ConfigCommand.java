package adris.altoclef.altomenu.command.impl;

import adris.altoclef.altomenu.command.ChatUtils;
import adris.altoclef.altomenu.command.Command;
import adris.altoclef.altomenu.config.Config;
import adris.altoclef.altomenu.config.configloader;

import java.io.IOException;

public class ConfigCommand extends Command {
    public ConfigCommand() {
        super("config", "Loads or saves a specified configuration", "*config <load/save> <configName>");
    }

    @Override
    public void onCmd(String message, String[] args) {
        if (args.length < 3) {
            ChatUtils.addChatMessage("Not enough arguments.");
            return;
        }

        String action = args[1].toLowerCase();
        String configName = args[2];

        if (action.equals("load")) {
            loadConfig(configName);
        } else if (action.equals("save")) {
            saveConfig(configName);
        } else {
            ChatUtils.addChatMessage("Invalid action. Use 'load' or 'save'.");
        }
    }

    private void loadConfig(String configName) {
        configloader configLoader = new configloader();
        try {
            configLoader.loadConfigs(); // Load available configs from file
            Config config = configLoader.getConfigByName(configName);
            if (config != null) {
                configLoader.loadConfig(config);
                ChatUtils.addChatMessage("Config loaded: " + configName);
            } else {
                ChatUtils.addChatMessage("Config not found: " + configName);
            }
        } catch (IOException e) {
            ChatUtils.addChatMessage("Failed to load config: " + configName);
            e.printStackTrace(); // Handle the exception appropriately
        }
    }

    private void saveConfig(String configName) {
        configloader configLoader = new configloader();
        try {
            configLoader.loadConfigs(); // Load available configs from file

            // Check if the config already exists
            Config existingConfig = configLoader.getConfigByName(configName);
            if (existingConfig != null) {
                existingConfig.save(); // Save the existing config
                ChatUtils.addChatMessage("Config updated: " + configName);
            } else {
                // Save a new config using the provided name
                configLoader.saveNewConfig(configName);
                ChatUtils.addChatMessage("New config created and saved: " + configName);
            }
        } catch (IOException e) {
            ChatUtils.addChatMessage("Failed to save config: " + configName);
            e.printStackTrace(); // Handle the exception appropriately
        }
    }
}
