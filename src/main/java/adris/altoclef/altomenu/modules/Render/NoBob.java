package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.CMoveUtil;
import adris.altoclef.altomenu.managers.ModuleManager;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.EventHandler;
import adris.altoclef.eventbus.events.HeldItemRenderEvent;
import adris.altoclef.eventbus.events.TickEvent;

public class NoBob extends Mod {

    private float translation = 0.0f;
    private static final float TARGET_TRANSLATE = -0.2f;
    private static final float SMOOTHING_SPEED = 0.02f; //change this value to change the speed of the bob

    public NoBob() {
        super("NoBob", "Funne", Mod.Category.RENDER);
        EventBus.subscribe(HeldItemRenderEvent.class, this::onHeldItemRender);
    }

    public BooleanSetting cool = new BooleanSetting("Cool", true);


    //todo: clean up math
    public void onHeldItemRender(HeldItemRenderEvent event) {
        if (ModuleManager.INSTANCE.getModuleByClass(NoBob.class).isEnabled()) {
            if (cool.isEnabled()) {
                if (CMoveUtil.isMoving() && mc.player.isOnGround()) {
                    translation = lerp(translation, TARGET_TRANSLATE, SMOOTHING_SPEED);
                } else {
                    translation = lerp(translation, 0.0f, SMOOTHING_SPEED);
                }
                event.matrix.translate(0, translation, 0);
            }
        }
    }

    private float lerp(float current, float target, float speed) {
        return current + (target - current) * speed;
    }
}
