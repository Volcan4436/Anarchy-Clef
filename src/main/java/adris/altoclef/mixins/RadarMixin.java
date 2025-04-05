package adris.altoclef.mixins;

import adris.altoclef.altomenu.modules.Render.Radar;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(InGameHud.class)
public abstract class RadarMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At("HEAD"))
    private void renderRadar(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (this.client.options.hudHidden || !Radar.Instance.isEnabled()) {
            return;
        }

        // Radar box dimensions and position
        int width = 100;
        int height = 100;
        int x = 5;  // 5 pixels from left
        int y = 5;  // 5 pixels from top

        // Draw semi-transparent background
        context.fill(x, y, x + width, y + height, 0x80000000);

        // Draw radar border
        context.drawBorder(x, y, width, height, 0xFFAAAAAA);

        // Center of radar (player position)
        int centerX = x + width / 2;
        int centerY = y + height / 2;

        // Get player and world
        ClientPlayerEntity player = this.client.player;
        if (player == null) return;

        World world = player.getWorld();

        // Calculate chunk radius (6 chunks = 96 blocks)
        double radius = 6 * 16;

        // Find hostile mobs in radius
        Box searchBox = player.getBoundingBox().expand(radius);
        List<HostileEntity> hostiles = world.getEntitiesByClass(
            HostileEntity.class,
            searchBox,
            e -> true
        );

        // Draw hostiles on radar
        for (HostileEntity hostile : hostiles) {
            Vec3d relativePos = hostile.getPos().subtract(player.getPos());

            // Scale position to radar size (1 chunk = ~8 pixels)
            int hostileX = centerX + (int)(relativePos.x / 2);
            int hostileY = centerY + (int)(relativePos.z / 2);

            // Ensure position is within radar bounds
            hostileX = MathHelper.clamp(hostileX, x + 1, x + width - 2);
            hostileY = MathHelper.clamp(hostileY, y + 1, y + height - 2);

            // Draw hostile as red square
            context.fill(hostileX - 2, hostileY - 2, hostileX + 2, hostileY + 2, 0xFFFF0000);
        }

        // Draw player position marker
        context.fill(centerX - 1, centerY - 1, centerX + 1, centerY + 1, 0xFF00FF00);
    }
}