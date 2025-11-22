package adris.altoclef.altomenu.modules.Development;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrystalAura extends Mod {

    private static final int MAX_RANGE = 6;

    ModeSetting targetMode = new ModeSetting("TargetMode", "Player", "Player", "Hostiles", "Passive", "All");
    NumberSetting placeRange = new NumberSetting("PlaceRange", 1, MAX_RANGE, 5, 1);
    NumberSetting breakRange = new NumberSetting("BreakRange", 1, MAX_RANGE, 5, 1);
    NumberSetting speed = new NumberSetting("Speed", 0, 20, 10, 1);
    BooleanSetting safe = new BooleanSetting("Safe", true);
    ModeSetting sequential = new ModeSetting("Sequential", "Vanilla", "Vanilla", "Weak", "Minimum", "Strong");
    BooleanSetting swing = new BooleanSetting("Swing", true);

    // XCalc (existing)
    BooleanSetting xCalc = new BooleanSetting("XCalc", true);
    // NEW: CEXPL — attack crystals the moment the server spawns them (insta-break)
    BooleanSetting cexpl = new BooleanSetting("CEXPL", false);

    // Spawn-detection state
    private final Set<Integer> knownEntityIds = new HashSet<>();
    private BlockPos lastPlacedPos = null;
    private long lastPlacedTime = 0L;
    private final long placeWatchMs = 1500L; // watch for new crystals for this many ms after place

    private long lastActionTime = 0;
    private int placeDelayMs = 50;
    private int breakDelayMs = 50;
    private List<Entity> targets = new ArrayList<>();

    // instance for mixin hook
    private static CrystalAura INSTANCE = null;

    public CrystalAura() {
        super("CrystalAura", "Places and breaks end crystals near enemies.", Category.DEVELOPMENT);
        INSTANCE = this;
    }

    public static CrystalAura getInstance() {
        return INSTANCE;
    }

    // Called by a mixin (or any packet hook) when the client receives a spawn packet and the entity has been added to the world.
    // Mixin approach is recommended (inject into ClientPlayNetworkHandler spawn packet handlers).
    public static void onEntitySpawned(Entity e) {
        CrystalAura inst = getInstance(); // keep your existing getter
        System.out.println("[CA MODULE] onEntitySpawned called. entity="
                + (e == null ? "null" : (e.getId() + ":" + e.getClass().getSimpleName()))
                + " INST=" + (inst == null ? "null" : "ok")
                + " CEXPL=" + (inst == null ? "?" : inst.cexpl.isEnabled()));
        if (inst != null) inst.onEntitySpawnedInternal(e);
    }

    private void onEntitySpawnedInternal(Entity e) {
        // Debug/info print
        System.out.println("[CA ATTACK] onEntitySpawnedInternal: e="
                + (e == null ? "null" : (e.getId() + ":" + e.getClass().getSimpleName()))
                + " lastPlaced=" + lastPlacedPos
                + " lastPlacedAgeMs=" + (lastPlacedTime == 0 ? "na" : (System.currentTimeMillis() - lastPlacedTime))
                + " CEXPL=" + cexpl.isEnabled());

        if (e == null) return;
        if (!(e instanceof EndCrystalEntity)) return;
        if (!cexpl.isEnabled()) {
            System.out.println("[CA ATTACK] CEXPL disabled, skipping instant attack.");
            return;
        }
        if (mc.player == null) {
            System.out.println("[CA ATTACK] mc.player null, skipping.");
            return;
        }

        // Range check
        double maxRange = breakRange.getValue();
        double distSq = mc.player.squaredDistanceTo(e);
        System.out.println("[CA ATTACK] distSq=" + distSq + " maxRange^2=" + (maxRange * maxRange));
        if (distSq > maxRange * maxRange) {
            System.out.println("[CA ATTACK] crystal out of configured break range, skipping.");
            return;
        }

        int id = e.getId();
        // Avoid duplicate handling
        if (knownEntityIds.contains(id)) {
            System.out.println("[CA ATTACK] entity id " + id + " already known/handled, skipping.");
            return;
        }
        knownEntityIds.add(id);

        // Perform the immediate break using your existing break-sending logic
        try {
            System.out.println("[CA ATTACK] sending break packets for entity id=" + id);
            sendBreakPackets(e);
        } catch (Exception ex) {
            System.out.println("[CA ATTACK] error sending break packets: " + ex);
        }

        // If this spawned crystal matches our last placed pos, clear lastPlacedPos so the detector won't double-attack
        if (lastPlacedPos != null) {
            Vec3d placedCenter = new Vec3d(lastPlacedPos.getX() + 0.5, lastPlacedPos.getY() + 1.0, lastPlacedPos.getZ() + 0.5);
            double sq = e.getPos().squaredDistanceTo(placedCenter);
            if (sq <= 2.25) {
                System.out.println("[CA ATTACK] spawned crystal matches lastPlacedPos — clearing lastPlacedPos.");
                lastPlacedPos = null;
                lastPlacedTime = 0L;
            }
        }
    }


    @Override
    public void onEnable() {
        lastActionTime = 0;
        updateDelays();
        knownEntityIds.clear();
        if (mc.world != null) {
            for (Entity e : mc.world.getEntities()) {
                knownEntityIds.add(e.getId());
            }
        }
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        targets.clear();
        knownEntityIds.clear();
        lastPlacedPos = null;
        if (INSTANCE == this) INSTANCE = null;
    }

    @EventHandler
    public boolean onShitTick() {
        if (mc.player == null || mc.world == null) return false;

        // detect newly spawned crystals (fallback) and attack if they match our last placed pos
        detectAndAttackSpawnedCrystals();

        updateDelays();

        long now = System.currentTimeMillis();
        if (now - lastActionTime < Math.min(placeDelayMs, breakDelayMs)) return false;

        targets = getTargets();

        // Try break first, then place
        boolean didAction = tryBreakCrystals();

        if (!didAction) {
            didAction = tryPlaceCrystals();
        }

        if (didAction) lastActionTime = now;

        return false;
    }

    private void detectAndAttackSpawnedCrystals() {
        if (mc.world == null || mc.player == null) return;

        // Iterate world entities and look for IDs not yet seen
        for (Entity e : mc.world.getEntities()) {
            int id = e.getId();
            if (knownEntityIds.contains(id)) continue; // already known
            // new entity detected
            knownEntityIds.add(id);

            if (!(e instanceof EndCrystalEntity)) continue;

            // If we recently placed at a pos, and this new crystal is at/near that pos, attack it immediately
            if (lastPlacedPos != null && System.currentTimeMillis() - lastPlacedTime <= placeWatchMs) {
                Vec3d spawnVec = e.getPos();
                Vec3d placedCenter = new Vec3d(lastPlacedPos.getX() + 0.5, lastPlacedPos.getY() + 1.0, lastPlacedPos.getZ() + 0.5);
                double sqDist = spawnVec.squaredDistanceTo(placedCenter);
                // allow small epsilon distance (1.5 blocks² ~= 2.25)
                if (sqDist <= 2.25) {
                    try {
                        mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(e, mc.player.isSneaking()));
                        if (swing.isEnabled()) mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(getCrystalHandOrMain()));
                    } catch (Exception ignored) {}
                    // once attacked, clear lastPlacedPos to avoid repeated immediate attacks
                    lastPlacedPos = null;
                    lastPlacedTime = 0L;
                }
            }
        }
    }

    /**
     * Map Speed(0..20) -> base ms delay, then apply sequential multiplier.
     *  - Speed: 0 -> 20ms (fast), 20 -> 320ms (slow)
     *  - Sequential multipliers reduce delay to be more aggressive.
     */
    private void updateDelays() {
        // baseDelay: 20ms -> 320ms
        int baseDelay = 20 + speed.getValueInt() * 15;

        switch (sequential.getMode()) {
            case "Strong" -> {
                placeDelayMs = Math.max(1, baseDelay * 20 / 100);   // 20% of base
                breakDelayMs = Math.max(1, baseDelay * 20 / 100);
            }
            case "Minimum" -> {
                placeDelayMs = Math.max(2, baseDelay * 45 / 100);   // 45% of base
                breakDelayMs = Math.max(2, baseDelay * 45 / 100);
            }
            case "Weak" -> {
                placeDelayMs = Math.max(3, baseDelay * 70 / 100);   // 70% of base
                breakDelayMs = Math.max(3, baseDelay * 70 / 100);
            }
            default -> { // Vanilla
                placeDelayMs = baseDelay;
                breakDelayMs = baseDelay;
            }
        }
    }

    private int getPacketCountForMode() {
        return switch (sequential.getMode()) {
            case "Strong" -> 2;
            case "Minimum" -> 1;
            case "Weak" -> 1;
            default -> 1; // Vanilla
        };
    }

    private List<Entity> getTargets() {
        List<Entity> found = new ArrayList<>();
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity)) continue;
            if (e == mc.player) continue;

            double dist = mc.player.squaredDistanceTo(e);
            if (dist > placeRange.getValue() * placeRange.getValue()) continue;

            switch (targetMode.getMode()) {
                case "Player" -> {
                    if (e instanceof PlayerEntity) found.add(e);
                }
                case "Hostiles" -> {
                    if (e instanceof HostileEntity) found.add(e);
                }
                case "Passive" -> {
                    if (e instanceof PassiveEntity) found.add(e);
                }
                case "All" -> found.add(e);
            }
        }
        found.sort(Comparator.comparingDouble(mc.player::squaredDistanceTo));
        return found;
    }

    // ---------- PLACEMENT (choose by sum distance to target + player) ----------
    private boolean tryPlaceCrystals() {
        Hand crystalHand = getCrystalHand();
        if (crystalHand == null) return false;

        double placeRangeSq = placeRange.getValue() * placeRange.getValue();
        double breakRangeSq = breakRange.getValue() * breakRange.getValue();
        Vec3d playerPos = mc.player.getPos();

        Direction[] four = new Direction[] { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };

        for (Entity target : targets) {
            Vec3d targetVec = target.getPos();
            BlockPos feet = target.getBlockPos();

            // pick baseY (actual block under player if needed)
            int baseY;
            if (mc.world.isAir(feet) && !mc.world.isAir(feet.down())) baseY = feet.down().getY();
            else baseY = feet.getY();

            // Decide offsetDistance using XCalc detection
            int offsetDistance = 1;
            if (xCalc.isEnabled()) {
                boolean singleBlock = isStandingOnSingleBlock(target, feet);
                offsetDistance = singleBlock ? 1 : 2;
            }

            // 1) collect feet-level candidates (at baseY) with chosen offsetDistance
            List<BlockPos> feetCandidates = new ArrayList<>();
            for (Direction d : four) {
                BlockPos p = new BlockPos(feet.getX() + d.getOffsetX() * offsetDistance, baseY, feet.getZ() + d.getOffsetZ() * offsetDistance);
                if (isValidBase(p) && isAirAbove(p)) feetCandidates.add(p);
            }

            BlockPos chosen = null;
            if (!feetCandidates.isEmpty()) {
                chosen = feetCandidates.stream()
                        .min(Comparator.comparingDouble(pos -> {
                            Vec3d v = crystalVec(pos);
                            return targetVec.squaredDistanceTo(v) + playerPos.squaredDistanceTo(v);
                        })).orElse(null);
            } else {
                // face-level candidates
                List<BlockPos> faceCandidates = new ArrayList<>();
                for (Direction d : four) {
                    BlockPos p = new BlockPos(feet.getX() + d.getOffsetX() * offsetDistance, baseY + 1, feet.getZ() + d.getOffsetZ() * offsetDistance);
                    if (isValidBase(p) && isAirAbove(p)) faceCandidates.add(p);
                }
                if (!faceCandidates.isEmpty()) {
                    chosen = faceCandidates.stream()
                            .min(Comparator.comparingDouble(pos -> {
                                Vec3d v = crystalVec(pos);
                                return targetVec.squaredDistanceTo(v) + playerPos.squaredDistanceTo(v);
                            })).orElse(null);
                } else {
                    // fallback nearby search
                    int radius = 2;
                    double bestScore = Double.MAX_VALUE;
                    BlockPos best = null;
                    for (int x = -radius; x <= radius; x++) {
                        for (int y = -1; y <= 1; y++) {
                            for (int z = -radius; z <= radius; z++) {
                                BlockPos p = new BlockPos(feet.getX() + x, baseY + y, feet.getZ() + z);
                                if (p.equals(feet)) continue;
                                if (!isValidBase(p) || !isAirAbove(p)) continue;
                                Vec3d v = crystalVec(p);
                                double distToTargetSq = targetVec.squaredDistanceTo(v);
                                double distToPlayerSq = playerPos.squaredDistanceTo(v);
                                if (distToTargetSq <= placeRangeSq && distToPlayerSq <= placeRangeSq && distToPlayerSq <= breakRangeSq) {
                                    double score = distToTargetSq + distToPlayerSq;
                                    if (score < bestScore) {
                                        bestScore = score;
                                        best = p;
                                    }
                                }
                            }
                        }
                    }
                    chosen = best;
                }
            }

            if (chosen == null) continue;

            Vec3d chosenVec = crystalVec(chosen);
            double chosenTargetSq = targetVec.squaredDistanceTo(chosenVec);
            double chosenPlayerSq = playerPos.squaredDistanceTo(chosenVec);
            if (!(chosenTargetSq <= placeRangeSq && chosenPlayerSq <= placeRangeSq && chosenPlayerSq <= breakRangeSq)) continue;
            if (safe.isEnabled() && mc.player.getBlockPos().isWithinDistance(chosen, 2)) continue;

            BlockHitResult bhr = new BlockHitResult(chosenVec, Direction.UP, chosen, false);
            sendPlacePackets(crystalHand, bhr);

            lastPlacedPos = chosen;
            lastPlacedTime = System.currentTimeMillis();

            return true;
        }

        return false;
    }

    // helper that decides if target is centered on a single block (true) or straddling (false)
    private boolean isStandingOnSingleBlock(Entity target, BlockPos feet) {
        double fx = target.getX() - feet.getX();
        double fz = target.getZ() - feet.getZ();
        // if both axis are near center (0.35..0.65) we treat as single-block standing
        return fx > 0.35 && fx < 0.65 && fz > 0.35 && fz < 0.65;
    }

    private static int sequence = 0;
    private void sendPlacePackets(Hand hand, BlockHitResult bhr) {
        int packets = getPacketCountForMode();
        for (int i = 0; i < packets; i++) {
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, bhr, sequence++));
        }
        if (swing.isEnabled()) mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(hand));
    }

    private Vec3d crystalVec(BlockPos pos) { return new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5); }

    private boolean isValidBase(BlockPos pos) {
        if (mc.world == null) return false;
        return mc.world.getBlockState(pos).isOf(Blocks.OBSIDIAN) || mc.world.getBlockState(pos).isOf(Blocks.BEDROCK);
    }

    private boolean isAirAbove(BlockPos pos) {
        if (mc.world == null) return false;
        BlockPos above = pos.up();
        if (!mc.world.isAir(above)) return false;
        VoxelShape shape = mc.world.getBlockState(above).getOutlineShape(mc.world, above);
        if (shape.isEmpty()) return true;
        return mc.world.getOtherEntities(null, shape.getBoundingBox().offset(above.getX(), above.getY(), above.getZ())).isEmpty();
    }

    // ---------- BREAK ----------
    private boolean tryBreakCrystals() {
        double maxRangeVal = breakRange.getValue();
        Entity closestCrystal = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof EndCrystalEntity)) continue;
            double dist = mc.player.squaredDistanceTo(e);
            if (dist > maxRangeVal * maxRangeVal) continue;
            if (dist < closestDist) {
                closestDist = dist;
                closestCrystal = e;
            }
        }

        if (closestCrystal != null) {
            sendBreakPackets(closestCrystal);
            return true;
        }
        return false;
    }

    private void sendBreakPackets(Entity crystal) {
        int packets = getPacketCountForMode();
        for (int i = 0; i < packets; i++) {
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
        }
        if (sequential.getMode().equals("Strong") && crystal instanceof EndCrystalEntity) {
            //try { ((EndCrystalEntity) crystal).discard(); } catch (Exception ignored) {}
        }
        if (swing.isEnabled()) mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(getCrystalHandOrMain()));
    }

    private Hand getCrystalHand() {
        if (mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) return Hand.MAIN_HAND;
        if (mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) return Hand.OFF_HAND;
        return null;
    }

    private Hand getCrystalHandOrMain() {
        Hand hand = getCrystalHand();
        return hand != null ? hand : Hand.MAIN_HAND;
    }
}
