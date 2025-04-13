package adris.altoclef.eventbus.events;

public interface ICancellable {
    void setCancelled(boolean cancelled);

    default void cancel() {
        setCancelled(true);
    }

    boolean isCancelled();
}
