package adris.altoclef.altomenu.modules.Render;

import adris.altoclef.altomenu.Mod;

//todo Render through walls
//todo Hide Real Tags
//todo Recode this to not use a Mixin
public class RenderTags extends Mod {
    public static RenderTags Instance;
    public RenderTags() {
        super("RenderTags", "Tag Renderer for entitys.", Mod.Category.RENDER);
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