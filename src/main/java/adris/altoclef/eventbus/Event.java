package adris.altoclef.eventbus;

public abstract class Event {
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

