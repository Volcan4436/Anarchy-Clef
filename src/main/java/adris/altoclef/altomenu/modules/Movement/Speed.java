package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.CMoveUtil;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.eventbus.EventHandler;

//todo
// Add Modes: PhysicsCalc, onGround, Strict, NCPStrict, Grim (Entity + YawSpoof), Matrix7, 5b5tFast, 5b5tStrict
// Add SetBack Toggle
// Add LowHop (Similar to Future Client)
// Create AdvancedSpeed Module
public class Speed extends Mod {

    public Speed() {
        super("Speed", "Changes your speed", Mod.Category.MOVEMENT);
    }
    ModeSetting mode = new ModeSetting("Mode", "Legit", "Legit", "Strafe", "StrafeHop", "GroundStrafe", "GroundStrafeHop");

    @EventHandler
    public boolean onShitTick() {
        if (mode.getMode() == "Legit") {
            mc.options.jumpKey.setPressed(false);
            if (CMoveUtil.isMoving() && CMoveUtil.isOnGround()) {
                mc.player.jump();
            }
        }
        else if (mode.getMode() == "Strafe") {
            CMoveUtil.strafe();
        }
        else if (mode.getMode() == "StrafeHop") {
            CMoveUtil.strafe();
            if (CMoveUtil.isMoving() && CMoveUtil.isOnGround()) {
                mc.player.jump();
            }
        }
        else if (mode.getMode() == "GroundStrafe") {
            if (CMoveUtil.isOnGround()) {
                CMoveUtil.strafe();
            }
        }
        else if (mode.getMode() == "GroundStrafeHop") {
            if (CMoveUtil.isOnGround()) {
                CMoveUtil.strafe();
                if (CMoveUtil.isMoving()) mc.player.jump();
            }
        }

        return false;
    }
}
