package adris.altoclef.altomenu.modules.Player;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.eventbus.EventHandler;

public class AntiHunger extends Mod {

    public AntiHunger() {
        super("AntiHunger", "Prevents hunger from decreasing.", Mod.Category.PLAYER);
    }

    BooleanSetting spoof = new BooleanSetting("Spoof", true);


    @EventHandler
    public boolean onShitTick() {
        if (mc.player == null) return true;
        else if (spoof.isEnabled() && mc.player.getHungerManager().getFoodLevel() != 20) {
            mc.player.getHungerManager().setFoodLevel(20);
        }

        return false;
    }
}
