package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.CMoveUtil;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.block.Blocks;

public class WaterSpeed extends Mod {

    public WaterSpeed() {
        super("WaterSpeed", "Changes your speed while in water", Mod.Category.MOVEMENT);
    }

    NumberSetting speed = new NumberSetting("Speed", 0.1, 10, 0.6, 0.1);


    boolean isInsideWater = false;
    boolean isAboveWater = false;

    @EventHandler
    public boolean onShitTick() {
        isInsideWater();
        isAboveWater();

        if (isInsideWater) {
            float yaw = mc.player.getYaw();
            double radians = Math.toRadians(yaw);
            double speed = this.speed.getValuefloat();

            // Calculate the offset based on the player's viewing direction
            double xOffset = -Math.sin(radians) * (speed / 100); // Calculate X offset based on yaw
            double zOffset = Math.cos(radians) * (speed / 100);  // Calculate Z offset based on yaw
            CMoveUtil.strafe();

            if (mc.options.forwardKey.isPressed()) {
                mc.player.setVelocity(mc.player.getVelocity().x + xOffset, mc.player.getVelocity().y, mc.player.getVelocity().z + zOffset);
            } else if (mc.options.backKey.isPressed()) {
                mc.player.setVelocity(mc.player.getVelocity().x - xOffset, mc.player.getVelocity().y, mc.player.getVelocity().z - zOffset);
            } else if (mc.options.leftKey.isPressed()) {
                mc.player.setVelocity(mc.player.getVelocity().x + zOffset, mc.player.getVelocity().y, mc.player.getVelocity().z - xOffset);
            } else if (mc.options.rightKey.isPressed()) {
                mc.player.setVelocity(mc.player.getVelocity().x - zOffset, mc.player.getVelocity().y, mc.player.getVelocity().z + xOffset);
            }
        }
        return false;
    }

    public void isInsideWater() {
        if (mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == Blocks.WATER) {
            isInsideWater = true;
        }
        else isInsideWater = false;
    }

    public void isAboveWater() {
        if (mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock() == Blocks.WATER) {
            isAboveWater = true;
        }
        else isAboveWater = false;
    }
}
