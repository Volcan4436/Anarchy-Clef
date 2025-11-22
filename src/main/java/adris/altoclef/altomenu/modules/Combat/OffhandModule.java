package adris.altoclef.altomenu.modules.Combat;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;

public class OffhandModule extends Mod {

    ModeSetting strictness = new ModeSetting("Strictness", "Normal", "Normal", "Strict", "Strict++");
    BooleanSetting totIntegrate = new BooleanSetting("TotIntegrate", true);
    NumberSetting minimumHealth = new NumberSetting("MinimumHealth", 0, 36, 8, 1);
    NumberSetting delayTicks = new NumberSetting("Delay", 0, 10, 3, 1); // ticks

    private long lastSwapTime = 0L;

    public OffhandModule() {
        super("OffhandModule", "Automatically moves crystals/totems to offhand.", Category.COMBAT);
        addSettings(strictness, totIntegrate, minimumHealth, delayTicks);
    }

    @EventHandler
    public boolean onShitTick() {
        if (mc.player == null || mc.world == null) return false;

        long now = System.currentTimeMillis();
        if (now - lastSwapTime < (long) delayTicks.getValue() * 50L) return false; // ticks -> ms

        boolean useTotem = totIntegrate.isEnabled() && mc.player.getHealth() <= minimumHealth.getValue();
        Item targetItem = useTotem ? Items.TOTEM_OF_UNDYING : Items.END_CRYSTAL;

        // Already holding the desired item
        if (mc.player.getOffHandStack().getItem() == targetItem) return false;

        // Find item index in player inventory (main list 0..35)
        int invIndex = findItemIndexInPlayerInventory(targetItem);
        if (invIndex == -1) return false;

        boolean swapped;
        switch (strictness.getMode()) {
            case "Normal" -> swapped = doNormalSwap(invIndex);
            case "Strict" -> swapped = doStrictSwap(invIndex, false);
            case "Strict++" -> swapped = doStrictSwap(invIndex, true);
            default -> swapped = false;
        }

        if (swapped) lastSwapTime = now;
        return swapped;
    }

    /**
     * Find index in player's main inventory (0..35). Searches main inventory and hotbar.
     */
    private int findItemIndexInPlayerInventory(Item item) {
        DefaultedList<ItemStack> inv = mc.player.getInventory().main;
        for (int i = 0; i < inv.size(); i++) {
            if (inv.get(i).getItem() == item) return i;
        }
        return -1;
    }

    private boolean doNormalSwap(int invIndex) {
        return sendSwapPackets(invIndex, false);
    }

    private boolean doStrictSwap(int invIndex, boolean strictPlus) {
        boolean swapped = sendSwapPackets(invIndex, strictPlus);

        // Only freeze motion if swap actually succeeded and strictPlus requested it
        if (strictPlus && swapped) {
            mc.player.setVelocity(new Vec3d(0, mc.player.getVelocity().y, 0));
        }
        return swapped;
    }

    /**
     * Sends the sequence of ClickSlot packets to move the item at player-inventory index `invIndex`
     * into the offhand (handler slot id 45). This method maps the player-inventory index to the
     * container/handler slot index so we don't accidentally click crafting slots.
     *
     * Returns true if we sent packets (success assumed). It is defensive: catches exceptions and returns false on error.
     */
    private boolean sendSwapPackets(int playerInvIndex, boolean strictMode) {
        try {
            var handler = mc.player.currentScreenHandler;
            DefaultedList<ItemStack> handlerStacks = handler.getStacks();

            // Map player-inventory index (0..35) to handler slot index by iterating handler.slots
            int handlerFromIndex = -1;
            int seenInventorySlots = 0;
            for (int i = 0; i < handler.slots.size(); i++) {
                Slot s = handler.slots.get(i);
                // slot.inventory references the Inventory backing this Slot.
                // The player's main inventory appears as mc.player.getInventory()
                if (s.inventory == mc.player.getInventory()) {
                    if (seenInventorySlots == playerInvIndex) {
                        handlerFromIndex = i;
                        break;
                    }
                    seenInventorySlots++;
                }
            }

            if (handlerFromIndex == -1) {
                // fallback: try hotbar mapping (36..44). Many handlers map hotbar differently.
                // If mapping failed, we bail to avoid mis-clicks.
                return false;
            }

            // Prepare the stack map required by ClickSlotC2SPacket
            var changed = new Int2ObjectOpenHashMap<ItemStack>();
            for (int i = 0; i < handlerStacks.size(); i++) {
                changed.put(i, handlerStacks.get(i).copy());
            }

            // Optionally send a position packet before (helpful for Strict++)
            if (strictMode) {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()
                ));
            }

            // PICKUP from source
            mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(
                    handler.syncId,
                    handler.getRevision(),
                    handlerFromIndex,
                    0,
                    SlotActionType.PICKUP,
                    handlerStacks.get(handlerFromIndex).copy(),
                    changed
            ));

            // PICKUP to offhand slot (45)
            // 45 is the vanilla window slot index for offhand in the player container.
            mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(
                    handler.syncId,
                    handler.getRevision(),
                    45,
                    0,
                    SlotActionType.PICKUP,
                    handlerStacks.size() > 45 ? handlerStacks.get(45).copy() : ItemStack.EMPTY,
                    changed
            ));

            // Close (Strict/Strict++): simulate closing the inventory so server applies transaction
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(handler.syncId));

            return true;
        } catch (Exception e) {
            // swallow and return false to not spam motion freeze etc.
            return false;
        }
    }
}
