package adris.altoclef.scripting.api;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskChain;
// import adris.altoclef.util.ItemTarget; // TODO: For future advanced task creation with item specifications
import net.minecraft.item.Item;
// import net.minecraft.item.Items; // TODO: For future convenient access to vanilla items (Items.DIAMOND, etc.)
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
// import org.luaj.vm2.lib.ThreeArgFunction; // TODO: For future complex API functions with 3+ parameters
import org.luaj.vm2.lib.VarArgFunction;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Lua API for AltoClef Task System integration
 * Allows scripts to create custom tasks, run existing tasks, and manage task execution
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class LuaTaskSystemAPI extends LuaTable {
    private final AltoClef mod;
    private final Map<String, LuaTask> scriptTasks;
    
    public LuaTaskSystemAPI(AltoClef mod) {
        this.mod = mod;
        this.scriptTasks = new ConcurrentHashMap<>();
        initializeAPI();
    }
    
    /**
     * Initializes all API functions for Task System integration
     */
    private void initializeAPI() {
        // Task creation and management
        set("createTask", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both TaskSystem.createTask() and TaskSystem:createTask() syntax
                int offset = args.narg() == 3 ? 1 : 0;
                
                if (args.narg() < 2 + offset) return LuaValue.NIL;
                
                String name = args.arg(1 + offset).tojstring();
                LuaValue taskLogic = args.arg(2 + offset);
                
                return createTask(name, taskLogic);
            }
        });
        
        // Run existing catalogued tasks
        set("runTask", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                int offset = args.narg() >= 4 ? 1 : 0;
                
                if (args.narg() < 2 + offset) return LuaValue.FALSE;
                
                String taskName = args.arg(1 + offset).tojstring();
                int count = args.narg() > 1 + offset ? args.arg(2 + offset).toint() : 1;
                
                return LuaValue.valueOf(runCataloguedTask(taskName, count));
            }
        });
        
        // Run item collection tasks
        set("getItem", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                int offset = args.narg() >= 4 ? 1 : 0;
                
                if (args.narg() < 2 + offset) return LuaValue.FALSE;
                
                String itemName = args.arg(1 + offset).tojstring();
                int count = args.narg() > 1 + offset ? args.arg(2 + offset).toint() : 1;
                
                return LuaValue.valueOf(runItemTask(itemName, count));
            }
        });
        
        // Task status and control
        set("getCurrentTask", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                return getCurrentTask();
            }
        });
        
        set("isTaskRunning", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                return LuaValue.valueOf(isTaskRunning());
            }
        });
        
        set("stopCurrentTask", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                stopCurrentTask();
                return LuaValue.NIL;
            }
        });
        
        // Task catalogue access
        set("getAvailableTasks", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                return getAvailableTasks();
            }
        });
        
        set("taskExists", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue self, LuaValue taskName) {
                return LuaValue.valueOf(TaskCatalogue.taskExists(taskName.tojstring()));
            }
        });
        
        // Utility functions
        set("hasItem", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                int offset = args.narg() >= 4 ? 1 : 0;
                
                if (args.narg() < 2 + offset) return LuaValue.FALSE;
                
                String itemName = args.arg(1 + offset).tojstring();
                int count = args.narg() > 1 + offset ? args.arg(2 + offset).toint() : 1;
                
                return LuaValue.valueOf(hasItem(itemName, count));
            }
        });
    }
    
    /**
     * Creates a custom Lua task
     */
    private LuaTable createTask(String name, LuaValue taskLogic) {
        try {
            // Check if task already exists
            if (scriptTasks.containsKey(name)) {
                mod.logWarning("Lua task already exists: " + name);
                return new LuaTable();
            }
            
            // Create the Lua task wrapper
            LuaTask luaTask = new LuaTask(name, taskLogic, mod);
            scriptTasks.put(name, luaTask);
            
            mod.log("Created Lua task: " + name);
            
            // Return Lua table representing the task
            return createTaskTable(luaTask);
            
        } catch (Exception e) {
            mod.logWarning("Error creating Lua task '" + name + "': " + e.getMessage());
            return new LuaTable();
        }
    }
    
    /**
     * Runs a catalogued task from TaskCatalogue
     */
    private boolean runCataloguedTask(String taskName, int count) {
        try {
            if (!TaskCatalogue.taskExists(taskName)) {
                mod.logWarning("Task does not exist in catalogue: " + taskName);
                return false;
            }
            
            Task task = TaskCatalogue.getItemTask(taskName, count);
            if (task == null) {
                mod.logWarning("Failed to create task: " + taskName);
                return false;
            }
            
            mod.runUserTask(task);
            mod.log("Started task: " + taskName + " (count: " + count + ")");
            return true;
            
        } catch (Exception e) {
            mod.logWarning("Error running task '" + taskName + "': " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Runs an item collection task
     */
    private boolean runItemTask(String itemName, int count) {
        try {
            Item item = parseItemName(itemName);
            if (item == null) {
                mod.logWarning("Unknown item: " + itemName);
                return false;
            }
            
            // Try catalogue first, then direct item task
            if (TaskCatalogue.taskExists(item)) {
                Task task = TaskCatalogue.getItemTask(item, count);
                mod.runUserTask(task);
                mod.log("Started item task: " + itemName + " (count: " + count + ")");
                return true;
            } else {
                mod.logWarning("No task available for item: " + itemName);
                return false;
            }
            
        } catch (Exception e) {
            mod.logWarning("Error running item task '" + itemName + "': " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets information about the currently running task
     */
    private LuaTable getCurrentTask() {
        LuaTable taskInfo = new LuaTable();
        
        try {
            if (mod.getUserTaskChain().isActive()) {
                var tasks = mod.getUserTaskChain().getTasks();
                if (!tasks.isEmpty()) {
                    Task currentTask = tasks.get(tasks.size() - 1); // Get the leaf task
                    
                    taskInfo.set("name", LuaValue.valueOf(currentTask.getClass().getSimpleName()));
                    taskInfo.set("active", LuaValue.valueOf(currentTask.isActive()));
                    taskInfo.set("finished", LuaValue.valueOf(currentTask.isFinished(mod)));
                    taskInfo.set("debug", LuaValue.valueOf(currentTask.toString()));
                    
                    // Add task chain info
                    taskInfo.set("chainLength", LuaValue.valueOf(tasks.size()));
                    taskInfo.set("isIdleTask", LuaValue.valueOf(mod.getUserTaskChain().isRunningIdleTask()));
                }
            }
        } catch (Exception e) {
            mod.logWarning("Error getting current task info: " + e.getMessage());
        }
        
        return taskInfo;
    }
    
    /**
     * Checks if any task is currently running
     */
    private boolean isTaskRunning() {
        return mod.getUserTaskChain().isActive() && !mod.getUserTaskChain().getTasks().isEmpty();
    }
    
    /**
     * Stops the currently running task
     */
    private void stopCurrentTask() {
        try {
            mod.cancelUserTask();
            mod.log("Stopped current task");
        } catch (Exception e) {
            mod.logWarning("Error stopping current task: " + e.getMessage());
        }
    }
    
    /**
     * Gets list of all available tasks in the catalogue
     */
    private LuaTable getAvailableTasks() {
        LuaTable tasks = new LuaTable();
        
        try {
            var resourceNames = TaskCatalogue.resourceNames();
            int index = 1;
            
            for (String name : resourceNames) {
                tasks.set(index++, LuaValue.valueOf(name));
            }
        } catch (Exception e) {
            mod.logWarning("Error getting available tasks: " + e.getMessage());
        }
        
        return tasks;
    }
    
    /**
     * Checks if the player has the specified item and count
     */
    private boolean hasItem(String itemName, int count) {
        try {
            Item item = parseItemName(itemName);
            if (item == null) return false;
            
            return mod.getItemStorage().getItemCount(item) >= count;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Creates a Lua table representing a custom task
     */
    private LuaTable createTaskTable(LuaTask luaTask) {
        LuaTable taskTable = new LuaTable();
        
        taskTable.set("name", LuaValue.valueOf(luaTask.getName()));
        taskTable.set("active", LuaValue.valueOf(luaTask.isActive()));
        taskTable.set("finished", LuaValue.valueOf(luaTask.isFinished(mod)));
        
        // Task control functions
        taskTable.set("run", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                mod.runUserTask(luaTask);
                return LuaValue.NIL;
            }
        });
        
        taskTable.set("stop", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (luaTask.isActive()) {
                    luaTask.stop(mod);
                }
                return LuaValue.NIL;
            }
        });
        
        taskTable.set("isFinished", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                return LuaValue.valueOf(luaTask.isFinished(mod));
            }
        });
        
        return taskTable;
    }
    
    /**
     * Helper function to parse item names to Item objects
     */
    private Item parseItemName(String itemName) {
        try {
            // Handle common variations
            String trimmedName = itemName.toLowerCase().trim();
            
            // Add minecraft namespace if not present
            if (!trimmedName.contains(":")) {
                trimmedName = "minecraft:" + trimmedName;
            }
            
            Identifier identifier = new Identifier(trimmedName);
            if (Registries.ITEM.containsId(identifier)) {
                return Registries.ITEM.get(identifier);
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Cleanup script tasks when scripts are unloaded
     */
    public void cleanup() {
        for (LuaTask task : scriptTasks.values()) {
            if (task.isActive()) {
                task.stop(mod);
            }
        }
        scriptTasks.clear();
    }
} 