package adris.altoclef.altomenu.managers;

import adris.altoclef.altomenu.command.Command;
import adris.altoclef.altomenu.command.impl.*;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    public static CommandManager INSTANCE = new CommandManager();
    private final List<Command> cmds = new ArrayList<>();
    private static final String PREFIX = "$";
    public CommandManager() {
        init();
    }

    private void init() {
        add(new Bind());
        add(new ConfigCommand());
        add(new ToggleHud());
        add(new Help());
        add(new SelfCrash());
        add(new HClip());
        add(new VClip());
    }

    public void add(Command command) {
        if (!cmds.contains(command)) {
            cmds.add(command);
        }
    }

    public String getPrefix() {
        return PREFIX;
    }

    public void remove(Command command) {
        cmds.remove(command);
    }

    public List<Command> getCmds() {
        return cmds;
    }
}
