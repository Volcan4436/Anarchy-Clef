package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import net.minecraft.entity.Entity;

import java.util.UUID;

// todo:
//  add modes: Matrix, NCP, Position, Velocity, Vanilla, Tick (Similar to TickBase), Frame (Take a few Ticks to Complete the Step by lagging), Jump (Similar to AutoJump Setting but Better), PacketAbuse (Jitter your way upwards), RubberBand (Fake a LagBack every few steps then teleport back where you are stepping to), Blink
public class Step extends Mod {

    private UUID lastVehicleUUID;
    private float originalVehicleStepHeight;

    public Step() {
        super("Step", "Step", Mod.Category.MOVEMENT);
    }

    ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "Dev");

    NumberSetting stepHeight = new NumberSetting("Height", 0.1, 10, 1, 0.1);
    BooleanSetting vehicleFix = new BooleanSetting("VehicleFix", true);

    @Override
    public boolean onShitTick() {
        if (mc.player.getStepHeight() != stepHeight.getValuefloat() && mode.getMode().equals("Vanilla")) mc.player.setStepHeight(stepHeight.getValuefloat());

        if (vehicleFix.isEnabled() && mc.player.getVehicle() != null) {
            Entity vehicle = mc.player.getVehicle();

            // Store original vehicle step height + UUID
            if (lastVehicleUUID == null || !vehicle.getUuid().equals(lastVehicleUUID)) {
                originalVehicleStepHeight = vehicle.getStepHeight();
                lastVehicleUUID = vehicle.getUuid();
            }

            vehicle.setStepHeight(stepHeight.getValuefloat());

        } else if (lastVehicleUUID != null) {
            // Look for the vehicle by UUID and reset its step height
            for (Entity entity : mc.world.getEntities()) {
                if (entity.getUuid().equals(lastVehicleUUID)) {
                    entity.setStepHeight(originalVehicleStepHeight);
                    break;
                }
            }
            lastVehicleUUID = null;
        }

        else if (!mode.getMode().equals("Vanilla")) mc.player.setStepHeight(0.6f);
        else return true;
        return false;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setStepHeight(0.6f);

            if (vehicleFix.isEnabled()) {
                Entity vehicle = mc.player.getVehicle();

                if (vehicle != null) {
                    vehicle.setStepHeight(originalVehicleStepHeight);
                } else if (lastVehicleUUID != null && mc.world != null) {
                    for (Entity entity : mc.world.getEntities()) {
                        if (entity.getUuid().equals(lastVehicleUUID)) {
                            entity.setStepHeight(originalVehicleStepHeight);
                            break;
                        }
                    }
                }
            }
        }

        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }
}
