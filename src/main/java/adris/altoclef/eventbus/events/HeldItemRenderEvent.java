package adris.altoclef.eventbus.events;

import adris.altoclef.eventbus.Event;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;

public class HeldItemRenderEvent extends Event {

    private static final HeldItemRenderEvent INSTANCE = new HeldItemRenderEvent();

    public Hand hand;
    public MatrixStack matrix;

    public static HeldItemRenderEvent get(Hand hand, MatrixStack matrices) {
        INSTANCE.hand = hand;
        INSTANCE.matrix = matrices;
        return INSTANCE;
    }

}
