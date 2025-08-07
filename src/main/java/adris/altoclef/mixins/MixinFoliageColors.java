package adris.altoclef.mixins;


import adris.altoclef.altomenu.managers.AmbienceColorHolder;
import adris.altoclef.altomenu.modules.Render.Ambience;
import net.minecraft.client.color.world.FoliageColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(FoliageColors.class)
public class MixinFoliageColors {

    @Shadow
    @Mutable
    private static int[] colorMap;

    @Unique
    private static int lastColor = -1;

    @Unique
    private static int[] originalColorMap = null;

    @Inject(method = "setColorMap", at = @At("HEAD"), cancellable = true)
    private static void onSetColorMap(int[] pixels, CallbackInfo ci) {
        // Save the original vanilla color map on first call
        if (originalColorMap == null) {
            originalColorMap = Arrays.copyOf(pixels, pixels.length);
        }

        if (Ambience.INSTANCE.isEnabled()) {
            int currentColor = AmbienceColorHolder.currentColor;

            if (colorMap == null || currentColor != lastColor) {
                lastColor = currentColor;

                int[] filledMap = new int[pixels.length];
                Arrays.fill(filledMap, currentColor);
                colorMap = filledMap;
            }
            ci.cancel(); // override vanilla only if Ambience enabled
        } else {
            // Ambience disabled - restore original color map
            if (originalColorMap != null) {
                colorMap = Arrays.copyOf(originalColorMap, originalColorMap.length);
                lastColor = -1;
            }
            // Allow vanilla to proceed normally after restoring
        }
    }

    @Inject(method = "getColor", at = @At("HEAD"))
    private static void onGetColor(CallbackInfoReturnable<Integer> cir) {
        int currentColor = AmbienceColorHolder.currentColor;
        if (Ambience.INSTANCE.isEnabled() && currentColor != lastColor) {
            // If color changed while enabled, force re-apply color map with new color
            FoliageColors.setColorMap(new int[colorMap.length]);
        } else if (!Ambience.INSTANCE.isEnabled() && lastColor != -1) {
            // If module disabled but lastColor set, restore original color map
            lastColor = -1;
            if (originalColorMap != null) {
                FoliageColors.setColorMap(Arrays.copyOf(originalColorMap, originalColorMap.length));
            }
        }
    }
}
