package adris.altoclef.cheatmenu.modules.settings.Movement;

import adris.altoclef.cheatmenu.Mod;
import adris.altoclef.cheatmenu.modules.settings.ModeSetting;
import adris.altoclef.eventbus.EventHandler;
import adris.altoclef.eventbus.events.TickEvent;
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
