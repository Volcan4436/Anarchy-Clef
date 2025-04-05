package adris.altoclef.altomenu.command;

import adris.altoclef.altomenu.command.impl.Bind;
import adris.altoclef.altomenu.command.impl.ConfigCommand;
import adris.altoclef.altomenu.command.impl.TEMPLATE;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    public static CommandManager INSTANCE = new CommandManager();
    private final List<Command> cmds = new ArrayList<>();

    public CommandManager() {
        init();
    }

    private void init() {
        add(new Bind());
        add(new ConfigCommand());
        add(new TEMPLATE());

    }

    public void add(Command command) {
        if (!cmds.contains(command)) {
            cmds.add(command);
        }
    }

    public void remove(Command command) {
        cmds.remove(command);
    }

    public List<Command> getCmds() {
        return cmds;
    }
}
