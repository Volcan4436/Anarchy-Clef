package adris.altoclef.altomenu.modules.Combat;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CrystalPlace extends Mod {

    // Timing Settings
    private final BooleanSetting boost = new BooleanSetting("Boost", true);
    private final BooleanSetting antiPing = new BooleanSetting("AntiPing", true);
    private final BooleanSetting sequential = new BooleanSetting("Sequential", true);
    private final BooleanSetting strictSequential = new BooleanSetting("StrictSeq", true);

    // Behavior Settings
    private final BooleanSetting antiDeath = new BooleanSetting("AntiDeath", true);
    private final BooleanSetting everyEntity = new BooleanSetting("EveryEntity", true);
    private final BooleanSetting packetAttack = new BooleanSetting("PacketAttack", true);
    private final BooleanSetting instantBreak = new BooleanSetting("InstantBreak", true);
    private final BooleanSetting outline = new BooleanSetting("Outline", false);

    // Distance Settings
    private final NumberSetting speed = new NumberSetting("Speed", 0, 20, 4, 0.5);
    private final NumberSetting entityDistance = new NumberSetting("EntityRange", 1, 14, 6, 0.5);
    private final NumberSetting placeDistance = new NumberSetting("PlaceRange", 1, 6, 4.5, 0.5);
    private final NumberSetting breakDistance = new NumberSetting("BreakRange", 1, 6, 5, 0.5);

    private int placeTimer = 0;
    private int breakTimer = 0;
    private boolean wasPlacing = false;
    private long lastPacketTime = 0;

    public CrystalPlace() {
        super("CrystalAura", "Advanced crystal PvP module", Category.COMBAT);
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    private void handlePacket(Packet<ClientPlayPacketListener> packet) {
        if (packet instanceof EntityStatusS2CPacket) {
            EntityStatusS2CPacket statusPacket = (EntityStatusS2CPacket) packet;
            if (statusPacket.getStatus() == 35) { // Crystal break status
                breakTimer = 0;
            }
        }
    }

    @Override
    public boolean onShitTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return false;

        Entity target = everyEntity.isEnabled() ?
                findClosestEntity(entityDistance.getValue()) :
                findClosestPlayer(entityDistance.getValue());
        if (target == null) return false;

        if (sequential.isEnabled()) {
            return handleSequentialMode(target);
        } else {
            return handleVanillaMode(target);
        }
    }

    private boolean handleSequentialMode(Entity target) {
        boolean actionTaken = false;

        if (strictSequential.isEnabled() && wasPlacing) {
            if (tryBreakCrystal()) {
                wasPlacing = false;
                return true;
            }
        }

        if ((!strictSequential.isEnabled() || !wasPlacing) && placeTimer <= 0) {
            if (tryPlaceCrystal(target)) {
                wasPlacing = true;
                placeTimer = getActionDelay(true);
                actionTaken = true;
            }
        }

        if ((!strictSequential.isEnabled() || wasPlacing) && breakTimer <= 0) {
            if (tryBreakCrystal()) {
                wasPlacing = false;
                breakTimer = getActionDelay(false);
                actionTaken = true;
            }
        }

        if (placeTimer > 0) placeTimer--;
        if (breakTimer > 0) breakTimer--;

        return actionTaken;
    }

    private boolean handleVanillaMode(Entity target) {
        if (tryBreakCrystal()) {
            return true;
        }
        return tryPlaceCrystal(target);
    }

    private boolean tryPlaceCrystal(Entity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || !hasCrystalInOffhand()) return false;

        BlockPos placePos = findPlacePosNearTarget(target);
        if (placePos == null || !canPlaceCrystal(placePos)) return false;

        placeCrystal(placePos);
        if (outline.isEnabled()) drawOutline(placePos);
        return true;
    }

    private boolean tryBreakCrystal() {
        EndCrystalEntity crystal = findClosestEndCrystal(breakDistance.getValue());
        if (crystal == null) return false;

        breakCrystal(crystal);
        return true;
    }

    private int getActionDelay(boolean isPlace) {
        int baseDelay = (int) (10 / speed.getValue());
        if (boost.isEnabled()) baseDelay /= 2;
        if (antiPing.isEnabled()) baseDelay /= 2;
        return Math.max(0, baseDelay);
    }

    private BlockPos findPlacePosNearTarget(Entity target) {
        World world = MinecraftClient.getInstance().world;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (world == null || player == null) return null;

        Vec3d targetPos = antiPing.isEnabled() ?
                getPredictedPosition(target) : target.getPos();

        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    mutablePos.set(targetPos.x + x, targetPos.y + y, targetPos.z + z);

                    if (isValidCrystalBase(mutablePos) &&
                            world.getBlockState(mutablePos.up()).isAir()) {

                        Vec3d crystalPos = new Vec3d(
                                mutablePos.getX() + 0.5,
                                mutablePos.getY() + 1.0,
                                mutablePos.getZ() + 0.5
                        );
                        double playerDist = player.squaredDistanceTo(crystalPos);
                        double score = playerDist;

                        if (score < bestScore) {
                            bestScore = score;
                            bestPos = mutablePos.toImmutable();
                        }
                    }
                }
            }
        }
        return bestPos;
    }

    private Vec3d getPredictedPosition(Entity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!antiPing.isEnabled() || mc.getNetworkHandler() == null) {
            return target.getPos();
        }

        int ping = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()).getLatency();
        Vec3d velocity = target.getVelocity();
        return target.getPos().add(velocity.multiply(ping / 1000.0));
    }

    private void placeCrystal(BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.player.networkHandler == null) return;

        Vec3d hitVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, pos, false);

        int packetsToSend = boost.isEnabled() ? 3 : (antiPing.isEnabled() ? 2 : 1);
        for (int i = 0; i < packetsToSend; i++) {
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(
                    Hand.OFF_HAND, hitResult, 0
            ));
        }
        mc.player.swingHand(Hand.OFF_HAND);
        lastPacketTime = System.currentTimeMillis();
    }

    private void breakCrystal(EndCrystalEntity crystal) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (packetAttack.isEnabled() && mc.player.networkHandler != null) {
            int packetsToSend = boost.isEnabled() ? 3 : (antiPing.isEnabled() ? 2 : 1);
            for (int i = 0; i < packetsToSend; i++) {
                mc.player.networkHandler.sendPacket(
                        PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking())
                );
            }
        } else if (mc.interactionManager != null) {
            mc.interactionManager.attackEntity(mc.player, crystal);
        }

        mc.player.swingHand(Hand.MAIN_HAND);

        if (instantBreak.isEnabled() || boost.isEnabled() || antiPing.isEnabled()) {
            crystal.discard();
        }
        lastPacketTime = System.currentTimeMillis();
    }

    private Entity findClosestEntity(double maxDistance) {
        World world = MinecraftClient.getInstance().world;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (world == null || player == null) return null;

        double maxDistSq = maxDistance * maxDistance;
        Entity closest = null;
        double closestDistSq = Double.MAX_VALUE;

        for (Entity entity : ((ClientWorld) world).getEntities()) {
            if (entity == player) continue;

            double distSq = player.squaredDistanceTo(entity);
            if (distSq < closestDistSq && distSq <= maxDistSq) {
                closestDistSq = distSq;
                closest = entity;
            }
        }
        return closest;
    }

    private PlayerEntity findClosestPlayer(double maxDistance) {
        World world = MinecraftClient.getInstance().world;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (world == null || player == null) return null;

        double maxDistSq = maxDistance * maxDistance;
        PlayerEntity closest = null;
        double closestDistSq = Double.MAX_VALUE;

        for (PlayerEntity p : world.getPlayers()) {
            if (p == player) continue;

            double distSq = player.squaredDistanceTo(p);
            if (distSq < closestDistSq && distSq <= maxDistSq) {
                closestDistSq = distSq;
                closest = p;
            }
        }
        return closest;
    }

    private EndCrystalEntity findClosestEndCrystal(double maxDistance) {
        World world = MinecraftClient.getInstance().world;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (world == null || player == null) return null;

        double maxDistSq = maxDistance * maxDistance;
        EndCrystalEntity closest = null;
        double closestDistSq = Double.MAX_VALUE;

        for (Entity entity : ((ClientWorld) world).getEntities()) {
            if (!(entity instanceof EndCrystalEntity)) continue;

            double distSq = player.squaredDistanceTo(entity);
            if (distSq < closestDistSq && distSq <= maxDistSq) {
                closestDistSq = distSq;
                closest = (EndCrystalEntity) entity;
            }
        }
        return closest;
    }

    private boolean canPlaceCrystal(BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return false;

        Vec3d playerPos = mc.player.getPos();
        Vec3d blockCenter = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        double distSq = playerPos.squaredDistanceTo(blockCenter);
        double placeDist = placeDistance.getValue();

        if (antiDeath.isEnabled() && distSq <= 9.0) {
            return false;
        }

        return distSq <= placeDist * placeDist;
    }

    private boolean isValidCrystalBase(BlockPos pos) {
        World world = MinecraftClient.getInstance().world;
        if (world == null) return false;
        BlockState state = world.getBlockState(pos);
        return state.isOf(Blocks.OBSIDIAN) || state.isOf(Blocks.BEDROCK);
    }

    private boolean hasCrystalInOffhand() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        return player != null && player.getOffHandStack().getItem() == Items.END_CRYSTAL;
    }

    private void drawOutline(BlockPos pos) {
        World world = MinecraftClient.getInstance().world;
        if (world == null) return;

        double x = pos.getX();
        double y = pos.getY() + 1.0;
        double z = pos.getZ();

        for (int i = 0; i < 10; i++) {
            double progress = i / 10.0;
            world.addParticle(ParticleTypes.TOTEM_OF_UNDYING,
                    x + 0.1 + progress * 0.8, y, z + 0.1, 0, 0.1, 0);
            world.addParticle(ParticleTypes.TOTEM_OF_UNDYING,
                    x + 0.1 + progress * 0.8, y, z + 0.9, 0, 0.1, 0);
            world.addParticle(ParticleTypes.TOTEM_OF_UNDYING,
                    x + 0.1, y, z + 0.1 + progress * 0.8, 0, 0.1, 0);
            world.addParticle(ParticleTypes.TOTEM_OF_UNDYING,
                    x + 0.9, y, z + 0.1 + progress * 0.8, 0, 0.1, 0);
        }
    }
}