package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;

//TODO
// add Light Colour Changer (Similar to Future Client)
// add NightVision Spoof mode
// add Intensity Slider
// Somehow add Support For Shaders like SuesPTGI
public class Fullbright extends Mod {
    public static Fullbright Instance;
    public Fullbright() {
        super("Fullbright", "Full bright bull shit.", Mod.Category.DEVELOPMENT);
        Instance = this;
    }
    @Override
    public void onEnable() {
        System.out.println("fullbright enabled");
        super.onEnable();
    }

    @Override
    public void onDisable() {
        System.out.println("fullbright disabled");
        super.onDisable();
    }
}