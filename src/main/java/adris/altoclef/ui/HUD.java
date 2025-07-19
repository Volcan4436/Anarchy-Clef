package adris.altoclef.ui;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.managers.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.realms.Ping;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.util.Identifier;

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
public class HUD {
    private static final Identifier CUSTOM_ICON = new Identifier("altoclef", "textures/gui/kisaragi.png");
    protected static MinecraftClient mc = MinecraftClient.getInstance();

    //Client WaterMark
    // Update this text whenever we do a major update
    public static String clientVersion = "AnarchyClef - b0.2.0 ";
    public static String cvUpdateName = "Visual Update";

    private static float hueOffset = 0f;
    private static final float RAINBOW_SPEED = 0.01f; // ~33s per full color cycle

    //HUD
    public static void render(DrawContext context, float tickDelta) {
        hueOffset = (hueOffset + tickDelta * RAINBOW_SPEED) % 1.0f;

        //Scale alongside GUIScale Setting and Monitor Size
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();


        context.drawText(mc.textRenderer, "HP: " + mc.player.getHealth(), screenWidth - mc.textRenderer.getWidth("HP: " + mc.player.getHealth()) - 3, screenHeight - 10, Color.red.getRGB(), false);
        String yawText = Math.round(-mc.player.getYaw() % 360) + " :Yaw";
        String pitchText = Math.round(mc.player.getPitch()) + " :Pitch";

        int yawX = screenWidth - 3 - mc.textRenderer.getWidth(yawText);
        context.drawText(mc.textRenderer, yawText, yawX, screenHeight - 20, -1, false);

        int pitchX = screenWidth - 3 - mc.textRenderer.getWidth(pitchText);
        context.drawText(mc.textRenderer, pitchText, pitchX, screenHeight - 30, -1, false);
        context.drawText(mc.textRenderer, "X: " + Math.round(mc.player.getX()) + " Y: " + Math.round(mc.player.getY()) + " Z: " + Math.round(mc.player.getZ()), screenWidth % 2 + 3, screenHeight - 40, -1, false);
        context.drawText(mc.textRenderer, "UUID: " + mc.player.getUuid().toString(), screenWidth % 2 + 3, screenHeight - 30, -1, false);
        context.drawText(mc.textRenderer, "FPS: " + mc.getCurrentFps(), screenWidth % 2 + 3, screenHeight - 20, Color.green.getRGB(), false);


        //Watermark
        int mainTextWidth = mc.textRenderer.getWidth(clientVersion);

        context.drawText(mc.textRenderer, clientVersion, screenWidth % 2 + 3, screenHeight - 10, -1, false);

        context.drawText(mc.textRenderer, cvUpdateName, screenWidth % 2 + 3 + mainTextWidth, screenHeight - 10, 0xFFFFAA00, false);

        renderArrayList(context, tickDelta);


        int textureWidth = 305;
        int textureHeight = 581;
        float scale = 0.2f;

        int drawWidth = (int) (textureWidth * scale);
        int drawHeight = (int) (textureHeight * scale);

        int x = 5;
        int y = 10;

        context.drawTexture(CUSTOM_ICON, x, y, drawWidth, drawHeight, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);

    }

    private static void renderArrayList(DrawContext context, float tickDelta) {
        TextRenderer tr = mc.textRenderer;
        int sw = mc.getWindow().getScaledWidth();
        int fh = tr.fontHeight;

        List<Mod> mods = ModuleManager.INSTANCE.getEnabledModules();
        mods.sort(Comparator.comparingInt(m -> tr.getWidth(m.getDisplayName())));

        for (int i = 0; i < mods.size(); i++) {
            String name = mods.get(i).getDisplayName();
            int textWidth = tr.getWidth(name);
            int y0 = 46 + i * fh;
            int y1 = y0 + fh - 1;
            int x0 = sw - textWidth - 2;
            int x1 = sw - 2;

            // Calculate baseX (position of the first letter)
            int baseX = sw - 4 - textWidth;

            // Draw background extending from just before first letter to just after vertical line
            int backgroundX0 = baseX - 2;
            int backgroundX1 = x1 + 1;
            context.fill(backgroundX0, y0 - 1, backgroundX1, y1, 0x80000000);

            // Draw per-letter rainbow text
            for (int j = 0; j < name.length(); j++) {
                float letterHue = (hueOffset + (float) j / name.length()) % 1.0f;
                int color = hsvToRgb(letterHue, 1f, 1f) | 0xFF000000;
                String ch = name.substring(j, j + 1);
                int charX = baseX + tr.getWidth(name.substring(0, j));
                context.drawText(tr, ch, charX, y0, color, false);
            }

            // Draw rainbow right-hand line
            float lineHue = (hueOffset + (float) i / mods.size()) % 1.0f;
            int lineColor = hsvToRgb(lineHue, 1f, 1f) | 0xFF000000;
            context.drawVerticalLine(x1, y0 - 1, y1, lineColor);
        }
    }


    // Converts HSV (0.0–1.0) to RGB packed int (0xRRGGBB)
    private static int hsvToRgb(float h, float s, float v) {
        int r, g, b;
        int i = (int) (h * 6);
        float f = h * 6 - i;
        int p = (int) (v * (1 - s) * 255);
        int q = (int) (v * (1 - f * s) * 255);
        int t = (int) (v * (1 - (1 - f) * s) * 255);
        int vi = (int) (v * 255);

        switch (i % 6) {
            case 0 -> {
                r = vi;
                g = t;
                b = p;
            }
            case 1 -> {
                r = q;
                g = vi;
                b = p;
            }
            case 2 -> {
                r = p;
                g = vi;
                b = t;
            }
            case 3 -> {
                r = p;
                g = q;
                b = vi;
            }
            case 4 -> {
                r = t;
                g = p;
                b = vi;
            }
            case 5 -> {
                r = vi;
                g = p;
                b = q;
            }
            default -> {
                r = 0;
                g = 0;
                b = 0;
            }
        }

        return (r << 16) | (g << 8) | b;
    }
}
