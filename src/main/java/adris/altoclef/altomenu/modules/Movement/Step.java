package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.NumberSetting;

// todo:
//  add modes: Matrix, NCP, Position, Velocity, Vanilla, Tick (Similar to TickBase), Frame (Take a few Ticks to Complete the Step by lagging), Jump (Similar to AutoJump Setting but Better), PacketAbuse (Jitter your way upwards), RubberBand (Fake a LagBack every few steps then teleport back where you are stepping to), Blink
public class Step extends Mod {

    private float originalStepHeight;

    public Step() {
        super("Step", "Step", Mod.Category.MOVEMENT);
    }

    NumberSetting stepHeight = new NumberSetting("Height", 0.1, 10, 1, 0.1);

    @Override
    public boolean onShitTick() {
        if (mc.player.getStepHeight() != stepHeight.getValuefloat()) mc.player.setStepHeight(stepHeight.getValuefloat());
        else return true;
        return false;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.setStepHeight(originalStepHeight);
        super.onDisable();
    }

    @Override
    public void onEnable() {
        originalStepHeight = mc.player.getStepHeight();
        System.out.println("Step Height: " + mc.player.getStepHeight());
        super.onEnable();
    }
}
