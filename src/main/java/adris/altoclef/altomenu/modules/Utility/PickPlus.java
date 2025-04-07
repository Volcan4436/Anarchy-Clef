package adris.altoclef.altomenu.modules.Utility;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WritableBookItem;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class PickPlus extends Mod {

    public PickPlus() {
        super("PacketMine", "Leploit", Mod.Category.DEVELOPMENT);
    }

    ModeSetting mode = new ModeSetting("Mode", "DefaultMode", "Mode1", "Mode2", "Mode3");
    NumberSetting number = new NumberSetting("NumSet", 1, 5, 3, 1); // Minimum 1, Maximum 5, Default 3, Increment 1
    BooleanSetting booleanset = new BooleanSetting("NameBooleanSet", true);
    boolean Shouldmine = false;
    @Override
    public boolean onCockAndBallTorture() {
        // Check if the attack key was pressed
        if (mc.options.attackKey.wasPressed()) {
            Shouldmine = true;

            // If the crosshair target is a block, start mining
            if (mc.crosshairTarget instanceof BlockHitResult) {
                BlockHitResult hitResult = (BlockHitResult) mc.crosshairTarget;
                BlockPos blockPos = hitResult.getBlockPos();

                // Send the START_DESTROY_BLOCK packet to start mining
                Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, hitResult.getSide()));
            }
        }

        // Handle block destruction if the target is a block
        if (Shouldmine && mc.crosshairTarget instanceof BlockHitResult) {
            BlockHitResult hitResult = (BlockHitResult) mc.crosshairTarget;
            BlockPos blockPos = hitResult.getBlockPos();
            assert mc.world != null;
            BlockState blockState = mc.world.getBlockState(blockPos);

            // Only stop mining if the target is not air (if it's a block)
            if (blockState.getBlock() != Blocks.AIR) {
                Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, hitResult.getSide()));
                Shouldmine = false;
            }
            else {
                assert mc.player != null;
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }

        // Skip block mining logic if the target is an entity (like a player)
        if (mc.crosshairTarget instanceof EntityHitResult) {
            return false;  // Do nothing, so normal attack logic can proceed
        }

        return false;
    }
}

