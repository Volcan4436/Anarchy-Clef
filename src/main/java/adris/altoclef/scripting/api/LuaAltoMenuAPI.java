package adris.altoclef.scripting.api;

import adris.altoclef.AltoClef;
import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.managers.ModuleManager;
import adris.altoclef.altomenu.UI.screens.clickgui.ClickGUI;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.altomenu.settings.ModeSetting;
import adris.altoclef.altomenu.settings.Setting;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Lua API for creating and managing AltoMenu modules from scripts
 * Allows scripts to create custom modules with settings and event handlers
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class LuaAltoMenuAPI extends LuaTable {
    private final AltoClef mod;
    private final Map<String, LuaModule> scriptModules;
    
    public LuaAltoMenuAPI(AltoClef mod) {
        this.mod = mod;
        this.scriptModules = new ConcurrentHashMap<>();
        initializeAPI();
    }
    
    /**
     * Initializes all API functions for AltoMenu integration
     */
    private void initializeAPI() {
        // Create a new module
        set("createModule", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both AltoMenu.createModule() and AltoMenu:createModule() syntax
                int offset = args.narg() == 4 ? 1 : 0; // Skip 'self' if called with colon
                
                if (args.narg() < 3 + offset) return new LuaTable();
                
                String name = args.arg(1 + offset).tojstring();
                String description = args.arg(2 + offset).tojstring();
                String category = args.arg(3 + offset).tojstring();
                
                return createModule(name, description, category);
            }
        });
        
        // Get an existing module by name
        set("getModule", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue self, LuaValue name) {
                return getModule(name.tojstring());
            }
        });
        
        // Get all modules in a category
        set("getModulesInCategory", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue self, LuaValue category) {
                return getModulesInCategory(category.tojstring());
            }
        });
        
        // Get list of all available categories
        set("getCategories", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                return getCategories();
            }
        });
    }
    
    /**
     * Creates a new AltoMenu module from Lua
     */
    private LuaTable createModule(String name, String description, String categoryName) {
        try {
                         // Parse category
             Mod.Category category = parseCategory(categoryName);
             if (category == null) {
                 mod.logWarning("Invalid module category: " + categoryName);
                 return new LuaTable();
             }
             
             // Check if module already exists
             if (ModuleManager.INSTANCE.getModuleByName(name) != null) {
                 mod.logWarning("Module already exists: " + name);
                 return new LuaTable();
             }
            
            // Create the Lua module wrapper
            LuaModule luaModule = new LuaModule(name, description, category, mod);
            scriptModules.put(name, luaModule);
            
            // Register with ModuleManager
            ModuleManager.INSTANCE.addModule(luaModule);
            
            // Refresh ClickGUI to show the new module
            ClickGUI.INSTANCE.refreshModules();
            
            mod.log("Created AltoMenu module: " + name);
            mod.log("ModuleManager now has " + ModuleManager.INSTANCE.getModules().size() + " total modules");
            mod.log("ClickGUI refreshed to show new module");
            
            // Return Lua table representing the module
            return createModuleTable(luaModule);
            
                 } catch (Exception e) {
             mod.logWarning("Error creating module '" + name + "': " + e.getMessage());
             return new LuaTable();
         }
    }
    
    /**
     * Gets an existing module by name
     */
         private LuaTable getModule(String name) {
         Mod module = ModuleManager.INSTANCE.getModuleByName(name);
         if (module == null) {
             return new LuaTable();
         }
        
        // If it's one of our Lua modules, return the enhanced table
        if (scriptModules.containsKey(name)) {
            return createModuleTable(scriptModules.get(name));
        }
        
        // Otherwise return basic module table
        return createBasicModuleTable(module);
    }
    
    /**
     * Gets all modules in a category
     */
    private LuaTable getModulesInCategory(String categoryName) {
        Mod.Category category = parseCategory(categoryName);
        if (category == null) {
            return new LuaTable();
        }
        
        var modules = ModuleManager.INSTANCE.getModulesInCategory(category);
        LuaTable result = new LuaTable();
        
        int index = 1;
        for (Mod module : modules) {
            if (scriptModules.containsKey(module.getName())) {
                result.set(index++, createModuleTable(scriptModules.get(module.getName())));
            } else {
                result.set(index++, createBasicModuleTable(module));
            }
        }
        
        return result;
    }
    
    /**
     * Gets list of all available categories
     */
    private LuaTable getCategories() {
        LuaTable categories = new LuaTable();
        int index = 1;
        
        for (Mod.Category category : Mod.Category.values()) {
            categories.set(index++, LuaValue.valueOf(category.name));
        }
        
        return categories;
    }
    
    /**
     * Creates a comprehensive Lua table for a script-created module
     */
    private LuaTable createModuleTable(LuaModule luaModule) {
        LuaTable moduleTable = new LuaTable();
        
        // Basic properties
        moduleTable.set("name", LuaValue.valueOf(luaModule.getName()));
        moduleTable.set("description", LuaValue.valueOf(luaModule.getDescription()));
        moduleTable.set("category", LuaValue.valueOf(luaModule.getCategory().name));
        moduleTable.set("enabled", LuaValue.valueOf(luaModule.isEnabled()));
        
        // Control functions
        moduleTable.set("toggle", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                luaModule.toggle();
                return LuaValue.NIL;
            }
        });
        
        moduleTable.set("setEnabled", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue self, LuaValue enabled) {
                luaModule.setEnabled(enabled.toboolean());
                return LuaValue.NIL;
            }
        });
        
        // Setting management
        moduleTable.set("addBooleanSetting", new ThreeArgFunction() {
            @Override
            public LuaValue call(LuaValue self, LuaValue name, LuaValue defaultValue) {
                BooleanSetting setting = new BooleanSetting(name.tojstring(), defaultValue.toboolean());
                luaModule.addSetting(setting);
                return createBooleanSettingTable(setting);
            }
        });
        
                 moduleTable.set("addNumberSetting", new VarArgFunction() {
             @Override
             public Varargs invoke(Varargs args) {
                 if (args.narg() < 5) return LuaValue.NIL;
                 
                 String name = args.arg(2).tojstring();
                 double min = args.arg(3).todouble();
                 double max = args.arg(4).todouble();
                 double defaultValue = args.arg(5).todouble();
                 double increment = args.narg() > 5 ? args.arg(6).todouble() : 0.1;
                 
                 NumberSetting setting = new NumberSetting(name, min, max, defaultValue, increment);
                 luaModule.addSetting(setting);
                 return createNumberSettingTable(setting);
             }
         });
        
                 moduleTable.set("addModeSetting", new VarArgFunction() {
             @Override
             public Varargs invoke(Varargs args) {
                 if (args.narg() < 4) return LuaValue.NIL;
                 
                 String name = args.arg(2).tojstring();
                 String defaultMode = args.arg(3).tojstring();
                 
                 // Collect mode options
                 String[] modes = new String[args.narg() - 3];
                 for (int i = 0; i < modes.length; i++) {
                     modes[i] = args.arg(i + 4).tojstring();
                 }
                 
                 ModeSetting setting = new ModeSetting(name, defaultMode, modes);
                 luaModule.addSetting(setting);
                 return createModeSettingTable(setting);
             }
         });
        
        moduleTable.set("getSetting", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue self, LuaValue name) {
                return getSetting(luaModule, name.tojstring());
            }
        });
        
        // Event handlers
        moduleTable.set("onEnable", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue self, LuaValue func) {
                luaModule.setOnEnableHandler(func);
                return LuaValue.NIL;
            }
        });
        
        moduleTable.set("onDisable", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue self, LuaValue func) {
                luaModule.setOnDisableHandler(func);
                return LuaValue.NIL;
            }
        });
        
        moduleTable.set("onTick", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue self, LuaValue func) {
                luaModule.setOnTickHandler(func);
                return LuaValue.NIL;
            }
        });
        
        return moduleTable;
    }
    
    /**
     * Creates a basic Lua table for existing AltoMenu modules
     */
    private LuaTable createBasicModuleTable(Mod module) {
        LuaTable moduleTable = new LuaTable();
        
        moduleTable.set("name", LuaValue.valueOf(module.getName()));
        moduleTable.set("description", LuaValue.valueOf(module.getDescription()));
        moduleTable.set("category", LuaValue.valueOf(module.getCategory().name));
        moduleTable.set("enabled", LuaValue.valueOf(module.isEnabled()));
        
        moduleTable.set("toggle", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                module.toggle();
                return LuaValue.NIL;
            }
        });
        
        moduleTable.set("setEnabled", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue self, LuaValue enabled) {
                module.setEnabled(enabled.toboolean());
                return LuaValue.NIL;
            }
        });
        
        return moduleTable;
    }
    
    /**
     * Creates a setting table for boolean settings
     */
    private LuaTable createBooleanSettingTable(BooleanSetting setting) {
        LuaTable settingTable = new LuaTable();
        
        settingTable.set("name", LuaValue.valueOf(setting.getName()));
        settingTable.set("type", LuaValue.valueOf("boolean"));
        settingTable.set("value", LuaValue.valueOf(setting.isEnabled()));
        
        settingTable.set("toggle", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                setting.toggle();
                return LuaValue.NIL;
            }
        });
        
        settingTable.set("setValue", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue self, LuaValue value) {
                setting.setEnabled(value.toboolean());
                return LuaValue.NIL;
            }
        });
        
        settingTable.set("getValue", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                return LuaValue.valueOf(setting.isEnabled());
            }
        });
        
        return settingTable;
    }
    
    /**
     * Creates a setting table for number settings
     */
    private LuaTable createNumberSettingTable(NumberSetting setting) {
        LuaTable settingTable = new LuaTable();
        
        settingTable.set("name", LuaValue.valueOf(setting.getName()));
        settingTable.set("type", LuaValue.valueOf("number"));
        settingTable.set("value", LuaValue.valueOf(setting.getValue()));
        settingTable.set("min", LuaValue.valueOf(setting.getMin()));
        settingTable.set("max", LuaValue.valueOf(setting.getMax()));
        settingTable.set("increment", LuaValue.valueOf(setting.getIncrement()));
        
        settingTable.set("setValue", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue self, LuaValue value) {
                setting.setValue(value.todouble());
                return LuaValue.NIL;
            }
        });
        
        settingTable.set("getValue", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                return LuaValue.valueOf(setting.getValue());
            }
        });
        
        settingTable.set("increment", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue self, LuaValue positive) {
                setting.increment(positive.toboolean());
                return LuaValue.NIL;
            }
        });
        
        return settingTable;
    }
    
    /**
     * Creates a setting table for mode settings
     */
    private LuaTable createModeSettingTable(ModeSetting setting) {
        LuaTable settingTable = new LuaTable();
        
        settingTable.set("name", LuaValue.valueOf(setting.getName()));
        settingTable.set("type", LuaValue.valueOf("mode"));
        settingTable.set("value", LuaValue.valueOf(setting.getMode()));
        settingTable.set("index", LuaValue.valueOf(setting.getIndex()));
        
        // Convert modes list to Lua table
        LuaTable modesTable = new LuaTable();
        for (int i = 0; i < setting.getModes().size(); i++) {
            modesTable.set(i + 1, LuaValue.valueOf(setting.getModes().get(i)));
        }
        settingTable.set("modes", modesTable);
        
        settingTable.set("setMode", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue self, LuaValue mode) {
                setting.setMode(mode.tojstring());
                return LuaValue.NIL;
            }
        });
        
        settingTable.set("cycle", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                setting.cycle();
                return LuaValue.NIL;
            }
        });
        
        settingTable.set("getValue", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                return LuaValue.valueOf(setting.getMode());
            }
        });
        
        return settingTable;
    }
    
    /**
     * Gets a setting from a module by name
     */
    private LuaValue getSetting(LuaModule module, String settingName) {
        for (Setting setting : module.getSettings()) {
            if (setting.getName().equals(settingName)) {
                if (setting instanceof BooleanSetting) {
                    return createBooleanSettingTable((BooleanSetting) setting);
                } else if (setting instanceof NumberSetting) {
                    return createNumberSettingTable((NumberSetting) setting);
                } else if (setting instanceof ModeSetting) {
                    return createModeSettingTable((ModeSetting) setting);
                }
            }
        }
        return LuaValue.NIL;
    }
    
    /**
     * Parses category string to Category enum
     */
    private Mod.Category parseCategory(String categoryName) {
        try {
            return Mod.Category.valueOf(categoryName.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try matching by display name
            for (Mod.Category category : Mod.Category.values()) {
                if (category.name.equalsIgnoreCase(categoryName)) {
                    return category;
                }
            }
            return null;
        }
    }
    
    /**
     * Cleanup script modules when scripts are unloaded
     */
    public void cleanup() {
        for (LuaModule module : scriptModules.values()) {
            if (module.isEnabled()) {
                module.setEnabled(false);
            }
            // TODO: Should we remove from ModuleManager? For now, keep modules registered
            // ModuleManager.INSTANCE.getModules().remove(module);
        }
        scriptModules.clear();
    }
} 