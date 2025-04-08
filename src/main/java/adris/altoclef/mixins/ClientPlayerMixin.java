package adris.altoclef.mixins;

import adris.altoclef.altomenu.managers.ModuleManager;
import adris.altoclef.altomenu.modules.Player.NoSlow;
import adris.altoclef.altomenu.settings.BooleanSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerMixin {

    @Shadow
    public abstract boolean isSneaking();

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"), require = 0)
    private boolean tickMovement(ClientPlayerEntity player) {
        if (ModuleManager.INSTANCE.getModuleByClass(NoSlow.class).isEnabled() &&
                ((BooleanSetting) ModuleManager.INSTANCE.getModuleByClass(NoSlow.class).getSettings().get(0)).isEnabled())
            return false;
        return player.isUsingItem();
    }

    @Inject(method = "shouldSlowDown", at = @At("HEAD"), cancellable = true)
    private void onShouldSlowDown(CallbackInfoReturnable<Boolean> info) {
        if (ModuleManager.INSTANCE.getModuleByClass(NoSlow.class).isEnabled()) {
            if (((BooleanSetting) ModuleManager.INSTANCE.getModuleByClass(NoSlow.class).getSettings().get(1)).isEnabled()) {
                info.setReturnValue(false);
            }
        }
        return;
    }
}
