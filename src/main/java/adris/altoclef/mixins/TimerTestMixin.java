package adris.altoclef.mixins;

import adris.altoclef.altomenu.modules.World.Timer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.class)
public class TimerTestMixin {
    @Shadow
    public float lastFrameDuration;

    @Inject(method = "beginRenderTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter;prevTimeMillis:J"))
    private void onBeingRenderTick(long a, CallbackInfoReturnable<Integer> info) {
        if (Timer.INSTANCE.isEnabled()) lastFrameDuration *= Timer.speed.getValuefloat();
        else lastFrameDuration *= 1;
    }
}
