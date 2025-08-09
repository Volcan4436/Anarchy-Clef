package adris.altoclef.altomenu.command.impl;

import adris.altoclef.altomenu.cheatUtils.CChatUtil;
import adris.altoclef.altomenu.command.Command;
import net.minecraft.util.math.MathHelper;

public class HClip extends Command {

    public HClip() {
        super("hclip", "Teleport Horizontally");
    }

    @Override
    public void onCmd(String message, String[] args) {
        int amount = args.length == 1 ? 0 : Integer.parseInt(args[1]);

        if (amount < -100 || amount > 100) {
            System.out.println("Amount must be between -100 and 100.");
            return;
        }
        float yaw = mc.player.getYaw();
        float cosYaw = MathHelper.cos(yaw * 0.017453292F);
        float sinYaw = MathHelper.sin(yaw * 0.017453292F);
        mc.player.setPosition(mc.player.getX() + cosYaw * amount, mc.player.getY(), mc.player.getZ() + sinYaw * amount);
        CChatUtil.addChatMessage("Teleported to " + mc.player.getX() + ", " + mc.player.getY() + ", " + mc.player.getZ());
    }
}