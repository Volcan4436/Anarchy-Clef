package adris.altoclef.altomenu.modules.Development;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.util.math.BlockPos;

public class PistonMove extends Mod {
    public static PistonMove Instance;

    public PistonMove() {
        super("Jesus", "Spins you like a dunce.", Mod.Category.DEVELOPMENT);
        Instance = this;
    }
    BlockPos targetPos = new BlockPos(-98, 81, -645);
    BlockPos playerPos = mc.player.getBlockPos();
    @EventHandler
    public boolean onShitTick() {
        assert mc.player != null;
        
        return false;
    }
}