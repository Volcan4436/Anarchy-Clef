package adris.altoclef.altomenu.modules.settings.Movement;

import adris.altoclef.altomenu.Mod;

public class Flight extends Mod {
    public Flight() {
        super("Flight", "Fly", Mod.Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player.isCreative() || mc.player.isSpectator()) return;
        else mc.player.getAbilities().allowFlying = true;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.player.isCreative() || mc.player.isSpectator()) return;
        else mc.player.getAbilities().allowFlying = false;
        super.onDisable();
    }
}