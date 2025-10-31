package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.CMoveUtil;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;

public class Strafe extends Mod {

    public Strafe() {
        super("Strafe", "Strafe", Mod.Category.MOVEMENT);
    }

    @EventHandler
    public boolean onShitTick() {
        CMoveUtil.strafe(0.6);
        return false;
    }
}

