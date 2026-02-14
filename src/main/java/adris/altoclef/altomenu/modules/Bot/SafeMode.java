package adris.altoclef.altomenu.modules.Bot;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.ModeSetting;

//todo: Implement
public class SafeMode extends Mod {
    //todo:Create List of Unsafe Modules depending on Mode (Server / AntiCheat) and Automatically Disable or Warn
    //todo:Allow Community to create their own SafeModes

    public SafeMode() {
        super("SafeMode", "Disables Certain Features to reduce ban risk", Mod.Category.BOT);
    }

    //Settings
    ModeSetting mode = new ModeSetting("Mode", "None", "None", "Matrix7", "MatrixLabs", "Vulcan", "GrimV2", "GrimV3", "NCP", "Spartan", "Verus", "Intave", "AACV3", "AACV4", "AACV5" , "MineMenClub", "MMC (Modern)" , "Hypixel", "CubeCraft", "HopLite", "StrikeAC", "Themis", "BlocksMC", "Hypixel (Strict)", "Hypixel (Skyblock)", "Ghost", "Semi-Rage");
}
