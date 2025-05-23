package adris.altoclef.eventbus.events;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;

public class TickEvent {
    public static class Pre extends TickEvent {
        private static final Pre INSTANCE = new Pre();

        public static Pre get() {
            return INSTANCE;
        }
    }

    public static class Post extends TickEvent {
        private static final Post INSTANCE = new Post();

        public static Post get() {
            return INSTANCE;
        }
    }


    private static final TickEvent INSTANCE = new TickEvent();

    public Hand hand;
    public MatrixStack matrix;

    public static TickEvent get(Hand hand, MatrixStack matrices) {
        INSTANCE.hand = hand;
        INSTANCE.matrix = matrices;
        return INSTANCE;
    }

    private boolean cancelled = false;


    public boolean isCancelled()
    {
        return cancelled;
    }

    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void cancel() { this.setCancelled(true); }
}
