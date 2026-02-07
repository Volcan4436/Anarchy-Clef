package adris.altoclef.altomenu.modules.Player;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.eventbus.EventHandler;
import adris.altoclef.eventbus.events.PacketEvent;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

public class AntiHunger extends Mod {

    public AntiHunger() {
        super("AntiHunger", "Prevents hunger from decreasing.", Mod.Category.PLAYER);

        // Automatically hook packet handling globally
        PacketEvent.addGlobalListener(this::onPacket);
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

    private void onPacket(PacketEvent evt) {
        // Only care about outgoing packets
        if (evt.direction != PacketEvent.Direction.SEND) return;
        if (!packetSpoof.isEnabled()) return;

        if (!AntiHunger.super.isEnabled()) return;

        // Cancel sprint packets
        if (evt.packet instanceof ClientCommandC2SPacket cmd) {
            ClientCommandC2SPacket.Mode mode = cmd.getMode();
            if (mode == ClientCommandC2SPacket.Mode.START_SPRINTING ||
                    mode == ClientCommandC2SPacket.Mode.STOP_SPRINTING) {

                evt.cancel(); // stops sprint packet
                System.out.println("Cancelled sprint packet: " + mode);
            }
        }
    }
}