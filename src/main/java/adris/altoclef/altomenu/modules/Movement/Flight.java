package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;

//todo:
// Add Modes: Glide, Velocity, Fireball (For Minigame Servers), AirWalk (Collision Spoof)
// Add Option to Start Fly Under a Block (For Legacy Anticheat Support)
// Add Option to Start Fly after Phasing into a Block (Similar to Old Hypixel Fly Methods) (For Legacy Anticheat Support)
public class Flight extends Mod {
    public Flight() {
        super("Flight", "Fly", Mod.Category.MOVEMENT);
    }

    ModeSetting mode = new ModeSetting("Mode", "Creative", "Creative", "Position");
    NumberSetting updateposY = new NumberSetting("Update Vertical", 0.1, 10, 1, 0.1);
    NumberSetting updateposH = new NumberSetting("Update Horizontal", 0.1, 10, 1, 0.1);

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