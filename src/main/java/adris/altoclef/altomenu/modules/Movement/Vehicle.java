package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.CChatUtil;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;

import java.util.Objects;

//TODO:
// - Add Phase
// - Add Teleport Method
// - Add Entity Size Editor (Lets you change the size of the entity you are riding)
// - Add ModelSwap (Swap the model of the vehicle with a custom model)
// - Add Force3rdPerson
// - Add AntiStuck
// - Add AutoDisable (onDisconnect, onDeath, onDimensionChange, onSetBack)
// - Add IceSpeedModifier
// - Add HorseJumpModifier
// - Add PerfectHorseJump (Forces the Horse Jump Bar to always equal 100% full when pressing jump button)
// - Add NoPush (Stops you from being pushed by flowing Water)
// - Add Speed Toggle
// - Add Speed Method (e.g calcSpeed or AttributeMod)
// - Find a way to add an option to stop you from flying into unloaded chunks
// - Add Fly Methods (Glide, Velocity, Position etc.)
public class Vehicle extends Mod {

    public Vehicle() {
        super("Vehicle (BETA)", "SPEEEED", Mod.Category.MOVEMENT);
    }

    int antikickticks = 0;

    BooleanSetting fly = new BooleanSetting("Fly", false);
    ModeSetting antiKick = new ModeSetting("Anti-Kick", "OFF", "OFF", "Position");
    NumberSetting antiKickTicksSet = new NumberSetting("Anti-Kick Ticks", 8, 20, 8, 1);
    NumberSetting speed = new NumberSetting("Speed", 0.1, 100, 1, 0.1);
    BooleanSetting yawFix = new BooleanSetting("YawFix", true);

    @Override
    public void onEnable() {
        CChatUtil.addChatMessage(" [Alert] Vehicle Module is still in BETA!");
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler
    public boolean onShitTick() {
        if (mc.player.getVehicle() == null) return true; // Fixes Crash
        if (yawFix.isEnabled()) mc.player.getVehicle().setYaw(mc.player.getYaw()); // Sync yaw with Player
        if (fly.isEnabled()) mc.player.getVehicle().setVelocity(mc.player.getVehicle().getVelocity().x, 0, mc.player.getVehicle().getVelocity().z);
        calcSpeed(); //We need a Util for this


        //---AntiKick---
        //Why is this code so fucking DOGSHIT! - Volcan
        if (antiKick.getMode() == "OFF") antikickticks = 0;
        if (!Objects.equals(antiKick.getMode(), "OFF")) {
            antikickticks++;
            if (antiKickTicksSet.getValuefloat() > antikickticks) {
                antikickticks = 0;
            }
        }
        if (antikickticks == antiKickTicksSet.getValuefloat()) {
            if (antiKick.getMode() == "Position") {
                mc.player.getVehicle().setPos(mc.player.getVehicle().getX(), mc.player.getVehicle().getY() + 1, mc.player.getVehicle().getZ());
            }
        }

        return false;
    }


    //TODO: move this to a Util
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
