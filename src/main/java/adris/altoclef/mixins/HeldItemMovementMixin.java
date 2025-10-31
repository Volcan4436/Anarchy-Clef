package adris.altoclef.mixins;

import adris.altoclef.altomenu.modules.Render.BetterCamera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemMovementMixin {

    private float smoothTiltX = 0f;
    private float smoothTiltZ = 0f;

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void onRenderItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                              float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                              VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (BetterCamera.bounce.isEnabled()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.player == null || player == null || mc.isPaused()) return;

            Vec3d vel = player.getVelocity();
            if (vel == null) return;

            // --- Convert world velocity to local (camera-relative) space ---
            float yawRad = (float) Math.toRadians(player.getYaw(tickDelta));
            double sin = Math.sin(yawRad);
            double cos = Math.cos(yawRad);

            // Forward/back (localZ) and side (localX) motion
            double localZ = vel.z * cos + vel.x * sin;
            double localX = vel.x * cos - vel.z * sin;

            // --- Target rotations ---
            float targetTiltX = 0f; // forward/back tilt
            if (localZ < -0.05) targetTiltX = 8f;   // moving forward → tilt back
            else if (localZ > 0.05) targetTiltX = -8f; // moving backward → tilt forward

            float targetTiltZ = 0f; // side tilt
            if (localX > 0.05) targetTiltZ = -5f;   // moving right → tilt left
            else if (localX < -0.05) targetTiltZ = 5f; // moving left → tilt right

            // --- Smooth interpolation ---
            smoothTiltX = MathHelper.lerp(0.1f, smoothTiltX, targetTiltX);
            smoothTiltZ = MathHelper.lerp(0.1f, smoothTiltZ, targetTiltZ);

            // --- Apply final transforms ---
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(smoothTiltX));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(smoothTiltZ));
        }
    }
}
