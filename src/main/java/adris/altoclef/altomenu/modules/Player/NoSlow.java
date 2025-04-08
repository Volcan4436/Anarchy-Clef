package adris.altoclef.altomenu.modules.Player;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class NoSlow extends Mod {

    public BooleanSetting items = new BooleanSetting("Items", true);
    public BooleanSetting sneaking = new BooleanSetting("Sneaking", false);
    public BooleanSetting webs = new BooleanSetting("Webs", false);

    public NoSlow() {
        super("NoSlow", "RAWHHHH", Category.MOVEMENT);
        addSettings(items, sneaking, webs);
    }
    private int ticks = 0;

    public static boolean doesBoxTouchBlock(Box box, Block block) {
        for (int x = (int) Math.floor(box.minX); x < Math.ceil(box.maxX); x++) {
            for (int y = (int) Math.floor(box.minY); y < Math.ceil(box.maxY); y++) {
                for (int z = (int) Math.floor(box.minZ); z < Math.ceil(box.maxZ); z++) {
                    if (mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == block) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onDisable() {
        ticks = 0;
        super.onDisable();
    }

    // items no slow handled in ClientPlayerMixin

    @Override
    public void onEnable() {
        ticks = 0;
        super.onEnable();
    }

    @EventHandler
    public boolean onShitTick() {
        if (webs.isEnabled() && doesBoxTouchBlock(mc.player.getBoundingBox(), Blocks.COBWEB)) {
            mc.player.slowMovement(mc.world.getBlockState(mc.player.getBlockPos()), new Vec3d(2.35, 1.75, 2.35));
        }
        return false;
    }
}
