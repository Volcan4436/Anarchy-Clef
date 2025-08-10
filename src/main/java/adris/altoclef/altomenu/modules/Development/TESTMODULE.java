package adris.altoclef.altomenu.modules.Development;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class TESTMODULE extends Mod {

    public TESTMODULE() {
        super("TESTMODULE", "TEST MODULE", Category.DEVELOPMENT);
    }
    // In modules, Specifically in onShitTick, you should use mc to access minecraft's internal code.
    // Example: mc.player.getPos(); or mc.world.getBlockState(pos).getMaterial();. Don't define MinecraftClient mc = MinecraftClient.getInstance(); or anything because its already defined somewhere in the client.
    BooleanSetting booleanSetting = new BooleanSetting("NAME OF SETTING", false);
    // This is a BooleanSetting, which can be turned on or off.
    // Boolean Settings work with 1st the name of the setting, and 2nd the default value.
    ModeSetting modeSetting = new ModeSetting("NAME OF SETTING", "DEFAULT MODE", "DEFAULT 1ST MODE", "2ND MODE", "3RD MODE", "4TH MODE");
    // This is a ModeSetting, which allows you to switch between multiple modes.
    // Mode Settings work with 1st the name of the setting, 2nd the default mode, 3rd the 1st mode, 4th the 2nd mode, 5th the 3rd mode, and 6th the 4th mode.
    NumberSetting numberSetting = new NumberSetting("NAME OF SETTING", 0.1, 10, 1, 0.1);
    // This is a NumberSetting, which allows you to set a number between a min and max value.
    // Number Settings work with 1st the minimum value, 2nd the maximum value, 3rd the default value, and 4th the increment/decrement value.

    // OnShitTick is called every tick when the module is toggled on
    // Don't replace with onTick or onPostTick, OnShitTick is normal. An inside joke if you will.
    // Replacing with onTick or onPostTick will break the module and cause issues.
    // Also, Don't replace public boolean for public void. also breaks module.
    @EventHandler
    public boolean onShitTick() {

        return false;
    }

    // onDisable is called when the module is toggled off
    @Override
    public void onDisable() {

    }

    // onEnable is called when the module is toggled on
    @Override
    public void onEnable() {

    }
}