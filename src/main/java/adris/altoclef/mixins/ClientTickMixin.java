package adris.altoclef.mixins;

import adris.altoclef.AltoClef;
import adris.altoclef.eventbus.ClefEventBus;
import adris.altoclef.eventbus.events.ClientTickEvent;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Changed this from player to client, I hope this doesn't break anything.
@Mixin(MinecraftClient.class)
public final class ClientTickMixin {
    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void clientTick(CallbackInfo ci) {
        ClefEventBus.publish(new ClientTickEvent());
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void onTick(CallbackInfo ci) {
        AltoClef.INSTANCE.onTick();
    }
}