package adris.altoclef.mixins;

import adris.altoclef.altomenu.modules.Render.ForceGlint;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "hasGlint", at = @At("HEAD"), cancellable = true)
    private void alwaysHasGlint(CallbackInfoReturnable<Boolean> cir) {
        if (ForceGlint.Instance.isEnabled()) cir.setReturnValue(true);
    }
}
