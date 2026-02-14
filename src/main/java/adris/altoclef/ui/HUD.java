package adris.altoclef.ui;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.command.HUDSettings;
import adris.altoclef.altomenu.command.impl.ToggleHud;
import adris.altoclef.altomenu.managers.ModuleManager;
import adris.altoclef.util.VersionUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.realms.Ping;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

//todo
// add Ping
// add TargetHUD
// add ArrayList
// add CPS Counter
// add Crystal Count
// add Obsidian Count
// add Totem Count
// add Potion Effect HUD
// show nearby players Positions on the HUD
// add minimap (?)
// add ServerIP
// add ServerLocation
// add ServerBrand
// add BPS Counter
// add Latest Death Position to HUD
// add Warning Popups
public class HUD {
    private static final Identifier CUSTOM_ICON = new Identifier("altoclef", "textures/gui/kisaragi.png");
    protected static MinecraftClient mc = MinecraftClient.getInstance();

    public static String clientVersion = VersionUtil.clientVersion;
    public static String cvUpdateName = VersionUtil.cvUpdateName;

    private static float hueOffset = 0f;
    private static final float RAINBOW_SPEED = 0.01f; // ~33s per cycle originally

    // Clock for frame-timed animation
    private static long lastMs = Util.getMeasuringTimeMs();

    public static void render(DrawContext context, float tickDelta) {
        if (HUDSettings.isToggleHUD()) {
        // Update hueOffset based on real elapsed time
        long nowMs = Util.getMeasuringTimeMs();
        float elapsed = (nowMs - lastMs) / 1000f; // seconds since last frame
        lastMs = nowMs;

        // Multiply elapsed by 120 FPS target
        hueOffset = (hueOffset + elapsed * 120f * RAINBOW_SPEED) % 1.0f;

        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        // Basic info
        context.drawText(mc.textRenderer, "HP: " + mc.player.getHealth(),
                screenWidth - mc.textRenderer.getWidth("HP: " + mc.player.getHealth()) - 3,
                screenHeight - 10, Color.red.getRGB(), false);

            String yawText = Math.round(((mc.player.getYaw() + 180f) % 360f + 360f) % 360f - 180f) + " :Yaw";
            String pitchText = Math.round(MathHelper.clamp(mc.player.getPitch(), -90f, 90f)) + " :Pitch";
        context.drawText(mc.textRenderer, yawText,
                screenWidth - mc.textRenderer.getWidth(yawText) - 3,
                screenHeight - 20, -1, false);
        context.drawText(mc.textRenderer, pitchText,
                screenWidth - mc.textRenderer.getWidth(pitchText) - 3,
                screenHeight - 30, -1, false);

        context.drawText(mc.textRenderer,
                "X: " + Math.round(mc.player.getX()) +
                        " Y: " + Math.round(mc.player.getY()) +
                        " Z: " + Math.round(mc.player.getZ()),
                screenWidth % 2 + 3, screenHeight - 40, -1, false);

        context.drawText(mc.textRenderer,
                "UUID: " + mc.player.getUuid().toString(),
                screenWidth % 2 + 3, screenHeight - 30, -1, false);

        context.drawText(mc.textRenderer,
                "FPS: " + mc.getCurrentFps(),
                screenWidth % 2 + 3, screenHeight - 20, Color.green.getRGB(), false);

        // Watermark
        int mainTextWidth = mc.textRenderer.getWidth(clientVersion);
        context.drawText(mc.textRenderer, clientVersion,
                screenWidth % 2 + 3, screenHeight - 10, -1, false);
        context.drawText(mc.textRenderer, cvUpdateName,
                screenWidth % 2 + 3 + mainTextWidth, screenHeight - 10,
                0xFFFFAA00, false);

        // Rainbow ArrayList
        renderArrayList(context);

        // Hibiki Icon
        int textureWidth = 305, textureHeight = 581;
        float scale = 0.2f;
        int drawWidth = (int)(textureWidth * scale), drawHeight = (int)(textureHeight * scale);
        context.drawTexture(CUSTOM_ICON, 5, 10, drawWidth, drawHeight,
                0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
        }
    }

    private static void renderArrayList(DrawContext context) {
        if (HUDSettings.isToggleHUD()) {
            TextRenderer tr = mc.textRenderer;
            int sw = mc.getWindow().getScaledWidth();
            int fh = tr.fontHeight;

            List<Mod> mods = ModuleManager.INSTANCE.getEnabledModules();
            mods.sort(Comparator.comparingInt(m -> tr.getWidth(m.getDisplayName())));

            for (int i = 0; i < mods.size(); i++) {
                String name = mods.get(i).getDisplayName();
                int textWidth = tr.getWidth(name);
                int baseX = sw - 4 - textWidth;
                int y0 = 46 + i * fh;
                int y1 = y0 + fh - 1;
                int x1 = sw - 2;

                // Background
                context.fill(baseX - 2, y0 - 1, x1 + 1, y1, 0x80000000);

                // Per-letter rainbow
                for (int j = 0; j < name.length(); j++) {
                    float letterHue = (hueOffset + (float) j / name.length()) % 1.0f;
                    int color = hsvToRgb(letterHue, 1f, 1f) | 0xFF000000;
                    String ch = name.substring(j, j + 1);
                    int charX = baseX + tr.getWidth(name.substring(0, j));
                    context.drawText(tr, ch, charX, y0, color, false);
                }

                // Right-hand rainbow line
                float lineHue = (hueOffset + (float) i / mods.size()) % 1.0f;
                int lineColor = hsvToRgb(lineHue, 1f, 1f) | 0xFF000000;
                context.drawVerticalLine(x1, y0 - 1, y1, lineColor);
            }
        }
    }

    private static int hsvToRgb(float h, float s, float v) {
        int i = (int)(h * 6);
        float f = h * 6 - i;
        int p = (int)(v * (1 - s) * 255),
                q = (int)(v * (1 - f * s) * 255),
                t = (int)(v * (1 - (1 - f) * s) * 255),
                vi = (int)(v * 255);
        switch (i % 6) {
            case 0 -> { return (vi << 16) | (t << 8) | p; }
            case 1 -> { return (q << 16) | (vi << 8) | p; }
            case 2 -> { return (p << 16) | (vi << 8) | t; }
            case 3 -> { return (p << 16) | (q << 8) | vi; }
            case 4 -> { return (t << 16) | (p << 8) | vi; }
            case 5 -> { return (vi << 16) | (p << 8) | q; }
            default -> { return 0; }
        }
    }
}
