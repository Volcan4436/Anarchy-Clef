package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;
public class OreTracers extends Mod {
    public static OreTracers Instance;
    public OreTracers() {
        super("OreTracers", "Ore Tracers", Mod.Category.RENDER);
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