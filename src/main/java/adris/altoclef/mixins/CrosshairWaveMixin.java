package adris.altoclef.mixins;

import adris.altoclef.altomenu.modules.Render.crosshairRGH;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(InGameHud.class)
public abstract class CrosshairWaveMixin {
    @Shadow
    @Final
    private MinecraftClient client;
    
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void renderRgbCrosshair(DrawContext context, CallbackInfo ci) {
        if (!crosshairRGH.Instance.isEnabled()) return;
        
        ci.cancel(); // Cancel the original crosshair rendering
        
        int width = this.client.getWindow().getScaledWidth();
        int height = this.client.getWindow().getScaledHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        
        // Calculate RGB wave effect
        long time = System.currentTimeMillis();
        float hue = (time % 2000) / 2000f; // 2 second cycle
        int rgbColor = Color.HSBtoRGB(hue, 1.0f, 1.0f);
        
        // Crosshair size
        int size = crosshairRGH.Size.getValueInt();
        int thickness = crosshairRGH.Thickness.getValueInt();
        
        // Draw RGB crosshair
        // Horizontal line
        context.fill(centerX - size, centerY - thickness, 
                    centerX + size, centerY + thickness, rgbColor);
        
        // Vertical line
        context.fill(centerX - thickness, centerY - size, 
                    centerX + thickness, centerY + size, rgbColor);
    }
}