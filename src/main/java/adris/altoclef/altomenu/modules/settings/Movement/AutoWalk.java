package adris.altoclef.altomenu.modules.settings.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.eventbus.EventHandler;

public class AutoWalk extends Mod {

    public AutoWalk() {
        super("AutoWalk", "Auto Walk", Mod.Category.MOVEMENT);
    }

    @EventHandler
    public boolean onShitTick() {
        mc.options.forwardKey.setPressed(true);
        return false;
    }

    @Override
    public void onDisable() {
        mc.options.forwardKey.setPressed(false);
        super.onDisable();
    }
}
