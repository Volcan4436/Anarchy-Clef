package adris.altoclef.altomenu.command;

public class HUDSettings {

    //Instance
    public static HUDSettings INSTANCE = new HUDSettings();

    private static boolean toggleHUD = true;

    public static boolean isToggleHUD() {
        return toggleHUD;
    }

    public static void toggleHUD() {
        toggleHUD = !toggleHUD;
    }

    private static boolean toggleDebug = false;

    public static boolean istoggleDebug() {
        return toggleDebug;
    }

    public static void toggleDebug() {
        toggleDebug = !toggleDebug;
    }
}
