package adris.altoclef.mixins;

import adris.altoclef.altomenu.managers.GlowManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void spoofGlow(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity)(Object)this;

        if (GlowManager.shouldGlow(self.getUuid())) {
            cir.setReturnValue(true);
        }
    }
}