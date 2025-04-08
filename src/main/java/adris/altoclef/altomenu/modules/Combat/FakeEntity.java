package adris.altoclef.altomenu.modules.Combat;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class FakeEntity extends Mod {

    public FakeEntity() {
        super("FakeEntity", "Create client side fake entity", Category.PLAYER);
    }

    @EventHandler
    public boolean onShitTick() {

        return false;
    }

}