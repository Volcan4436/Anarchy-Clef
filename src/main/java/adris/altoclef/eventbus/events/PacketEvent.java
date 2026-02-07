package adris.altoclef.eventbus.events;

import net.minecraft.network.packet.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PacketEvent extends Cancellable {

    public Packet<?> packet;
    public Direction direction;

    public enum Direction { SEND, RECEIVE }

    public PacketEvent(Packet<?> packet, Direction direction) {
        this.packet = packet;
        this.direction = direction;
    }

    // Global packet listeners
    private static final List<Consumer<PacketEvent>> GLOBAL_LISTENERS = new ArrayList<>();

    public static void addGlobalListener(Consumer<PacketEvent> listener) {
        GLOBAL_LISTENERS.add(listener);
    }

    public static void removeGlobalListener(Consumer<PacketEvent> listener) {
        GLOBAL_LISTENERS.remove(listener);
    }

    public void callGlobal() {
        for (Consumer<PacketEvent> l : GLOBAL_LISTENERS) {
            l.accept(this);
        }
    }
}