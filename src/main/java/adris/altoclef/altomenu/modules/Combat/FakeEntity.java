package adris.altoclef.altomenu.modules.Combat;

import adris.altoclef.altomenu.Mod;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import com.mojang.authlib.GameProfile;

import java.util.UUID;

public class FakeEntity extends Mod {

    private OtherClientPlayerEntity fakePlayer = null;

    public FakeEntity() {
        super("FakeEntity", "Create client side fake entity", Category.WORLD);
    }

    @Override
    public void onEnable() {
        if (mc.world == null || mc.player == null) return;

        GameProfile fakeProfile = new GameProfile(UUID.randomUUID(), "FakePlayer");
        fakePlayer = new OtherClientPlayerEntity(mc.world, fakeProfile);

        Vec3d pos = mc.player.getPos();
        fakePlayer.setPosition(pos);
        fakePlayer.setYaw(mc.player.getYaw());
        fakePlayer.setPitch(mc.player.getPitch());
        fakePlayer.setHeadYaw(mc.player.getHeadYaw());

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            fakePlayer.equipStack(slot, mc.player.getEquippedStack(slot));
        }

        mc.world.addEntity(fakePlayer); // No ID needed, Minecraft handles it
    }

    @Override
    public void onDisable() {
        if (fakePlayer != null && mc.world != null) {
            fakePlayer.remove(Entity.RemovalReason.DISCARDED); // Correct way in 1.20+
            fakePlayer = null;
        }
    }
}
