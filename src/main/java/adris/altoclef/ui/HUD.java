package adris.altoclef.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class HUD {

    protected static MinecraftClient mc = MinecraftClient.getInstance();

    //WaterMark
    public static void render(DrawContext context, float tickDelta) {
        context.drawText(mc.textRenderer, "AnarchyClef [BETA]", 3, 500, -1, false);
    }

}
