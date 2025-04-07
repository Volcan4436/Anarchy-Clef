package adris.altoclef.mixins;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.managers.ModuleManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public class WorldRenderMixin {
    @Unique
    private MatrixStack renderWorldMatrices;

    @Inject(method = "renderWorld", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = {"ldc=hand"}), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onRenderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo info) {
        renderWorldMatrices = matrices;
        for (Mod m : ModuleManager.INSTANCE.getModules()) {
            if (m.isEnabled()) {
                m.onWorldRender(matrices);
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        if (renderWorldMatrices != null) {
            for (Mod m : ModuleManager.INSTANCE.getModules()) {
                if (m.isEnabled()) {
                    m.onRender(renderWorldMatrices);
                }
            }
            renderWorldMatrices = null; // reset the field
        }
    }
}