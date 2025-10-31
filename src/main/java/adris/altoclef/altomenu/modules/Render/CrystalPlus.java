package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;

public class CrystalPlus extends Mod {

    // Settings
    public static BooleanSetting bounce = new BooleanSetting("Bounce", true);
    public static ModeSetting renderMode = new ModeSetting("RenderMode", "Normal", "Normal", "SColor", "Gradient", "CGradient");
    public static BooleanSetting rgb = new BooleanSetting("RGB", false);
    public static NumberSetting r = new NumberSetting("R", 0, 255, 255, 1);
    public static NumberSetting g = new NumberSetting("G", 0, 255, 255, 1);
    public static NumberSetting b = new NumberSetting("B", 0, 255, 255, 1);
    public static BooleanSetting spawnAnim = new BooleanSetting("SpawnAN", true);

    public CrystalPlus() {
        super("CrystalPlus", "Customizes End Crystal rendering", Category.RENDER);

        addSetting(bounce);
        addSetting(renderMode);
        addSetting(rgb);
        addSetting(r);
        addSetting(g);
        addSetting(b);
        addSetting(spawnAnim);
    }

    @EventHandler
    public boolean onShitTick() {
        // You can later handle runtime updates here (like color cycling for RGB)
        return false;
    }

    @Override
    public void onEnable() {
        // Optional: handle enabling logic
    }

    @Override
    public void onDisable() {
        // Optional: handle disabling logic
    }
}
