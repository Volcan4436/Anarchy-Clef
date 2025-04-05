package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
public class crosshairRGH extends Mod {
    public static crosshairRGH Instance;
    public static NumberSetting Size = new NumberSetting("Size", 1, 15, 8, 1);
    public static NumberSetting Thickness = new NumberSetting("Thickness", 1, 15, 1, 1);
    public crosshairRGH() {
        super("Crosshair++", "Monkey balls fuck dick ass nigga idfk", Mod.Category.RENDER);
        Instance = this;
    }
    @Override
    public void onEnable() {
        System.out.println("Crsh++ enabled");
        super.onEnable();
    }

    @Override
    public void onDisable() {
        System.out.println("Crsh++ disabled");
        super.onDisable();
    }
}