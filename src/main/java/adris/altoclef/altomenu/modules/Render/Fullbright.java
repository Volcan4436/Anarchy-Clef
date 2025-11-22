package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.eventbus.EventHandler;
import adris.altoclef.helpers.RainbowColor;

//TODO
// add Light Colour Changer (Similar to Future Client) *Kind of done but our fullbright method is just editing textures so this isn't possible with our current method*
// add NightVision Spoof mode
// add Intensity Slider
// Somehow add Support For Shaders like SuesPTGI
public class Fullbright extends Mod {
    public static Fullbright Instance;
    public Fullbright() {
        super("Fullbright", "Full bright bull shit.", Mod.Category.RENDER);
        Instance = this;
    }

    public BooleanSetting lsd = new BooleanSetting("LSD", false);

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

    private int rainbowColor;


    @EventHandler
    public void onTick() {
        rainbowColor = RainbowColor.getRainbowColor(0xFFFFFFFF);
    }

    public int getRainbowColor() {
        return rainbowColor;
    }
}