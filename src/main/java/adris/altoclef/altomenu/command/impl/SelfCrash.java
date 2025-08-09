package adris.altoclef.altomenu.command.impl;

import adris.altoclef.altomenu.command.Command;

public class SelfCrash extends Command {

    public SelfCrash() {
        super("selfcrash", "crash");
    }

    @Override
    public void onCmd(String message, String[] args) {
        throw new RuntimeException("Bang!");
    }
}
