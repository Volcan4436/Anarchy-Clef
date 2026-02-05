package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.managers.ModuleManager;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.ClefEventBus;
import adris.altoclef.eventbus.events.HeldItemRenderEvent;
import net.minecraft.util.Hand;

// Why did I code this like this
public class ViewModel extends Mod {

    public ViewModel() {
        super("ViewModel", "Funne", Mod.Category.RENDER);
        ClefEventBus.subscribe(HeldItemRenderEvent.class, this::onHeldItemRender);
    }

    public static final NumberSetting mainX = new NumberSetting("mainX", -10, 10, 0.4, 0.1);
    public static final NumberSetting mainY = new NumberSetting("mainY", -10, 10, -0.2, 0.1);
    public static final NumberSetting mainZ = new NumberSetting("mainZ", -10, 10, -0.4, 0.1);
    //public static final NumberSetting mainRotX = new NumberSetting("mainRotX", -10, 10, 0, 0.1);
    //public static final NumberSetting mainRotY = new NumberSetting("mainRotY", -10, 10, 0, 0.1);
    //public static final NumberSetting mainRotZ = new NumberSetting("mainRotZ", -10, 10, 0, 0.1);
    //public static final NumberSetting mainScaleX = new NumberSetting("mainScaleX", -10, 10, 1, 0.1);
    //public static final NumberSetting mainScaleY = new NumberSetting("mainScaleY", -10, 10, 1, 0.1);
    //public static final NumberSetting mainScaleZ = new NumberSetting("mainScaleZ", -10, 10, 1, 0.1);

    public static final NumberSetting offX = new NumberSetting("offX", -10, 10, -0.4, 0.1);
    public static final NumberSetting offY = new NumberSetting("offY", -10, 10, -0.2, 0.1);
    public static final NumberSetting offZ = new NumberSetting("offZ", -10, 10, -0.4, 0.1);
    //public static final NumberSetting offRotX = new NumberSetting("offRotX", -10, 10, 0, 0.1);
    //public static final NumberSetting offRotY = new NumberSetting("offRotY", -10, 10, 0, 0.1);
    //public static final NumberSetting offRotZ = new NumberSetting("offRotZ", -10, 10, 0, 0.1);
    //public static final NumberSetting offScaleX = new NumberSetting("offScaleX", -10, 10, 1, 0.1);
    //public static final NumberSetting offScaleY = new NumberSetting("offScaleY", -10, 10, 1, 0.1);
    //public static final NumberSetting offScaleZ = new NumberSetting("offScaleZ", -10, 10, 1, 0.1);

    public void onHeldItemRender(HeldItemRenderEvent event) {
        if (ModuleManager.INSTANCE.getModuleByClass(ViewModel.class).isEnabled()) {
            if (event.hand == Hand.MAIN_HAND) {
                event.matrix.translate(mainX.getValue(), mainY.getValue(), mainZ.getValue());
/*                event.matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mainRotX.getValuefloat()));
                event.matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(mainRotY.getValuefloat()));
                event.matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(mainRotZ.getValuefloat()));*/
                /*            event.matrix.scale(mainScaleX.getValuefloat(), mainScaleY.getValuefloat(), mainScaleZ.getValuefloat());*/
            } else if (event.hand == Hand.OFF_HAND) {
                float offsetX = offX.getValuefloat() - mainX.getValuefloat();
                float offsetY = offY.getValuefloat() - mainY.getValuefloat();
                float offsetZ = offZ.getValuefloat() - mainZ.getValuefloat();
/*                float offsetRotX = offRotX.getValuefloat() - mainRotX.getValuefloat();
                float offsetRotY = offRotY.getValuefloat() - mainRotY.getValuefloat();
                float offsetRotZ = offRotZ.getValuefloat() - mainRotZ.getValuefloat();*/
/*            float offsetScaleX = offScaleX.getValuefloat() * mainScaleX.getValuefloat();
            float offsetScaleY = offScaleY.getValuefloat() * mainScaleY.getValuefloat();
            float offsetScaleZ = offScaleZ.getValuefloat() * mainScaleZ.getValuefloat();*/

                event.matrix.translate(offsetX, offsetY, offsetZ);
/*                event.matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(offsetRotX));
                event.matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(offsetRotY));
                event.matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(offsetRotZ));*/
                /*            event.matrix.scale(offsetScaleX, offsetScaleY, offsetScaleZ);*/
            }
        }
    }
}
