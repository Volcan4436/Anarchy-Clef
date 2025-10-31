package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.CMoveUtil;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;

//todo:
// Implement SmoothCamera (Locks Camera to the Y Level the player is at so the camera doesn't jitter during odd speed setups)
// Add Speed FOV Canceller (Turns Speed Based FOV Features off)
// Add SetBack Toggle
public class AdvancedSpeed extends Mod {


    public AdvancedSpeed() {
        super("AdvancedSpeed", "AdvancedSpeed", Mod.Category.MOVEMENT);
    }

    ModeSetting autoJump = new ModeSetting("AutoJump", "off", "off", "always", "onMove");
    BooleanSetting jumpFix = new BooleanSetting("JumpFix", true); //Important: This needs to be added to CMoveUtil as a callable method
    ModeSetting strafeMode = new ModeSetting("StrafeMode", "off", "off", "always", "air", "ground");
    NumberSetting speed = new NumberSetting("Speed", 0.1, 10, 0.6, 0.1);
    BooleanSetting fastFall = new BooleanSetting("FastFall", false);
    NumberSetting fallSpeed = new NumberSetting("FallSpeed", 0.1, 10, 0.3, 0.1);
    NumberSetting fallDistanceNeeded = new NumberSetting("Min Fall Distance", 0.1, 10, 0.3, 0.1);

    @EventHandler
    public boolean onShitTick() {
        if (mc.player == null) return false;

        //Auto Jump
        // todo: make this cleaner
        if (autoJump.getMode() == "always" && CMoveUtil.isOnGround()) {
            if (jumpFix.isEnabled() && mc.options.jumpKey.isPressed()) return false;
            mc.player.jump();
        }

        //Strafe
        //todo: Change this to a Case Switch
        //This Code is Gross
        if (strafeMode.getMode() == "always") {
            CMoveUtil.strafe(speed.getValuefloat());
        }
        if (strafeMode.getMode() == "air") {
            if (!CMoveUtil.isOnGround()) CMoveUtil.strafe(speed.getValuefloat());
        }
        if (strafeMode.getMode() == "ground") {
            if (CMoveUtil.isOnGround()) CMoveUtil.strafe(speed.getValuefloat());
        }

        //FastFall
        if (fastFall.isEnabled()) {
            if (mc.player.fallDistance > fallDistanceNeeded.getValuefloat()) {
                mc.player.setVelocity(mc.player.getVelocity().x, -fallSpeed.getValuefloat(), mc.player.getVelocity().z);
            }
        }

        return false;
    }


    @EventHandler
    public void onMove() {
        if (mc.player == null) return;

        //This could be cleaner
        if (autoJump.getMode() == "onMove" && CMoveUtil.isOnGround()) {
            if (jumpFix.isEnabled() && mc.options.jumpKey.isPressed()) return;
            mc.player.jump();
        }
    }
}
