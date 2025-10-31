package adris.altoclef.altomenu.modules.Movement;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.item.ItemStack;
import net.minecraft.item.BlockItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.Hand;

public class Scaffold extends Mod {

    public Scaffold() {
        super("Scaffold", "Places blocks in front of you to prevent falling", Category.MOVEMENT);
    }

    ModeSetting modeSetting = new ModeSetting("Mode", "Normal", "Normal", "Rusher");
    NumberSetting placeDelay = new NumberSetting("Place Delay", 0.05, 1.0, 0.1, 0.01);
    BooleanSetting swingHand = new BooleanSetting("Swing Main Hand", true);

    private int tickCounter = 0;

    @EventHandler
    public boolean onShitTick() {
        tickCounter++;
        if (tickCounter < (int)(placeDelay.getValue() * 20)) return false;
        tickCounter = 0;

        if (mc.player == null || mc.world == null) return false;

        ItemStack stack = mc.player.getMainHandStack();
        if (!(stack.getItem() instanceof BlockItem)) return false;

        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos below = playerPos.down();

        // Determine movement offset to place slightly ahead
        Vec3d velocity = mc.player.getVelocity();
        double speed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);

        int offsetX = 0;
        int offsetZ = 0;

        if (Math.abs(velocity.x) > Math.abs(velocity.z)) {
            offsetX = velocity.x > 0 ? 1 : velocity.x < 0 ? -1 : 0;
        } else if (Math.abs(velocity.z) > 0) {
            offsetZ = velocity.z > 0 ? 1 : velocity.z < 0 ? -1 : 0;
        }

        // Apply extra offset proportional to speed (max 1 block)
        if (speed > 0.1) {
            if (offsetX != 0) offsetX = 1 * (offsetX > 0 ? 1 : -1);
            if (offsetZ != 0) offsetZ = 1 * (offsetZ > 0 ? 1 : -1);
        }

        BlockPos targetPos = below.add(offsetX, 0, offsetZ);

        // Rusher mode: if jumping, place 1 block lower
        if (modeSetting.getMode().equals("Rusher") && !mc.player.isOnGround()) {
            targetPos = targetPos.down();
        }

        // Only place if block is air and attachable
        if (mc.world.getBlockState(targetPos).isAir()) {
            boolean attachable = false;
            for (Direction dir : Direction.values()) {
                if (!mc.world.getBlockState(targetPos.offset(dir)).isAir()) {
                    attachable = true;
                    break;
                }
            }
            if (attachable) {
                placeBlock(targetPos);
            }
        }

        return true;
    }

    private void placeBlock(BlockPos pos) {
        Vec3d hitVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        BlockHitResult blockHit = new BlockHitResult(hitVec, Direction.UP, pos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHit);

        // Optionally swing main hand
        if (swingHand.isEnabled()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    @Override
    public void onDisable() {
        tickCounter = 0;
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
    }
}
