package adris.altoclef.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.realms.Ping;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;

import java.awt.*;
import java.util.Objects;

public class HUD {

    protected static MinecraftClient mc = MinecraftClient.getInstance();

    //WaterMark
    public static void render(DrawContext context, float tickDelta) {


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
        context.drawText(mc.textRenderer, "AnarchyClef [BETA]", screenWidth % 2 + 3, screenHeight - 10, -1, false);
    }
}
