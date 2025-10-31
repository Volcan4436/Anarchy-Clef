package adris.altoclef.altomenu.modules.Combat;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.modules.Development.FakeRotation;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.world.RaycastContext;

public class KillAuraModule extends Mod {
    public static KillAuraModule INSTANCE = new KillAuraModule();

    public ModeSetting targetMode = new ModeSetting("Target Mode", "Hostile", "Hostile", "Passive", "Players", "CrossTap");
    public BooleanSetting friendlyFire = new BooleanSetting("FriendlyFire", false);
    public NumberSetting attackSpeed = new NumberSetting("Attack Speed", 1.0, 20.0, 6.0, 0.1);
    public BooleanSetting weapCool = new BooleanSetting("WeapCool", true);
    public BooleanSetting rotate = new BooleanSetting("Rotate", true);
    public BooleanSetting antiJitter = new BooleanSetting("AntiJitter", true);
    public BooleanSetting swing = new BooleanSetting("Swing", true);
    public ModeSetting rotateMode = new ModeSetting("Rotate Mode", "Client", "Client", "Packet");
    public NumberSetting range = new NumberSetting("Range", 1.0, 6.0, 4.0, 0.1);
    public BooleanSetting movFix = new BooleanSetting("MovFix", true);
    public BooleanSetting nowall = new BooleanSetting("NoWall", true);
    public BooleanSetting falltap = new BooleanSetting("FallTap", true);

    private float prevYaw = 0f;
    private float prevPitch = 0f;
    private int attackTimer = 0;
    public static float RTYAW = 0;
    public static float RTPITCH = 0;
    public static boolean HasTarget = false;
    public KillAuraModule() {
        super("KillAura", "Automatically attacks entities.", Category.COMBAT);
        INSTANCE = this;
    }
    @EventHandler
    public boolean onShitTick() {
        if (mc.player == null || mc.world == null) return false;

        attackTimer++;
        double maxRange = range.getValue();

        Entity target = null;
        double closest = maxRange + 1.0;

        // Iterate over Iterable<Entity> directly (fix for mappings that return Iterable)
        Iterable<Entity> entities = mc.world.getEntities();
        for (Entity e : entities) {
            if (e == null) continue;
            if (!(e instanceof LivingEntity)) continue;
            if (e == mc.player) continue;

            // Check alive state — fallback to isRemoved() if isAlive() doesn't exist
            boolean alive = true;
            try {
                alive = ((LivingEntity) e).isAlive();
            } catch (Throwable ignored) {
                alive = !e.isRemoved();
            }
            if (!alive) continue;

            if (!filterByModeAndTameAndRange(e)) continue;

            double dist = mc.player.distanceTo(e);
            if (dist < closest) {
                closest = dist;
                target = e;
            }
        }

        // CrossTap mode: override with crosshair target
        if (targetMode.isMode("CrossTap")) {
            HitResult cross = mc.crosshairTarget;
            if (cross != null && cross.getType() == HitResult.Type.ENTITY) {
                EntityHitResult ehr = (EntityHitResult) cross;
                Entity ent = ehr.getEntity();
                if (ent != null && ent != mc.player && filterByModeAndTameAndRange(ent)) {
                    target = ent;
                } else target = null;
            } else target = null;
        }

        HasTarget = target != null;

        if (target == null) return false;

        // NoWall check
        if (nowall.isEnabled()) {
            Vec3d eye = mc.player.getEyePos();
            Vec3d targ = new Vec3d(target.getX(), target.getY() + target.getHeight() / 2.0, target.getZ());
            BlockHitResult bh = mc.world.raycast(new RaycastContext(eye, targ, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
            if (bh.getType() == HitResult.Type.BLOCK) return false;
        }

        // Rotation
        Vec3d lookAt = new Vec3d(target.getX(), target.getY() + target.getHeight() / 2.0, target.getZ());
        float[] desired = getYawPitchTo(lookAt);

        if (rotate.isEnabled()) {
            if (antiJitter.isEnabled()) {
                desired[0] = lerpAngle(prevYaw, desired[0], 0.35f);
                desired[1] = lerp(prevPitch, desired[1], 0.35f);
            }
            if (rotateMode.isMode("Client")) {
                mc.player.setYaw(desired[0]);
                mc.player.setPitch(desired[1]);
            } else {
                RTYAW = desired[0];
                RTPITCH = desired[1];
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(desired[0], desired[1], mc.player.isOnGround()));

            }
            prevYaw = desired[0];
            prevPitch = desired[1];
        }

        // MovFix
        if (movFix.isEnabled()) {
            float yawToTarget = getYawToEntity(target);
            float diff = Math.abs(MathHelper.wrapDegrees(mc.player.getYaw() - yawToTarget));
            if (diff > 90f) mc.player.setSprinting(false);
        }

        // Attack delay logic
        boolean canAttack;
        if (weapCool.isEnabled()) {
            float prog = mc.player.getAttackCooldownProgress(0.5F);
            canAttack = prog >= 1.0F;
        } else {
            double spd = attackSpeed.getValue();
            int ticksBetween = Math.max(1, (int) Math.round(20.0 / spd));
            canAttack = attackTimer >= ticksBetween;
        }

        if (!canAttack) return false;

        // Attack
        mc.interactionManager.attackEntity(mc.player, target);

        // Swing
        if (swing.isEnabled()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }

        // FallTap
        if (falltap.isEnabled() && mc.player.fallDistance < 0.6) {
            mc.interactionManager.attackEntity(mc.player, target);
        }

        attackTimer = 0;
        return false;
    }

    private boolean filterByModeAndTameAndRange(Entity e) {
        if (!(e instanceof LivingEntity)) return false;
        if (e == mc.player) return false;

        double dist = mc.player.distanceTo(e);
        if (dist > range.getValue()) return false;

        if (e instanceof TameableEntity && !friendlyFire.isEnabled()) return false;

        if (targetMode.isMode("Hostile")) {
            return !e.getType().getSpawnGroup().isPeaceful();
        } else if (targetMode.isMode("Passive")) {
            return e.getType().getSpawnGroup().isPeaceful();
        } else if (targetMode.isMode("Players")) {
            return e instanceof PlayerEntity;
        } else if (targetMode.isMode("CrossTap")) {
            return true;
        }
        return true;
    }
    @Override
    public void onRender() {
        if (mc.player != null && HasTarget) {
            mc.player.setBodyYaw(RTYAW); //Currently Works as Intended
        }
    }
    private float[] getYawPitchTo(Vec3d targetPos) {
        Vec3d eye = mc.player.getEyePos();
        double dx = targetPos.x - eye.x;
        double dy = targetPos.y - eye.y;
        double dz = targetPos.z - eye.z;
        double dist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        yaw = MathHelper.wrapDegrees(yaw);
        pitch = MathHelper.wrapDegrees(pitch);
        return new float[]{yaw, pitch};
    }

    private float getYawToEntity(Entity e) {
        double dx = e.getX() - mc.player.getX();
        double dz = e.getZ() - mc.player.getZ();
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        return MathHelper.wrapDegrees(yaw);
    }

    private float lerp(float a, float b, float f) {
        return a + (b - a) * f;
    }

    private float lerpAngle(float a, float b, float f) {
        float diff = MathHelper.wrapDegrees(b - a);
        return a + diff * f;
    }

    @Override
    public void onEnable() {
        attackTimer = 0;
        prevYaw = mc.player != null ? mc.player.getYaw() : 0f;
        prevPitch = mc.player != null ? mc.player.getPitch() : 0f;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        attackTimer = 0;
        super.onDisable();
    }
}
