package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;

public class Vehicle extends Mod {

    public Vehicle() {
        super("Vehicle (BETA)", "SPEEEED", Mod.Category.MOVEMENT);
    }

    BooleanSetting fly = new BooleanSetting("Fly", false);
    NumberSetting speed = new NumberSetting("Speed", 0.1, 100, 1, 0.1);
    BooleanSetting yawFix = new BooleanSetting("YawFix", true);

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler
    public boolean onShitTick() {
        if (mc.player.getVehicle() == null) return true;
        if (yawFix.isEnabled()) mc.player.getVehicle().setYaw(mc.player.getYaw()); // Sync yaw
        if (fly.isEnabled()) mc.player.getVehicle().setVelocity(mc.player.getVehicle().getVelocity().x, 0, mc.player.getVehicle().getVelocity().z);
        calcSpeed();

        return false;
    }


    public void calcSpeed() {
        float yaw = mc.player.getVehicle().getYaw();
        double radians = Math.toRadians(yaw);
        double speed = this.speed.getValuefloat();

        // Calculate the offset based on the player's viewing direction
        double xOffset = -Math.sin(radians) * (speed / 100); // Calculate X offset based on yaw
        double zOffset = Math.cos(radians) * (speed / 100);  // Calculate Z offset based on yaw

        if (mc.options.forwardKey.isPressed()) {
            mc.player.getVehicle().setVelocity(mc.player.getVehicle().getVelocity().x + xOffset, mc.player.getVehicle().getVelocity().y, mc.player.getVehicle().getVelocity().z + zOffset);
        } else if (mc.options.backKey.isPressed()) {
            mc.player.getVehicle().setVelocity(mc.player.getVehicle().getVelocity().x - xOffset, mc.player.getVehicle().getVelocity().y, mc.player.getVehicle().getVelocity().z - zOffset);
        } else if (mc.options.leftKey.isPressed()) {
            mc.player.getVehicle().setVelocity(mc.player.getVehicle().getVelocity().x + zOffset, mc.player.getVehicle().getVelocity().y, mc.player.getVehicle().getVelocity().z - xOffset);
        } else if (mc.options.rightKey.isPressed()) {
            mc.player.getVehicle().setVelocity(mc.player.getVehicle().getVelocity().x - zOffset, mc.player.getVehicle().getVelocity().y, mc.player.getVehicle().getVelocity().z + xOffset);
        }
        if (fly.isEnabled() && mc.options.jumpKey.isPressed()) mc.player.getVehicle().setVelocity(mc.player.getVehicle().getVelocity().x, 0.5, mc.player.getVehicle().getVelocity().z);
    }
}
