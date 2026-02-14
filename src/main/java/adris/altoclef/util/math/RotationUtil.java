package adris.altoclef.util.math;

import net.minecraft.util.math.MathHelper;

import static adris.altoclef.altomenu.command.ChatUtils.mc;

//todo: add a method for getting the Yaw and Pitch of any Entity(s) not just the player
public class RotationUtil {

    //Gets the true Pitch
    public static float getPitch() {
        assert mc.player != null;
        return MathHelper.clamp(mc.player.getPitch(), -90f, 90f);
    }

    //Gets the true Yaw
    public static float getYaw() {
        assert mc.player != null;
        return ((mc.player.getYaw() + 180f) % 360f + 360f) % 360f - 180f;
    }

    public void setYaw(float yaw) {
        assert mc.player != null;
        mc.player.setYaw(yaw);
    }

    public void setPitch(float pitch) {
        assert mc.player != null;
        mc.player.setPitch(pitch);
    }
}
