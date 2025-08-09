package adris.altoclef.altomenu.command.impl;

import adris.altoclef.altomenu.cheatUtils.CChatUtil;
import adris.altoclef.altomenu.command.Command;

public class VClip extends Command {

    public VClip() {
        super("vclip", "Teleport vertically");
    }

    @Override
    public void onCmd(String message, String[] args) {
        int amount = args.length == 1 ? 0 : Integer.parseInt(args[1]);

        if (amount < -100 || amount > 100) {
            System.out.println("Amount must be between -100 and 100.");
            return;
        }

        mc.player.setPosition(mc.player.getX(), mc.player.getY() + amount, mc.player.getZ());
        CChatUtil.addChatMessage("Teleported to " + mc.player.getX() + ", " + mc.player.getY() + ", " + mc.player.getZ());
    }
}