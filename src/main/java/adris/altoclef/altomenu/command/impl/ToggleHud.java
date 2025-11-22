package adris.altoclef.altomenu.command.impl;


import adris.altoclef.altomenu.command.ChatUtils;
import adris.altoclef.altomenu.command.Command;
import adris.altoclef.altomenu.command.HUDSettings;

public class ToggleHud extends Command {

    public boolean toggleHud = false;

    public ToggleHud() {
        super("togglehud", "Toggles the Hud", "*config <load/save> <configName>");
    }

    @Override
    public void onCmd(String message, String[] args) {
        if (args.length == 1) {
            // Toggle the HUD visibility
            HUDSettings.toggleHUD();
            ChatUtils.addChatMessage("HUD visibility toggled: " + HUDSettings.isToggleHUD());
        } else {
            ChatUtils.addChatMessage("Invalid usage. Use .togglehud without any arguments.");
        }
    }
}
