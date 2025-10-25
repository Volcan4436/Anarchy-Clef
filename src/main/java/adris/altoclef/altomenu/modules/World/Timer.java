package adris.altoclef.altomenu.modules.World;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.modules.Render.Ambience;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;

public class Timer extends Mod {
    public static Timer INSTANCE = new Timer();

    public Timer() {
        super("Timer", "Displays the current time", Mod.Category.WORLD);
        INSTANCE = this;
    }

    public static NumberSetting speed = new NumberSetting("Speed", 0.1, 20, 1, 0.1);}
