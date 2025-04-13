package adris.altoclef.altomenu.modules.Player;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.eventbus.EventHandler;
import adris.altoclef.eventbus.events.PacketEvent;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.Objects;

public class AntiHunger extends Mod {

    //todo:
    // Add Sprint Spoof (Packet & Legit) (Legit cancels Sprint Press Client Side) (Packet cancels Sprint Packet on Send)
    // Add Ground Spoof (Packet) (Send onGround Packet Every So Many Ticks)
    // Attempt to find a way to lower hunger usage when spamming jump button under a block that doesn't require Constant Packet Spoof (@ChiefWarCry can you attempt it)

    public AntiHunger() {
        super("AntiHunger", "Prevents hunger from decreasing.", Mod.Category.PLAYER);
    }

    BooleanSetting spoofValue = new BooleanSetting("SpoofValue", true);
    BooleanSetting packetSpoof = new BooleanSetting("Packet", true);


    @EventHandler
    public boolean onShitTick() {
        if (mc.player == null) return true;
        if (spoofValue.isEnabled() && mc.player.getHungerManager().getFoodLevel() != 20) {
            mc.player.getHungerManager().setFoodLevel(20);
        }

        return false;
    }
}
