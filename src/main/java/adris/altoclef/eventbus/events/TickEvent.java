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

}
