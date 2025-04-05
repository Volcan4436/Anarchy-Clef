package adris.altoclef.mixins;

import adris.altoclef.altomenu.modules.Render.Fullbright;
import adris.altoclef.altomenu.modules.Render.Radar;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImage;setColor(III)V"))
    private void update(Args args) {
        if (Fullbright.Instance.isEnabled()) {
            args.set(2, 0xFFFFFFFF);
        }
    }

}