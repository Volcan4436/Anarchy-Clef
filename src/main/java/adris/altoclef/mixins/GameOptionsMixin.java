package adris.altoclef.mixins;


import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.NarratorMode;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


// Credits: https://github.com/545u/NoNarrator
@Mixin(GameOptions.class)
public class GameOptionsMixin {
    @Final
    @Shadow
    private SimpleOption<NarratorMode> narrator;
    @Final
    @Shadow private SimpleOption<Boolean> narratorHotkey;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        // Disable the narrator and toggle off the narrator hotkey in the settings.
        narrator.setValue(NarratorMode.OFF);
        narratorHotkey.setValue(false);
    }
}