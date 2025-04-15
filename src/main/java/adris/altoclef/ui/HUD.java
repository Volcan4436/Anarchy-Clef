package adris.altoclef.ui;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.managers.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.realms.Ping;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;

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

    protected static MinecraftClient mc = MinecraftClient.getInstance();

    //Client WaterMark
    // Update this text whenever we do a major update
    public static String clientVersion = "AnarchyClef - b0.1.0 ";
    public static String cvUpdateName = "Stealer Update";

    //HUD
    public static void render(DrawContext context, float tickDelta) {


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

    }

    public static void renderArrayList(DrawContext context, float tickDelta) {
        int xOffset = -5;
        int yOffset = 5;
        int index = 0;
        List<Mod> enabled = ModuleManager.INSTANCE.getEnabledModules();
        int sWidth = mc.getWindow().getScaledWidth();
        int sHeight = mc.getWindow().getScaledHeight();
        int lastWidth;
        int fHeight = mc.textRenderer.fontHeight;
        int fromY = (fHeight - 1) * (index) + 1;
        int toX = sWidth - 2;
        int toY = (fHeight - 1) * (index) + fHeight;


        enabled.sort(Comparator.comparingInt(m -> (int)mc.textRenderer.getWidth(((Mod)m).getDisplayName())).reversed());

        for (Mod mod : enabled) {
            context.fill((sWidth + 100) - mc.textRenderer.getWidth(mod.getDisplayName()) - 1, 46 + (index * mc.textRenderer.fontHeight), (sWidth - 4) - mc.textRenderer.getWidth(mod.getDisplayName()) - 2, 47 + (index * mc.textRenderer.fontHeight - 1) + mc.textRenderer.fontHeight, 0x80000000);

            context.drawText(mc.textRenderer, mod.getDisplayName(), (sWidth - 4) - mc.textRenderer.getWidth(mod.getDisplayName()), 47 + (index * mc.textRenderer.fontHeight), Color.red.getRGB(), false);

            context.fill((sWidth - 4) - mc.textRenderer.getWidth(mod.getDisplayName()) - 1, 46 + (index * mc.textRenderer.fontHeight), (sWidth - 4) - mc.textRenderer.getWidth(mod.getDisplayName()) - 2, 46 + (index * mc.textRenderer.fontHeight) + mc.textRenderer.fontHeight, Color.red.getRGB());
            index++;
        }
    }
}
