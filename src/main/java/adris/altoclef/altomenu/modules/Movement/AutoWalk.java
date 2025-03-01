package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.eventbus.EventHandler;

//Todo
// Create an experimental mode that uses Baritone to walk in a straight line in the fastest way possible
// Support Jumping Over Blocks that get in our way
// Support Using Rockets if using an Elytra
// Add Yaw/Pitch Lock
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
