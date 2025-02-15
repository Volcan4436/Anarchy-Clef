package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.ModeSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sprint extends Mod {

    private static final Logger log = LoggerFactory.getLogger(Sprint.class);

    public Sprint() {
        super("Sprint", "Sprints for you", Mod.Category.MOVEMENT);
        addSetting(mode);
    }

    ModeSetting mode = new ModeSetting("Mode", "Always", "Always", "Smart");

    @Override
    public boolean onShitTick() {
        if (mode.getMode() == "Always") {
            mc.player.setSprinting(true);
        }
        else if (mode.getMode() == "Smart") {
            if (mc.player.forwardSpeed > 0.0F) {
                mc.options.sprintKey.setPressed(true);
            }
            else mc.options.sprintKey.setPressed(false);
        }
        super.onShitTick();
        return false;
    }
}
