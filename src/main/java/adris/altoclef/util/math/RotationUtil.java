package adris.altoclef.util.math;

import net.minecraft.util.math.MathHelper;

import static adris.altoclef.altomenu.command.ChatUtils.mc;

public class RotationUtil {

    public static float getPitch() {
        assert mc.player != null;
        return MathHelper.clamp(mc.player.getPitch(), -90f, 90f);
    }

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
