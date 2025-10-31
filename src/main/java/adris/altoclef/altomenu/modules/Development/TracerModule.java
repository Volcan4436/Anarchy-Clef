package adris.altoclef.altomenu.modules.Development;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
// Can't get the fucking tracer to stop bobbing with the hand. the dot is also broken..
public class TracerModule extends Mod {

    public ModeSetting drawMode = new ModeSetting("Draw Mode", "DrawFromFeet", "DrawFromFeet", "DrawFromCrosshair");
    public ModeSetting entityMode = new ModeSetting("Entity Mode", "Players", "Players", "Mobs", "Creatures", "Tames", "Named", "All");
    public ModeSetting colorMode = new ModeSetting("Color Mode", "Normal", "Normal", "Closest");

    public BooleanSetting rgb = new BooleanSetting("RGB", false);
    public BooleanSetting dot = new BooleanSetting("Dot", true);
    public BooleanSetting adaptiveDot = new BooleanSetting("Adaptive Dot", true);

    public NumberSetting rSet = new NumberSetting("R", 0, 255, 255, 1);
    public NumberSetting gSet = new NumberSetting("G", 0, 255, 0, 1);
    public NumberSetting bSet = new NumberSetting("B", 0, 255, 0, 1);
    public NumberSetting dotSize = new NumberSetting("Dot Size", 0.05, 1.0, 0.08, 0.01);

    private Vec3d prevPlayerPos = Vec3d.ZERO;
    private final Map<Entity, Vec3d> prevEntityPos = new HashMap<>();

    public TracerModule() {
        super("Tracers", "Draws tracers and dots to entities.", Category.RENDER);
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        if (!this.isEnabled() || mc.world == null || mc.player == null) return;

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        float tickDelta = mc.getTickDelta();

        double minDist = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || !(e instanceof LivingEntity) || !shouldRenderEntity(e)) continue;
            minDist = Math.min(minDist, mc.player.distanceTo(e));
        }

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || !(e instanceof LivingEntity) || !shouldRenderEntity(e)) continue;
            if (e.isInvisibleTo(mc.player)) continue;

            // Interpolated positions
            Vec3d playerPos = getStablePlayerPos(tickDelta);
            Vec3d entityPos = prevEntityPos.getOrDefault(e, e.getPos()).lerp(e.getPos(), tickDelta);

            // Start point of tracer
            Vec3d start = drawMode.isMode("DrawFromFeet")
                    ? new Vec3d(playerPos.x, playerPos.y + 0.1, playerPos.z)
                    : getScreenCenterVec(tickDelta);

            Vec3d end = entityPos.add(0, e.getHeight() / 2, 0);
            Color color = getColor(e, minDist);

            renderLine(matrices, start.subtract(camPos), end.subtract(camPos), color);

            if (dot.isEnabled()) {
                double dist = mc.player.distanceTo(e);
                renderDot(matrices, end.subtract(camPos), color, camera, dist);
            }
        }
    }

    private Vec3d getStablePlayerPos(float tickDelta) {
        double x = MathHelper.lerp(tickDelta, prevPlayerPos.x, mc.player.getX());
        double y = MathHelper.lerp(tickDelta, prevPlayerPos.y, mc.player.getY());
        double z = MathHelper.lerp(tickDelta, prevPlayerPos.z, mc.player.getZ());
        return new Vec3d(x, y, z);
    }

    private Vec3d getScreenCenterVec(float tickDelta) {
        float yaw = mc.player.getYaw(tickDelta);
        float pitch = mc.player.getPitch(tickDelta);
        Vec3d eye = mc.player.getEyePos();
        double f = Math.cos(-Math.toRadians(yaw) - Math.PI);
        double g = Math.sin(-Math.toRadians(yaw) - Math.PI);
        double h = -Math.cos(-Math.toRadians(pitch));
        double i = Math.sin(-Math.toRadians(pitch));
        Vec3d dir = new Vec3d(g * h, i, f * h);
        return eye.add(dir);
    }

    private void renderLine(MatrixStack stack, Vec3d start, Vec3d end, Color color) {
        RenderSystem.disableDepthTest();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1.5f);

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        Matrix4f matrix = stack.peek().getPositionMatrix();
        BufferBuilder buf = Tessellator.getInstance().getBuffer();
        buf.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        buf.vertex(matrix, (float) start.x, (float) start.y, (float) start.z).color(r, g, b, 1f).next();
        buf.vertex(matrix, (float) end.x, (float) end.y, (float) end.z).color(r, g, b, 1f).next();
        BufferRenderer.drawWithGlobalProgram(buf.end());

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private void renderDot(MatrixStack stack, Vec3d pos, Color color, Camera camera, double distance) {
        RenderSystem.disableDepthTest();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        double base = dotSize.getValue();
        double radius = adaptiveDot.isEnabled() ? base * Math.max(1, distance * 0.05) : base;

        Matrix4f matrix = stack.peek().getPositionMatrix();
        BufferBuilder buf = Tessellator.getInstance().getBuffer();
        buf.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        org.joml.Quaternionf rotation = new org.joml.Quaternionf(camera.getRotation()).conjugate();

        for (int i = 0; i <= 360; i += 10) {
            double ang = Math.toRadians(i);
            org.joml.Vector3f offset = new org.joml.Vector3f((float) (Math.cos(ang) * radius), (float) (Math.sin(ang) * radius), 0);
            offset.rotate(rotation);
            buf.vertex(matrix,
                            (float) (pos.x + offset.x),
                            (float) (pos.y + offset.y),
                            (float) (pos.z + offset.z))
                    .color(r, g, b, 1f)
                    .next();
        }

        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private boolean shouldRenderEntity(Entity e) {
        switch (entityMode.getMode()) {
            case "Players": return e instanceof PlayerEntity;
            case "Mobs": return !e.getType().getSpawnGroup().isPeaceful();
            case "Creatures": return e.getType().getSpawnGroup().isPeaceful();
            case "Tames": return e instanceof TameableEntity;
            case "Named": return e.hasCustomName();
            case "All": return true;
            default: return false;
        }
    }

    private Color getColor(Entity e, double minDist) {
        if (colorMode.isMode("Closest")) {
            double dist = mc.player.distanceTo(e);
            double t = MathHelper.clamp(dist / 30.0, 0.0, 1.0);
            int r = (int) (255 * (1 - t));
            int g = (int) (255 * t);
            return new Color(r, g, 0);
        }
        if (rgb.isEnabled()) {
            float hue = (System.currentTimeMillis() % 5000L) / 5000f;
            return Color.getHSBColor(hue, 1f, 1f);
        }
        return new Color((int) rSet.getValue(), (int) gSet.getValue(), (int) bSet.getValue());
    }

    @EventHandler
    public boolean onShitTick() {
        prevPlayerPos = mc.player.getPos();
        for (Entity e : mc.world.getEntities()) {
            if (shouldRenderEntity(e)) prevEntityPos.put(e, e.getPos());
        }
        prevEntityPos.keySet().removeIf(e -> !e.isAlive());
        return false;
    }

    @Override
    public void onEnable() {
        prevEntityPos.clear();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        prevEntityPos.clear();
        super.onDisable();
    }
}
