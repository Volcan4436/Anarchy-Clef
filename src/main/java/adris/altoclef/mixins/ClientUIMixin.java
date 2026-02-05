package adris.altoclef.mixins;

import adris.altoclef.eventbus.ClefEventBus;
import adris.altoclef.eventbus.events.ClientRenderEvent;
import adris.altoclef.ui.HUD;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public final class ClientUIMixin {
    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void clientRender(DrawContext context, float tickDelta, CallbackInfo ci) {
        ClefEventBus.publish(new ClientRenderEvent(context.getMatrices(), tickDelta));
    }

    //Hud Stuff
    @Inject(method = "render", at = @At("RETURN"), cancellable = true)
    public void renderHud(DrawContext context, float tickDelta, CallbackInfo ci) {
        HUD.render(context, tickDelta);
    }

}
