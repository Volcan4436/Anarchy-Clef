package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.ModeSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

//todo We need a true OmniSprint
public class Sprint extends Mod {

    private static final Logger log = LoggerFactory.getLogger(Sprint.class);

    public Sprint() {
        super("Sprint", "Sprints for you", Mod.Category.MOVEMENT);
        addSetting(mode);
    }

    ModeSetting mode = new ModeSetting("Mode", "Always", "Always", "Smart");

    @Override
    public boolean onShitTick() {
        if (Objects.equals(mode.getMode(), "Always")) {
            mc.player.setSprinting(true);
        }
        else if (Objects.equals(mode.getMode(), "Smart")) {
            mc.options.sprintKey.setPressed(mc.player.forwardSpeed > 0.0F);
        }
        super.onShitTick();
        return false;
    }
}
