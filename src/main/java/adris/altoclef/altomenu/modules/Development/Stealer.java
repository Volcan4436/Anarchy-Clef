package adris.altoclef.altomenu.modules.Development;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//todo:
// add Rotations + MovementCorrection
// add SilentSteal (Silently Opens the Chest inventory hides the menu from the player so we dont get interupted then steals items while its Silently Opened)
public class Stealer extends Mod {

    public Stealer() {
        super("Stealer", "Stealer Shits", Mod.Category.DEVELOPMENT);
    }
    ModeSetting mode = new ModeSetting("Mode", "Break", "Break", "Steal", "Pussy");
    NumberSetting distance = new NumberSetting("NumSet", 1, 5, 3, 1);

    @EventHandler
    public boolean onShitTick() {
        if (mode.getMode() == "Break") {
            if (mc.player == null) return true;

            World world = mc.player.getWorld();
            BlockPos playerPos = mc.player.getBlockPos();

            // Get the radius as a double
            double radius = distance.getValue();

            // Flag to prevent multiple packet sends for the same chest
            boolean packetSent = false;

            // Iterate through nearby blocks to find chests
            for (int x = (int) -radius; x <= radius; x++) {
                for (int y = (int) -radius; y <= radius; y++) {
                    for (int z = (int) -radius; z <= radius; z++) {
                        BlockPos checkPos = playerPos.add(x, y, z);

                        // Check if the block is a chest
                        if (world.getBlockState(checkPos).getBlock() == Blocks.CHEST && !packetSent) {
                            // Send the packet to break the chest on the server side
                            breakChestViaPacket(checkPos);
                            packetSent = true; // Ensure we only send the packet once
                            break; // Stop further searching after sending the packet
                        }
                    }
                    if (packetSent) break; // Stop searching in the Y-axis if the packet is already sent
                }
                if (packetSent) break; // Stop searching in the X-axis if the packet is already sent
            }
            return false; // No more logic needed, return false
        }
        return false;
    }
    private void breakChestViaPacket(BlockPos blockPos) {
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler != null && mc.player != null) {
            // Send the packet to start breaking the block at the specified position
            PlayerActionC2SPacket startPacket = new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.START_DESTROY_BLOCK,
                    blockPos,
                    mc.player.getHorizontalFacing()
            );
            networkHandler.sendPacket(startPacket);
            mc.player.swingHand(Hand.MAIN_HAND);

            // After a small delay, send the stop breaking packet to complete the block break
            // You can add a delay here for simulation, but we'll do it instantly for now.
            PlayerActionC2SPacket stopPacket = new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                    blockPos,
                    mc.player.getHorizontalFacing()
            );
            networkHandler.sendPacket(stopPacket);
        }
    }
}
