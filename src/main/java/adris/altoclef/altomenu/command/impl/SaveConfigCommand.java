package adris.altoclef.altomenu.command.impl;

import adris.altoclef.altomenu.command.ChatUtils;
import adris.altoclef.altomenu.command.Command;
import adris.altoclef.altomenu.config.ConfigManager;

public class SaveConfigCommand extends Command {

    public SaveConfigCommand() {
        super("savecfg", "Saves current config", "saveconfig");
    }

    @Override
    public void onCmd(String message, String[] args) {
        ConfigManager.saveConfig();
        ChatUtils.addChatMessage("Config saved successfully!");
    }
}
