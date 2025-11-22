package adris.altoclef.mixins;

import adris.altoclef.altomenu.modules.Development.CrystalAura;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Inject(method = "onEntitySpawn(Lnet/minecraft/network/packet/s2c/play/EntitySpawnS2CPacket;)V",
            at = @At("TAIL"), require = 0)
    private void onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
//        System.out.println("[CA MIXIN] onEntitySpawn packet id=" + packet.getId() + " type=" + packet.getEntityType() +
//                " pos=" + packet.getX() + "," + packet.getY() + "," + packet.getZ());
        Entity e = (client != null && client.world != null) ? client.world.getEntityById(packet.getId()) : null;
//        System.out.println("[CA MIXIN] entity present? " + (e != null));
        CrystalAura.onEntitySpawned(e);
    }
}
