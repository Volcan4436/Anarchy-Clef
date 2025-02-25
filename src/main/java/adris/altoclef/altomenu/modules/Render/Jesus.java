package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.util.math.BlockPos;

public class Jesus extends Mod {
    public static Jesus Instance;

    public Jesus() {
        super("Jesus", "Spins you like a dunce.", Mod.Category.DEVELOPMENT);
        Instance = this;
    }
    BlockPos targetPos = new BlockPos(-98, 81, -645);
    BlockPos playerPos = mc.player.getBlockPos();
    @EventHandler
    public boolean onShitTick() {
        assert mc.player != null;
        if (mc.player.isTouchingWater()) {
            mc.player.setVelocity(mc.player.getVelocity().x,0.7,mc.player.getVelocity().z);
        }
        return false;
    }
}