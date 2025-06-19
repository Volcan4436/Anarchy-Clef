package adris.altoclef.scripting.api;

import adris.altoclef.AltoClef;
import adris.altoclef.altomenu.Mod;
import org.luaj.vm2.LuaValue;

/**
 * A wrapper class for AltoMenu modules created from Lua scripts
 * Extends the base Mod class to support Lua event handlers
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class LuaModule extends Mod {
    private final AltoClef mod;
    private LuaValue onEnableHandler = LuaValue.NIL;
    private LuaValue onDisableHandler = LuaValue.NIL;
    private LuaValue onTickHandler = LuaValue.NIL;
    
    public LuaModule(String name, String description, Category category, AltoClef mod) {
        super(name, description, category);
        this.mod = mod;
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        
        if (!onEnableHandler.isnil()) {
            try {
                onEnableHandler.call();
            } catch (Exception e) {
                mod.logWarning("Error in module '" + getName() + "' onEnable: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        
        if (!onDisableHandler.isnil()) {
            try {
                onDisableHandler.call();
            } catch (Exception e) {
                mod.logWarning("Error in module '" + getName() + "' onDisable: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void onTick() {
        super.onTick();
        
        if (!onTickHandler.isnil()) {
            try {
                onTickHandler.call();
            } catch (Exception e) {
                mod.logWarning("Error in module '" + getName() + "' onTick: " + e.getMessage());
                // Disable module after error to prevent spam
                setEnabled(false);
            }
        }
    }
    
    // Setters for Lua event handlers
    public void setOnEnableHandler(LuaValue handler) {
        this.onEnableHandler = handler;
    }
    
    public void setOnDisableHandler(LuaValue handler) {
        this.onDisableHandler = handler;
    }
    
    public void setOnTickHandler(LuaValue handler) {
        this.onTickHandler = handler;
    }
    
    // Getters for Lua event handlers (for debugging)
    public boolean hasOnEnableHandler() {
        return !onEnableHandler.isnil();
    }
    
    public boolean hasOnDisableHandler() {
        return !onDisableHandler.isnil();
    }
    
    public boolean hasOnTickHandler() {
        return !onTickHandler.isnil();
    }
    
    @Override
    public void registerSettings() {
        // Override to prevent the base class from replacing our settings list
        // with an immutable List.of() which doesn't support add operations
        // We want to keep our mutable ArrayList for dynamic setting addition
    }
} 