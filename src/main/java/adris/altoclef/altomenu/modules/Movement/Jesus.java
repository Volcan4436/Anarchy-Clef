package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.block.Blocks;

//todo
// Add Bypass for UpdatedNoCheatPlus, Solid Mode (Collision) and DepthStriderSpoof (With EnchantLevel Option)
public class Jesus extends Mod {

    boolean isInsideWater = false;
    boolean isAboveWater = false;

    public Jesus() {
        super("Jesus", "Allows you to walk on water / Interact with water", Mod.Category.MOVEMENT);
        addSettings(mode);
    }

    //ModeSetting
    ModeSetting mode = new ModeSetting("Mode", "Trampoline", "Trampoline", "dev");

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }



    @EventHandler
    public boolean onShitTick() {
        //Check State
        isInsideWater();
        isAboveWater();

        //Modes
        if (mode.getMode() == "Trampoline") {
            if (isInsideWater) {
                mc.player.setVelocity(mc.player.getVelocity().x, 0.75, mc.player.getVelocity().z);
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
