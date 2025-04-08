package adris.altoclef.altomenu.modules.Combat;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Surround extends Mod {

    BooleanSetting Silent = new BooleanSetting("Silent", false);
    BooleanSetting AutoSelector = new BooleanSetting("AutoSelect", false);
    NumberSetting DelayNum = new NumberSetting("Delay", 1, 8, 5, 1);  // Delay between placements

    private int delay = 0;  // Delay counter
    private BlockPos[] surroundPositions = new BlockPos[4];  // Positions to surround player

    public Surround() {
        super("Surround", "Surrounds the players feet with obsidian.", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        // Calculate the surrounding block positions
        BlockPos playerPos = new BlockPos((int) mc.player.getX(), (int) mc.player.getY(), (int) mc.player.getZ());
        surroundPositions[0] = playerPos.north();  // North
        surroundPositions[1] = playerPos.south();  // South
        surroundPositions[2] = playerPos.east();   // East
        surroundPositions[3] = playerPos.west();   // West
    }

    @Override
    public void onDisable() {
        // Reset or clear any flags if needed
    }

    @Override
    public boolean onShitTick() {
        if (AutoSelector.isEnabled()) {
            if (delay >= DelayNum.getValue()) {
                delay = 0;  // Reset delay counter

                // Loop through each surrounding position and place obsidian if necessary
                for (BlockPos pos : surroundPositions) {
                    if (mc.world.getBlockState(pos).isAir()) {  // Only place if the block is air
                        placeObsidian(pos);
                    }
                }
            } else {
                delay++;  // Increment the delay counter
            }
        }

        return false;
    }

    private void placeObsidian(BlockPos pos) {
        // Determine the slot for obsidian (assuming obsidian is in the hotbar)
        int obsidianSlot = findObsidianSlot();

        if (obsidianSlot != -1) {
            // Silent mode: send packet to switch to obsidian and place it without client-side hotbar switch
            if (Silent.isEnabled()) {
                // Send a packet to switch to obsidian silently
                sendPacketToSwitchToObsidian(obsidianSlot);

                // Adjust position to ensure the block is placeable
                BlockPos adjustedPos = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()); // Adjust the Y-coordinate

                // Only attempt to place obsidian if the block is air or replaceable
                if (mc.world.getBlockState(adjustedPos).isReplaceable()) {
                    // Create a BlockHitResult based on player's position and the direction
                    Vec3d playerPos = mc.player.getPos();
                    BlockHitResult hitResult = new BlockHitResult(new Vec3d(adjustedPos.getX() + 0.5, adjustedPos.getY() + 0.5, adjustedPos.getZ() + 0.5), Direction.UP, adjustedPos, false);

                    // Debugging: Print hit result to check position and direction
                    System.out.println("Placing block at: " + adjustedPos);
                    System.out.println("Direction: " + Direction.UP);

                    // Send the packet to place the block
                    mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, 0));  // Sequence set to 0
                }
            } else {
                // Normal mode: switch to obsidian, place, then switch back
                mc.player.getInventory().selectedSlot = obsidianSlot;  // Switch to obsidian

                // Adjust position to make sure we're targeting the center of the block
                BlockPos adjustedPos = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());

                // Only attempt to place obsidian if the block is air or replaceable
                if (mc.world.getBlockState(adjustedPos).isReplaceable()) {
                    // Create a BlockHitResult with a proper position based on where you're standing
                    BlockHitResult hitResult = new BlockHitResult(mc.player.getPos().add(0, 1, 0), Direction.UP, adjustedPos, false);

                    // Debugging: Print hit result to check position and direction
                    System.out.println("Placing block at (normal mode): " + adjustedPos);
                    System.out.println("Direction (normal mode): " + Direction.UP);

                    // Send the packet to place the block
                    mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, 0));

                    // Switch back to the previous slot (optional, you can store the current selected slot and restore it)
                    mc.player.getInventory().selectedSlot = mc.player.getInventory().selectedSlot;
                }
            }
        }
    }




    private void sendPacketToSwitchToObsidian(int obsidianSlot) {
        // Send a packet to switch to obsidian in the hotbar silently
        if (obsidianSlot != -1) {
            // Create the packet to switch the slot to obsidian
            PlayerInteractItemC2SPacket switchPacket = new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0);  // Sequence set to 0

            // Send the packet to switch to obsidian without client-side hotbar switch
            System.out.println("Sending packet to switch to obsidian in slot " + obsidianSlot);
            mc.player.networkHandler.sendPacket(switchPacket);
        }
    }



    private int findObsidianSlot() {
        // Find the slot where obsidian is located (assuming it is in the hotbar)
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem().toString().contains("obsidian")) {
                return i;
            }
        }
        return -1;  // Return -1 if obsidian is not found
    }
}
