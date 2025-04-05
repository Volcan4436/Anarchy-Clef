package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.CMoveUtil;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;

//We Will Need a scrollable ClickGUI for this at some point
// as well as the ability to change Settings of a Module with commands
//todo:
// add ElytraPacket Spammer (Can Allow us to mess with older Anticheats)
// add Position Shifter (Shift our position in a really small area as we move to mess with Anticheats)
// add Stutter (Stutter our movement to mess with Anticheats)
// add GroundSpoof Spammer (Can help with FallDamage and might cause Disablers)
// add Blink
// add SlowFall Potion Effect Spoofer
// Add Strict Mode that uses a yawStep to slow your rotations that might help with bypassing
// Add Option to Start Fly Under a Block (For Legacy Anticheat Support)
// Add Option to Start Fly after Phasing into a Block (Similar to Old Hypixel Fly Methods) (For Legacy Anticheat Support)
public class AdvancedFly extends Mod {

    public AdvancedFly() {
        super("AdvancedFly", "Advanced Fly", Mod.Category.MOVEMENT);
    }

    int ticks = 0;

    //Settings
    BooleanSetting spoofCanFly = new BooleanSetting("Ability", false);
    ModeSetting antiKick = new ModeSetting("Anti-Kick", "None", "None", "Glide", "Position", "Jump");
    BooleanSetting strafe = new BooleanSetting("Strafe", false);
    ModeSetting flyMethod = new ModeSetting("Fly-Method", "Position", "Position", "Velocity", "None", "Dev");
    NumberSetting vertical = new NumberSetting("Vertical", 0.1, 10, 1, 0.1);
    NumberSetting horizontal = new NumberSetting("Horizontal", 0.1, 10, 1, 0.1);
    BooleanSetting forceRotation = new BooleanSetting("Force-Rotation", false);
    NumberSetting forceYaw = new NumberSetting("Force Yaw", 0, 360, 0, 0.1);
    NumberSetting forcePitch = new NumberSetting("Force Pitch", -90, 90, 0, 0.1);

    @EventHandler
    public boolean onShitTick() {
        assert mc.player != null;
        ticks++; //Keeps track of Ticks

        //Fly
        if (flyMethod.getMode() == "Position") {
            float yaw = mc.player.getYaw();
            double radians = Math.toRadians(yaw);
            double distance = horizontal.getValuefloat();

            // Calculate the offset based on the player's viewing direction
            double xOffset = -Math.sin(radians) * distance; // Calculate X offset based on yaw
            double zOffset = Math.cos(radians) * distance;  // Calculate Z offset based on yaw

            if (mc.options.jumpKey.isPressed()) {
                mc.player.updatePosition(mc.player.getX(), mc.player.getY() + vertical.getValuefloat(), mc.player.getZ());
            } else if (mc.options.sneakKey.isPressed()) {
                mc.player.updatePosition(mc.player.getX(), mc.player.getY() - vertical.getValuefloat(), mc.player.getZ());
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
        if (flyMethod.getMode().equals("Velocity")) {
            float yaw = mc.player.getYaw();
            double radians = Math.toRadians(yaw);
            double distance = horizontal.getValuefloat();

            // Calculate velocity components based on yaw
            double xVelocity = -Math.sin(radians) * distance;
            double zVelocity = Math.cos(radians) * distance;
            double yVelocity = 0.0;

            if (mc.options.jumpKey.isPressed()) {
                yVelocity = vertical.getValuefloat(); // Move upwards
            } else if (mc.options.sneakKey.isPressed()) {
                yVelocity = -vertical.getValuefloat(); // Move downwards
            }

            if (mc.options.forwardKey.isPressed()) {
                mc.player.setVelocity(xVelocity, yVelocity, zVelocity);
            } else if (mc.options.backKey.isPressed()) {
                mc.player.setVelocity(-xVelocity, yVelocity, -zVelocity);
            } else if (mc.options.leftKey.isPressed()) {
                mc.player.setVelocity(zVelocity, yVelocity, -xVelocity);
            } else if (mc.options.rightKey.isPressed()) {
                mc.player.setVelocity(-zVelocity, yVelocity, xVelocity);
            } else {
                mc.player.setVelocity(0, yVelocity, 0); // Stop movement if no key is pressed (except for vertical movement)
            }
        }


        //Strafe
        if (strafe.isEnabled()) {
            CMoveUtil.strafe();
        }

        //AntiKick
        // todo:
        //  - Add DelayOption
        //  - Add GroundSpoofOption
        if (antiKick.getMode() == "Glide") {
            if (!mc.player.isOnGround() && ticks >= 20) {
                mc.player.setVelocity(mc.player.getVelocity().x, -0.3, mc.player.getVelocity().z);
            }
        }
        else if (antiKick.getMode() == "Position") {
            if (!mc.player.isOnGround() && ticks >= 20) {
                mc.player.updatePosition(mc.player.getX(), mc.player.getY() - 0.3, mc.player.getZ());
            }
        }
        else if (antiKick.getMode() == "Jump") {
            if (!mc.player.isOnGround() && ticks >= 20) {
                mc.player.jump();
            }
        }

        //Spoof Fly
        // todo:
        //  - Add Flight Speed Option
        if (!mc.player.isCreative() && spoofCanFly.isEnabled() && !mc.player.getAbilities().allowFlying) mc.player.getAbilities().allowFlying = true;
        else if (!spoofCanFly.isEnabled() && !mc.player.isCreative()) mc.player.getAbilities().allowFlying = false;

        //Yaw & Pitch
        // todo:
        //  - Add ServerSide Spoofer Option
        if (forceRotation.isEnabled()) {
            mc.player.setYaw(forceYaw.getValuefloat());
            mc.player.setPitch(forcePitch.getValuefloat());
        }

        return false;
    }

    @Override
    public void onEnable() {
        assert mc.player != null;
        ticks = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        assert mc.player != null;
        ticks = 0;
        if (!mc.player.isCreative() && mc.player.getAbilities().allowFlying) mc.player.getAbilities().allowFlying = false;
        super.onDisable();
    }
}
