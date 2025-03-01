package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.Anarchy.AnarchyTask;
import adris.altoclef.tasks.stupid.OpenGUI;

public class AnarchyCommand extends Command {

    public AnarchyCommand() {
        super("anarchy", "Survive an Anarchy Server.");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        mod.runUserTask(new AnarchyTask(), this::finish);
    }

}
