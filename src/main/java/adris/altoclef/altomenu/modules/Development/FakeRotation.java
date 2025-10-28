package adris.altoclef.altomenu.modules.Development;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.NumberSetting;

public class FakeRotation extends Mod {

    public static FakeRotation INSTANCE = new FakeRotation();


    public FakeRotation() {
        super("FakeRotation", "FakeRotation", Category.DEVELOPMENT);
        INSTANCE = this;
    }

    public static NumberSetting bodyyaw = new NumberSetting("Yaw (body)", 0, 360, 0, 1);
    public static NumberSetting headpitch = new NumberSetting("Pitch (head)", -90, 90, 0, 1);
    public static NumberSetting headyaw = new NumberSetting("Yaw (head)", 0, 360, 0, 1);

    @Override
    public void onRender() {
        if (mc.player != null) {
            mc.player.setBodyYaw(bodyyaw.getValuefloat()); //Currently Works as Intended
        }
        //todo: add option to set pitch silently (Currently changes your actual camera)
    }
}
