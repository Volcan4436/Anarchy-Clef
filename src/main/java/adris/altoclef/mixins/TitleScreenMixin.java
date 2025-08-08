package adris.altoclef.mixins;

import adris.altoclef.util.VersionUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static adris.altoclef.altomenu.command.ChatUtils.mc;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo info) {
        int y = 2;
        context.drawTextWithShadow(mc.textRenderer, VersionUtil.clientVersion + " " + VersionUtil.cvUpdateName, 4, y, 0xFFFFAA00);
        context.drawTextWithShadow(mc.textRenderer, "by: Volcan & ChiefWarCry", 4, y + 12, Color.white.getRGB());

        context.drawTextWithShadow(mc.textRenderer, "Credits:", 4, y + 24, Color.white.getRGB());
        context.drawTextWithShadow(mc.textRenderer, "TacoTechnica (AltoClef Founder)", 4, y + 36, Color.white.getRGB());
        context.drawTextWithShadow(mc.textRenderer, "Lagoon (Coding Help)", 4, y + 48, Color.white.getRGB());
        context.drawTextWithShadow(mc.textRenderer, "Hearty (Implementing Scripting API)", 4, y + 60, Color.white.getRGB());
        context.drawTextWithShadow(mc.textRenderer, "Marvion (Base Fork)", 4, y + 72, Color.white.getRGB());

    }
}
