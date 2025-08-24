package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.CMoveUtil;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

//todo:
// Add Modes: Glide, Velocity, Fireball (For Minigame Servers), AirWalk (Collision Spoof), TNT (For Minigame Servers), Damage (Boost on Damage Client or Spoofed)
// Add Option to Start Fly Under a Block (For Legacy Anticheat Support)
// Add Option to Start Fly after Phasing into a Block (Similar to Old Hypixel Fly Methods) (For Legacy Anticheat Support)
// Add SetBack Detection
public class Flight extends Mod {
    public Flight() {
        super("Flight", "Fly", Mod.Category.MOVEMENT);
    }

    ModeSetting mode = new ModeSetting("Mode", "Creative", "Creative", "Position", "Velocity");
    NumberSetting updateposY = new NumberSetting("Update Vertical", 0.1, 10, 1, 0.1);
    NumberSetting updateposH = new NumberSetting("Update Horizontal", 0.1, 10, 1, 0.1);
    NumberSetting horizontalspeed = new NumberSetting("Horizontal Speed", 0.1, 20, 0.6, 0.1);
    NumberSetting verticalspeed = new NumberSetting("Vertical Speed", 0.1, 20, 0.6, 0.1);

    @EventHandler
    public boolean onShitTick() {
        if (mode.getMode() == "Position") {
            float yaw = mc.player.getYaw();
            double radians = Math.toRadians(yaw);
            double teleportDistance = updateposH.getValuefloat();

            // Calculate the offset based on the player's viewing direction
            double xOffset = -Math.sin(radians) * teleportDistance; // Calculate X offset based on yaw
            double zOffset = Math.cos(radians) * teleportDistance;  // Calculate Z offset based on yaw

            mc.player.setVelocity(mc.player.getVelocity().x, 0, mc.player.getVelocity().z); // Keep Player from Falling (We need to make fake Collision instead ngl)

            if (mc.options.jumpKey.isPressed()) {
                mc.player.updatePosition(mc.player.getX(), mc.player.getY() + updateposY.getValuefloat(), mc.player.getZ());
            } else if (mc.options.sneakKey.isPressed()) {
                mc.player.updatePosition(mc.player.getX(), mc.player.getY() - updateposY.getValuefloat(), mc.player.getZ());
            } else if (mc.options.forwardKey.isPressed()) {
                mc.player.updatePosition(mc.player.getX() + xOffset, mc.player.getY(), mc.player.getZ() + zOffset);
            } else if (mc.options.backKey.isPressed()) {
                mc.player.updatePosition(mc.player.getX() - xOffset, mc.player.getY(), mc.player.getZ() - zOffset);
            } else if (mc.options.leftKey.isPressed()) {
                mc.player.updatePosition(mc.player.getX() + zOffset, mc.player.getY(), mc.player.getZ() - xOffset);
            } else if (mc.options.rightKey.isPressed()) {
                mc.player.updatePosition(mc.player.getX() - zOffset, mc.player.getY(), mc.player.getZ() + xOffset);
            }
        } else if (mode.getMode() == "Velocity") {
                float yaw = mc.player.getYaw();
                double radians = Math.toRadians(yaw);
                double speed = this.horizontalspeed.getValuefloat();
    
                // Calculate the offset based on the player's viewing direction
                double xOffset = -Math.sin(radians) * (speed / 100); // Calculate X offset based on yaw
                double zOffset = Math.cos(radians) * (speed / 100);  // Calculate Z offset based on yaw
    
                if (mc.options.jumpKey.isPressed()) {
                    mc.player.setVelocity(mc.player.getVelocity().x, verticalspeed.getValuefloat(), mc.player.getVelocity().z);
                }
                else if (mc.options.sneakKey.isPressed()) {
                    mc.player.setVelocity(mc.player.getVelocity().x, -verticalspeed.getValuefloat(), mc.player.getVelocity().z);
                }
                else if (mc.options.forwardKey.isPressed()) {
                    mc.player.setVelocity(mc.player.getVelocity().x + xOffset, 0, mc.player.getVelocity().z + zOffset);
                    CMoveUtil.strafe();
                } else if (mc.options.backKey.isPressed()) {
                    mc.player.setVelocity(mc.player.getVelocity().x - xOffset, 0, mc.player.getVelocity().z - zOffset);
                    CMoveUtil.strafe();
                } else if (mc.options.leftKey.isPressed()) {
                    mc.player.setVelocity(mc.player.getVelocity().x + zOffset, 0, mc.player.getVelocity().z - xOffset);
                    CMoveUtil.strafe();
                } else if (mc.options.rightKey.isPressed()) {
                    mc.player.setVelocity(mc.player.getVelocity().x - zOffset, 0, mc.player.getVelocity().z + xOffset);
                    CMoveUtil.strafe();
                }
                else mc.player.setVelocity(mc.player.getVelocity().x, 0, mc.player.getVelocity().z);
            }
        return false;
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        if (mc.player.isCreative() || mc.player.isSpectator()) return;
        else if (mode.getMode() == "Creative") mc.player.getAbilities().allowFlying = true;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (mc.player.isCreative() || mc.player.isSpectator()) return;
        else if (mode.getMode() == "Creative") mc.player.getAbilities().allowFlying = false;
        super.onDisable();
    }
}