package adris.altoclef.altomenu.cheatUtils;


public final class RenderCallGate {
    private static final ThreadLocal<Boolean> IN_RENDER = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private RenderCallGate() {}

    public static void enterRender() {
        IN_RENDER.set(Boolean.TRUE);
    }

    public static void exitRender() {
        IN_RENDER.set(Boolean.FALSE);
    }

    public static boolean isInRender() {
        return IN_RENDER.get();
    }
}
