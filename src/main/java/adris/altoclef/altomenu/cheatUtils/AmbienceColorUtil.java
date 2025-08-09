package adris.altoclef.altomenu.cheatUtils;

import adris.altoclef.altomenu.managers.AmbienceColorHolder;
import net.minecraft.util.math.Vec3d;

public class AmbienceColorUtil {
    public static Vec3d getCurrentAmbienceVec3() {
        int color = AmbienceColorHolder.currentColor;
        double r = ((color >> 16) & 0xFF) / 255.0;
        double g = ((color >> 8) & 0xFF) / 255.0;
        double b = (color & 0xFF) / 255.0;
        return new Vec3d(r, g, b);
    }
}

