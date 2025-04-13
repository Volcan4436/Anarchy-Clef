package adris.altoclef.mixins;

import adris.altoclef.eventbus.events.PacketEvent;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {


    @Inject(at = @At("HEAD"), method = "send(Lnet/minecraft/network/packet/Packet;)V", remap = false, cancellable = true)
    private void onSendPacketHead(Packet<?> packet, CallbackInfo info) {
        if (PacketEvent.Send.get(packet).isCancelled()) info.cancel();
    }
}
