package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.NumberSetting;

public class PlayerScale extends Mod {

    public static PlayerScale Instance;

    public PlayerScale() {
        super("PlayerScale", "Funne", Mod.Category.RENDER);
        Instance = this;
    }

    public NumberSetting scale = new NumberSetting("Scale", 0.1, 4, 1, 0.1);
    public BooleanSetting allPlayers = new BooleanSetting("AllPlayers", false);
}
