package adris.altoclef.altomenu.managers;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.modules.Baritone.ChatBot;
import adris.altoclef.altomenu.modules.Combat.NewCrystalAura;
import adris.altoclef.altomenu.modules.Development.FakeRotation;
import adris.altoclef.altomenu.modules.Exploit.UnlockRecipes;
import adris.altoclef.altomenu.modules.Movement.*;
import adris.altoclef.altomenu.modules.Player.AntiHunger;
import adris.altoclef.altomenu.modules.Player.NoFall;
import adris.altoclef.altomenu.modules.Player.Velocity;
import adris.altoclef.altomenu.modules.Render.*;
import adris.altoclef.altomenu.modules.Utility.PickPlus;
import adris.altoclef.altomenu.modules.Utility.Stealer;

import java.util.ArrayList;
import java.util.List;

//todo
// Make this auto register modules on Compile
// Make it Also Register on the WebPanel once its Merged with Main Branch
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
    
    public void addModule(final Mod module) {
        modules.add(module);
        module.registerSettings();
    }
    
    //This is where you add the modules to show on the clickgui
    private void addModules() {
        //Baritone
        addModule(new ChatBot());

        //Combat
        addModule(new NewCrystalAura());

        //Development
        addModule(new FakeRotation());

        //Exploit
        addModule(new UnlockRecipes());
        addModule(new PickPlus());

        //Movement
        addModule(new AdvancedFly());
        addModule(new AutoJump());
        addModule(new AutoWalk());
        addModule(new Flight());
        addModule(new Infjump());
        addModule(new Jesus());
        addModule(new Speed());
        addModule(new Sprint());
        addModule(new Step());
        addModule(new Velocity());

        //Player
        addModule(new AntiHunger());
        addModule(new NoFall());

        //Render
        addModule(new Fullbright());
        addModule(new RenderTags());
        addModule(new Jesus());
        addModule(new Radar());
        addModule(new crosshairRGH());
        addModule(new ESP());


        //Utility
        addModule(new Stealer());

        //World
    }
}
