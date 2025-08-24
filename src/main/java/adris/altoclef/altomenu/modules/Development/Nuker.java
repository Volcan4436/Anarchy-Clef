package adris.altoclef.altomenu.modules.Development;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;


//todo:
// - Create a LegitNuker that uses real rotations and tries to look legit from another players perspective
// - Implement Breaker inside this module (Bed, Cake, DragonEgg)
// - Add InstaBreak Exploit
// - Add Option to RayCast the break so that it is more legit (Modes: None, Lenient, Strict, Perfect)
// - Add MoveFix to this Module (So it works on Grim and Polar)
// - Add FOV Option (Only Break Blocks within the FOV Circle)
// - Add AutoThirdPerson (Automatically changes to third person view when Breaking Blocks)
// - Add Option to AutoDisable on Respawn, Disconnect and Death
// - Add SpeedMine Option (Effect, Percentage)
// - Add Option for the nuker to attempt to keep the mine progress of the last block it was mining
// - Add MultiBreak (Useful for Griefing)
// - Add a Whitelist and BlackList .txt file in the Config Folder that lets you configure what blocks it should mine (call the file NukerWhiteList.txt and NukerBlackList.txt)
// - Add Option to AutoDisable if no blocks are targeted after 30 seconds
public class Nuker extends Mod {

    private static final int MAX_BREAK_RANGE = 6;

    BooleanSetting flat = new BooleanSetting("Flat", false);
    BooleanSetting swing = new BooleanSetting("Swing", true);
    NumberSetting range = new NumberSetting("Range", 1, MAX_BREAK_RANGE, 5, 1);
    ModeSetting nukeType = new ModeSetting("NukeType", "Cuboid", "Cuboid", "Rounded");
    ModeSetting breakage = new ModeSetting("Breakage", "Creative", "Creative", "Survival");

    private List<BlockPos> blocksToBreak = new ArrayList<>();
    private int currentIndex = 0;

    public Nuker() {
        super("Nuker", "Destroys blocks in a configurable area.", Category.DEVELOPMENT);
    }

    @Override
    public void onEnable() {
        resetBlockList();
    }

    @Override
    public void onDisable() {
        blocksToBreak.clear();
        currentIndex = 0;
    }

    @EventHandler
    public boolean onShitTick() {
        if (mc.player == null || mc.world == null) return false;

        int r = Math.min(range.getValueInt(), MAX_BREAK_RANGE);
        boolean isRounded = nukeType.getMode().equalsIgnoreCase("Rounded");

        if (breakage.getMode().equalsIgnoreCase("Creative")) {
            // Creative mode: break all blocks immediately (as before)
            BlockPos playerPos = mc.player.getBlockPos();
            for (int y = (flat.isEnabled() ? 0 : -r); y <= r; y++) {
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        if (flat.isEnabled() && y < 0) continue;
                        if (isRounded && Math.sqrt(x*x + y*y + z*z) > r) continue;

                        BlockPos target = playerPos.add(x, y, z);
                        if (!mc.world.isAir(target)) {
                            sendBreakPackets(target);
                        }
                    }
                }
            }
        } else {
            // Survival mode: break one block per tick
            if (blocksToBreak.isEmpty()) {
                resetBlockList();
            }

            while (currentIndex < blocksToBreak.size()) {
                BlockPos target = blocksToBreak.get(currentIndex);
                currentIndex++;

                // Check if block still exists
                if (!mc.world.isAir(target)) {
                    sendBreakPackets(target);
                    break; // break only one block this tick
                }
            }

            // Reset if we reached end
            if (currentIndex >= blocksToBreak.size()) {
                resetBlockList();
            }
        }

        return false;
    }

    private void resetBlockList() {
        blocksToBreak.clear();
        currentIndex = 0;

        if (mc.player == null) return;

        int r = Math.min(range.getValueInt(), MAX_BREAK_RANGE);
        boolean isRounded = nukeType.getMode().equalsIgnoreCase("Rounded");
        BlockPos playerPos = mc.player.getBlockPos();

        for (int y = (flat.isEnabled() ? 0 : -r); y <= r; y++) {
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    if (flat.isEnabled() && y < 0) continue;
                    if (isRounded && Math.sqrt(x*x + y*y + z*z) > r) continue;

                    blocksToBreak.add(playerPos.add(x, y, z));
                }
            }
        }
    }

    private void sendBreakPackets(BlockPos pos) {
        mc.player.networkHandler.sendPacket(
                new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP)
        );
        mc.player.networkHandler.sendPacket(
                new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP)
        );

        if (swing.isEnabled()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
