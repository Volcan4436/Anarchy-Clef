package adris.altoclef.scripting.api;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import org.luaj.vm2.LuaValue;

/**
 * A wrapper class for AltoClef tasks created from Lua scripts
 * Extends the base Task class to support Lua-defined task logic
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class LuaTask extends Task {
    private final String name;
    private final LuaValue taskLogic;
    private final AltoClef mod;
    
    // Cached Lua functions for performance
    private LuaValue onStartFunc;
    private LuaValue onTickFunc;
    private LuaValue onStopFunc;
    private LuaValue isFinishedFunc;
    private LuaValue isEqualFunc;
    private LuaValue toDebugStringFunc;
    
    public LuaTask(String name, LuaValue taskLogic, AltoClef mod) {
        this.name = name;
        this.taskLogic = taskLogic;
        this.mod = mod;
        
        // Cache function references for better performance
        cacheTaskFunctions();
    }
    
    /**
     * Caches references to Lua task functions for better performance
     */
    private void cacheTaskFunctions() {
        try {
            if (taskLogic.istable()) {
                LuaValue table = taskLogic;
                onStartFunc = table.get("onStart");
                onTickFunc = table.get("onTick");
                onStopFunc = table.get("onStop");
                isFinishedFunc = table.get("isFinished");
                isEqualFunc = table.get("isEqual");
                toDebugStringFunc = table.get("toDebugString");
            } else {
                // If it's a function, assume it's the onTick function
                onTickFunc = taskLogic;
                onStartFunc = LuaValue.NIL;
                onStopFunc = LuaValue.NIL;
                isFinishedFunc = LuaValue.NIL;
                isEqualFunc = LuaValue.NIL;
                toDebugStringFunc = LuaValue.NIL;
            }
        } catch (Exception e) {
            mod.logWarning("Error caching Lua task functions for '" + name + "': " + e.getMessage());
        }
    }
    
    @Override
    protected void onStart(AltoClef mod) {
        try {
            if (!onStartFunc.isnil()) {
                onStartFunc.call(taskLogic);
            }
        } catch (Exception e) {
            mod.logWarning("Error in Lua task '" + name + "' onStart: " + e.getMessage());
        }
    }
    
    @Override
    protected Task onTick(AltoClef mod) {
        try {
            if (!onTickFunc.isnil()) {
                LuaValue result = onTickFunc.call(taskLogic);
                
                // If the result is a task, return it for chaining
                if (result.isuserdata() && result.touserdata() instanceof Task) {
                    return (Task) result.touserdata();
                }
                
                // If the result is nil or false, we're done
                if (result.isnil() || (result.isboolean() && !result.toboolean())) {
                    return null;
                }
            }
        } catch (Exception e) {
            mod.logWarning("Error in Lua task '" + name + "' onTick: " + e.getMessage());
            // Stop task on error to prevent spam
            stop(mod);
        }
        
        return null;
    }
    
    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        try {
            if (!onStopFunc.isnil()) {
                if (interruptTask != null) {
                    // Call with self and interrupt task info
                    onStopFunc.call(taskLogic, LuaValue.valueOf(interruptTask.toString()));
                } else {
                    onStopFunc.call(taskLogic);
                }
            }
        } catch (Exception e) {
            mod.logWarning("Error in Lua task '" + name + "' onStop: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isFinished(AltoClef mod) {
        try {
            if (!isFinishedFunc.isnil()) {
                // Call with the task logic table as self parameter
                LuaValue result = isFinishedFunc.call(taskLogic);
                return result.toboolean();
            }
        } catch (Exception e) {
            mod.logWarning("Error in Lua task '" + name + "' isFinished: " + e.getMessage());
            return true; // Assume finished on error
        }
        
        return false; // Default: not finished
    }
    
    @Override
    protected boolean isEqual(Task other) {
        if (!(other instanceof LuaTask)) {
            return false;
        }
        
        LuaTask otherLuaTask = (LuaTask) other;
        
        try {
            if (!isEqualFunc.isnil()) {
                LuaValue result = isEqualFunc.call(taskLogic, LuaValue.valueOf(otherLuaTask.name));
                return result.toboolean();
            }
        } catch (Exception e) {
            mod.logWarning("Error in Lua task '" + name + "' isEqual: " + e.getMessage());
        }
        
        // Default: compare by name
        return name.equals(otherLuaTask.name);
    }
    
    @Override
    protected String toDebugString() {
        try {
            if (!toDebugStringFunc.isnil()) {
                LuaValue result = toDebugStringFunc.call(taskLogic);
                return result.tojstring();
            }
        } catch (Exception e) {
            mod.logWarning("Error in Lua task '" + name + "' toDebugString: " + e.getMessage());
        }
        
        // Default: return the task name
        return "LuaTask[" + name + "]";
    }
    
    /**
     * Gets the name of this Lua task
     */
    public String getName() {
        return name;
    }
    
    /**
     * Checks if this task has a specific function defined
     */
    public boolean hasFunction(String functionName) {
        try {
            if (taskLogic.istable()) {
                LuaValue func = taskLogic.get(functionName);
                return !func.isnil() && func.isfunction();
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
    
    /**
     * Calls a custom function in the task logic
     */
    public LuaValue callFunction(String functionName, LuaValue... args) {
        try {
            if (taskLogic.istable()) {
                LuaValue func = taskLogic.get(functionName);
                if (!func.isnil() && func.isfunction()) {
                    // Always pass taskLogic as self parameter
                    LuaValue[] fullArgs = new LuaValue[args.length + 1];
                    fullArgs[0] = taskLogic;
                    System.arraycopy(args, 0, fullArgs, 1, args.length);
                    
                    if (fullArgs.length == 1) {
                        return func.call(fullArgs[0]);
                    } else if (fullArgs.length == 2) {
                        return func.call(fullArgs[0], fullArgs[1]);
                    } else if (fullArgs.length == 3) {
                        return func.call(fullArgs[0], fullArgs[1], fullArgs[2]);
                    } else {
                        return func.invoke(fullArgs).arg1();
                    }
                }
            }
        } catch (Exception e) {
            mod.logWarning("Error calling function '" + functionName + "' in Lua task '" + name + "': " + e.getMessage());
        }
        return LuaValue.NIL;
    }
} 