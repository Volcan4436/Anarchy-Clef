package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.CMoveUtil;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;

//todo
// Add Modes: PhysicsCalc, onGround, Strict, NCPStrict, Grim (Entity + YawSpoof), Matrix7, 5b5tFast, 5b5tStrict, Karhu
// Add SetBack Toggle
// Add LowHop (Similar to Future Client)
// Create AdvancedSpeed Module
// Improve the Legit Mode by letting you customise it for better Ghost Cheating
public class Speed extends Mod {

    public Speed() {
        super("Speed", "Changes your speed", Mod.Category.MOVEMENT);
    }
    ModeSetting mode = new ModeSetting("Mode", "Legit", "Legit", "Strafe", "StrafeHop", "GroundStrafe", "GroundStrafeHop");
    NumberSetting speed = new NumberSetting("Speed", 0.1, 10, 0.6, 0.1);

    @EventHandler
    public boolean onShitTick() {
        if (mode.getMode() == "Legit") { //todo: add Speed Equivalent (Similar to Rise 6) (Makes you Move at the speed that you go when going diagonally but in any direction without rotations)
            mc.options.jumpKey.setPressed(false);
            if (CMoveUtil.isMoving() && CMoveUtil.isOnGround()) {
                mc.player.jump();
            }
            //todo: Silent Rotate for Perfect Strafe
        }
        else if (mode.getMode() == "Strafe") {
            CMoveUtil.strafe(speed.getValuefloat()); //BUG: This Doesn't Abide by Slowdown (Items, Blocks, etc.)
        }
        else if (mode.getMode() == "StrafeHop") {
            CMoveUtil.strafe(speed.getValuefloat());
            if (CMoveUtil.isMoving() && CMoveUtil.isOnGround()) {
                mc.player.jump();
            }
        }
        else if (mode.getMode() == "GroundStrafe") {
            if (CMoveUtil.isOnGround()) {
                CMoveUtil.strafe(speed.getValuefloat());
            }
        }
        else if (mode.getMode() == "GroundStrafeHop") {
            if (CMoveUtil.isOnGround()) {
                CMoveUtil.strafe(speed.getValuefloat());
                if (CMoveUtil.isMoving()) mc.player.jump();
            }
        }
        return false;
    }
}
