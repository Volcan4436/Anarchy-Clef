package adris.altoclef.altomenu.modules.Player;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.modules.Render.Fullbright;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.Objects;

// This is Currently a Mixin
public class Velocity extends Mod {

    //todo:
    // modes: Vanilla, Clutch (Attempts to Decrease Damage by Placing Blocks below you), PacketAbuse, NCP, Matrix (Decrease + Full), GroundSpoof, NoGround, Collision, Jump (Legit)
    // Add 1.8 Fix (Makes our modes work on 1.8) (Essentially GroundSpoofing)
    // Add Boat Clutch (And AutoBreak option using an Axe)
    // Support WaterBucket (Use the Bot's Clutch Feature)
    // Hook into Baritone (Allow Baritone to Toggle this Module as a FailSafe if there is no Bucket and SafeMode isn't on)
    public static Velocity Instance;
    public Velocity() {
        super("Velocity", "Decreased the amount of damage you take from falling", Mod.Category.PLAYER);
        Instance = this;
    }
    @Override
    public void onEnable() {
        System.out.println("fullbright enabled");
        super.onEnable();
    }

    @Override
    public void onDisable() {
        System.out.println("fullbright disabled");
        super.onDisable();
    }

    @EventHandler
    public boolean onShitTick() {

        return false;
    }
}
