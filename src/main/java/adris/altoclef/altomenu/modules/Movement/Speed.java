package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.CMoveUtil;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
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
    NumberSetting speed = new NumberSetting("Speed", 0.1, 10, 0.6, 0.1);
    @EventHandler
    public boolean onShitTick() {
        if (mode.getMode() == "Legit") { //todo: add Speed Equivalent (Similar to Rise 6)
            mc.options.jumpKey.setPressed(false);
            if (CMoveUtil.isMoving() && CMoveUtil.isOnGround()) {
                mc.player.jump();
            }
        }
        else if (mode.getMode() == "Strafe") {
            CMoveUtil.strafe();
            calcSpeed();
        }
        else if (mode.getMode() == "StrafeHop") {
            CMoveUtil.strafe();
            calcSpeed();
            if (CMoveUtil.isMoving() && CMoveUtil.isOnGround()) {
                mc.player.jump();
            }
        }
        else if (mode.getMode() == "GroundStrafe") {
            if (CMoveUtil.isOnGround()) {
                CMoveUtil.strafe();
                calcSpeed();
            }
        }
        else if (mode.getMode() == "GroundStrafeHop") {
            if (CMoveUtil.isOnGround()) {
                CMoveUtil.strafe();
                calcSpeed();
                if (CMoveUtil.isMoving()) mc.player.jump();
            }
        }
        return false;
    }
    public void calcSpeed() {
        float yaw = mc.player.getYaw();
        double radians = Math.toRadians(yaw);
        double speed = this.speed.getValuefloat();

        // Calculate the offset based on the player's viewing direction
        double xOffset = -Math.sin(radians) * (speed / 100); // Calculate X offset based on yaw
        double zOffset = Math.cos(radians) * (speed / 100);  // Calculate Z offset based on yaw

        if (mc.options.forwardKey.isPressed()) {
            //devide speed by 100
            mc.player.setVelocity(mc.player.getVelocity().x + xOffset, mc.player.getVelocity().y, mc.player.getVelocity().z + zOffset);
        } else if (mc.options.backKey.isPressed()) {
            mc.player.setVelocity(mc.player.getVelocity().x - xOffset, mc.player.getVelocity().y, mc.player.getVelocity().z - zOffset);
        } else if (mc.options.leftKey.isPressed()) {
            mc.player.setVelocity(mc.player.getVelocity().x + zOffset, mc.player.getVelocity().y, mc.player.getVelocity().z - xOffset);
        } else if (mc.options.rightKey.isPressed()) {
            mc.player.setVelocity(mc.player.getVelocity().x - zOffset, mc.player.getVelocity().y, mc.player.getVelocity().z + xOffset);
        }
    }
}
