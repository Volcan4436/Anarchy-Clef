package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.eventbus.EventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ESP extends Mod {

    public ESP() {
        super("ESP", "ESP", Mod.Category.RENDER);
    }


    public BooleanSetting players = new BooleanSetting("Players", true);
    public BooleanSetting monsters = new BooleanSetting("Monsters", true);
    public BooleanSetting passives = new BooleanSetting("Passives", true);
    public BooleanSetting invisibles = new BooleanSetting("Invisibles", true);
    public BooleanSetting tracers = new BooleanSetting("Tracers", true);


    @EventHandler
    public boolean onShitTick() {
        super.onTick();
        return false;
    }
    @Override
    public void onEnable() {
        super.onEnable();
    }
    @Override
    public void onDisable() {
        super.onDisable();
    }
    public boolean shouldRenderEntity(Entity entity) {
        if (players.isEnabled() && entity instanceof PlayerEntity) return true;
        if (monsters.isEnabled() && entity instanceof Monster) return true;
        if (passives.isEnabled() && (entity instanceof PassiveEntity || entity instanceof Entity)) return true;
        return invisibles.isEnabled() && entity.isInvisible();
    }
    @Override
    public void onWorldRender(MatrixStack matrices) {
        if (this.isEnabled()) {
            assert mc.world != null;
            for (PlayerEntity entity : mc.world.getPlayers()) {
                if (!(entity instanceof ClientPlayerEntity) && entity instanceof PlayerEntity) {
                    renderOutline(entity, new Color(255, 255, 255), matrices);
                    renderLine(entity, new Color(255, 255, 255), matrices);

                    renderHealthBG(entity, new Color(0, 0, 0, 255), matrices);
                    if (entity.getHealth() > 13) renderHealth(entity, new Color(0, 255, 0), matrices);
                    if (entity.getHealth() > 8 && entity.getHealth() <= 13)
                        renderHealth(entity, new Color(255, 255, 0), matrices);
                    if (entity.getHealth() <= 8) renderHealth(entity, new Color(255, 0, 0), matrices);
                    renderHealthOutline(entity, new Color(0, 0, 0), matrices);
                }
            }
            // do the same for monsters
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof Monster) {
                    renderOutline(entity, new Color(255, 0, 0), matrices);
                    renderLine(entity, new Color(255, 0, 0), matrices);
                }
            }
            // do the same for passives
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof PassiveEntity) {
                    renderOutline(entity, new Color(0, 255, 0), matrices);
                    renderLine(entity, new Color(0, 255, 0), matrices);
                }
            }
        }
        super.onWorldRender(matrices);
    }



    void renderOutline(Entity e, Color color, MatrixStack stack) {
        float red = color.getRed() / 255f;
        float green = color.getGreen() / 255f;
        float blue = color.getBlue() / 255f;
        float alpha = color.getAlpha() / 255f;
        Camera c = mc.gameRenderer.getCamera();
        Vec3d camPos = c.getPos();
        Vec3d start = e.getPos().subtract(camPos);
        float x = (float) start.x;
        float y = (float) start.y;
        float z = (float) start.z;

        double r = Math.toRadians(-c.getYaw() + 90);
        float sin = (float) (Math.sin(r) * (e.getWidth() / 1.7));
        float cos = (float) (Math.cos(r) * (e.getWidth() / 1.7));
        stack.push();

        Matrix4f matrix = stack.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES,
                VertexFormats.POSITION_COLOR);

        buffer.vertex(matrix, x + sin, y, z + cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin, y, z - cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin, y, z - cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin, y + e.getHeight(), z - cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin, y + e.getHeight(), z - cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin, y + e.getHeight(), z + cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin, y + e.getHeight(), z + cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin, y, z + cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin, y, z + cos).color(red, green, blue, alpha).next();

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
        stack.pop();
    }
    void renderHealthOutline(PlayerEntity e, Color color, MatrixStack stack) {
        float red = color.getRed() / 255f;
        float green = color.getGreen() / 255f;
        float blue = color.getBlue() / 255f;
        float alpha = color.getAlpha() / 255f;
        Camera c = mc.gameRenderer.getCamera();
        Vec3d camPos = c.getPos();
        Vec3d start = e.getPos().subtract(camPos);
        float x = (float) start.x;
        float y = (float) start.y;
        float z = (float) start.z;

        double r = Math.toRadians(-c.getYaw() + 90);
        float sin = (float) (Math.sin(r) * (e.getWidth() / 20));
        float cos = (float) (Math.cos(r) * (e.getWidth() / 20));
        stack.push();

        Matrix4f matrix = stack.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES,
                VertexFormats.POSITION_COLOR);

        buffer.vertex(matrix, x + sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y, z + cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y, z - cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y, z - cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y + e.getHeight(), z - cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y + e.getHeight(), z - cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y + e.getHeight(), z + cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y + e.getHeight(), z + cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y, z + cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y, z + cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
        stack.pop();
    }
    void renderHealth(PlayerEntity e, Color color, MatrixStack stack) {
        float red = color.getRed() / 255f;
        float green = color.getGreen() / 255f;
        float blue = color.getBlue() / 255f;
        float alpha = color.getAlpha() / 255f;
        Camera c = mc.gameRenderer.getCamera();
        Vec3d camPos = c.getPos();
        Vec3d start = e.getPos().subtract(camPos);
        float x = (float) start.x;
        float y = (float) start.y;
        float z = (float) start.z;

        double r = Math.toRadians(-c.getYaw() + 90);
        float sin = (float) (Math.sin(r) * (e.getWidth() / 20));
        float cos = (float) (Math.cos(r) * (e.getWidth() / 20));
        stack.push();

        Matrix4f matrix = stack.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        RenderSystem.setShaderColor(1f, 1f, 1f, (float) 0.50);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();   //debug lines
        buffer.begin(VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION_COLOR);

        buffer.vertex(matrix, x + sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y, z + cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y, z - cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y, z - cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y + e.getHeight() * (e.getHealth() / 20), z - cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y + e.getHeight() * (e.getHealth() / 20), z - cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y + e.getHeight() * (e.getHealth() / 20), z + cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y + e.getHeight() * (e.getHealth() / 20), z + cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y, z + cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
        stack.pop();
    }
    void renderHealthBG(PlayerEntity e, Color color, MatrixStack stack) {
        float red = color.getRed() / 255f;
        float green = color.getGreen() / 255f;
        float blue = color.getBlue() / 255f;
        float alpha = color.getAlpha() / 255f;
        Camera c = mc.gameRenderer.getCamera();
        Vec3d camPos = c.getPos();
        Vec3d start = e.getPos().subtract(camPos);
        float x = (float) start.x;
        float y = (float) start.y;
        float z = (float) start.z;

        double r = Math.toRadians(-c.getYaw() + 90);
        float sin = (float) (Math.sin(r) * (e.getWidth() / 20));
        float cos = (float) (Math.cos(r) * (e.getWidth() / 20));
        stack.push();

        Matrix4f matrix = stack.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        RenderSystem.setShaderColor(1f, 1f, 1f, 0.7f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();   //debug lines
        buffer.begin(VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION_COLOR);

        buffer.vertex(matrix, x + sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y, z + cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y, z - cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y, z - cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y + e.getHeight(), z - cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y + e.getHeight(), z - cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y + e.getHeight(), z + cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y + e.getHeight(), z + cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin + (float) (Math.sin(r) * (e.getWidth() - 0.2)), y, z + cos + (float) (Math.cos(r) * (e.getWidth() - 0.2))).color(red, green, blue, alpha).next();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
        stack.pop();
    }

    //Render a Line to all entities from the player
    void renderLine(Entity entity, Color color, MatrixStack stack) {
        float red = color.getRed() / 255f;
        float green = color.getGreen() / 255f;
        float blue = color.getBlue() / 255f;
        float alpha = color.getAlpha() / 255f;

        Vec3d playerPos = mc.player.getPos();
        Vec3d entityPos = entity.getPos();

        Vec3d playerVec = new Vec3d(playerPos.getX() + 0.5, playerPos.getY() + 0.5, playerPos.getZ() + 0.5);
        Vec3d entityVec = new Vec3d(entityPos.getX() + 0.5, entityPos.getY() + 0.5, entityPos.getZ() + 0.5);

        Camera c = mc.gameRenderer.getCamera();
        Vec3d camPos = c.getPos();

        Vec3d start = playerVec.subtract(camPos);
        Vec3d end = entityVec.subtract(camPos);

        float startX = (float) start.x;
        float startY = (float) start.y;
        float startZ = (float) start.z;

        float endX = (float) end.x;
        float endY = (float) end.y;
        float endZ = (float) end.z;

// Interpolation factor (0 = start, 1 = end)
        float interpFactor = 0.5f; // adjust this value to change the interpolation speed

        float interpX = startX + (endX - startX) * interpFactor;
        float interpY = startY + (endY - startY) * interpFactor;
        float interpZ = startZ + (endZ - startZ) * interpFactor;

        stack.push();

        Matrix4f matrix = stack.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        buffer.vertex(matrix, interpX, interpY, interpZ).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, endX, endY, endZ).color(red, green, blue, alpha).next();

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableBlend();
        stack.pop();
    }
}

