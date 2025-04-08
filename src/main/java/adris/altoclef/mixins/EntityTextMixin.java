package adris.altoclef.mixins;

import adris.altoclef.altomenu.modules.Render.RenderTags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.systems.RenderSystem;

@Mixin(EntityRenderDispatcher.class)
public class EntityTextMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(Entity entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || !RenderTags.Instance.isEnabled()) {
            return;
        }

        if (entity instanceof ItemEntity || entity == client.player) {
            return; // Skip items and player
        }

        String entityName = entity.getType().getName().getString();

        if (entity instanceof ItemEntity) {
            Item item = ((ItemEntity) entity).getStack().getItem();
            entityName = item.getName().getString();
        }


        double distanceSq = client.player.squaredDistanceTo(entity);
        if (distanceSq > 400) {
            return; // Only render entities within 20 blocks
        }

        TextRenderer textRenderer = client.textRenderer;
        String entityText = entity.getType().getName().getString() + " | " + (int) entity.getX() + " " + (int) entity.getY() + " " + (int) entity.getZ();
        Text text = Text.literal(entityText);

        matrices.push();
        matrices.translate(x, y + entity.getHeight() + 0.5, z); // Position above entity
        matrices.multiply(client.getEntityRenderDispatcher().getRotation()); // Make text face the player

        // Fix mirroring issue
        matrices.scale(-0.025f, -0.025f, 0.025f);

        // ✅ Ensure the text is drawn without depth testing (so it renders through walls)
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false); // Prevent depth writing
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float textWidth = textRenderer.getWidth(text) / 2f;
        textRenderer.drawWithOutline(text.asOrderedText(), -textWidth, 0, 0xFFFFFF, 0x000000, matrices.peek().getPositionMatrix(), vertexConsumers, 0xF000F0);

        // ✅ Restore normal rendering behavior after drawing the text
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }
}

