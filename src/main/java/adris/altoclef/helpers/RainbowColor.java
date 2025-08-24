package adris.altoclef.helpers;

import net.minecraft.util.math.MathHelper;

public class RainbowColor {
    public static int getRainbow(float sat, float bri, double speed, int offset) {
        double rainbowState = Math.ceil((System.currentTimeMillis() + offset) / 10.0) % 360;
        return 0xff000000 | MathHelper.hsvToRgb((float) (rainbowState / 360.0), sat, bri);
    }

    public static int getRainbowColor(int originalColor) {
        int rainbowColor = getRainbow(1.0f, 1.0f, 1000.0, 0);
        return (originalColor & 0xFF000000) | (rainbowColor & 0x00FFFFFF);
    }

    public static void main(String[] args) {
        int originalColor = 0xFFFFFFFF;
        int rainbowColor = getRainbowColor(originalColor);
        System.out.println("Rainbow Color: " + Integer.toHexString(rainbowColor));
    }
}