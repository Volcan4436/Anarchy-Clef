package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.eventbus.EventHandler;

// todo: add a JumpHeight Modifier Module that works well with this
//  add: Height Limiter
//  add: Keep Height (auto jumps to keep you above a certain height)
public class Infjump extends Mod {
    public Infjump() {
        super("InfJump", "Jump Forever!", Mod.Category.MOVEMENT);
    }
    
    @EventHandler
    public boolean onShitTick() {
        if (mc.options.jumpKey.wasPressed()) {
            mc.player.jump();
        }
        return false;
    }
}