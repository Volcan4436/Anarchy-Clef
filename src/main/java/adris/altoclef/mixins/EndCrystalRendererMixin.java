//package adris.altoclef.mixins;
//
//import adris.altoclef.altomenu.modules.Render.CrystalPlus;
//import adris.altoclef.altomenu.managers.ModuleManager;
//import net.minecraft.client.render.OverlayTexture;
//import net.minecraft.client.render.RenderLayer;
//import net.minecraft.client.render.VertexConsumerProvider;
//import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
//import net.minecraft.client.util.math.MatrixStack;
//import net.minecraft.entity.decoration.EndCrystalEntity;
//import net.minecraft.util.Identifier;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.MathHelper;
//import net.minecraft.util.math.RotationAxis;
//import org.joml.Quaternionf;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(EndCrystalEntityRenderer.class)
//public class EndCrystalRendererMixin {
//
//    // Use the vanilla texture & render layer constants like vanilla.
//    private static Identifier TEXTURE = new Identifier("textures/entity/end_crystal/end_crystal.png");
//    private static RenderLayer END_CRYSTAL_LAYER = RenderLayer.getEntityCutoutNoCull(TEXTURE);
//
//    /**
//     * Inject at the start of vanilla render and replace it if our module is active.
//     * Signature matches the decompiled method:
//     * render(EndCrystalEntity, float f, float g, MatrixStack, VertexConsumerProvider, int light)
//     */
//    @Inject(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
//            at = @At("HEAD"), cancellable = true)
//    private void onRender(EndCrystalEntity endCrystalEntity, float f, float tickDelta, MatrixStack matrixStack,
//                          VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo ci) {
//
//        // Find module by name; ensure it's the same name you used in the Mod constructor.
//        CrystalPlus module = (CrystalPlus) ModuleManager.INSTANCE.getModuleByName("CrystalPlus");
//        if (module == null || !module.isEnabled()) {
//            // not present or not enabled — let vanilla run
//            return;
//        }
//
//        // Cancel vanilla rendering — we'll reimplement with our options.
//        ci.cancel();
//
//        // replicate variables from vanilla renderer
//        float yOffset = EndCrystalRendererMixin.getYOffset(endCrystalEntity, tickDelta);
//        float spin = ((float) endCrystalEntity.endCrystalAge + tickDelta) * 3.0F;
//
//        // VertexConsumer to use for default/normal textured rendering
//        net.minecraft.client.render.VertexConsumer vanillaConsumer = vertexConsumerProvider.getBuffer(END_CRYSTAL_LAYER);
//
//        // Bounce control: either vanilla getYOffset or 0
//        boolean bounceEnabled = module.bounce.isEnabled(); // BooleanSetting
//        float effectiveYOffset = bounceEnabled ? yOffset : 0.0f;
//
//        // Spawn animation control
//        boolean spawnAnim = module.spawnAnim.isEnabled(); // BooleanSetting
//        float spawnScale = 1.0f;
//        if (spawnAnim) {
//            // Use endCrystalAge to compute a scale ramp (vanilla age -> smoothly grow)
//            float age = (float) endCrystalEntity.endCrystalAge + tickDelta;
//            final float fullTime = 10.0f; // grow duration in ticks (adjustable)
//            float s = MathHelper.clamp(age / fullTime, 0.0f, 1.0f);
//            // apply ease-out (smooth)
//            spawnScale = s * s * (3f - 2f * s); // smoothstep
//            if (spawnScale < 0.01f) spawnScale = 0.01f;
//        }
//
//        // Color handling
//        boolean rgb = module.rgb.isEnabled(); // BooleanSetting
//        // NumberSettings for RGB (assumed range 0..255)
//        float rBase = (float) module.r.getValue() / 255f;
//        float gBase = (float) module.g.getValue() / 255f;
//        float bBase = (float) module.b.getValue() / 255f;
//
//        // If RGB rainbow mode enabled, compute HSB->RGB smoothly
//        if (rgb) {
//            // time-based hue
//            float hue = ((System.currentTimeMillis() % 5000L) / 5000f); // 5-second cycle
//            int rgbInt = java.awt.Color.HSBtoRGB(hue, 0.9f, 0.9f);
//            rBase = ((rgbInt >> 16) & 0xFF) / 255f;
//            gBase = ((rgbInt >> 8) & 0xFF) / 255f;
//            bBase = (rgbInt & 0xFF) / 255f;
//        }
//
//        // Read render mode
//        String mode = module.renderMode.getMode(); // ModeSetting: "Normal", "SColor", "Gradient", "CGradient"
//
//        // Start replicating the vanilla drawing with matrix transformations
//        matrixStack.push();
//        matrixStack.scale(2.0F * spawnScale, 2.0F * spawnScale, 2.0F * spawnScale);
//        matrixStack.translate(0.0F, -0.5F, 0.0F);
//
//        // bottom (base)
//        int overlay = OverlayTexture.DEFAULT_UV;
//        if (endCrystalEntity.shouldShowBottom()) {
//            if (mode.equalsIgnoreCase("SColor")) {
//                // solid-color bottom
//                // render with tint using vanilla consumer (we can just reuse same render layer)
//                // but need a consumer and to call ModelPart#render with color. The vanilla renderer used `this.bottom.render(matrixStack, vertexConsumer, light, k);`
//                // We will call the same consumer with color via the ModelPart.render overload. To get model parts we must call vanilla renderer's model parts,
//                // but since we're in a mixin and don't have access to private fields, we will re-use vanilla render path by using the same render layer and letting texture be drawn
//                // then overlay a colored translucent fill. Simpler: use vanillaConsumer (textured) then render an extra translucent colored layer using Translucent.
//                // Draw textured bottom first:
//                vertexConsumerProvider.getBuffer(END_CRYSTAL_LAYER); // textured draw done below when reusing model parts via reflection is painful
//            } else {
//                // vanilla textured bottom: to match exact model parts we can call the vanilla render via reflection, but simplest is to use the same RenderLayer and let vanilla ModelParts render.
//            }
//        }
//
//        // Because in the original renderer the model parts (frame/core/bottom) are private fields of EndCrystalEntityRenderer
//        // we will call the render method on the renderer instance via a cast trick: we are inside a mixin of EndCrystalEntityRenderer so `this` is the renderer.
//        EndCrystalEntityRenderer self = (EndCrystalEntityRenderer) (Object) this;
//
//        // For colorized rendering we use two layers:
//        // - vanilla textured layer (END_CRYSTAL_LAYER)
//        // - an extra translucent layer to tint the model parts using our RGB values (if mode != Normal)
//        net.minecraft.client.render.VertexConsumer tintConsumer = null;
//        RenderLayer tintLayer = RenderLayer.getEntityTranslucent(TEXTURE);
//        if (!mode.equalsIgnoreCase("Normal")) {
//            tintConsumer = vertexConsumerProvider.getBuffer(tintLayer);
//        }
//
//        // Prepare rotations & transforms exactly as vanilla
//        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin));
//        matrixStack.translate(0.0F, 1.5F + effectiveYOffset / 2.0F, 0.0F);
//        matrixStack.multiply((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), (float)Math.sin(Math.PI / 4.0D), 0.0F, (float)Math.sin(Math.PI / 4.0D)));
//
//        // Render the "frame" (glass) textured (vanilla)
//        // Equivalent to: this.frame.render(matrixStack, vertexConsumer, light, overlay);
//        // Use reflection to access private model parts (frame/core/bottom) on the renderer instance.
//        try {
//            java.lang.reflect.Field frameField = EndCrystalEntityRenderer.class.getDeclaredField("frame");
//            java.lang.reflect.Field coreField = EndCrystalEntityRenderer.class.getDeclaredField("core");
//            java.lang.reflect.Field bottomField = EndCrystalEntityRenderer.class.getDeclaredField("bottom");
//            frameField.setAccessible(true);
//            coreField.setAccessible(true);
//            bottomField.setAccessible(true);
//
//            Object framePart = frameField.get(self);
//            Object corePart = coreField.get(self);
//            Object bottomPart = bottomField.get(self);
//
//            // safe-cast to ModelPart and call render
//            if (framePart instanceof net.minecraft.client.model.ModelPart) {
//                net.minecraft.client.model.ModelPart frame = (net.minecraft.client.model.ModelPart) framePart;
//                net.minecraft.client.model.ModelPart core = (net.minecraft.client.model.ModelPart) corePart;
//                net.minecraft.client.model.ModelPart bottom = (net.minecraft.client.model.ModelPart) bottomPart;
//
//                // First textured frame (vanilla appearance)
//                frame.render(matrixStack, vanillaConsumer, light, overlay);
//
//                // scale down and rotate as vanilla
//                float l = 0.875F;
//                matrixStack.scale(l, l, l);
//                matrixStack.multiply((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), (float)Math.sin(Math.PI / 4.0D), 0.0F, (float)Math.sin(Math.PI / 4.0D)));
//                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin));
//                frame.render(matrixStack, vanillaConsumer, light, overlay);
//
//                matrixStack.scale(l, l, l);
//                matrixStack.multiply((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), (float)Math.sin(Math.PI / 4.0D), 0.0F, (float)Math.sin(Math.PI / 4.0D)));
//                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin));
//                core.render(matrixStack, vanillaConsumer, light, overlay);
//
//                // After textured draw, if a colored mode is active apply tint/different rendering
//                if (mode.equalsIgnoreCase("SColor")) {
//                    // Solid single color overlay (glass slightly transparent, bottom opaque)
//                    // Render bottom (base) tinted
//                    matrixStack.push();
//                    bottom.render(matrixStack, tintConsumer, light, overlay, rBase, gBase, bBase, 1.0F);
//                    matrixStack.pop();
//
//                    // Glass tinted (semi)
//                    matrixStack.push();
//                    frame.render(matrixStack, tintConsumer, light, overlay, rBase, gBase, bBase, 0.6F);
//                    matrixStack.pop();
//
//                } else if (mode.equalsIgnoreCase("Gradient")) {
//                    // Simple two-tone gradient: bottom = base color, glass = darker variant
//                    float topR = MathHelper.clamp(rBase * 0.6f, 0f,1f);
//                    float topG = MathHelper.clamp(gBase * 0.6f, 0f,1f);
//                    float topB = MathHelper.clamp(bBase * 0.6f, 0f,1f);
//
//                    matrixStack.push();
//                    bottom.render(matrixStack, tintConsumer, light, overlay, rBase, gBase, bBase, 1.0F);
//                    matrixStack.pop();
//
//                    matrixStack.push();
//                    frame.render(matrixStack, tintConsumer, light, overlay, topR, topG, topB, 0.8F);
//                    matrixStack.pop();
//
//                } else if (mode.equalsIgnoreCase("CGradient")) {
//                    // Circular-ish gradient that shifts over time
//                    float phase = (MathHelper.sin(((float)endCrystalEntity.endCrystalAge + tickDelta) * 0.1F) * 0.5F) + 0.5F;
//                    float cR = MathHelper.clamp(rBase * phase, 0f,1f);
//                    float cG = MathHelper.clamp(gBase * (1f - phase), 0f,1f);
//                    float cB = bBase;
//                    matrixStack.push();
//                    bottom.render(matrixStack, tintConsumer, light, overlay, rBase, gBase, bBase, 1.0F);
//                    matrixStack.pop();
//
//                    matrixStack.push();
//                    frame.render(matrixStack, tintConsumer, light, overlay, cR, cG, cB, 0.85F);
//                    matrixStack.pop();
//                } // else Normal already drawn
//
//                // Draw bottom normally if not already drawn above (vanilla did it before transforms)
//                // Vanilla drew bottom earlier before rotations; to keep parity call bottom.render as vanilla did (only if shouldShowBottom)
//                // We drew textured parts already; bottom textured was not explicitly called above before transforms if shouldShowBottom() was true.
//                // To be safe, render bottom textured at beginning (matching vanilla): but that was skipped above. Let's render it now if necessary.
//                // (This keeps visuals consistent.)
//                matrixStack.push();
//                matrixStack.scale(1.0f,1.0f,1.0f); // identity
//                if (endCrystalEntity.shouldShowBottom()) {
//                    // bottom textured
//                    bottom.render(matrixStack, vanillaConsumer, light, overlay);
//                }
//                matrixStack.pop();
//            }
//        } catch (NoSuchFieldException | IllegalAccessException ex) {
//            // Reflection failed — fallback to letting vanilla render (rare)
//            ex.printStackTrace();
//            // let vanilla do its thing by not cancelling (but we've already cancelled).
//            // As fallback, just return to avoid crashing.
//            matrixStack.pop();
//            return;
//        }
//
//        matrixStack.pop();
//
//        // Beam rendering: same as vanilla
//        BlockPos beamTarget = endCrystalEntity.getBeamTarget();
//        if (beamTarget != null) {
//            float m = (float)beamTarget.getX() + 0.5F;
//            float n = (float)beamTarget.getY() + 0.5F;
//            float o = (float)beamTarget.getZ() + 0.5F;
//            float p = (float)((double)m - endCrystalEntity.getX());
//            float q = (float)((double)n - endCrystalEntity.getY());
//            float r = (float)((double)o - endCrystalEntity.getZ());
//            matrixStack.translate(p, q, r);
//            // delegate to vanilla's static beam renderer
//            try {
//                Class.forName("net.minecraft.client.render.entity.EnderDragonEntityRenderer")
//                        .getMethod("renderCrystalBeam", float.class, float.class, float.class, float.class, int.class,
//                                MatrixStack.class, VertexConsumerProvider.class, int.class)
//                        .invoke(null, -p, -q + effectiveYOffset, -r, tickDelta, endCrystalEntity.endCrystalAge, matrixStack, vertexConsumerProvider, light);
//            } catch (Exception e) {
//                // reflection fallback: ignore if can't call — beam is cosmetic
//                e.printStackTrace();
//            }
//        }
//
//        // super.render(...) not called because we replaced it
//    }
//
//    // Copy of vanilla getYOffset helper so behavior matches exactly
//    private static float getYOffset(EndCrystalEntity crystal, float tickDelta) {
//        float f = (float)crystal.endCrystalAge + tickDelta;
//        float g = MathHelper.sin(f * 0.2F) / 2.0F + 0.5F;
//        g = (g * g + g) * 0.4F;
//        return g - 1.4F;
//    }
//}
