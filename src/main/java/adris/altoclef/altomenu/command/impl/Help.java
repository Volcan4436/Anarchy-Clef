package adris.altoclef.altomenu.command.impl;

import adris.altoclef.altomenu.cheatUtils.CChatUtil;
import adris.altoclef.altomenu.command.Command;
import adris.altoclef.altomenu.managers.CommandManager;

public class Help extends Command {
    public Help() {
        super("help", "Lists all available commands");
    }

    @Override
    public void onCmd(String message, String[] args) {
        StringBuilder helpMessage = new StringBuilder();
        if (CommandManager.INSTANCE.getPrefix() != null) {
            CChatUtil.addChatMessage("Prefix: " + CommandManager.INSTANCE.getPrefix());
        }
        helpMessage.append("Commands:\n");
        for (Command command : CommandManager.INSTANCE.getCmds()) {
            CChatUtil.addChatMessage("-" + command.getName());
        }
        // Send the help message to the user
        // (You can use a messaging system or a GUI to display the message)
    }
}