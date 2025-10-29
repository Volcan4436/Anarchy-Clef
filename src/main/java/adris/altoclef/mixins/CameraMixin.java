package adris.altoclef.mixins;



import adris.altoclef.altomenu.modules.Render.BetterCamera;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow public abstract void reset();

    @Shadow protected abstract double clipToSpace(double desiredCameraDistance);

    /**
     * Modify the distance used by third-person camera before clipping.
     * The argument to clipToSpace() is the desired camera distance (default 4.0D).
     */
    @ModifyArg(
            method = "update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/Camera;clipToSpace(D)D"
            ),
            index = 0
    )
    private double modifyCameraDistance(double original) {
        // Default is 4.0D — increase or decrease as you wish
        if (BetterCamera.Instance.isEnabled()) {
            return BetterCamera.Instance.cameradistance.getValuefloat();
        }
        else return 4.0D;
    }


    @Redirect(
            method = "update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/Camera;clipToSpace(D)D"
            )
    )
    private double ignoreClipToSpace(Camera instance, double desiredDistance) {
        // If BetterCamera is enabled and noclip is on, skip clipping
        if (BetterCamera.Instance.isEnabled() && BetterCamera.Instance.noclip.isEnabled()) {
            return desiredDistance; // ignore clipToSpace
        }

        // Otherwise call the original clipToSpace method
        // Use the @Shadow method here instead of trying to call instance.clipToSpace
        return this.clipToSpace(desiredDistance);
    }
}