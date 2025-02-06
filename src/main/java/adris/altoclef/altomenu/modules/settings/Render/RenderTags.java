package adris.altoclef.altomenu.modules.settings.Render;

import adris.altoclef.altomenu.Mod;

public class RenderTags extends Mod {
    public static RenderTags Instance;
    public RenderTags() {
        super("RenderTags", "Tag Renderer for entitys.", Mod.Category.DEVELOPMENT);
        Instance = this;
    }
    @Override
    public void onEnable() {
        System.out.println("RenderTags Enabled");
        super.onEnable();
    }

    @Override
    public void onDisable() {
        System.out.println("RenderTags Disabled");
        super.onDisable();
    }
}