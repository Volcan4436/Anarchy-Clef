package adris.altoclef.altomenu.sqdpkg;

import net.minecraft.client.MinecraftClient;

public class testbin {
    protected static MinecraftClient mc = MinecraftClient.getInstance();

    static String CRSHAIR;

    public static String getCRSHAIRData(String crosshair) {
       crosshair = mc.crosshairTarget == null ? "null" : mc.crosshairTarget.getType().toString();
        return crosshair;
    }
    public static String getCRSHAIR() {

        return null;
    }
}
