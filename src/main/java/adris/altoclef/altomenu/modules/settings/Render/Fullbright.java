package adris.altoclef.altomenu.modules.settings.Render;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.eventbus.EventHandler;

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