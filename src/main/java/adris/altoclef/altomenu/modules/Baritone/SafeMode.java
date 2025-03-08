package adris.altoclef.altomenu.modules.Baritone;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.ModeSetting;

public class SafeMode extends Mod {
    //TODO Create List of Unsafe Modules depending on Mode (Server / AntiCheat) and Automatically Disable or Warn

    public SafeMode() {
        super("SafeMode", "Disables Certain Features to reduce ban risk", Mod.Category.BARITONE);
    }

    //Settings
    ModeSetting mode = new ModeSetting("Mode", "None", "None", "Matrix7", "MatrixLabs", "Vulcan", "GrimV2", "GrimV3", "NCP", "Spartan", "Verus", "Intave", "AACV3", "AACV4", "AACV5" , "MineMenClub" , "Hypixel", "CubeCraft", "HopLite");
}
