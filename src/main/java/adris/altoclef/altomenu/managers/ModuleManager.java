package adris.altoclef.altomenu.managers;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.modules.Baritone.ChatBot;
import adris.altoclef.altomenu.modules.Movement.*;
import adris.altoclef.altomenu.modules.Render.Jesus;
import adris.altoclef.altomenu.modules.Render.Fullbright;
import adris.altoclef.altomenu.modules.Render.RenderTags;
import adris.altoclef.altomenu.modules.Utility.Stealer;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final ModuleManager INSTANCE = new ModuleManager();
    private List<Mod> modules = new ArrayList<>();

    public ModuleManager() {
        addModules();
    }

    public List<Mod> getModules() {
        return modules;
    }

    public List<Mod> getEnabledModules() {

        List<Mod> enabled = new ArrayList<>();
        for (Mod module : modules) {
            if (module.isEnabled()) enabled.add(module);
        }
        return enabled;
    }

    public Mod getModuleByName(String name) {

        for (Mod module : modules) {
            if (module.getName().equalsIgnoreCase(name)) return module;
        }

        return null;
    }


    public List<Mod> getModulesInCategory(Mod.Category category) {
        List<Mod> categoryModules = new ArrayList<>();

        for (Mod mod : modules) {
            if (mod.getCategory() == category) {
                categoryModules.add(mod);
            }
        }
        return categoryModules;
    }


    public Mod getModuleByClass(Class<? extends Mod> cls) {
        for (Mod mod : modules) {
            if (mod.getClass() != cls) {
                continue;
            }
            return mod;
        }

        return null;
    }


    //This is where you add the modules to show on the clickgui
    private void addModules() {

        //Baritone
        modules.add(new ChatBot());

        //Movement
        modules.add(new Infjump());
        modules.add(new Sprint());
        modules.add(new Speed());
        modules.add(new AutoWalk());
        modules.add(new Flight());
        modules.add(new AdvancedFly());

        //Render
        modules.add(new Fullbright());
        modules.add(new RenderTags());
        modules.add(new Jesus());

        //Util-Dev
        modules.add(new Stealer());
    }
}
