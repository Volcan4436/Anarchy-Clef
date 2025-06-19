package adris.altoclef.scripting.api;

import adris.altoclef.AltoClef;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.OneArgFunction;

/**
 * Main API interface between Lua scripts and AltoClef functionality
 * Provides safe access to bot functions like logging, player info, world access, etc.
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class LuaAltoClefAPI extends LuaTable {
    private final AltoClef mod;
    
    public LuaAltoClefAPI(AltoClef mod) {
        this.mod = mod;
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
} 