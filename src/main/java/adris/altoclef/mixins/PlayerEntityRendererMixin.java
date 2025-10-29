package adris.altoclef.mixins;

import adris.altoclef.altomenu.modules.Render.PlayerScale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Unique
    float newScale = 1f; // Set the desired scale value

    @Inject(method = "scale*",
            at = @At(value = "HEAD"),
            cancellable = true)
    private void scale(AbstractClientPlayerEntity player, MatrixStack matrixStack, float scale, CallbackInfo ci) {
        if (PlayerScale.Instance.isEnabled()) {
            newScale = PlayerScale.Instance.scale.getValuefloat(); // Set the desired scale value
        }
        if (player != MinecraftClient.getInstance().player) {
            if (PlayerScale.Instance.allPlayers.isEnabled()) {
                matrixStack.scale(newScale, newScale, newScale);
            }
            return;
        } else newScale = 1f;
        matrixStack.scale(newScale, newScale, newScale);
        ci.cancel();
    }

    //debugger
/*    @Inject(method = "scale(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;F)V",
            at = @At("HEAD"),
            cancellable = true)
    private void scale(AbstractClientPlayerEntity player, MatrixStack matrixStack, float amount, CallbackInfo ci) {
        System.out.println("Vanilla scale amount: " + amount);
        // ... your logic ...
    }*/
}