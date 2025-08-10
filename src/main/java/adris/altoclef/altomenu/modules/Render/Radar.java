package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;

//TODO:
// Add Option to Move its Position in X and Y On Hud
// Add Option to Change Scale
// Add Option to Change Shape (Square, Circle, RoundedEdge)
// Add Option to Change Color
// Add Option to Show Entity Names
// Add Option to Show Player Head Icons
// Add Option to show Slime Chunks
// Add Option to Show Chunks
// Add Option to Rotate with Player
// Add Option to Show in Browser
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