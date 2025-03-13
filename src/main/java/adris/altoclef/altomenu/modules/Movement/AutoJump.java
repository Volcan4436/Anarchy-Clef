package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.CMoveUtil;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.eventbus.EventHandler;

public class AutoJump extends Mod {

    public AutoJump() {
        super("AutoJump", "AutoJump", Mod.Category.MOVEMENT);
    }

    BooleanSetting onMove = new BooleanSetting("onMove", false);

    //todo
    // Make this Cleaner
    @EventHandler
    public boolean onShitTick() {
        if (CMoveUtil.isMoving() && CMoveUtil.isOnGround() && onMove.isEnabled()) mc.player.jump();
        else if (CMoveUtil.isOnGround()) mc.player.jump();
        return false;
    }
}
