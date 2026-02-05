package adris.altoclef.mixins;

import adris.altoclef.eventbus.ClefEventBus;
import adris.altoclef.eventbus.events.GameOverlayEvent;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class GameOverlayMixin {

/*    @Inject(
            method = "setOverlayMessage",
            at = @At("HEAD")
    )
    public void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        String text = message.getString();
        ClefEventBus.publish(new GameOverlayEvent(text));
    }*/


    //todo: Shitty Crash Work Around but will work for now - Volcan
    // Only noticed crash when booting with Latest RusherHack Version on 1.20.4
    // Original Crash reason: java.lang.NullPointerException: Cannot invoke "net.minecraft.class_2561.getString()" because "message" is null
    @Inject(method = "setOverlayMessage", at = @At("HEAD"))
    public void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        String text = null;

        if (message != null) {
            try {
                text = message.getString();
            } catch (Throwable t) {
                // Text object was broken or mid-mutation by another mod
                text = null;
            }
        }

        ClefEventBus.publish(new GameOverlayEvent(text));
    }
}
