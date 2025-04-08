package adris.altoclef.altomenu.modules.Development;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;
import adris.altoclef.eventbus.events.TickEvent;

public class FakeRotation extends Mod {

    public FakeRotation() {
        super("FakeRotation", "FakeRotation", Category.DEVELOPMENT);
    }

    NumberSetting yaw = new NumberSetting("Yaw", 0, 360, 0, 1);
    NumberSetting pitch = new NumberSetting("Pitch", -90, 90, 0, 1);

    @Override
    public void onRender() {
        if (mc.player != null) {
            mc.player.setBodyYaw(yaw.getValuefloat());
        }
    }
}
