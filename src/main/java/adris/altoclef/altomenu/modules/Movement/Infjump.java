package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.CMoveUtil;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

import java.util.Objects;

// todo: add a JumpHeight Modifier Module that works well with this
//  add: Height Limiter
//  add: Keep Height (auto jumps to keep you above a certain height)
public class Infjump extends Mod {

    public Infjump() {
        super("InfJump", "Jump Forever!", Mod.Category.MOVEMENT);
    }

    BooleanSetting onBlockHold = new BooleanSetting("Require Held Block", false);
    BooleanSetting onMove = new BooleanSetting("onMove", false);
    ModeSetting mode = new ModeSetting("Mode", "Hold", "Hold", "Toggle");

    private boolean toggled = false;

    @EventHandler
    public boolean onShitTick() {
        if (mc.player == null) return false;
        if (mc.options.jumpKey.wasPressed()) toggled = !toggled;
        if (onMove.isEnabled() && !CMoveUtil.isMoving()) return false;
        if (onBlockHold.isEnabled()) {
            ItemStack stack = mc.player.getMainHandStack();
            if (!(stack.getItem() instanceof BlockItem)) return false;
        }
        if (mode.getMode().equals("Hold")) {
            if (mc.options.jumpKey.isPressed()) mc.player.jump();
        }
        else if (mode.getMode().equals("Toggle")) {
            if (toggled) mc.player.jump();
            else return false;
        }
        return false;
    }
}