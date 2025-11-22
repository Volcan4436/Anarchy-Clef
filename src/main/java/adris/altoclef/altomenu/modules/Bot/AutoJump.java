package adris.altoclef.altomenu.modules.Bot;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.CMoveUtil;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.eventbus.EventHandler;

public class AutoJump extends Mod {

    public AutoJump() {
        super("AutoJump", "AutoJump", Mod.Category.BOT);
    }

    BooleanSetting onMove = new BooleanSetting("onMove", false);
    BooleanSetting inputFix = new BooleanSetting("InputFix", true);

    //todo
    // Make this Cleaner
    // add Smart Mode (Better Version of the Vanilla AutoJump)
    @EventHandler
    public boolean onShitTick() {
        if (mc.player == null) return true;
        if (inputFix.isEnabled() && mc.player.isOnGround()) mc.options.jumpKey.setPressed(false);
        if (CMoveUtil.isMoving() && CMoveUtil.isOnGround() && onMove.isEnabled()) mc.player.jump();
        else if (CMoveUtil.isOnGround() && !onMove.isEnabled()) mc.player.jump();
        return false;
    }
}
