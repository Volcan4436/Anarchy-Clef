package adris.altoclef.mixins;


import adris.altoclef.eventbus.ClefEventBus;
import adris.altoclef.eventbus.events.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    @Inject(
            method = "send(Lnet/minecraft/network/packet/Packet;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        PacketEvent evt = new PacketEvent(packet, PacketEvent.Direction.SEND);
        ClefEventBus.publish(evt);
        evt.callGlobal();
        if (evt.isCancelled()) ci.cancel();
    }

    @Inject(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onIncoming(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        // Wrap packet in your PacketEvent
        PacketEvent evt = new PacketEvent(packet, PacketEvent.Direction.RECEIVE);
        ClefEventBus.publish(evt);
        evt.callGlobal();

        // Cancel if any module cancelled it
        if (evt.isCancelled()) {
            ci.cancel();
        }
    }
}
