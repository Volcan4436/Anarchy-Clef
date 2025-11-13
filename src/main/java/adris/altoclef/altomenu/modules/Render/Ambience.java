package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.CChatUtil;
import adris.altoclef.altomenu.managers.AmbienceColorHolder;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;
import adris.altoclef.helpers.RainbowColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;

import java.util.Objects;

public class Ambience extends Mod {

    public final NumberSetting red = new NumberSetting("Red", 0, 255, 0, 1);
    public final NumberSetting green = new NumberSetting("Green", 0, 255, 0, 1);
    public final NumberSetting blue = new NumberSetting("Blue", 0, 255, 0, 1);
    public final ModeSetting time = new ModeSetting("Time Mode", "Vanilla", "Vanilla", "Custom");
    public final NumberSetting timeslider = new NumberSetting("Time (ticks)", 0, 100000, 6000, 1);

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

    @Override
    public void onRender() {
        if (mc.world != null) {
            if (Objects.equals(time.getMode(), "Custom")) {
                mc.world.setTime(timeslider.getValueInt());
            }
        }
    }
}
