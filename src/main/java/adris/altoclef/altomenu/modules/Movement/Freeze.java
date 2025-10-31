package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.eventbus.EventHandler;

public class Freeze extends Mod {

    public Freeze() {
        super("Freeze", "Freeze", Mod.Category.MOVEMENT);
    }

    BooleanSetting onSneak = new BooleanSetting("onSneak", true);

    @EventHandler
    public boolean onShitTick() {
        if (mc.player == null) return false;
        if (onSneak.isEnabled() && !mc.options.sneakKey.isPressed()) return false;
        mc.player.setVelocity(0, 0, 0);
        return false;
    }
}
