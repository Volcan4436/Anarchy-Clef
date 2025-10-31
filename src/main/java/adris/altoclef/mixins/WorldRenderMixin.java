package adris.altoclef.mixins;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.RenderCallGate;
import adris.altoclef.altomenu.managers.ModuleManager;
import net.minecraft.client.MinecraftClient;
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

    // set the gate at the start of render
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        RenderCallGate.enterRender();
    }

    // clear the gate at the end of render and call modules
    @Inject(method = "render", at = @At(value = "RETURN"))
    private void onRenderReturn(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        for (Mod m : ModuleManager.INSTANCE.getModules()) {
            if (m.isEnabled()) {
                m.onRender();
            }
        }
        RenderCallGate.exitRender();
    }
}