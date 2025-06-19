package adris.altoclef.scripting.script;

import adris.altoclef.scripting.LuaScriptEngine;
import org.luaj.vm2.*;

/**
 * Represents a loaded Lua script with its execution context and metadata
 * Handles script lifecycle, function caching, and performance tracking
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class LuaScript {
    private final String name;
    private final String sourceCode;
    private final LuaValue chunk;
    private final Globals scriptGlobals;
    private final LuaScriptEngine engine;
    private final ScriptMetadata metadata;
    
    private boolean enabled = true;
    private long lastTickTime = 0;
    private int tickCount = 0;
    private long totalExecutionTime = 0;
    
    // Script lifecycle functions - cached for performance
    private LuaValue onTickFunc;
    private LuaValue onEnableFunc;
    private LuaValue onDisableFunc;
    private LuaValue onCleanupFunc;
    private LuaValue onLoadFunc;
    
    public LuaScript(String name, String sourceCode, LuaValue chunk, Globals globals, LuaScriptEngine engine) {
        this.name = name;
        this.sourceCode = sourceCode;
        this.chunk = chunk;
        this.scriptGlobals = globals;
        this.engine = engine;
        this.metadata = ScriptMetadata.parseFromSource(sourceCode);
        
        // Initialize function references to NIL - they'll be cached after script execution
        onTickFunc = LuaValue.NIL;
        onEnableFunc = LuaValue.NIL;
        onDisableFunc = LuaValue.NIL;
        onCleanupFunc = LuaValue.NIL;
        onLoadFunc = LuaValue.NIL;
        
        // Note: cacheScriptFunctions() will be called after script execution
    }
    
    /**
     * Called after script execution to cache function references
     * This must be called AFTER chunk.call() in LuaScriptEngine
     */
    public void cacheScriptFunctionsAfterExecution() {
        cacheScriptFunctions();
    }
    
    /**
     * Caches references to common script functions for better performance
     */
    private void cacheScriptFunctions() {
        try {
            onTickFunc = scriptGlobals.get("onTick");
            onEnableFunc = scriptGlobals.get("onEnable");
            onDisableFunc = scriptGlobals.get("onDisable");
            onCleanupFunc = scriptGlobals.get("onCleanup");
            onLoadFunc = scriptGlobals.get("onLoad");
            
            // Debug function caching
            System.out.println("🔧 Script '" + name + "' function cache:");
            System.out.println("  onTick: " + (onTickFunc.isnil() ? "NOT FOUND" : "FOUND"));
            System.out.println("  onEnable: " + (onEnableFunc.isnil() ? "NOT FOUND" : "FOUND"));
            System.out.println("  onDisable: " + (onDisableFunc.isnil() ? "NOT FOUND" : "FOUND"));
            System.out.println("  onCleanup: " + (onCleanupFunc.isnil() ? "NOT FOUND" : "FOUND"));
            System.out.println("  onLoad: " + (onLoadFunc.isnil() ? "NOT FOUND" : "FOUND"));
            
        } catch (Exception e) {
            // Function caching failed - not critical, will check dynamically
            System.err.println("❌ Function caching failed for script '" + name + "': " + e.getMessage());
        }
    }
    
    /**
     * Called every game tick if the script is enabled
     */
    public void tick() {
        if (!enabled || onTickFunc.isnil()) {
            return;
        }
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Call the script's onTick function
            onTickFunc.call();
            
            // Update performance metrics
            long executionTime = System.currentTimeMillis() - startTime;
            lastTickTime = startTime;
            tickCount++;
            totalExecutionTime += executionTime;
            
            // Warn if script is taking too long
            if (executionTime > 50) { // 50ms is quite long for a tick
                engine.getErrorHandler().handlePerformanceWarning(name, 
                    "Script tick took " + executionTime + "ms (should be <50ms)");
            }
            
        } catch (LuaError e) {
            engine.getErrorHandler().handleScriptError(name, "Error in onTick", e);
            setEnabled(false); // Disable on error to prevent spam
        } catch (Exception e) {
            engine.getErrorHandler().handleScriptError(name, "Unexpected error in onTick", e);
            setEnabled(false);
        }
    }
    
    /**
     * Enables or disables the script, calling appropriate lifecycle functions
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        
        this.enabled = enabled;
        
        try {
            if (enabled && !onEnableFunc.isnil()) {
                onEnableFunc.call();
            } else if (!enabled && !onDisableFunc.isnil()) {
                onDisableFunc.call();
            }
        } catch (LuaError e) {
            engine.getErrorHandler().handleScriptError(name, "Error in enable/disable", e);
        } catch (Exception e) {
            engine.getErrorHandler().handleScriptError(name, "Unexpected error in enable/disable", e);
        }
    }
    
    /**
     * Cleans up the script and releases resources
     */
    public void cleanup() {
        try {
            // Call cleanup function if it exists
            if (!onCleanupFunc.isnil()) {
                onCleanupFunc.call();
            }
        } catch (LuaError e) {
            // Cleanup errors are logged but not critical
            engine.getErrorHandler().handleScriptError(name, "Error during cleanup", e);
        } catch (Exception e) {
            engine.getErrorHandler().handleScriptError(name, "Unexpected error during cleanup", e);
        }
        
        // Cleanup TaskSystem tasks created by this script
        try {
            LuaValue altoClef = scriptGlobals.get("AltoClef");
            if (!altoClef.isnil()) {
                LuaValue taskSystem = altoClef.get("TaskSystem");
                if (!taskSystem.isnil()) {
                    LuaValue cleanup = taskSystem.get("cleanup");
                    if (!cleanup.isnil() && cleanup.isfunction()) {
                        cleanup.call();
                    }
                }
                
                // Cleanup Utils API
                LuaValue utils = altoClef.get("Utils");
                if (!utils.isnil()) {
                    LuaValue utilsCleanup = utils.get("cleanup");
                    if (!utilsCleanup.isnil() && utilsCleanup.isfunction()) {
                        utilsCleanup.call();
                    }
                }
            }
        } catch (Exception e) {
            engine.getErrorHandler().handleScriptError(name, "Error cleaning up APIs", e);
        }
        
        // Clear function references to help with garbage collection
        onTickFunc = LuaValue.NIL;
        onEnableFunc = LuaValue.NIL;
        onDisableFunc = LuaValue.NIL;
        onCleanupFunc = LuaValue.NIL;
        onLoadFunc = LuaValue.NIL;
        
        // Clear script globals
        if (scriptGlobals != null) {
            scriptGlobals.set("onTick", LuaValue.NIL);
            scriptGlobals.set("onEnable", LuaValue.NIL);
            scriptGlobals.set("onDisable", LuaValue.NIL);
            scriptGlobals.set("onCleanup", LuaValue.NIL);
            scriptGlobals.set("onLoad", LuaValue.NIL);
        }
    }
    
    /**
     * Calls a custom function in the script by name
     */
    public LuaValue callFunction(String functionName, LuaValue... args) {
        try {
            LuaValue function = scriptGlobals.get(functionName);
            if (!function.isnil()) {
                if (args.length == 0) {
                    return function.call();
                } else if (args.length == 1) {
                    return function.call(args[0]);
                } else if (args.length == 2) {
                    return function.call(args[0], args[1]);
                } else if (args.length == 3) {
                    return function.call(args[0], args[1], args[2]);
                } else {
                    return function.invoke(args).arg1();
                }
            }
        } catch (LuaError e) {
            engine.getErrorHandler().handleScriptError(name, "Error calling function " + functionName, e);
        } catch (Exception e) {
            engine.getErrorHandler().handleScriptError(name, "Unexpected error calling function " + functionName, e);
        }
        return LuaValue.NIL;
    }
    
    /**
     * Gets a global variable from the script
     */
    public LuaValue getGlobal(String name) {
        try {
            return scriptGlobals.get(name);
        } catch (Exception e) {
            return LuaValue.NIL;
        }
    }
    
    /**
     * Sets a global variable in the script
     */
    public void setGlobal(String name, LuaValue value) {
        try {
            scriptGlobals.set(name, value);
        } catch (Exception e) {
            engine.getErrorHandler().handleScriptError(this.name, "Error setting global " + name, e);
        }
    }
    
    // Getters for script information
    public String getName() { return name; }
    public String getSourceCode() { return sourceCode; }
    public boolean isEnabled() { return enabled; }
    public ScriptMetadata getMetadata() { return metadata; }
    public long getLastTickTime() { return lastTickTime; }
    public int getTickCount() { return tickCount; }
    public long getTotalExecutionTime() { return totalExecutionTime; }
    

    
    /**
     * Gets average execution time per tick in milliseconds
     */
    public double getAverageExecutionTime() {
        if (tickCount == 0) return 0.0;
        return (double) totalExecutionTime / tickCount;
    }
    
    /**
     * Checks if the script has a specific function
     */
    public boolean hasFunction(String functionName) {
        try {
            LuaValue function = scriptGlobals.get(functionName);
            return !function.isnil() && function.isfunction();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Resets performance statistics
     */
    public void resetStats() {
        tickCount = 0;
        totalExecutionTime = 0;
        lastTickTime = 0;
    }
    
    @Override
    public String toString() {
        return String.format("LuaScript{name='%s', enabled=%s, ticks=%d, avgTime=%.2fms}", 
            name, enabled, tickCount, getAverageExecutionTime());
    }
} 