package adris.altoclef.altomenu.command.impl;


import adris.altoclef.altomenu.command.ChatUtils;
import adris.altoclef.altomenu.command.Command;
import adris.altoclef.altomenu.command.HUDSettings;

public class ExampleCommand extends Command {
    // Strings for ExampleCommand are "Name", "Description" and "Aliases".
    public ExampleCommand() {
        super("togglehud", "hudtoggler", "*hudcommand <load/save> <other thing>");
    }
    // Example Module that would Hide a hud element for example.
    // Every command already has a prefix of "$" to execute commands in a different class, Example: $togglehud and thats it.
    @Override
    public void onCmd(String message, String[] args) {
        if (args.length == 1) {
            // Toggle the HUD visibility
            ChatUtils.addChatMessage("HUD visibility toggled: " + HUDSettings.isToggleHUD());
        } else {
            ChatUtils.addChatMessage("Invalid usage. Use .togglehud without any arguments.");
        }
    }
}
