package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.NumberSetting;

public class BetterCamera extends Mod {

    public static BetterCamera Instance;

    public BetterCamera() {
        super("Better Camera", "Better Camera", Mod.Category.RENDER);
        Instance = this;
    }

    public NumberSetting cameradistance = new NumberSetting("CameraDistance", 1, 20, 4, 1);
    public BooleanSetting noclip = new BooleanSetting("NoClip", false);

}
