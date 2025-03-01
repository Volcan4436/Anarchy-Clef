package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind", "Lets you Toggle Modules with a keybind by name, Usage: bind <module name> <keybind>");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        //todo implement
    }
}
