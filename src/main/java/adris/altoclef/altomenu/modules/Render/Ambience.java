package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.CChatUtil;
import adris.altoclef.altomenu.managers.AmbienceColorHolder;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;

public class Ambience extends Mod {

    public final NumberSetting red = new NumberSetting("Red", 0, 255, 0, 1);
    public final NumberSetting green = new NumberSetting("Green", 0, 255, 0, 1);
    public final NumberSetting blue = new NumberSetting("Blue", 0, 255, 0, 1);

    // Cache the last RGB values to detect changes
    private int lastR = -1, lastG = -1, lastB = -1;

    public static Ambience INSTANCE = new Ambience();

    public Ambience() {
        super("Ambience (BETA)", "Ambience", Mod.Category.RENDER);
        INSTANCE = this;
    }

    @EventHandler
    public void onEnable() {
    }

    @EventHandler
    public void onTick() {
        int r = red.getValueInt() & 0xFF;
        int g = green.getValueInt() & 0xFF;
        int b = blue.getValueInt() & 0xFF;

        if (r != lastR || g != lastG || b != lastB) {
            lastR = r;
            lastG = g;
            lastB = b;

            AmbienceColorHolder.currentColor = (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
    }
}
