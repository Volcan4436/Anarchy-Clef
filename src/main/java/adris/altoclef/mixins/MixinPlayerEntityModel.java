package adris.altoclef.mixins;

import adris.altoclef.altomenu.cheatUtils.RenderCallGate;
import adris.altoclef.altomenu.modules.Development.FakeRotation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class MixinPlayerEntityModel<T extends LivingEntity> {

    @Inject(
            method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
            at = @At("TAIL")
    )
    private void fakeHeadRotation(T entity, float limbSwing, float limbSwingAmount,
                                  float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {

        // Only apply fake rotation if the render gate is active and module enabled.
        if (!RenderCallGate.isInRender()) return;
        if (!FakeRotation.INSTANCE.isEnabled()) return;

        if (entity == MinecraftClient.getInstance().player) {
            PlayerEntityModel<?> model = (PlayerEntityModel<?>) (Object) this;

            float fakeYaw = FakeRotation.headyaw.getValuefloat();
            float fakePitch = FakeRotation.headpitch.getValuefloat();

            model.head.yaw = fakeYaw;
            model.head.pitch = fakePitch;
        }
    }
}