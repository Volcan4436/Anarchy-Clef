package adris.altoclef.altomenu.command.impl;

import adris.altoclef.altomenu.command.Command;

public class SelfCrash extends Command {

    public SelfCrash() {
        super("selfcrash", "Crashes the Game");
    }

    @Override
    public void onCmd(String message, String[] args) {
        throw new RuntimeException("Bang!");
    }
}
