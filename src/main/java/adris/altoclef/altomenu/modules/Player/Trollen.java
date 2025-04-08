package adris.altoclef.altomenu.modules.Player;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;

public class Trollen extends Mod {

    private int buildStage = 0;
    private int timer = 0;
    private final int delayTicks = 0;
    private BlockPos startPos;
    private boolean hasStarted = false;

    public Trollen() {
        super("FakePlayer", "Creates a client-side clone of yourself", Category.PLAYER);
    }

    @EventHandler
    public boolean onShitTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) {
            this.toggle();
            return false;
        }

        // Check for building block in offhand
        if (mc.player.getOffHandStack().isEmpty()) {
            this.toggle();
            return false;
        }

        // Timer logic
        if (timer < delayTicks) {
            timer++;
            return false;
        }
        timer = 0;

        // Only set start position once at beginning
        if (!hasStarted) {
            Vec3d lookVec = mc.player.getRotationVector().normalize();
            startPos = new BlockPos(
                    (int) Math.floor(mc.player.getX() + lookVec.x * 2),
                    (int) Math.floor(mc.player.getY() + lookVec.y * 2),
                    (int) Math.floor(mc.player.getZ() + lookVec.z * 2)
            );
            startPos = findGroundPos(startPos);
            if (startPos == null) {
                this.toggle();
                return false;
            }
            hasStarted = true;
        }

        // Determine current target position
        BlockPos targetPos;
        switch (buildStage) {
            case 0: targetPos = startPos; break;
            case 1: targetPos = startPos.offset(getLeftDirection()); break;
            case 2: targetPos = startPos.offset(getRightDirection()); break;
            case 3: targetPos = startPos.up(); break;
            case 4: targetPos = startPos.up(2); break;
            default:
                this.toggle(); // Turn off when done
                return false;
        }

        // Face the target block
        faceBlock(targetPos);

        // Attempt to place block
        if (tryPlaceBlock(targetPos)) {
            buildStage++;
        }

        return false;
    }

    private Direction getLeftDirection() {
        return mc.player.getHorizontalFacing().rotateYCounterclockwise();
    }

    private Direction getRightDirection() {
        return mc.player.getHorizontalFacing().rotateYClockwise();
    }

    private void faceBlock(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(mc.player.getX(),
                mc.player.getEyeY(),
                mc.player.getZ());
        Vec3d targetVec = new Vec3d(
                pos.getX() + 0.5 - eyesPos.x,
                pos.getY() + 0.5 - eyesPos.y,
                pos.getZ() + 0.5 - eyesPos.z
        ).normalize();

        float yaw = (float)Math.toDegrees(Math.atan2(targetVec.z, targetVec.x)) - 90;
        float pitch = (float)-Math.toDegrees(Math.asin(targetVec.y));

        mc.player.setYaw(MathHelper.wrapDegrees(yaw));
        mc.player.setPitch(MathHelper.wrapDegrees(pitch));
    }

    private BlockPos findGroundPos(BlockPos startPos) {
        for (int y = startPos.getY(); y > mc.world.getBottomY(); y--) {
            BlockPos checkPos = new BlockPos(startPos.getX(), y, startPos.getZ());
            if (!mc.world.getBlockState(checkPos).isAir()) {
                return checkPos.up();
            }
        }
        return null;
    }

    private boolean tryPlaceBlock(BlockPos pos) {
        if (!mc.world.getBlockState(pos).isAir()) {
            return true; // Consider already placed as success
        }

        for (Direction dir : Direction.values()) {
            BlockPos adjacent = pos.offset(dir);
            if (!mc.world.getBlockState(adjacent).isAir()) {
                Vec3d hitVec = new Vec3d(
                        pos.getX() + 0.5 - dir.getOffsetX() * 0.5,
                        pos.getY() + 0.5 - dir.getOffsetY() * 0.5,
                        pos.getZ() + 0.5 - dir.getOffsetZ() * 0.5
                );

                mc.interactionManager.interactBlock(
                        mc.player,
                        Hand.OFF_HAND,
                        new BlockHitResult(hitVec, dir.getOpposite(), adjacent, false)
                );
                mc.player.swingHand(Hand.OFF_HAND);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDisable() {
        hasStarted = false;
        buildStage = 0;
        timer = 0;
    }
}