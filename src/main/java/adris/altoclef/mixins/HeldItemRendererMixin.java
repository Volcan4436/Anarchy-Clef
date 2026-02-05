package adris.altoclef.mixins;

import adris.altoclef.altomenu.managers.ModuleManager;
import adris.altoclef.altomenu.modules.Render.OldSwing;
import adris.altoclef.eventbus.ClefEventBus;
import adris.altoclef.eventbus.events.HeldItemRenderEvent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttackCooldownProgress(F)F"),
            method = "updateHeldItems")
    public float getAttackCooldownProgress(ClientPlayerEntity entity, float baseTime) {
        if (ModuleManager.INSTANCE.getModuleByClass(OldSwing.class).isEnabled())
            return 1;
        else
            return entity.getAttackCooldownProgress(baseTime);
    }

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void onRenderItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        HeldItemRenderEvent event = HeldItemRenderEvent.get(hand, matrices);
        ClefEventBus.publish(event);
    }
}
