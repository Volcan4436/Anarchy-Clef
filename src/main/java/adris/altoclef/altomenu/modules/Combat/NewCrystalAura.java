package adris.altoclef.altomenu.modules.Combat;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class NewCrystalAura extends Mod {

    BooleanSetting AntiDeath = new BooleanSetting("AntiDeath", true);
    BooleanSetting Rotate = new BooleanSetting("Rotate", false);
    BooleanSetting EveryEntity = new BooleanSetting("EveryEntity", true);
    BooleanSetting PacketAttack = new BooleanSetting("PacketAttack", false);
    BooleanSetting InstantBreak = new BooleanSetting("InstantBreak", false);
    BooleanSetting SwingTolerance = new BooleanSetting("SwingTolerance", true);
    BooleanSetting Outline = new BooleanSetting("Outline", false);
    BooleanSetting MultiPlace = new BooleanSetting("MultiPlace", false);
    BooleanSetting MultiBreak = new BooleanSetting("MultiBreak", false);
    BooleanSetting SmartTargeting = new BooleanSetting("SmartTargeting", false);

    NumberSetting Speed = new NumberSetting("Speed", 0, 20, 4, 1);
    NumberSetting EntityDistance = new NumberSetting("EntityDistance", 1, 8, 5, 1);
    NumberSetting BreakDistance = new NumberSetting("BreakDistance", 1, 8, 5, 1);
    NumberSetting PlaceDistance = new NumberSetting("PlaceDistance", 1, 8, 5, 1);

    private int timer = 0;
    private boolean shouldPlace = true;
    private BlockPos lastPlacePos = null;
    private EndCrystalEntity lastCrystal = null;

    public NewCrystalAura() {
        super("CrystalAura++", "Optimized Crystal Aura", Category.COMBAT);
        addSettings(AntiDeath, Rotate, EveryEntity, PacketAttack, InstantBreak,
                SwingTolerance, Outline, MultiPlace, MultiBreak, SmartTargeting,
                Speed, EntityDistance, BreakDistance, PlaceDistance);
    }

    @Override
    public void onRender() {
        // Optional visual stuff
    }

    @Override
    public boolean onShitTick() {
        if (mc.player == null || mc.world == null) return false;

        timer++;
        if (timer < Speed.getValue()) return false;
        timer = 0;

        EndCrystalEntity crystal = findOptimalCrystal();
        if (crystal != null) {
            breakCrystal(crystal);
            shouldPlace = true;
            return true;
        }

        if (shouldPlace && hasCrystalInOffhand()) {
            BlockPos placePos = findOptimalPlacePos();
            if (placePos != null) {
                placeCrystal(placePos);
                shouldPlace = !MultiPlace.isEnabled();
                return true;
            }
        } else {
            shouldPlace = true;
        }

        return false;
    }

    private EndCrystalEntity findOptimalCrystal() {
        double maxDistSq = BreakDistance.getValue() * BreakDistance.getValue();
        EndCrystalEntity bestCrystal = null;
        double bestScore = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity)) continue;

            double distSq = mc.player.squaredDistanceTo(entity);
            if (distSq > maxDistSq) continue;

            double score = distSq * (entity == lastCrystal ? 0.9 : 1.0);
            if (score < bestScore) {
                bestScore = score;
                bestCrystal = (EndCrystalEntity) entity;
            }
        }

        lastCrystal = bestCrystal;
        return bestCrystal;
    }

    private BlockPos findOptimalPlacePos() {
        Entity target = EveryEntity.isEnabled() ?
                findClosestEntity(EntityDistance.getValue()) :
                findClosestPlayer(EntityDistance.getValue());
        if (target == null) return null;

        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;
        double maxDistSq = PlaceDistance.getValue() * PlaceDistance.getValue();
        Vec3d targetPos = target.getPos();

        int range = PlaceDistance.getValueInt();
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    mutablePos.set(targetPos.x + x, targetPos.y + y, targetPos.z + z);

                    if (!isValidCrystalBase(mutablePos)) continue;
                    if (!mc.world.getBlockState(mutablePos.up()).isAir()) continue;
                    if (AntiDeath.isEnabled() && isInDeathZone(mutablePos)) continue;

                    double distSq = targetPos.squaredDistanceTo(Vec3d.ofCenter(mutablePos));
                    if (distSq > maxDistSq) continue;

                    double score = distSq * (mutablePos.equals(lastPlacePos) ? 0.9 : 1.0);
                    if (score < bestScore) {
                        bestScore = score;
                        bestPos = mutablePos.toImmutable();
                    }
                }
            }
        }

        lastPlacePos = bestPos;
        return bestPos;
    }

    private void breakCrystal(EndCrystalEntity crystal) {
        if (SwingTolerance.isEnabled() && mc.player.getAttackCooldownProgress(0) < 1.0f) {
            return;
        }

        if (PacketAttack.isEnabled()) {
            mc.player.networkHandler.sendPacket(
                    PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking())
            );
        } else {
            mc.interactionManager.attackEntity(mc.player, crystal);
        }

        mc.player.swingHand(Hand.MAIN_HAND);
        if (InstantBreak.isEnabled()) {
            crystal.discard();
        }

        if (Outline.isEnabled()) {
            drawOutline(crystal.getBlockPos());
        }
    }

    private void placeCrystal(BlockPos pos) {
        // Hit vector must simulate a real player click (center of top of block)
        Vec3d hitVec = Vec3d.ofCenter(pos).add(0, 0.5, 0);
        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, pos, false);

        mc.player.networkHandler.sendPacket(
                new PlayerInteractBlockC2SPacket(Hand.OFF_HAND, hitResult, 0)
        );
        mc.player.swingHand(Hand.OFF_HAND);

        if (Outline.isEnabled()) {
            drawOutline(pos);
        }
    }

    private void drawOutline(BlockPos pos) {
        Vec3d center = Vec3d.ofCenter(pos);
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * 2 * (i / 8.0);
            double x = center.x + 0.5 * Math.cos(angle);
            double z = center.z + 0.5 * Math.sin(angle);
            mc.world.addParticle(ParticleTypes.FLAME, x, center.y + 1, z, 0, 0.05, 0);
        }
    }

    private Entity findClosestEntity(double maxDistance) {
        double maxDistSq = maxDistance * maxDistance;
        Entity closest = null;
        double closestDistSq = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;

            double distSq = mc.player.squaredDistanceTo(entity);
            if (distSq < closestDistSq && distSq <= maxDistSq) {
                closestDistSq = distSq;
                closest = entity;
            }
        }
        return closest;
    }

    private PlayerEntity findClosestPlayer(double maxDistance) {
        double maxDistSq = maxDistance * maxDistance;
        PlayerEntity closest = null;
        double closestDistSq = Double.MAX_VALUE;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;

            double distSq = mc.player.squaredDistanceTo(player);
            if (distSq < closestDistSq && distSq <= maxDistSq) {
                closestDistSq = distSq;
                closest = player;
            }
        }
        return closest;
    }

    private boolean isValidCrystalBase(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        return state.isOf(Blocks.OBSIDIAN) || state.isOf(Blocks.BEDROCK);
    }

    private boolean isInDeathZone(BlockPos pos) {
        Vec3d playerPos = mc.player.getPos();
        Vec3d blockCenter = Vec3d.ofCenter(pos);
        return playerPos.squaredDistanceTo(blockCenter) <= 9.0;
    }

    private boolean hasCrystalInOffhand() {
        return mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
    }
}
