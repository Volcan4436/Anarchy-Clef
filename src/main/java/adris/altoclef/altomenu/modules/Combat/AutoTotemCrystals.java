package adris.altoclef.altomenu.modules.Combat;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.NumberSetting;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotemCrystals extends Mod {
    private final NumberSetting healthThreshold = new NumberSetting("Switch Health", 1, 20, 14, 1);
    private int switchCooldown = 0;

    //todo: Improve Name to make this shorter in the menu
    public AutoTotemCrystals() {
        super("AutoTotemCrystals", "Automatically switches between crystals and totems based on health", Category.COMBAT);
        this.addSetting(healthThreshold);
    }

    @Override
    public boolean onShitTick() {
        if (mc.player == null || mc.getNetworkHandler() == null) return false;

        // Handle cooldown
        if (switchCooldown > 0) {
            switchCooldown--;
            return false;
        }

        float currentHealth = mc.player.getHealth();
        float threshold = (float) healthThreshold.getValue();
        Item offhandItem = mc.player.getOffHandStack().getItem();

        // Determine what we should have in offhand
        boolean shouldHaveTotem = currentHealth <= threshold;
        Item desiredItem = shouldHaveTotem ? Items.TOTEM_OF_UNDYING : Items.END_CRYSTAL;

        // Only switch if needed
        if (offhandItem != desiredItem) {
            int slot = findItemSlot(desiredItem);
            if (slot != -1) {
                sendSwapPacket(slot);
                switchCooldown = 3; // 3 tick cooldown
            }
        }

        return false;
    }

    private int findItemSlot(Item item) {
        // Check hotbar first (slots 0-8)
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) {
                return i + 36; // Hotbar slots are 36-44 in container
            }
        }

        // Check main inventory (slots 9-35)
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) {
                return i; // Main inventory slots are 9-35 in container
            }
        }
        return -1;
    }

    private void sendSwapPacket(int slot) {
        // Create empty changes map
        Int2ObjectMap<ItemStack> changes = new Int2ObjectOpenHashMap<>();

        // Send the swap packet (slot <-> offhand)
        mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(
                mc.player.currentScreenHandler.syncId,
                mc.player.currentScreenHandler.getRevision(),
                slot,       // Source slot
                40,          // Offhand slot (always 40)
                SlotActionType.SWAP,
                ItemStack.EMPTY, // Empty cursor stack
                changes      // Empty changes map
        ));
    }
}