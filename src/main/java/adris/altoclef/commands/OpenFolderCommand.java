package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.util.helpers.ConfigHelper;

import java.awt.*;
import java.io.File;
import java.io.IOException;

//todo: make a cleaner implementation
public class OpenFolderCommand extends Command {

    public OpenFolderCommand() {
        super("openfolder", "Opens the config file directly.");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        // Get the config file using the existing method
        File configFile = ConfigHelper.getConfigFile("your_config_file.json");  // Replace with your config file name

        // Check if the file exists
        if (configFile.exists()) {
            try {
                // Open the config file using the default system application (e.g., text editor)
                Desktop.getDesktop().open(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Config file not found: " + configFile.getAbsolutePath());
        }

        finish();
    }
}
