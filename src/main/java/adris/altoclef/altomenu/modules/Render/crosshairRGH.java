package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;


//TODO:
// - Add Shape Setting
// - Add Color Setting
// - Add Opacity Setting
// - Add Custom Weapon Cooldown Indicator
// - Add Option to Show Health Next to CrossHair
// - Add Option to Change Crosshair Color if you can hit the Entity
// - Add Option to Hide when not holding Weapon
// - Add Option to Hide in 3rd Person
// - Add Option to Show Mining Progress Indicator
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