package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.managers.ModuleManager;
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
import java.util.HashMap;
import java.util.Map;

public class ESP extends Mod {

    public ESP() {
        super("ESP", "ESP", Mod.Category.RENDER);
    }

    public BooleanSetting players = new BooleanSetting("Players", true);
    public BooleanSetting monsters = new BooleanSetting("Monsters", true);
    public BooleanSetting passives = new BooleanSetting("Passives", true);
    public BooleanSetting invisibles = new BooleanSetting("Invisibles", true);
    public BooleanSetting tracers = new BooleanSetting("Tracers", true);

    public static ESP getInstance() {
        return (ESP) ModuleManager.INSTANCE.getModuleByName("ESP");
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
                    if (tracers.isEnabled()) renderLine(entity, new Color(255, 255, 255), matrices);

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
                    if (tracers.isEnabled()) renderLine(entity, new Color(255, 0, 0), matrices);
                }
            }
            // do the same for passives
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof PassiveEntity) {
                    renderOutline(entity, new Color(0, 255, 0), matrices);
                    if (tracers.isEnabled()) renderLine(entity, new Color(0, 255, 0), matrices);
                }
            }
        }
        super.onWorldRender(matrices);
    }
    void renderOutline(Entity entity, Color color, MatrixStack stack) {
        // Disable depth test for through-block rendering
        RenderSystem.disableDepthTest();
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        // Convert color components
        float red = color.getRed() / 255f;
        float green = color.getGreen() / 255f;
        float blue = color.getBlue() / 255f;
        float alpha = color.getAlpha() / 255f;

        // Get interpolated position
        float tickDelta = mc.getTickDelta();
        Vec3d entityPos = prevEntityPositions.getOrDefault(entity, entity.getPos())
                .lerp(entity.getPos(), tickDelta);

        // Get camera position
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        // Calculate relative position (center of entity)
        Vec3d center = entityPos.subtract(camPos).add(0, entity.getHeight()/2, 0);
        float x = (float) center.x;
        float y = (float) center.y;
        float z = (float) center.z;

        // Calculate rotation once
        double rotation = Math.toRadians(-camera.getYaw() + 90);
        float sin = (float) (Math.sin(rotation) * (entity.getWidth() / 1.7));
        float cos = (float) (Math.cos(rotation) * (entity.getWidth() / 1.7));

        // Setup rendering
        stack.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        // Enhanced line rendering
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(1.8f);
        GL11.glEnable(GL11.GL_POLYGON_SMOOTH);

        // Get matrix after all transformations
        Matrix4f matrix = stack.peek().getPositionMatrix();

        // Build buffer
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        // Draw box outline (optimized to avoid duplicate vertices)
        float halfHeight = entity.getHeight()/2;

        // Bottom square
        buffer.vertex(matrix, x + sin, y - halfHeight, z + cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin, y - halfHeight, z - cos).color(red, green, blue, alpha).next();

        buffer.vertex(matrix, x - sin, y - halfHeight, z - cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin, y + halfHeight, z - cos).color(red, green, blue, alpha).next();

        buffer.vertex(matrix, x - sin, y + halfHeight, z - cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin, y + halfHeight, z + cos).color(red, green, blue, alpha).next();

        buffer.vertex(matrix, x + sin, y + halfHeight, z + cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin, y - halfHeight, z + cos).color(red, green, blue, alpha).next();

        // Vertical lines
        buffer.vertex(matrix, x + sin, y - halfHeight, z + cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x + sin, y + halfHeight, z + cos).color(red, green, blue, alpha).next();

        buffer.vertex(matrix, x - sin, y - halfHeight, z - cos).color(red, green, blue, alpha).next();
        buffer.vertex(matrix, x - sin, y + halfHeight, z - cos).color(red, green, blue, alpha).next();

        // Draw
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // Restore GL state
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
        RenderSystem.disableBlend();
        stack.pop();

        // Restore depth test
        RenderSystem.enableDepthTest();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
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
    private Vec3d prevPlayerPos = Vec3d.ZERO;
    private Vec3d prevEntityPos = Vec3d.ZERO;
    private Map<Entity, Vec3d> prevEntityPositions = new HashMap<>();
    void renderLine(Entity entity, Color color, MatrixStack stack) {
        // Enable rendering through blocks
        RenderSystem.disableDepthTest();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        // Convert color components
        float red = color.getRed() / 255f;
        float green = color.getGreen() / 255f;
        float blue = color.getBlue() / 255f;
        float alpha = color.getAlpha() / 255f;

        // Get interpolated positions
        float tickDelta = mc.getTickDelta();
        Vec3d playerPos = prevPlayerPos.lerp(mc.player.getPos(), tickDelta);
        Vec3d entityPos = prevEntityPositions.getOrDefault(entity, entity.getPos())
                .lerp(entity.getPos(), tickDelta);

        // Get camera position
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        // Calculate relative positions
        Vec3d start = playerPos.subtract(camPos);
        Vec3d end = entityPos.subtract(camPos);
        // Setup rendering
        stack.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1.5f);

        // Get matrix
        Matrix4f matrix = stack.peek().getPositionMatrix();

        // Build buffer
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        // Add vertices
        buffer.vertex(matrix, (float)start.x, (float)start.y, (float)start.z)
                .color(red, green, blue, alpha)
                .next();
        buffer.vertex(matrix, (float)end.x, (float)end.y, (float)end.z)
                .color(red, green, blue, alpha)
                .next();

        // Draw and cleanup
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.disableBlend();
        stack.pop();
    }

    // Add this method to update previous positions each tick
    @EventHandler
    public boolean onShitTick() {
        prevPlayerPos = mc.player.getPos();

        // Update all tracked entities
        for (Entity entity : mc.world.getEntities()) {
            if (shouldRenderEntity(entity)) {
                prevEntityPositions.put(entity, entity.getPos());
            }
        }

        // Clean up old entities
        prevEntityPositions.keySet().removeIf(e -> !e.isAlive());
        return false;
    }
}

