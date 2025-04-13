package adris.altoclef.altomenu.modules.Player;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.Objects;

public class NoFall extends Mod {

    //todo:
    // modes: Vanilla, Clutch (Attempts to Decrease Damage by Placing Blocks below you), PacketAbuse, NCP, Matrix (Decrease + Full), GroundSpoof, NoGround, Collision
    // Add 1.8 Fix (Makes our modes work on 1.8) (Essentially GroundSpoofing)
    // Add Boat Clutch (And AutoBreak option using an Axe)
    // Support WaterBucket (Use the Bot's Clutch Feature)
    // Hook into Baritone (Allow Baritone to Toggle this Module as a FailSafe if there is no Bucket and SafeMode isn't on)

    public NoFall() {
        super("NoFall", "Decreased the amount of damage you take from falling", Mod.Category.PLAYER);
    }

    ModeSetting mode = new ModeSetting("Mode", "Packet", "Packet", "Velocity", "Jump", "Position", "dev");
    boolean velocityCheck = false; //we need a cleaner implementation
    boolean positionCheck = false; //we need a cleaner implementation
    boolean jumpCheck = false; //we need a cleaner implementation

    //TODO ASAP: Fix these to work at any fall distance @ChiefWarCry
    @EventHandler
    public boolean onShitTick() {
        if (mc.world == null || mc.player == null) return true;
        Block getBlockBelow = mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock();
        Block getBlockBelowFailSafe = mc.world.getBlockState(mc.player.getBlockPos().down(2)).getBlock();
        if (Objects.equals(mode.getMode(), "Packet")) {
            if (mc.player.fallDistance != 0) {
                Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
            }
        }
        if (Objects.equals(mode.getMode(), "Velocity")) {
            if (getBlockBelow != Blocks.AIR && mc.player.fallDistance > 3 && !velocityCheck) {
                mc.player.setVelocity(mc.player.getVelocity().x, 0.1, mc.player.getVelocity().z);
                velocityCheck = true;
            }
            if (mc.player.fallDistance == 0 && velocityCheck) velocityCheck = false;
        }
        else if (Objects.equals(mode.getMode(), "Jump")) {
            if (getBlockBelow != Blocks.AIR && mc.player.fallDistance > 3 && !jumpCheck) {
                mc.player.jump();
                jumpCheck = true;
            }
            if (mc.player.fallDistance == 0 && jumpCheck) jumpCheck = false;
        }
        else if (Objects.equals(mode.getMode(), "Position")) {
            if (getBlockBelow != Blocks.AIR && mc.player.fallDistance > 3 && !positionCheck) {
                mc.player.updatePosition(mc.player.getX(), mc.player.getY() + 1, mc.player.getZ());
                positionCheck = true;
            }
            if (mc.player.fallDistance == 0 && positionCheck) positionCheck = false;
        }
        else if (Objects.equals(mode.getMode(), "dev")) {
            System.out.println("This is For Debug Purposes");
            //System.out.println("Fall Distance: " + mc.player.fallDistance);
            toggle();
        }

        return false;
    }
}
