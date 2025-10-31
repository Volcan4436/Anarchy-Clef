package adris.altoclef.mixins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Environment(EnvType.CLIENT)
@Mixin(LogoDrawer.class)
public class LogoDrawerMixin {

    @Mutable
    @Shadow @Final public static Identifier LOGO_TEXTURE;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/UIMods.json");

    // Custom logo location in your resources
    private static final Identifier ALTOCLEF_LOGO =
            new Identifier("altoclef", "assetchangeres/minecraft_title.png");

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onStaticInit(CallbackInfo ci) {
        boolean showCustom = shouldShowCustomLogo();

        if (showCustom) {
            LOGO_TEXTURE = ALTOCLEF_LOGO;
            System.out.println("[AltoClef] Custom logo enabled!");
        } else {
            System.out.println("[AltoClef] Using default Minecraft logo.");
        }
    }

    private static boolean shouldShowCustomLogo() {
        if (!CONFIG_FILE.exists()) {
            try {
                CONFIG_FILE.getParentFile().mkdirs();
                JsonObject defaultJson = new JsonObject();
                defaultJson.addProperty("ShowAltoClefLogo", true);
                try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                    GSON.toJson(defaultJson, writer);
                }
                return true; // default to showing custom logo
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject obj = GSON.fromJson(reader, JsonObject.class);
            if (obj != null && obj.has("ShowAltoClefLogo")) {
                return obj.get("ShowAltoClefLogo").getAsBoolean();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
