package adris.altoclef.mixins;

import adris.altoclef.AltoClef;
import adris.altoclef.altomenu.managers.ModuleManager;
import adris.altoclef.altomenu.modules.Movement.Flight;
import adris.altoclef.altomenu.modules.Player.NoSlow;
import adris.altoclef.altomenu.modules.Render.NoBob;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @ModifyVariable(method = "bobView", at = @At(value = "STORE", ordinal = 0), ordinal = 3)
    private float onBobView(float value) {
        if (ModuleManager.INSTANCE.getModuleByClass(NoBob.class).isEnabled()) return 0;
        return value;
    }
}
