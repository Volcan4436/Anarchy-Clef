package adris.altoclef.altomenu.modules.Baritone;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.ModeSetting;

public class SafeMode extends Mod {
    //TODO Create List of Unsafe Modules depending on Mode (Server / AntiCheat) and Automatically Disable or Warn

    public SafeMode() {
        super("SafeMode", "Disables Certain Features to reduce ban risk", Mod.Category.BARITONE);
        addSettings(mode);
    }

    //Settings
    ModeSetting mode = new ModeSetting("Mode", "None", "None", "Matrix", "Vulcan", "Grim", "NCP", "Spartan", "Verus", "Intave", "MineMenClub" , "Hypixel", "CubeCraft", "HopLite");
}
