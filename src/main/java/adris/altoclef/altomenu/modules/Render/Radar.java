package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;


public class Radar extends Mod {
    public static Radar Instance;
    public Radar() {
        super("RADAR", "Monkey balls", Mod.Category.RENDER);
        Instance = this;
    }
    @Override
    public void onEnable() {
        System.out.println("raydar enabled");
        super.onEnable();
    }

    @Override
    public void onDisable() {
        System.out.println("raydar disabled");
        super.onDisable();
    }
}