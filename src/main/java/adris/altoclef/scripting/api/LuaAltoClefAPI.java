package adris.altoclef.scripting.api;

import adris.altoclef.AltoClef;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.ui.MessagePriority;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main API interface between Lua scripts and AltoClef functionality
 * Provides safe access to bot functions like logging, player info, world access, etc.
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class LuaAltoClefAPI extends LuaTable {
    private final AltoClef mod;
    private final String scriptName;
    
    // Event handlers for chat and commands
    private LuaValue onChatHandler = LuaValue.NIL;
    private LuaValue onCommandHandler = LuaValue.NIL;
    
    // Registered commands from this script
    private final Map<String, LuaScriptCommand> registeredCommands = new ConcurrentHashMap<>();
    
    public LuaAltoClefAPI(AltoClef mod) {
        this.mod = mod;
        this.scriptName = "unknown";
        initializeAPI();
    }
    
    public LuaAltoClefAPI(AltoClef mod, String scriptName) {
        this.mod = mod;
        this.scriptName = scriptName;
        initializeAPI();
    }
    
    /**
     * Initializes all API functions available to scripts
     */
    private void initializeAPI() {
        // Logging functions
        set("log", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue message) {
                mod.log("[Lua] " + message.tojstring());
                return LuaValue.NIL;
            }
        });
        
        set("logWarning", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue message) {
                mod.logWarning("[Lua] " + message.tojstring());
                return LuaValue.NIL;
            }
        });
        
        // Player information
        set("getPlayer", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return createPlayerTable();
                }
                return LuaValue.NIL;
            }
        });
        
        // World information
        set("getWorld", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getWorld() != null) {
                    return createWorldTable();
                }
                return LuaValue.NIL;
            }
        });
        
        // Dimension information
        set("getCurrentDimension", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(WorldHelper.getCurrentDimension().toString());
                } catch (Exception e) {
                    return LuaValue.valueOf("UNKNOWN");
                }
            }
        });
        
        // Basic bot status
        set("isInGame", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                return LuaValue.valueOf(AltoClef.inGame());
            }
        });
        
        // Direct player info methods for easier access
        set("getHunger", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return LuaValue.valueOf(mod.getPlayer().getHungerManager().getFoodLevel());
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("getSaturation", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return LuaValue.valueOf(mod.getPlayer().getHungerManager().getSaturationLevel());
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("isHungry", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return LuaValue.valueOf(mod.getPlayer().getHungerManager().getFoodLevel() < 20);
                }
                return LuaValue.FALSE;
            }
        });
        
        set("getHealth", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return LuaValue.valueOf(mod.getPlayer().getHealth());
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("getMaxHealth", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return LuaValue.valueOf(mod.getPlayer().getMaxHealth());
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("getHealthPercent", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    float health = mod.getPlayer().getHealth();
                    float maxHealth = mod.getPlayer().getMaxHealth();
                    return LuaValue.valueOf((health / maxHealth) * 100);
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("isLowHealth", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    float health = mod.getPlayer().getHealth();
                    float maxHealth = mod.getPlayer().getMaxHealth();
                    return LuaValue.valueOf((health / maxHealth) < 0.5);
                }
                return LuaValue.FALSE;
            }
        });
        
        set("getPlayerPos", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    LuaTable pos = new LuaTable();
                    pos.set("x", LuaValue.valueOf(mod.getPlayer().getX()));
                    pos.set("y", LuaValue.valueOf(mod.getPlayer().getY()));
                    pos.set("z", LuaValue.valueOf(mod.getPlayer().getZ()));
                    return pos;
                }
                return LuaValue.NIL;
            }
        });
        
        set("getPlayerX", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return LuaValue.valueOf(mod.getPlayer().getX());
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("getPlayerY", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return LuaValue.valueOf(mod.getPlayer().getY());
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("getPlayerZ", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return LuaValue.valueOf(mod.getPlayer().getZ());
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("getDimension", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(WorldHelper.getCurrentDimension().toString());
                } catch (Exception e) {
                    return LuaValue.valueOf("UNKNOWN");
                }
            }
        });
        
        set("hasFood", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                return LuaValue.valueOf(mod.getFoodChain().hasFood());
            }
        });
        
        set("needsFood", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                return LuaValue.valueOf(mod.getFoodChain().needsToEat());
            }
        });
        
        // Jump functionality - direct API access
        set("isJumping", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                // Check if player is currently jumping (not on ground and has positive Y velocity)
                return LuaValue.valueOf(!mod.getPlayer().isOnGround() && mod.getPlayer().getVelocity().y > 0);
            }
        });
        
        set("jump", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Use AltoClef's input controls to trigger a jump
                    mod.getInputControls().tryPress(baritone.api.utils.input.Input.JUMP);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.jump: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Velocity functionality - direct API access
        set("getVelocity", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.NIL;
                try {
                    Vec3d velocity = mod.getPlayer().getVelocity();
                    LuaTable result = new LuaTable();
                    result.set("x", LuaValue.valueOf(velocity.x));
                    result.set("y", LuaValue.valueOf(velocity.y));
                    result.set("z", LuaValue.valueOf(velocity.z));
                    return result;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getVelocity: " + e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });
        
        set("setVelocity", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Handle both AltoClef.setVelocity() and AltoClef:setVelocity() syntax
                    int offset = args.narg() == 4 ? 1 : 0;
                    if (args.narg() < 3 + offset) return LuaValue.FALSE;
                    
                    double x = args.arg(1 + offset).todouble();
                    double y = args.arg(2 + offset).todouble();
                    double z = args.arg(3 + offset).todouble();
                    
                    mod.getPlayer().setVelocity(x, y, z);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.setVelocity: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("runCommand", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue command) {
                try {
                    String cmd = command.tojstring();
                    if (!cmd.startsWith(mod.getModSettings().getCommandPrefix())) {
                        cmd = mod.getModSettings().getCommandPrefix() + cmd;
                    }
                    AltoClef.getCommandExecutor().execute(cmd);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Script command execution failed: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("getGameTime", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getWorld() != null) {
                    return LuaValue.valueOf(mod.getWorld().getTime());
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("isSingleplayer", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                return LuaValue.valueOf(MinecraftClient.getInstance().isInSingleplayer());
            }
        });
        
        set("getDifficulty", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getWorld() != null) {
                    return LuaValue.valueOf(mod.getWorld().getDifficulty().toString());
                }
                return LuaValue.valueOf("UNKNOWN");
            }
        });
        
        // Item storage access (basic)
        set("getItemStorage", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                return createItemStorageTable();
            }
        });
        
        // Command execution (limited)
        set("executeCommand", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue command) {
                try {
                    String cmd = command.tojstring();
                    if (!cmd.startsWith(mod.getModSettings().getCommandPrefix())) {
                        cmd = mod.getModSettings().getCommandPrefix() + cmd;
                    }
                    AltoClef.getCommandExecutor().execute(cmd);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Script command execution failed: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Task System API - Phase 2
        set("TaskSystem", new LuaTaskSystemAPI(mod));
        
        // Chat and Command System APIs
        initializeChatAndCommandAPIs();
        
        // Note: Utils API is now created per-script in LuaScriptEngine 
        // to support persistence with script-specific context
        
        // TODO: Add more APIs in future phases
        // These will be implemented as we develop Phase 2
        /*
        set("getEntityTracker", new ZeroArgFunction() { ... });
        set("getBlockTracker", new ZeroArgFunction() { ... });
        set("getBehaviour", new ZeroArgFunction() { ... });
        */
    }
    
    /**
     * Creates a Lua table with player information
     */
    private LuaTable createPlayerTable() {
        LuaTable playerTable = new LuaTable();
        
        try {
            if (mod.getPlayer() != null) {
                playerTable.set("name", LuaValue.valueOf(mod.getPlayer().getName().getString()));
                playerTable.set("health", LuaValue.valueOf(mod.getPlayer().getHealth()));
                playerTable.set("maxHealth", LuaValue.valueOf(mod.getPlayer().getMaxHealth()));
                playerTable.set("hunger", LuaValue.valueOf(mod.getPlayer().getHungerManager().getFoodLevel()));
                playerTable.set("saturation", LuaValue.valueOf(mod.getPlayer().getHungerManager().getSaturationLevel()));
                
                // Position
                LuaTable pos = new LuaTable();
                pos.set("x", LuaValue.valueOf(mod.getPlayer().getX()));
                pos.set("y", LuaValue.valueOf(mod.getPlayer().getY()));
                pos.set("z", LuaValue.valueOf(mod.getPlayer().getZ()));
                playerTable.set("position", pos);
                
                // Basic status
                playerTable.set("isOnGround", LuaValue.valueOf(mod.getPlayer().isOnGround()));
                playerTable.set("isInWater", LuaValue.valueOf(mod.getPlayer().isTouchingWater()));
                playerTable.set("isInLava", LuaValue.valueOf(mod.getPlayer().isInLava()));
                playerTable.set("isSneaking", LuaValue.valueOf(mod.getPlayer().isSneaking()));
                playerTable.set("isSprinting", LuaValue.valueOf(mod.getPlayer().isSprinting()));
            }
        } catch (Exception e) {
            mod.logWarning("Error creating player table for script: " + e.getMessage());
        }
        
        return playerTable;
    }
    
    /**
     * Creates a Lua table with world information
     */
    private LuaTable createWorldTable() {
        LuaTable worldTable = new LuaTable();
        
        try {
            if (mod.getWorld() != null) {
                worldTable.set("time", LuaValue.valueOf(mod.getWorld().getTimeOfDay()));
                worldTable.set("isDay", LuaValue.valueOf(mod.getWorld().isDay()));
                worldTable.set("isNight", LuaValue.valueOf(mod.getWorld().isNight()));
                worldTable.set("isRaining", LuaValue.valueOf(mod.getWorld().isRaining()));
                worldTable.set("isThundering", LuaValue.valueOf(mod.getWorld().isThundering()));
                
                // Difficulty
                worldTable.set("difficulty", LuaValue.valueOf(mod.getWorld().getDifficulty().toString()));
                
                // Player count (if available)
                if (mod.getWorld().getPlayers() != null) {
                    worldTable.set("playerCount", LuaValue.valueOf(mod.getWorld().getPlayers().size()));
                }
            }
        } catch (Exception e) {
            mod.logWarning("Error creating world table for script: " + e.getMessage());
        }
        
        return worldTable;
    }
    
    /**
     * Creates a Lua table with basic item storage information
     */
    private LuaTable createItemStorageTable() {
        LuaTable storageTable = new LuaTable();
        
        try {
            // Add basic item checking function
            storageTable.set("hasItem", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue itemName) {
                    try {
                        Item item = parseItemName(itemName.tojstring());
                        if (item != null) {
                            return LuaValue.valueOf(mod.getItemStorage().hasItem(item));
                        }
                        return LuaValue.FALSE;
                    } catch (Exception e) {
                        return LuaValue.FALSE;
                    }
                }
            });
            
            storageTable.set("getItemCount", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue itemName) {
                    try {
                        Item item = parseItemName(itemName.tojstring());
                        if (item != null) {
                            return LuaValue.valueOf(mod.getItemStorage().getItemCount(item));
                        }
                        return LuaValue.valueOf(0);
                    } catch (Exception e) {
                        return LuaValue.valueOf(0);
                    }
                }
            });
            
            // TODO: Add more storage functions in Phase 2
            // getInventorySlots(), hasItemInHotbar(), etc.
            
        } catch (Exception e) {
            mod.logWarning("Error creating item storage table for script: " + e.getMessage());
        }
        
        return storageTable;
    }
    
    /**
     * Helper function to convert item name strings to Item objects
     */
    private Item parseItemName(String itemName) {
        try {
            // Trim and format the item name
            String trimmedName = ItemHelper.trimItemName(itemName);
            
            // Handle common cases where users might not include namespace
            if (!trimmedName.contains(":")) {
                trimmedName = "minecraft:" + trimmedName;
            }
            
            Identifier identifier = new Identifier(trimmedName);
            if (Registries.ITEM.containsId(identifier)) {
                return Registries.ITEM.get(identifier);
            }
            
            mod.logWarning("Script tried to access unknown item: " + itemName);
            return null;
        } catch (Exception e) {
            mod.logWarning("Script item parsing error for '" + itemName + "': " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Initializes chat and command functionality
     */
    private void initializeChatAndCommandAPIs() {
        // Chat sending functionality
        set("chat", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue message) {
                try {
                    if (MinecraftClient.getInstance().player != null) {
                        String msg = message.tojstring();
                        MinecraftClient.getInstance().player.networkHandler.sendChatMessage(msg);
                        return LuaValue.TRUE;
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error sending chat message from script '" + scriptName + "': " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Set chat event handler
        set("onchat", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue handler) {
                if (handler.isfunction()) {
                    onChatHandler = handler;
                    mod.log("Chat handler registered for script: " + scriptName);
                    return LuaValue.TRUE;
                } else {
                    onChatHandler = LuaValue.NIL;
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Set command event handler
        set("oncommand", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue handler) {
                if (handler.isfunction()) {
                    onCommandHandler = handler;
                    mod.log("Command handler registered for script: " + scriptName);
                    return LuaValue.TRUE;
                } else {
                    onCommandHandler = LuaValue.NIL;
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Create custom command
        set("createcommand", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (args.narg() < 3) {
                    mod.logWarning("createcommand requires 3 arguments: name, description, handler");
                    return LuaValue.FALSE;
                }
                
                try {
                    String cmdName = args.arg(1).tojstring();
                    String cmdDescription = args.arg(2).tojstring();
                    LuaValue cmdHandler = args.arg(3);
                    
                    if (!cmdHandler.isfunction()) {
                        mod.logWarning("Command handler must be a function");
                        return LuaValue.FALSE;
                    }
                    
                    // Create and register the command
                    LuaScriptCommand luaCommand = new LuaScriptCommand(cmdName, cmdDescription, cmdHandler, scriptName);
                    AltoClef.getCommandExecutor().registerNewCommand(luaCommand);
                    registeredCommands.put(cmdName, luaCommand);
                    
                    mod.log("Registered command '@" + cmdName + "' from script: " + scriptName);
                    return LuaValue.TRUE;
                    
                } catch (Exception e) {
                    mod.logWarning("Error creating command in script '" + scriptName + "': " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
    }
    
    /**
     * Custom command created by Lua scripts
     */
    private class LuaScriptCommand extends Command {
        private final LuaValue luaHandler;
        private final String scriptName;
        
        public LuaScriptCommand(String name, String description, LuaValue handler, String scriptName) {
            super(name, description); // No args for now, we'll parse them manually
            this.luaHandler = handler;
            this.scriptName = scriptName;
        }
        
        @Override
        protected void call(AltoClef mod, ArgParser parser) throws CommandException {
            try {
                if (!luaHandler.isnil()) {
                    // Create arguments table for Lua
                    LuaTable args = new LuaTable();
                    String[] argUnits = parser.getArgUnits();
                    if (argUnits != null && argUnits.length > 0) {
                        for (int i = 0; i < argUnits.length; i++) {
                            args.set(i + 1, LuaValue.valueOf(argUnits[i]));
                        }
                    }
                    
                    // Call the Lua handler with arguments
                    luaHandler.call(args);
                } else {
                    mod.log("Command handler is nil for script: " + scriptName);
                }
            } catch (Exception e) {
                throw new CommandException("Error executing Lua command '" + getName() + "': " + e.getMessage());
            }
            finish();
        }
    }
    
    /**
     * Handle chat events from the event bus
     */
    public void handleChatEvent(String message, String sender, String senderUUID, boolean isSelf) {
        if (!onChatHandler.isnil()) {
            try {
                // Create chat info table
                LuaTable chatInfo = new LuaTable();
                chatInfo.set("message", LuaValue.valueOf(message));
                chatInfo.set("sender", LuaValue.valueOf(sender));
                chatInfo.set("senderUUID", LuaValue.valueOf(senderUUID));
                chatInfo.set("isSelf", LuaValue.valueOf(isSelf));
                chatInfo.set("timestamp", LuaValue.valueOf(System.currentTimeMillis()));
                
                // Call the Lua handler
                onChatHandler.call(chatInfo);
            } catch (Exception e) {
                mod.logWarning("Error in Lua chat handler for script '" + scriptName + "': " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle command events
     */
    public void handleCommandEvent(String command, String args) {
        if (!onCommandHandler.isnil()) {
            try {
                // Create command info table
                LuaTable cmdInfo = new LuaTable();
                cmdInfo.set("command", LuaValue.valueOf(command));
                cmdInfo.set("args", LuaValue.valueOf(args != null ? args : ""));
                cmdInfo.set("timestamp", LuaValue.valueOf(System.currentTimeMillis()));
                
                // Call the Lua handler
                onCommandHandler.call(cmdInfo);
            } catch (Exception e) {
                mod.logWarning("Error in Lua command handler for script '" + scriptName + "': " + e.getMessage());
            }
        }
    }
    
    /**
     * Cleanup when script is unloaded
     */
    public void cleanup() {
        // Unregister any commands created by this script
        for (String commandName : registeredCommands.keySet()) {
            // Note: We'd need access to CommandExecutor to properly unregister
            // For now, we'll just clear our local tracking
        }
        registeredCommands.clear();
        onChatHandler = LuaValue.NIL;
        onCommandHandler = LuaValue.NIL;
    }
} 