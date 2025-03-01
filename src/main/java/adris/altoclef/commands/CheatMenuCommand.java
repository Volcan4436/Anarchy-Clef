package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.stupid.OpenGUI;
import net.minecraft.client.MinecraftClient;

import static java.lang.Thread.sleep;

public class CheatMenuCommand extends Command {

    public CheatMenuCommand() { super("cheatmenu", "Opens the cheat menu.");}
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        mod.runUserTask(new OpenGUI(), this::finish);
    }
}
