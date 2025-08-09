package adris.altoclef.mixins;

import adris.altoclef.altomenu.modules.Render.Ambience;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static adris.altoclef.altomenu.cheatUtils.AmbienceColorUtil.getCurrentAmbienceVec3;

@Mixin(ClientWorld.class)
public abstract class SkyColorMixin {
    @Unique
    private static Vec3d originalSkyColor = null;

    @Unique
    private static Vec3d originalCloudColor = null;

    @Unique
    private static boolean capturingOriginal = false;

    /**
     * Modify sky color based on Ambience setting.
     */
    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void modifySkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        if (!capturingOriginal && originalSkyColor == null) {
            capturingOriginal = true;
            originalSkyColor = ((ClientWorld) (Object) this).getSkyColor(cameraPos, tickDelta);
            capturingOriginal = false;
        }

        if (!capturingOriginal) {
            if (Ambience.INSTANCE.isEnabled()) {
                cir.setReturnValue(getCurrentAmbienceVec3());
            } else if (originalSkyColor != null) {
                cir.setReturnValue(originalSkyColor);
            }
        }
    }

    /**
     * Modify clouds color based on Ambience setting.
     */
    @Inject(method = "getCloudsColor", at = @At("HEAD"), cancellable = true)
    private void modifyCloudsColor(float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        if (!capturingOriginal && originalCloudColor == null) {
            capturingOriginal = true;
            originalCloudColor = ((ClientWorld) (Object) this).getCloudsColor(tickDelta);
            capturingOriginal = false;
        }

        if (!capturingOriginal) {
            if (Ambience.INSTANCE.isEnabled()) {
                cir.setReturnValue(getCurrentAmbienceVec3());
            } else if (originalCloudColor != null) {
                cir.setReturnValue(originalCloudColor);
            }
        }
    }
}