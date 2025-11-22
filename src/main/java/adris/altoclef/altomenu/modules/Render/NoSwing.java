package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.eventbus.EventHandler;

public class NoSwing extends Mod {

    public NoSwing() {
        super("NoSwing", "Funne", Mod.Category.RENDER);
    }

    @EventHandler
    public boolean onShitTick() {
        mc.player.handSwinging = false; //This could be done better
        return false;
    }
}
