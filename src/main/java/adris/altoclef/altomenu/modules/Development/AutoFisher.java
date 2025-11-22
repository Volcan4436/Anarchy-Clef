package adris.altoclef.altomenu.modules.Development;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import java.util.Random;
import net.minecraft.client.option.KeyBinding;

public class AutoFisher extends Mod {

    BooleanSetting autoSwap = new BooleanSetting("AutoSwap", true);
    ModeSetting swapMode = new ModeSetting("Swap", "Hotbar", "Hotbar", "EquiptOFF", "EquiptMAIN");

    BooleanSetting refresh = new BooleanSetting("Refresh", false);
    NumberSetting refreshStamp = new NumberSetting("RefreshStamp", 1, 40, 10, 1);

    BooleanSetting antiAFK = new BooleanSetting("AntiAFK", false);
    ModeSetting afkMode = new ModeSetting("AFKFilter", "MultiTap", "MultiTap", "Rotate", "Jump", "Multitap+Rot", "Multitap+Jump", "All");

    BooleanSetting swing = new BooleanSetting("Swing", true);

    private int lastRodSlot = -1;
    private int lastOffhandSlot = -1;
    private long lastCatchTime = 0;
    private long nextAFKAction = 0;
    private final Random rand = new Random();

    private long lastCastTime = 0;

    // For Multitap key tapping
    private KeyBinding lastTappedKey = null;
    private long keyPressTime = 0;

    public AutoFisher() {
        super("AutoFisher", "Automatic fishing system", Category.DEVELOPMENT);
    }

    @Override
    public void onEnable() {
        lastCatchTime = System.currentTimeMillis();
        lastCastTime = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        restoreItems();
    }

    @EventHandler
    public boolean onShitTick() {
        if (mc.player == null) return false;

        if (autoSwap.isEnabled()) handleSwap();

        // Only check for bites if enough time passed after cast
        if (System.currentTimeMillis() - lastCastTime > 600) {
            if (detectBobberBite()) {
                reelRod();
                lastCatchTime = System.currentTimeMillis();
                lastCastTime = System.currentTimeMillis();
                castRod();
            }
        }

        if (refresh.isEnabled()) handleRefresh();
        if (antiAFK.isEnabled()) handleAFK();

        return false;
    }

    // --------------------------
    // SWAP HANDLING
    // --------------------------
    private void handleSwap() {
        switch (swapMode.getMode()) {
            case "Hotbar" -> swapHotbar();
            case "EquiptOFF" -> equipOffhand();
            case "EquiptMAIN" -> equipMainHand();
        }
    }

    private void swapHotbar() {
        int rodSlot = findFishingRodInHotbar();
        if (rodSlot == -1) return;
        lastRodSlot = mc.player.getInventory().selectedSlot;
        mc.player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(rodSlot));
        mc.player.getInventory().selectedSlot = rodSlot;
    }

    private void equipOffhand() {
        int rodSlot = findFishingRod();
        if (rodSlot == -1) return;
        lastOffhandSlot = rodSlot;
        clickSwap(rodSlot, 45);
    }

    private void equipMainHand() {
        int rodSlot = findFishingRod();
        if (rodSlot == -1) return;
        lastRodSlot = mc.player.getInventory().selectedSlot;
        clickSwap(rodSlot, lastRodSlot + 36);
    }

    private void restoreItems() {
        if (lastRodSlot != -1) mc.player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(lastRodSlot));
        if (lastOffhandSlot != -1) clickSwap(lastOffhandSlot, 45);
    }

    private void clickSwap(int from, int to) {
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, from, 0,
                net.minecraft.screen.slot.SlotActionType.SWAP, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, to, 0,
                net.minecraft.screen.slot.SlotActionType.SWAP, mc.player);
    }

    private int findFishingRodInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.getItem() == Items.FISHING_ROD) return i;
        }
        return -1;
    }

    private int findFishingRod() {
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.getItem() == Items.FISHING_ROD) return i;
        }
        return -1;
    }

    // --------------------------
    // FISHING ACTIONS
    // --------------------------
    private void castRod() {
        Hand hand = mc.player.getMainHandStack().getItem() == Items.FISHING_ROD ? Hand.MAIN_HAND : Hand.OFF_HAND;
        if (swing.isEnabled()) mc.player.swingHand(hand);
        mc.interactionManager.interactItem(mc.player, hand);
        lastCastTime = System.currentTimeMillis();
    }

    private void reelRod() {
        Hand hand = mc.player.getMainHandStack().getItem() == Items.FISHING_ROD ? Hand.MAIN_HAND : Hand.OFF_HAND;
        if (swing.isEnabled()) mc.player.swingHand(hand);
        mc.interactionManager.interactItem(mc.player, hand);
    }

    private boolean detectBobberBite() {
        if (mc.player.fishHook == null) return false;
        if (!mc.player.fishHook.isTouchingWater()) return false;
        double velY = mc.player.fishHook.getVelocity().y;
        long now = System.currentTimeMillis();
        if (now - lastCatchTime < 500) return false;
        return velY < -0.25;
    }

    // --------------------------
    // REFRESH SYSTEM
    // --------------------------
    private void handleRefresh() {
        long now = System.currentTimeMillis();
        if (now - lastCatchTime > refreshStamp.getValue() * 1000) {
            reelRod();
            castRod();
            lastCatchTime = now;
        }
    }

    // --------------------------
    // ANTI-AFK
    // --------------------------
    private void handleAFK() {
        long now = System.currentTimeMillis();
        if (now < nextAFKAction) return;

        nextAFKAction = now + (3 + rand.nextInt(6)) * 1000; // 3-8 sec interval

        switch (afkMode.getMode()) {
            case "MultiTap" -> doMultiTap();
            case "Rotate" -> doRotate();
            case "Jump" -> doJump();
            case "Multitap+Rot" -> { doMultiTap(); doRotate(); }
            case "Multitap+Jump" -> { doMultiTap(); doJump(); }
            case "All" -> { doMultiTap(); doRotate(); doJump(); }
        }
    }

    private void doMultiTap() {
        long now = System.currentTimeMillis();

        // Release key if pressed for >50ms
        if (lastTappedKey != null && now - keyPressTime > 50) {
            lastTappedKey.setPressed(false);
            lastTappedKey = null;
        }

        // If no key is pressed, randomly tap a key
        if (lastTappedKey == null) {
            int r = rand.nextInt(4);
            KeyBinding key = switch (r) {
                case 0 -> mc.options.forwardKey;
                case 1 -> mc.options.backKey;
                case 2 -> mc.options.leftKey;
                default -> mc.options.rightKey;
            };
            key.setPressed(true);
            lastTappedKey = key;
            keyPressTime = now;
        }
    }

    private void doRotate() {
        mc.player.setYaw(mc.player.getYaw() + (rand.nextBoolean() ? 3 : -3));
    }

    private void doJump() {
        if (mc.player.isOnGround()) mc.player.jump();
    }
}
