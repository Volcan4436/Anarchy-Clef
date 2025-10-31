//package adris.altoclef.mixins;
//
//import com.mojang.blaze3d.systems.RenderSystem;
//import net.minecraft.client.render.*;
//import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.entity.Entity;
//import net.minecraft.util.math.Vec3d;
//import org.joml.Matrix4f;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import static adris.altoclef.altomenu.modules.Development.TracerModule.renderTracers;
//
//@Mixin(WorldRenderer.class)
//public class WorldRendererMixin {
//
//    @Inject(method = "render", at = @At("TAIL"))
//    private void onRender(MatrixStack matrices, float tickDelta, long limitTime,
//                          boolean renderBlockOutline, Camera camera,
//                          GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager,
//                          Matrix4f matrix4f, CallbackInfo ci) {
//        renderTracers(matrices, camera, tickDelta);
//    }
//}
