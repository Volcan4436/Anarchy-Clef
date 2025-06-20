package adris.altoclef.scripting.api;

import adris.altoclef.AltoClef;
import adris.altoclef.scripting.persistence.ScriptPersistenceManager;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Comprehensive utility functions library for Lua scripting
 * Provides math, string, table, time, position, and debug utilities
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class LuaUtilsAPI extends LuaTable {
    private final AltoClef mod;
    private final Map<String, Long> timers;
    private final ScriptPersistenceManager persistenceManager;
    private final String currentScriptName;
    
    public LuaUtilsAPI(AltoClef mod, ScriptPersistenceManager persistenceManager, String scriptName) {
        this.mod = mod;
        this.timers = new ConcurrentHashMap<>();
        this.persistenceManager = persistenceManager;
        this.currentScriptName = scriptName;
        initializeAPI();
    }
    
    /**
     * Initializes all utility API functions
     */
    private void initializeAPI() {
        // Math utilities
        set("Math", createMathUtils());
        
        // String utilities
        set("String", createStringUtils());
        
        // Table utilities
        set("Table", createTableUtils());
        
        // Time utilities
        set("Time", createTimeUtils());
        
        // Position/Vector utilities
        set("Position", createPositionUtils());
        
        // Item/Block utilities
        set("Item", createItemUtils());
        
        // Player utilities
        set("Player", createPlayerUtils());
        
        // World utilities
        set("World", createWorldUtils());
        
        // Control utilities
        set("Control", createControlUtils());
        
        // Chat & command utilities
        set("Chat", createChatUtils());
        
        // Inventory utilities
        set("Inventory", createInventoryUtils());
        
        // Entity utilities
        set("Entity", createEntityUtils());
        
        // Debug utilities
        set("Debug", createDebugUtils());
        
        // Data persistence utilities
        set("Data", createDataUtils());
        
        // Cleanup function for script unloading
        set("cleanup", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                cleanup();
                return LuaValue.NIL;
            }
        });
    }
    
    /**
     * Creates math utility functions
     */
    private LuaTable createMathUtils() {
        LuaTable mathUtils = new LuaTable();
        
        // Distance calculations
        mathUtils.set("distance2D", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Math.distance2D() and Math:distance2D() syntax
                int offset = args.narg() == 3 ? 1 : 0;
                if (args.narg() < 2 + offset) return LuaValue.valueOf(0);
                
                LuaValue pos1 = args.arg(1 + offset);
                LuaValue pos2 = args.arg(2 + offset);
                try {
                    if (!pos1.istable() || !pos2.istable()) return LuaValue.valueOf(0);
                    
                    LuaValue x1Val = pos1.get("x");
                    LuaValue z1Val = pos1.get("z");
                    LuaValue x2Val = pos2.get("x");
                    LuaValue z2Val = pos2.get("z");
                    
                    if (x1Val.isnil() || z1Val.isnil() || x2Val.isnil() || z2Val.isnil()) return LuaValue.valueOf(0);
                    
                    double x1 = x1Val.todouble();
                    double z1 = z1Val.todouble();
                    double x2 = x2Val.todouble();
                    double z2 = z2Val.todouble();
                    
                    double distance = Math.sqrt((x2 - x1) * (x2 - x1) + (z2 - z1) * (z2 - z1));
                    return LuaValue.valueOf(distance);
                } catch (Exception e) {
                    mod.logWarning("Error in distance2D: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        mathUtils.set("distance3D", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Math.distance3D() and Math:distance3D() syntax
                int offset = args.narg() == 3 ? 1 : 0;
                if (args.narg() < 2 + offset) return LuaValue.valueOf(0);
                
                LuaValue pos1 = args.arg(1 + offset);
                LuaValue pos2 = args.arg(2 + offset);
                
                try {
                    if (!pos1.istable() || !pos2.istable()) return LuaValue.valueOf(0);
                    
                    LuaValue x1Val = pos1.get("x");
                    LuaValue y1Val = pos1.get("y");
                    LuaValue z1Val = pos1.get("z");
                    LuaValue x2Val = pos2.get("x");
                    LuaValue y2Val = pos2.get("y");
                    LuaValue z2Val = pos2.get("z");
                    
                    if (x1Val.isnil() || y1Val.isnil() || z1Val.isnil() || x2Val.isnil() || y2Val.isnil() || z2Val.isnil()) {
                        return LuaValue.valueOf(0);
                    }
                    
                    double x1 = x1Val.todouble();
                    double y1 = y1Val.todouble();
                    double z1 = z1Val.todouble();
                    double x2 = x2Val.todouble();
                    double y2 = y2Val.todouble();
                    double z2 = z2Val.todouble();
                    
                    double distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1));
                    return LuaValue.valueOf(distance);
                } catch (Exception e) {
                    mod.logWarning("Error in distance3D: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        // Angle calculations
        mathUtils.set("angleTo", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Math.angleTo() and Math:angleTo() syntax
                int offset = args.narg() == 3 ? 1 : 0;
                if (args.narg() < 2 + offset) return LuaValue.valueOf(0);
                
                LuaValue from = args.arg(1 + offset);
                LuaValue to = args.arg(2 + offset);
                
                try {
                    if (!from.istable() || !to.istable()) return LuaValue.valueOf(0);
                    
                    LuaValue x1Val = from.get("x");
                    LuaValue z1Val = from.get("z");
                    LuaValue x2Val = to.get("x");
                    LuaValue z2Val = to.get("z");
                    
                    if (x1Val.isnil() || z1Val.isnil() || x2Val.isnil() || z2Val.isnil()) return LuaValue.valueOf(0);
                    
                    double x1 = x1Val.todouble();
                    double z1 = z1Val.todouble();
                    double x2 = x2Val.todouble();
                    double z2 = z2Val.todouble();
                    
                    double angle = Math.atan2(z2 - z1, x2 - x1) * 180.0 / Math.PI;
                    return LuaValue.valueOf(angle);
                } catch (Exception e) {
                    mod.logWarning("Error in angleTo: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        // Utility math functions
        mathUtils.set("clamp", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Math.clamp() and Math:clamp() syntax
                int offset = args.narg() == 4 ? 1 : 0;
                if (args.narg() < 3 + offset) return LuaValue.valueOf(0);
                
                LuaValue value = args.arg(1 + offset);
                LuaValue min = args.arg(2 + offset);
                LuaValue max = args.arg(3 + offset);
                
                double val = value.todouble();
                double minVal = min.todouble();
                double maxVal = max.todouble();
                return LuaValue.valueOf(Math.max(minVal, Math.min(maxVal, val)));
            }
        });
        
        mathUtils.set("lerp", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Math.lerp() and Math:lerp() syntax
                int offset = args.narg() == 4 ? 1 : 0;
                if (args.narg() < 3 + offset) return LuaValue.valueOf(0);
                
                LuaValue a = args.arg(1 + offset);
                LuaValue b = args.arg(2 + offset);
                LuaValue t = args.arg(3 + offset);
                
                double aVal = a.todouble();
                double bVal = b.todouble();
                double tVal = t.todouble();
                return LuaValue.valueOf(aVal + tVal * (bVal - aVal));
            }
        });
        
        mathUtils.set("round", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Math.round() and Math:round() syntax
                int offset = args.narg() == 3 ? 1 : 0;
                if (args.narg() < 2 + offset) return LuaValue.valueOf(0);
                
                LuaValue value = args.arg(1 + offset);
                LuaValue decimals = args.arg(2 + offset);
                
                double val = value.todouble();
                int dec = decimals.toint();
                double multiplier = Math.pow(10, dec);
                return LuaValue.valueOf(Math.round(val * multiplier) / multiplier);
            }
        });
        
        return mathUtils;
    }
    
    /**
     * Creates string utility functions
     */
    private LuaTable createStringUtils() {
        LuaTable stringUtils = new LuaTable();
        
        stringUtils.set("split", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both String.split() and String:split() syntax
                int offset = args.narg() == 3 ? 1 : 0;
                if (args.narg() < 2 + offset) return new LuaTable();
                
                LuaValue str = args.arg(1 + offset);
                LuaValue delimiter = args.arg(2 + offset);
                
                try {
                    String text = str.tojstring();
                    String delim = delimiter.tojstring();
                    String[] parts = text.split(java.util.regex.Pattern.quote(delim));
                    LuaTable result = new LuaTable();
                    for (int i = 0; i < parts.length; i++) {
                        result.set(i + 1, LuaValue.valueOf(parts[i]));
                    }
                    return result;
                } catch (Exception e) {
                    mod.logWarning("Error in string split: " + e.getMessage());
                    return new LuaTable();
                }
            }
        });
        
        stringUtils.set("trim", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both String.trim() and String:trim() syntax
                int offset = args.narg() == 2 ? 1 : 0;
                if (args.narg() < 1 + offset) return LuaValue.valueOf("");
                
                LuaValue str = args.arg(1 + offset);
                
                try {
                    return LuaValue.valueOf(str.tojstring().trim());
                } catch (Exception e) {
                    mod.logWarning("Error in string trim: " + e.getMessage());
                    return str;
                }
            }
        });
        
        stringUtils.set("startsWith", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both String.startsWith() and String:startsWith() syntax
                int offset = args.narg() == 3 ? 1 : 0;
                if (args.narg() < 2 + offset) return LuaValue.FALSE;
                
                LuaValue str = args.arg(1 + offset);
                LuaValue prefix = args.arg(2 + offset);
                
                try {
                    return LuaValue.valueOf(str.tojstring().startsWith(prefix.tojstring()));
                } catch (Exception e) {
                    mod.logWarning("Error in string startsWith: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        stringUtils.set("endsWith", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both String.endsWith() and String:endsWith() syntax
                int offset = args.narg() == 3 ? 1 : 0;
                if (args.narg() < 2 + offset) return LuaValue.FALSE;
                
                LuaValue str = args.arg(1 + offset);
                LuaValue suffix = args.arg(2 + offset);
                
                try {
                    return LuaValue.valueOf(str.tojstring().endsWith(suffix.tojstring()));
                } catch (Exception e) {
                    mod.logWarning("Error in string endsWith: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        stringUtils.set("contains", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both String.contains() and String:contains() syntax
                int offset = args.narg() == 3 ? 1 : 0;
                if (args.narg() < 2 + offset) return LuaValue.FALSE;
                
                LuaValue str = args.arg(1 + offset);
                LuaValue substring = args.arg(2 + offset);
                
                try {
                    return LuaValue.valueOf(str.tojstring().contains(substring.tojstring()));
                } catch (Exception e) {
                    mod.logWarning("Error in string contains: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        stringUtils.set("capitalize", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both String.capitalize() and String:capitalize() syntax
                int offset = args.narg() == 2 ? 1 : 0;
                if (args.narg() < 1 + offset) return LuaValue.valueOf("");
                
                LuaValue str = args.arg(1 + offset);
                
                try {
                    String s = str.tojstring();
                    if (s.isEmpty()) return str;
                    return LuaValue.valueOf(s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase());
                } catch (Exception e) {
                    mod.logWarning("Error in string capitalize: " + e.getMessage());
                    return str;
                }
            }
        });
        
        return stringUtils;
    }
    
    /**
     * Creates table utility functions
     */
    private LuaTable createTableUtils() {
        LuaTable tableUtils = new LuaTable();
        
        tableUtils.set("length", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Table.length() and Table:length() syntax
                int offset = args.narg() == 2 ? 1 : 0;
                if (args.narg() < 1 + offset) return LuaValue.valueOf(0);
                
                LuaValue table = args.arg(1 + offset);
                
                if (!table.istable()) return LuaValue.valueOf(0);
                return LuaValue.valueOf(table.length());
            }
        });
        
        tableUtils.set("isEmpty", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Table.isEmpty() and Table:isEmpty() syntax
                int offset = args.narg() == 2 ? 1 : 0;
                if (args.narg() < 1 + offset) return LuaValue.TRUE;
                
                LuaValue table = args.arg(1 + offset);
                
                return LuaValue.valueOf(!table.istable() || table.length() == 0);
            }
        });
        
        tableUtils.set("contains", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Table.contains() and Table:contains() syntax
                int offset = args.narg() == 3 ? 1 : 0;
                if (args.narg() < 2 + offset) return LuaValue.FALSE;
                
                LuaValue table = args.arg(1 + offset);
                LuaValue value = args.arg(2 + offset);
                
                if (!table.istable()) return LuaValue.FALSE;
                
                LuaValue k = LuaValue.NIL;
                while (true) {
                    Varargs n = table.next(k);
                    if ((k = n.arg1()).isnil()) break;
                    LuaValue v = n.arg(2);
                    if (v.equals(value)) return LuaValue.TRUE;
                }
                return LuaValue.FALSE;
            }
        });
        
        tableUtils.set("indexOf", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Table.indexOf() and Table:indexOf() syntax
                int offset = args.narg() == 3 ? 1 : 0;
                if (args.narg() < 2 + offset) return LuaValue.valueOf(-1);
                
                LuaValue table = args.arg(1 + offset);
                LuaValue value = args.arg(2 + offset);
                
                if (!table.istable()) return LuaValue.valueOf(-1);
                
                for (int i = 1; i <= table.length(); i++) {
                    if (table.get(i).equals(value)) {
                        return LuaValue.valueOf(i);
                    }
                }
                return LuaValue.valueOf(-1);
            }
        });
        
        tableUtils.set("reverse", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Table.reverse() and Table:reverse() syntax
                int offset = args.narg() == 2 ? 1 : 0;
                if (args.narg() < 1 + offset) return new LuaTable();
                
                LuaValue table = args.arg(1 + offset);
                
                if (!table.istable()) return table;
                
                LuaTable result = new LuaTable();
                int len = table.length();
                for (int i = 1; i <= len; i++) {
                    result.set(len - i + 1, table.get(i));
                }
                return result;
            }
        });
        
        return tableUtils;
    }
    
    /**
     * Creates time utility functions
     */
    private LuaTable createTimeUtils() {
        LuaTable timeUtils = new LuaTable();
        
        timeUtils.set("startTimer", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue name) {
                timers.put(name.tojstring(), System.currentTimeMillis());
                return LuaValue.NIL;
            }
        });
        
        timeUtils.set("getElapsed", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue name) {
                Long startTime = timers.get(name.tojstring());
                if (startTime == null) return LuaValue.valueOf(-1);
                return LuaValue.valueOf(System.currentTimeMillis() - startTime);
            }
        });
        
        timeUtils.set("hasElapsed", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue name, LuaValue milliseconds) {
                Long startTime = timers.get(name.tojstring());
                if (startTime == null) return LuaValue.FALSE;
                return LuaValue.valueOf(System.currentTimeMillis() - startTime >= milliseconds.tolong());
            }
        });
        
        timeUtils.set("resetTimer", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue name) {
                timers.put(name.tojstring(), System.currentTimeMillis());
                return LuaValue.NIL;
            }
        });
        
        timeUtils.set("removeTimer", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue name) {
                timers.remove(name.tojstring());
                return LuaValue.NIL;
            }
        });
        
        timeUtils.set("getCurrentTime", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                return LuaValue.valueOf(System.currentTimeMillis());
            }
        });
        
        return timeUtils;
    }
    
    /**
     * Creates position/vector utility functions
     */
    private LuaTable createPositionUtils() {
        LuaTable posUtils = new LuaTable();
        
        posUtils.set("create", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Position.create() and Position:create() syntax
                int offset = args.narg() == 4 ? 1 : 0;
                if (args.narg() < 3 + offset) return new LuaTable();
                
                LuaValue x = args.arg(1 + offset);
                LuaValue y = args.arg(2 + offset);
                LuaValue z = args.arg(3 + offset);
                
                LuaTable pos = new LuaTable();
                pos.set("x", x);
                pos.set("y", y);
                pos.set("z", z);
                return pos;
            }
        });
        
        posUtils.set("add", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Position.add() and Position:add() syntax
                int offset = args.narg() == 3 ? 1 : 0;
                if (args.narg() < 2 + offset) return new LuaTable();
                
                LuaValue pos1 = args.arg(1 + offset);
                LuaValue pos2 = args.arg(2 + offset);
                
                try {
                    if (!pos1.istable() || !pos2.istable()) return new LuaTable();
                    
                    LuaValue x1 = pos1.get("x");
                    LuaValue y1 = pos1.get("y");
                    LuaValue z1 = pos1.get("z");
                    LuaValue x2 = pos2.get("x");
                    LuaValue y2 = pos2.get("y");
                    LuaValue z2 = pos2.get("z");
                    
                    if (x1.isnil() || y1.isnil() || z1.isnil() || x2.isnil() || y2.isnil() || z2.isnil()) {
                        return new LuaTable();
                    }
                    
                    LuaTable result = new LuaTable();
                    result.set("x", LuaValue.valueOf(x1.todouble() + x2.todouble()));
                    result.set("y", LuaValue.valueOf(y1.todouble() + y2.todouble()));
                    result.set("z", LuaValue.valueOf(z1.todouble() + z2.todouble()));
                    return result;
                } catch (Exception e) {
                    mod.logWarning("Error in position add: " + e.getMessage());
                    return new LuaTable();
                }
            }
        });
        
        posUtils.set("subtract", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue pos1, LuaValue pos2) {
                try {
                    if (!pos1.istable() || !pos2.istable()) return new LuaTable();
                    
                    LuaValue x1 = pos1.get("x");
                    LuaValue y1 = pos1.get("y");
                    LuaValue z1 = pos1.get("z");
                    LuaValue x2 = pos2.get("x");
                    LuaValue y2 = pos2.get("y");
                    LuaValue z2 = pos2.get("z");
                    
                    if (x1.isnil() || y1.isnil() || z1.isnil() || x2.isnil() || y2.isnil() || z2.isnil()) {
                        return new LuaTable();
                    }
                    
                    LuaTable result = new LuaTable();
                    result.set("x", LuaValue.valueOf(x1.todouble() - x2.todouble()));
                    result.set("y", LuaValue.valueOf(y1.todouble() - y2.todouble()));
                    result.set("z", LuaValue.valueOf(z1.todouble() - z2.todouble()));
                    return result;
                } catch (Exception e) {
                    mod.logWarning("Error in position subtract: " + e.getMessage());
                    return new LuaTable();
                }
            }
        });
        
        posUtils.set("multiply", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Position.multiply() and Position:multiply() syntax
                int offset = args.narg() == 3 ? 1 : 0;
                if (args.narg() < 2 + offset) return new LuaTable();
                
                LuaValue pos = args.arg(1 + offset);
                LuaValue scalar = args.arg(2 + offset);
                
                try {
                    if (!pos.istable()) return new LuaTable();
                    
                    LuaValue xVal = pos.get("x");
                    LuaValue yVal = pos.get("y");
                    LuaValue zVal = pos.get("z");
                    
                    if (xVal.isnil() || yVal.isnil() || zVal.isnil()) return new LuaTable();
                    
                    double s = scalar.todouble();
                    LuaTable result = new LuaTable();
                    result.set("x", LuaValue.valueOf(xVal.todouble() * s));
                    result.set("y", LuaValue.valueOf(yVal.todouble() * s));
                    result.set("z", LuaValue.valueOf(zVal.todouble() * s));
                    return result;
                } catch (Exception e) {
                    mod.logWarning("Error in position multiply: " + e.getMessage());
                    return new LuaTable();
                }
            }
        });
        
        posUtils.set("normalize", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue pos) {
                try {
                    if (!pos.istable()) return new LuaTable();
                    
                    LuaValue xVal = pos.get("x");
                    LuaValue yVal = pos.get("y");
                    LuaValue zVal = pos.get("z");
                    
                    if (xVal.isnil() || yVal.isnil() || zVal.isnil()) return pos;
                    
                    double x = xVal.todouble();
                    double y = yVal.todouble();
                    double z = zVal.todouble();
                    double length = Math.sqrt(x * x + y * y + z * z);
                    
                    if (length == 0) return pos;
                    
                    LuaTable result = new LuaTable();
                    result.set("x", LuaValue.valueOf(x / length));
                    result.set("y", LuaValue.valueOf(y / length));
                    result.set("z", LuaValue.valueOf(z / length));
                    return result;
                } catch (Exception e) {
                    mod.logWarning("Error in position normalize: " + e.getMessage());
                    return pos;
                }
            }
        });
        
        posUtils.set("toBlockPos", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Position.toBlockPos() and Position:toBlockPos() syntax
                int offset = args.narg() == 2 ? 1 : 0;
                if (args.narg() < 1 + offset) return new LuaTable();
                
                LuaValue pos = args.arg(1 + offset);
                
                try {
                    if (!pos.istable()) return new LuaTable();
                    
                    LuaValue xVal = pos.get("x");
                    LuaValue yVal = pos.get("y");
                    LuaValue zVal = pos.get("z");
                    
                    if (xVal.isnil() || yVal.isnil() || zVal.isnil()) return new LuaTable();
                    
                    LuaTable result = new LuaTable();
                    result.set("x", LuaValue.valueOf((int) Math.floor(xVal.todouble())));
                    result.set("y", LuaValue.valueOf((int) Math.floor(yVal.todouble())));
                    result.set("z", LuaValue.valueOf((int) Math.floor(zVal.todouble())));
                    return result;
                } catch (Exception e) {
                    mod.logWarning("Error in position toBlockPos: " + e.getMessage());
                    return new LuaTable();
                }
            }
        });
        
        return posUtils;
    }
    
    /**
     * Creates item utility functions
     */
    private LuaTable createItemUtils() {
        LuaTable itemUtils = new LuaTable();
        
        itemUtils.set("exists", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Item.exists() and Item:exists() syntax
                int offset = args.narg() == 2 ? 1 : 0;
                if (args.narg() < 1 + offset) return LuaValue.FALSE;
                
                LuaValue itemName = args.arg(1 + offset);
                
                try {
                    String name = itemName.tojstring().toLowerCase();
                    if (!name.contains(":")) {
                        name = "minecraft:" + name;
                    }
                    Identifier id = new Identifier(name);
                    return LuaValue.valueOf(Registries.ITEM.containsId(id));
                } catch (Exception e) {
                    return LuaValue.FALSE;
                }
            }
        });
        
        itemUtils.set("getDisplayName", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Item.getDisplayName() and Item:getDisplayName() syntax
                int offset = args.narg() == 2 ? 1 : 0;
                if (args.narg() < 1 + offset) return LuaValue.valueOf("Error");
                
                LuaValue itemName = args.arg(1 + offset);
                
                try {
                    String name = itemName.tojstring().toLowerCase();
                    if (!name.contains(":")) {
                        name = "minecraft:" + name;
                    }
                    Identifier id = new Identifier(name);
                    if (Registries.ITEM.containsId(id)) {
                        Item item = Registries.ITEM.get(id);
                        return LuaValue.valueOf(item.getName().getString());
                    }
                    return LuaValue.valueOf("Unknown Item");
                } catch (Exception e) {
                    return LuaValue.valueOf("Error");
                }
            }
        });
        
        itemUtils.set("getId", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    String name = itemName.tojstring().toLowerCase();
                    if (!name.contains(":")) {
                        name = "minecraft:" + name;
                    }
                    return LuaValue.valueOf(name);
                } catch (Exception e) {
                    return LuaValue.valueOf("");
                }
            }
        });
        
        return itemUtils;
    }
    
    /**
     * Creates player utility functions
     */
    private LuaTable createPlayerUtils() {
        LuaTable playerUtils = new LuaTable();
        
        playerUtils.set("isHealthLow", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue threshold) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                return LuaValue.valueOf(mod.getPlayer().getHealth() < threshold.tofloat());
            }
        });
        
        playerUtils.set("isHungerLow", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue threshold) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                return LuaValue.valueOf(mod.getPlayer().getHungerManager().getFoodLevel() < threshold.toint());
            }
        });
        
        playerUtils.set("getDistanceToSpawn", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getPlayer() == null || mod.getWorld() == null) return LuaValue.valueOf(-1);
                
                BlockPos spawn = mod.getWorld().getSpawnPos();
                Vec3d playerPos = mod.getPlayer().getPos();
                
                double distance = Math.sqrt(
                    Math.pow(playerPos.x - spawn.getX(), 2) +
                    Math.pow(playerPos.z - spawn.getZ(), 2)
                );
                
                return LuaValue.valueOf(distance);
            }
        });
        
        // Jump functionality
        playerUtils.set("isJumping", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                // Check if player is currently jumping (not on ground and has positive Y velocity)
                return LuaValue.valueOf(!mod.getPlayer().isOnGround() && mod.getPlayer().getVelocity().y > 0);
            }
        });
        
        playerUtils.set("jump", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Use AltoClef's input controls to trigger a jump
                    mod.getInputControls().tryPress(baritone.api.utils.input.Input.JUMP);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in Player.jump: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Velocity functionality
        playerUtils.set("getVelocity", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.NIL;
                try {
                    Vec3d velocity = mod.getPlayer().getVelocity();
                    LuaTable result = new LuaTable();
                    result.set("x", LuaValue.valueOf(velocity.x));
                    result.set("y", LuaValue.valueOf(velocity.y));
                    result.set("z", LuaValue.valueOf(velocity.z));
                    return result;
                } catch (Exception e) {
                    mod.logWarning("Error in Player.getVelocity: " + e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });
        
        playerUtils.set("setVelocity", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Handle both Player.setVelocity() and Player:setVelocity() syntax
                    int offset = args.narg() == 4 ? 1 : 0;
                    if (args.narg() < 3 + offset) return LuaValue.FALSE;
                    
                    double x = args.arg(1 + offset).todouble();
                    double y = args.arg(2 + offset).todouble();
                    double z = args.arg(3 + offset).todouble();
                    
                    mod.getPlayer().setVelocity(x, y, z);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in Player.setVelocity: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== INPUT CONTROLS =====
        
        // Attack/punch (left click)
        playerUtils.set("attack", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    mod.getInputControls().tryPress(baritone.api.utils.input.Input.CLICK_LEFT);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Player.attack: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Use/interact (right click)
        playerUtils.set("use", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    mod.getInputControls().tryPress(baritone.api.utils.input.Input.CLICK_RIGHT);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Player.use: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Sneak control
        playerUtils.set("sneak", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    if (args.narg() >= 1) {
                        // Set specific sneak state
                        boolean state = args.arg(1).toboolean();
                        if (state) {
                            mod.getInputControls().hold(baritone.api.utils.input.Input.SNEAK);
                        } else {
                            mod.getInputControls().release(baritone.api.utils.input.Input.SNEAK);
                        }
                    } else {
                        // Just trigger a sneak press
                        mod.getInputControls().tryPress(baritone.api.utils.input.Input.SNEAK);
                    }
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Player.sneak: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Sprint control
        playerUtils.set("sprint", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    if (args.narg() >= 1) {
                        // Set specific sprint state
                        boolean state = args.arg(1).toboolean();
                        if (state) {
                            mod.getInputControls().hold(baritone.api.utils.input.Input.SPRINT);
                        } else {
                            mod.getInputControls().release(baritone.api.utils.input.Input.SPRINT);
                        }
                    } else {
                        // Just trigger a sprint press
                        mod.getInputControls().tryPress(baritone.api.utils.input.Input.SPRINT);
                    }
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Player.sprint: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Hotbar slot selection
        playerUtils.set("selectHotbarSlot", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue slot) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    int slotNum = slot.toint();
                    if (slotNum >= 1 && slotNum <= 9) {
                        mod.getPlayer().getInventory().selectedSlot = slotNum - 1; // Convert to 0-based
                        return LuaValue.TRUE;
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Player.selectHotbarSlot: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Get current hotbar slot
        playerUtils.set("getSelectedHotbarSlot", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getPlayer().getInventory().selectedSlot + 1); // Convert to 1-based
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Player.getSelectedHotbarSlot: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        // ===== PLAYER STATUS & EFFECTS =====
        
        // Experience system
        playerUtils.set("getXP", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getPlayer().totalExperience);
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Player.getXP: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        playerUtils.set("getLevel", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getPlayer().experienceLevel);
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Player.getLevel: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        // Gamemode detection
        playerUtils.set("getGameMode", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf("UNKNOWN");
                try {
                    if (mod.getPlayer().isCreative()) {
                        return LuaValue.valueOf("CREATIVE");
                    } else if (mod.getPlayer().isSpectator()) {
                        return LuaValue.valueOf("SPECTATOR");
                    } else {
                        return LuaValue.valueOf("SURVIVAL");
                    }
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Player.getGameMode: " + e.getMessage());
                    return LuaValue.valueOf("UNKNOWN");
                }
            }
        });
        
        // Player abilities
        playerUtils.set("canFly", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getPlayer().getAbilities().allowFlying);
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Player.canFly: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        return playerUtils;
    }
    
    /**
     * Creates world utility functions
     */
    private LuaTable createWorldUtils() {
        LuaTable worldUtils = new LuaTable();
        
        // ===== WORLD INFORMATION =====
        
        // Time and weather
        worldUtils.set("getWorldTime", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getWorld() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getWorld().getTime());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.getWorldTime: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        worldUtils.set("getTimeOfDay", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getWorld() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getWorld().getTimeOfDay());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.getTimeOfDay: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        worldUtils.set("isDay", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getWorld().isDay());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.isDay: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        worldUtils.set("isNight", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getWorld().isNight());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.isNight: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        worldUtils.set("isRaining", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getWorld().isRaining());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.isRaining: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        worldUtils.set("isThundering", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getWorld().isThundering());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.isThundering: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        worldUtils.set("canSleep", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(WorldHelper.canSleep());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.canSleep: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Dimension information
        worldUtils.set("getCurrentDimension", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(WorldHelper.getCurrentDimension().toString());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.getCurrentDimension: " + e.getMessage());
                    return LuaValue.valueOf("UNKNOWN");
                }
            }
        });
        
        worldUtils.set("getDimensionName", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(WorldHelper.getCurrentDimension().toString());
                } catch (Exception e) {
                    return LuaValue.valueOf("UNKNOWN");
                }
            }
        });
        
        worldUtils.set("isInOverworld", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(WorldHelper.getCurrentDimension() == adris.altoclef.util.Dimension.OVERWORLD);
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.isInOverworld: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        worldUtils.set("isInNether", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(WorldHelper.getCurrentDimension() == adris.altoclef.util.Dimension.NETHER);
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.isInNether: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        worldUtils.set("isInEnd", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(WorldHelper.getCurrentDimension() == adris.altoclef.util.Dimension.END);
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.isInEnd: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Biome information
        worldUtils.set("getCurrentBiome", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getPlayer() == null || mod.getWorld() == null) return LuaValue.valueOf("UNKNOWN");
                try {
                    BlockPos playerPos = mod.getPlayer().getBlockPos();
                    var biome = mod.getWorld().getBiome(playerPos);
                    // Get the biome's registry key if available
                    var biomeKey = biome.getKey();
                    if (biomeKey.isPresent()) {
                        return LuaValue.valueOf(biomeKey.get().getValue().toString());
                    }
                    return LuaValue.valueOf("UNKNOWN");
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.getCurrentBiome: " + e.getMessage());
                    return LuaValue.valueOf("UNKNOWN");
                }
            }
        });
        
        worldUtils.set("getBiomeAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.valueOf("UNKNOWN");
                try {
                    if (args.narg() < 3) return LuaValue.valueOf("UNKNOWN");
                    
                    int x = args.arg(1).toint();
                    int y = args.arg(2).toint();
                    int z = args.arg(3).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    var biome = mod.getWorld().getBiome(pos);
                    var biomeKey = biome.getKey();
                    if (biomeKey.isPresent()) {
                        return LuaValue.valueOf(biomeKey.get().getValue().toString());
                    }
                    return LuaValue.valueOf("UNKNOWN");
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.getBiomeAt: " + e.getMessage());
                    return LuaValue.valueOf("UNKNOWN");
                }
            }
        });
        
        worldUtils.set("isInOcean", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getPlayer() == null || mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    BlockPos playerPos = mod.getPlayer().getBlockPos();
                    var biome = mod.getWorld().getBiome(playerPos);
                    return LuaValue.valueOf(WorldHelper.isOcean(biome));
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.isInOcean: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== BLOCK INFORMATION =====
        
        // Block state queries
        worldUtils.set("getBlockAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.valueOf("air");
                try {
                    if (args.narg() < 3) return LuaValue.valueOf("air");
                    
                    int x = args.arg(1).toint();
                    int y = args.arg(2).toint();
                    int z = args.arg(3).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    Block block = mod.getWorld().getBlockState(pos).getBlock();
                    return LuaValue.valueOf(Registries.BLOCK.getId(block).toString());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.getBlockAt: " + e.getMessage());
                    return LuaValue.valueOf("air");
                }
            }
        });
        
        worldUtils.set("isBlockAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    if (args.narg() < 4) return LuaValue.FALSE;
                    
                    int x = args.arg(1).toint();
                    int y = args.arg(2).toint();
                    int z = args.arg(3).toint();
                    String blockName = args.arg(4).tojstring();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    Block currentBlock = mod.getWorld().getBlockState(pos).getBlock();
                    
                    // Parse block name
                    String trimmedName = blockName.toLowerCase();
                    if (!trimmedName.contains(":")) {
                        trimmedName = "minecraft:" + trimmedName;
                    }
                    
                    Identifier blockId = new Identifier(trimmedName);
                    if (Registries.BLOCK.containsId(blockId)) {
                        Block targetBlock = Registries.BLOCK.get(blockId);
                        return LuaValue.valueOf(currentBlock == targetBlock);
                    }
                    
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.isBlockAt: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        worldUtils.set("isAirAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.TRUE;
                try {
                    if (args.narg() < 3) return LuaValue.TRUE;
                    
                    int x = args.arg(1).toint();
                    int y = args.arg(2).toint();
                    int z = args.arg(3).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    return LuaValue.valueOf(WorldHelper.isAir(mod, pos));
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.isAirAt: " + e.getMessage());
                    return LuaValue.TRUE;
                }
            }
        });
        
        worldUtils.set("isSolidAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    if (args.narg() < 3) return LuaValue.FALSE;
                    
                    int x = args.arg(1).toint();
                    int y = args.arg(2).toint();
                    int z = args.arg(3).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    return LuaValue.valueOf(WorldHelper.isSolid(mod, pos));
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.isSolidAt: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Light levels
        worldUtils.set("getLightLevelAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.valueOf(0);
                try {
                    if (args.narg() < 3) return LuaValue.valueOf(0);
                    
                    int x = args.arg(1).toint();
                    int y = args.arg(2).toint();
                    int z = args.arg(3).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    int lightLevel = mod.getWorld().getLightLevel(pos);
                    return LuaValue.valueOf(lightLevel);
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.getLightLevelAt: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        worldUtils.set("getBlockLightAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.valueOf(0);
                try {
                    if (args.narg() < 3) return LuaValue.valueOf(0);
                    
                    int x = args.arg(1).toint();
                    int y = args.arg(2).toint();
                    int z = args.arg(3).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    int blockLight = mod.getWorld().getLightLevel(net.minecraft.world.LightType.BLOCK, pos);
                    return LuaValue.valueOf(blockLight);
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.getBlockLightAt: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        worldUtils.set("getSkyLightAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.valueOf(0);
                try {
                    if (args.narg() < 3) return LuaValue.valueOf(0);
                    
                    int x = args.arg(1).toint();
                    int y = args.arg(2).toint();
                    int z = args.arg(3).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    int skyLight = mod.getWorld().getLightLevel(net.minecraft.world.LightType.SKY, pos);
                    return LuaValue.valueOf(skyLight);
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.getSkyLightAt: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        // Block properties
        worldUtils.set("getBlockHardnessAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.valueOf(-1);
                try {
                    if (args.narg() < 3) return LuaValue.valueOf(-1);
                    
                    int x = args.arg(1).toint();
                    int y = args.arg(2).toint();
                    int z = args.arg(3).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = mod.getWorld().getBlockState(pos);
                    float hardness = state.getHardness(mod.getWorld(), pos);
                    return LuaValue.valueOf(hardness);
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.getBlockHardnessAt: " + e.getMessage());
                    return LuaValue.valueOf(-1);
                }
            }
        });
        
        // Special block detection
        worldUtils.set("isChestAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    if (args.narg() < 3) return LuaValue.FALSE;
                    
                    int x = args.arg(1).toint();
                    int y = args.arg(2).toint();
                    int z = args.arg(3).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    return LuaValue.valueOf(WorldHelper.isChest(mod, pos));
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.isChestAt: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        worldUtils.set("isInteractableAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    if (args.narg() < 3) return LuaValue.FALSE;
                    
                    int x = args.arg(1).toint();
                    int y = args.arg(2).toint();
                    int z = args.arg(3).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    return LuaValue.valueOf(WorldHelper.isInteractableBlock(mod, pos));
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.isInteractableAt: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== WORLD INTERACTION =====
        
        // Position validation
        worldUtils.set("canReach", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    if (args.narg() < 3) return LuaValue.FALSE;
                    
                    int x = args.arg(1).toint();
                    int y = args.arg(2).toint();
                    int z = args.arg(3).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    return LuaValue.valueOf(WorldHelper.canReach(mod, pos));
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.canReach: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        worldUtils.set("canBreak", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    if (args.narg() < 3) return LuaValue.FALSE;
                    
                    int x = args.arg(1).toint();
                    int y = args.arg(2).toint();  
                    int z = args.arg(3).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    return LuaValue.valueOf(WorldHelper.canBreak(mod, pos));
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.canBreak: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        worldUtils.set("canPlace", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    if (args.narg() < 3) return LuaValue.FALSE;
                    
                    int x = args.arg(1).toint();
                    int y = args.arg(2).toint();
                    int z = args.arg(3).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    return LuaValue.valueOf(WorldHelper.canPlace(mod, pos));
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.canPlace: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Ground height calculation
        worldUtils.set("getGroundHeight", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    if (args.narg() < 2) return LuaValue.valueOf(-1);
                    
                    int x = args.arg(1).toint();
                    int z = args.arg(2).toint();
                    
                    return LuaValue.valueOf(WorldHelper.getGroundHeight(mod, x, z));
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.World.getGroundHeight: " + e.getMessage());
                    return LuaValue.valueOf(-1);
                }
            }
        });
        
        return worldUtils;
    }
    
    /**
     * Creates control utility functions
     */
    private LuaTable createControlUtils() {
        LuaTable controlUtils = new LuaTable();
        
        // Drop item (Q key functionality)
        controlUtils.set("dropItem", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Execute on main thread to avoid issues
                    net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
                        try {
                            net.minecraft.client.MinecraftClient.getInstance().options.dropKey.setPressed(true);
                            net.minecraft.client.option.KeyBinding.onKeyPressed(net.minecraft.client.MinecraftClient.getInstance().options.dropKey.getDefaultKey());
                            net.minecraft.client.MinecraftClient.getInstance().options.dropKey.setPressed(false);
                        } catch (Exception e) {
                            mod.logWarning("Error executing drop on main thread: " + e.getMessage());
                        }
                    });
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Control.dropItem: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Open inventory (E key functionality)
        controlUtils.set("openInventory", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Execute on main thread to avoid issues
                    net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
                        try {
                            net.minecraft.client.MinecraftClient.getInstance().options.inventoryKey.setPressed(true);
                            net.minecraft.client.option.KeyBinding.onKeyPressed(net.minecraft.client.MinecraftClient.getInstance().options.inventoryKey.getDefaultKey());
                            net.minecraft.client.MinecraftClient.getInstance().options.inventoryKey.setPressed(false);
                        } catch (Exception e) {
                            mod.logWarning("Error executing inventory open on main thread: " + e.getMessage());
                        }
                    });
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Control.openInventory: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Open chat (T key functionality)
        controlUtils.set("openChat", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Execute on main thread to avoid issues
                    net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
                        try {
                            net.minecraft.client.MinecraftClient.getInstance().options.chatKey.setPressed(true);
                            net.minecraft.client.option.KeyBinding.onKeyPressed(net.minecraft.client.MinecraftClient.getInstance().options.chatKey.getDefaultKey());
                            net.minecraft.client.MinecraftClient.getInstance().options.chatKey.setPressed(false);
                        } catch (Exception e) {
                            mod.logWarning("Error executing chat open on main thread: " + e.getMessage());
                        }
                    });
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Control.openChat: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Advanced look control
        controlUtils.set("lookAt", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue yaw, LuaValue pitch) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    float yawValue = (float) yaw.todouble();
                    float pitchValue = (float) pitch.todouble();
                    
                    mod.getInputControls().forceLook(yawValue, pitchValue);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Control.lookAt: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Force camera/look direction instantly
        controlUtils.set("setLook", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue yaw, LuaValue pitch) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    float yawValue = (float) yaw.todouble();
                    float pitchValue = (float) pitch.todouble();
                    
                    mod.getInputControls().forceLook(yawValue, pitchValue);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Control.setLook: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Check if breaking a block
        controlUtils.set("isBreakingBlock", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(mod.getControllerExtras().isBreakingBlock());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Control.isBreakingBlock: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Get block breaking position
        controlUtils.set("getBreakingBlockPos", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    net.minecraft.util.math.BlockPos pos = mod.getControllerExtras().getBreakingBlockPos();
                    if (pos != null) {
                        org.luaj.vm2.LuaTable result = new org.luaj.vm2.LuaTable();
                        result.set("x", LuaValue.valueOf(pos.getX()));
                        result.set("y", LuaValue.valueOf(pos.getY()));
                        result.set("z", LuaValue.valueOf(pos.getZ()));
                        return result;
                    }
                    return LuaValue.NIL;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Control.getBreakingBlockPos: " + e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });
        
        // Get block breaking progress (0.0 to 1.0)
        controlUtils.set("getBreakingProgress", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(mod.getControllerExtras().getBreakingBlockProgress());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Control.getBreakingProgress: " + e.getMessage());
                    return LuaValue.valueOf(0.0);
                }
            }
        });
        
        // Hold a key down
        controlUtils.set("holdKey", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue key) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    String keyName = key.tojstring().toLowerCase();
                    baritone.api.utils.input.Input input = switch (keyName) {
                        case "sneak" -> baritone.api.utils.input.Input.SNEAK;
                        case "sprint" -> baritone.api.utils.input.Input.SPRINT;
                        case "jump" -> baritone.api.utils.input.Input.JUMP;
                        case "attack", "left_click" -> baritone.api.utils.input.Input.CLICK_LEFT;
                        case "use", "right_click" -> baritone.api.utils.input.Input.CLICK_RIGHT;
                        case "forward", "w" -> baritone.api.utils.input.Input.MOVE_FORWARD;
                        case "back", "s" -> baritone.api.utils.input.Input.MOVE_BACK;
                        case "left", "a" -> baritone.api.utils.input.Input.MOVE_LEFT;
                        case "right", "d" -> baritone.api.utils.input.Input.MOVE_RIGHT;
                        default -> null;
                    };
                    
                    if (input != null) {
                        mod.getInputControls().hold(input);
                        return LuaValue.TRUE;
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Control.holdKey: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Release a held key
        controlUtils.set("releaseKey", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue key) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    String keyName = key.tojstring().toLowerCase();
                    baritone.api.utils.input.Input input = switch (keyName) {
                        case "sneak" -> baritone.api.utils.input.Input.SNEAK;
                        case "sprint" -> baritone.api.utils.input.Input.SPRINT;
                        case "jump" -> baritone.api.utils.input.Input.JUMP;
                        case "attack", "left_click" -> baritone.api.utils.input.Input.CLICK_LEFT;
                        case "use", "right_click" -> baritone.api.utils.input.Input.CLICK_RIGHT;
                        case "forward", "w" -> baritone.api.utils.input.Input.MOVE_FORWARD;
                        case "back", "s" -> baritone.api.utils.input.Input.MOVE_BACK;
                        case "left", "a" -> baritone.api.utils.input.Input.MOVE_LEFT;
                        case "right", "d" -> baritone.api.utils.input.Input.MOVE_RIGHT;
                        default -> null;
                    };
                    
                    if (input != null) {
                        mod.getInputControls().release(input);
                        return LuaValue.TRUE;
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Control.releaseKey: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Press a key once (single press)
        controlUtils.set("pressKey", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue key) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    String keyName = key.tojstring().toLowerCase();
                    baritone.api.utils.input.Input input = switch (keyName) {
                        case "sneak" -> baritone.api.utils.input.Input.SNEAK;
                        case "sprint" -> baritone.api.utils.input.Input.SPRINT;
                        case "jump" -> baritone.api.utils.input.Input.JUMP;
                        case "attack", "left_click" -> baritone.api.utils.input.Input.CLICK_LEFT;
                        case "use", "right_click" -> baritone.api.utils.input.Input.CLICK_RIGHT;
                        case "forward", "w" -> baritone.api.utils.input.Input.MOVE_FORWARD;
                        case "back", "s" -> baritone.api.utils.input.Input.MOVE_BACK;
                        case "left", "a" -> baritone.api.utils.input.Input.MOVE_LEFT;
                        case "right", "d" -> baritone.api.utils.input.Input.MOVE_RIGHT;
                        default -> null;
                    };
                    
                    if (input != null) {
                        mod.getInputControls().tryPress(input);
                        return LuaValue.TRUE;
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Control.pressKey: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Check if key is held down
        controlUtils.set("isHoldingKey", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue key) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    String keyName = key.tojstring().toLowerCase();
                    baritone.api.utils.input.Input input = switch (keyName) {
                        case "sneak" -> baritone.api.utils.input.Input.SNEAK;
                        case "sprint" -> baritone.api.utils.input.Input.SPRINT;
                        case "jump" -> baritone.api.utils.input.Input.JUMP;
                        case "attack", "left_click" -> baritone.api.utils.input.Input.CLICK_LEFT;
                        case "use", "right_click" -> baritone.api.utils.input.Input.CLICK_RIGHT;
                        case "forward", "w" -> baritone.api.utils.input.Input.MOVE_FORWARD;
                        case "back", "s" -> baritone.api.utils.input.Input.MOVE_BACK;
                        case "left", "a" -> baritone.api.utils.input.Input.MOVE_LEFT;
                        case "right", "d" -> baritone.api.utils.input.Input.MOVE_RIGHT;
                        default -> null;
                    };
                    
                    if (input != null) {
                        return LuaValue.valueOf(mod.getInputControls().isHeldDown(input));
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Control.isHoldingKey: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        return controlUtils;
    }
    
    /**
     * Creates chat and command utility functions
     */
    private LuaTable createChatUtils() {
        LuaTable chatUtils = new LuaTable();
        
        // Send whisper message to specific player
        chatUtils.set("whisper", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue username, LuaValue message) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    String user = username.tojstring();
                    String msg = message.tojstring();
                    mod.getMessageSender().enqueueWhisper(user, msg, adris.altoclef.ui.MessagePriority.OPTIONAL);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Chat.whisper: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Send priority message
        chatUtils.set("chatPriority", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue message, LuaValue priority) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    String msg = message.tojstring();
                    String priorityStr = priority.tojstring().toUpperCase();
                    
                    adris.altoclef.ui.MessagePriority msgPriority = switch (priorityStr) {
                        case "LOW" -> adris.altoclef.ui.MessagePriority.OPTIONAL;
                        case "NORMAL", "INFO" -> adris.altoclef.ui.MessagePriority.OPTIONAL;
                        case "IMPORTANT" -> adris.altoclef.ui.MessagePriority.TIMELY;
                        case "TIMELY" -> adris.altoclef.ui.MessagePriority.TIMELY;
                        case "WARNING" -> adris.altoclef.ui.MessagePriority.ASAP;
                        case "UNAUTHORIZED" -> adris.altoclef.ui.MessagePriority.UNAUTHORIZED;
                        default -> adris.altoclef.ui.MessagePriority.OPTIONAL;
                    };
                    
                    mod.getMessageSender().enqueueChat(msg, msgPriority);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Chat.chatPriority: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Send whisper with priority
        chatUtils.set("whisperPriority", new ThreeArgFunction() {
            @Override
            public LuaValue call(LuaValue username, LuaValue message, LuaValue priority) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    String user = username.tojstring();
                    String msg = message.tojstring();
                    String priorityStr = priority.tojstring().toUpperCase();
                    
                    adris.altoclef.ui.MessagePriority msgPriority = switch (priorityStr) {
                        case "LOW" -> adris.altoclef.ui.MessagePriority.OPTIONAL;
                        case "NORMAL", "INFO" -> adris.altoclef.ui.MessagePriority.OPTIONAL;
                        case "IMPORTANT" -> adris.altoclef.ui.MessagePriority.TIMELY;
                        case "TIMELY" -> adris.altoclef.ui.MessagePriority.TIMELY;
                        case "WARNING" -> adris.altoclef.ui.MessagePriority.ASAP;
                        case "UNAUTHORIZED" -> adris.altoclef.ui.MessagePriority.UNAUTHORIZED;
                        default -> adris.altoclef.ui.MessagePriority.OPTIONAL;
                    };
                    
                    mod.getMessageSender().enqueueWhisper(user, msg, msgPriority);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Chat.whisperPriority: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Check if user is authorized for butler commands
        chatUtils.set("isUserAuthorized", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue username) {
                try {
                    String user = username.tojstring();
                    return LuaValue.valueOf(mod.getButler().isUserAuthorized(user));
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Chat.isUserAuthorized: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Get current butler user (if any)
        chatUtils.set("getCurrentUser", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    String currentUser = mod.getButler().getCurrentUser();
                    return currentUser != null ? LuaValue.valueOf(currentUser) : LuaValue.NIL;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Chat.getCurrentUser: " + e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });
        
        // Check if butler has a current user
        chatUtils.set("hasCurrentUser", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(mod.getButler().hasCurrentUser());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Chat.hasCurrentUser: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        return chatUtils;
    }
    
    /**
     * Creates inventory utility functions
     */
    private LuaTable createInventoryUtils() {
        LuaTable inventoryUtils = new LuaTable();
        
        // ===== ITEM COUNTING & DETECTION =====
        
        inventoryUtils.set("getItemCount", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(mod.getItemStorage().getItemCount(item));
                    }
                    return LuaValue.valueOf(0);
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.getItemCount: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        inventoryUtils.set("getItemCountInventory", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(mod.getItemStorage().getItemCountInventoryOnly(item));
                    }
                    return LuaValue.valueOf(0);
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.getItemCountInventory: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        inventoryUtils.set("hasItem", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(mod.getItemStorage().hasItem(item));
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.hasItem: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        inventoryUtils.set("hasItemInventory", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(mod.getItemStorage().hasItemInventoryOnly(item));
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.hasItemInventory: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== EQUIPMENT MANAGEMENT =====
        
        inventoryUtils.set("isEquipped", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.isEquipped(mod, item));
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.isEquipped: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        inventoryUtils.set("equipItem", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(mod.getSlotHandler().forceEquipItem(item));
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.equipItem: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        inventoryUtils.set("equipItemToOffhand", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        mod.getSlotHandler().forceEquipItemToOffhand(item);
                        return LuaValue.TRUE;
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.equipItemToOffhand: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        inventoryUtils.set("isArmorEquipped", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.isArmorEquipped(mod, item));
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.isArmorEquipped: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== INVENTORY STATE =====
        
        inventoryUtils.set("getTotalSlots", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getPlayer().getInventory().main.size());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.getTotalSlots: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        inventoryUtils.set("hasEmptySlot", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(mod.getItemStorage().hasEmptyInventorySlot());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.hasEmptySlot: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        inventoryUtils.set("getFoodCount", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.calculateInventoryFoodScore(mod));
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.getFoodCount: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        inventoryUtils.set("getBuildingMaterialCount", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.getBuildingMaterialCount(mod));
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.getBuildingMaterialCount: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        // ===== SCREEN & CONTAINER DETECTION =====
        
        inventoryUtils.set("isInventoryOpen", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.isPlayerInventoryOpen());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.isInventoryOpen: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        inventoryUtils.set("isCraftingTableOpen", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.isBigCraftingOpen());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.isCraftingTableOpen: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        inventoryUtils.set("isFurnaceOpen", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.isFurnaceOpen());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.isFurnaceOpen: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        inventoryUtils.set("isSmokerOpen", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.isSmokerOpen());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.isSmokerOpen: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        inventoryUtils.set("isBlastFurnaceOpen", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.isBlastFurnaceOpen());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.isBlastFurnaceOpen: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        inventoryUtils.set("closeScreen", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    // Execute on main thread to avoid render thread issues
                    net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
                        try {
                            adris.altoclef.util.helpers.StorageHelper.closeScreen();
                        } catch (Exception e) {
                            mod.logWarning("Error closing screen on main thread: " + e.getMessage());
                        }
                    });
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.closeScreen: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== FURNACE OPERATIONS =====
        
        inventoryUtils.set("getFurnaceFuel", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.getFurnaceFuel());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.getFurnaceFuel: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        inventoryUtils.set("getFurnaceCookProgress", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.getFurnaceCookPercent());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.getFurnaceCookProgress: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        inventoryUtils.set("getSmokerFuel", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.getSmokerFuel());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.getSmokerFuel: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        inventoryUtils.set("getSmokerCookProgress", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.getSmokerCookPercent());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.getSmokerCookProgress: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        inventoryUtils.set("getBlastFurnaceFuel", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.getBlastFurnaceFuel());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.getBlastFurnaceFuel: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        inventoryUtils.set("getBlastFurnaceCookProgress", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.getBlastFurnaceCookPercent());
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.getBlastFurnaceCookProgress: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        // ===== CONTAINER INFORMATION =====
        
        inventoryUtils.set("getContainerItemCount", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(mod.getItemStorage().getItemCountContainer(item));
                    }
                    return LuaValue.valueOf(0);
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.getContainerItemCount: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        inventoryUtils.set("hasItemInContainer", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(mod.getItemStorage().hasItemContainer(item));
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.hasItemInContainer: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== UTILITY FUNCTIONS =====
        
        inventoryUtils.set("refreshInventory", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                try {
                    mod.getSlotHandler().refreshInventory();
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Inventory.refreshInventory: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== LEGACY FUNCTIONS (for backward compatibility) =====
        
        inventoryUtils.set("getTotalItemCount", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getItemStorage() == null) return LuaValue.valueOf(0);
                
                int total = 0;
                for (Item item : Registries.ITEM) {
                    total += mod.getItemStorage().getItemCount(item);
                }
                return LuaValue.valueOf(total);
            }
        });
        
        inventoryUtils.set("getUniqueItemCount", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getItemStorage() == null) return LuaValue.valueOf(0);
                
                int unique = 0;
                for (Item item : Registries.ITEM) {
                    if (mod.getItemStorage().getItemCount(item) > 0) {
                        unique++;
                    }
                }
                return LuaValue.valueOf(unique);
            }
        });
        
        return inventoryUtils;
    }
    
    /**
     * Creates entity utility functions
     */
    private LuaTable createEntityUtils() {
        LuaTable entityUtils = new LuaTable();
        
        // Get nearby entities with range
        entityUtils.set("getNearbyEntities", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both Entity.getNearbyEntities() and Entity:getNearbyEntities() syntax
                    int offset = args.narg() >= 2 && args.arg(1).istable() ? 1 : 0;
                    double range = args.narg() > offset ? args.arg(1 + offset).todouble() : 20.0;
                    
                    if (mod.getPlayer() == null) return new LuaTable();
                    
                    LuaTable entities = new LuaTable();
                    int index = 1;
                    
                    for (net.minecraft.entity.Entity entity : mod.getEntityTracker().getCloseEntities()) {
                        double distance = entity.distanceTo(mod.getPlayer());
                        if (distance <= range) {
                            LuaTable entityInfo = createSimpleEntityTable(entity, distance);
                            entities.set(index++, entityInfo);
                        }
                    }
                    
                    return entities;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Entity.getNearbyEntities: " + e.getMessage());
                    return new LuaTable();
                }
            }
        });
        
        // Get closest entity
        entityUtils.set("getClosestEntity", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    if (mod.getPlayer() == null) return LuaValue.NIL;
                    
                    java.util.List<net.minecraft.entity.Entity> closeEntities = mod.getEntityTracker().getCloseEntities();
                    if (closeEntities.isEmpty()) return LuaValue.NIL;
                    
                    net.minecraft.entity.Entity closest = null;
                    double minDistance = Double.MAX_VALUE;
                    
                    for (net.minecraft.entity.Entity entity : closeEntities) {
                        double distance = entity.distanceTo(mod.getPlayer());
                        if (distance < minDistance) {
                            minDistance = distance;
                            closest = entity;
                        }
                    }
                    
                    if (closest != null) {
                        return createSimpleEntityTable(closest, minDistance);
                    }
                    
                    return LuaValue.NIL;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Entity.getClosestEntity: " + e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });
        
        // Get entities of specific type
        entityUtils.set("getEntitiesOfType", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both Entity.getEntitiesOfType() and Entity:getEntitiesOfType() syntax
                    int offset = args.narg() >= 2 && args.arg(1).istable() ? 1 : 0;
                    if (args.narg() < 1 + offset) return new LuaTable();
                    
                    String entityType = args.arg(1 + offset).tojstring().toLowerCase();
                    
                    if (mod.getPlayer() == null) return new LuaTable();
                    
                    LuaTable entities = new LuaTable();
                    int index = 1;
                    
                    // Use simple entity filtering based on type string
                    for (net.minecraft.entity.Entity entity : mod.getEntityTracker().getCloseEntities()) {
                        String entityTypeName = entity.getType().toString().toLowerCase();
                        if (entityTypeName.contains(entityType) || entityType.equals("all") || entityType.equals("entity")) {
                            double distance = entity.distanceTo(mod.getPlayer());
                            LuaTable entityInfo = createSimpleEntityTable(entity, distance);
                            entities.set(index++, entityInfo);
                        }
                    }
                    
                    return entities;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Entity.getEntitiesOfType: " + e.getMessage());
                    return new LuaTable();
                }
            }
        });
        
        // Get nearby players
        entityUtils.set("getPlayersNearby", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both Entity.getPlayersNearby() and Entity:getPlayersNearby() syntax
                    int offset = args.narg() >= 2 && args.arg(1).istable() ? 1 : 0;
                    double range = args.narg() > offset ? args.arg(1 + offset).todouble() : 50.0;
                    
                    if (mod.getPlayer() == null) return new LuaTable();
                    
                    LuaTable players = new LuaTable();
                    int index = 1;
                    
                    java.util.List<net.minecraft.entity.player.PlayerEntity> playerEntities = mod.getEntityTracker().getTrackedEntities(net.minecraft.entity.player.PlayerEntity.class);
                    for (net.minecraft.entity.player.PlayerEntity player : playerEntities) {
                        double distance = player.distanceTo(mod.getPlayer());
                        if (distance <= range) {
                            LuaTable playerInfo = createSimpleEntityTable(player, distance);
                            // Add player-specific info
                            playerInfo.set("username", LuaValue.valueOf(player.getName().getString()));
                            playerInfo.set("uuid", LuaValue.valueOf(player.getUuid().toString()));
                            players.set(index++, playerInfo);
                        }
                    }
                    
                    return players;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Entity.getPlayersNearby: " + e.getMessage());
                    return new LuaTable();
                }
            }
        });
        
        // Get hostile entities
        entityUtils.set("getHostileEntities", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    if (mod.getPlayer() == null) return new LuaTable();
                    
                    LuaTable hostiles = new LuaTable();
                    int index = 1;
                    
                    java.util.List<net.minecraft.entity.Entity> hostileEntities = mod.getEntityTracker().getHostiles();
                    for (net.minecraft.entity.Entity entity : hostileEntities) {
                        double distance = entity.distanceTo(mod.getPlayer());
                        LuaTable entityInfo = createSimpleEntityTable(entity, distance);
                        entityInfo.set("isHostile", LuaValue.TRUE);
                        hostiles.set(index++, entityInfo);
                    }
                    
                    return hostiles;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Entity.getHostileEntities: " + e.getMessage());
                    return new LuaTable();
                }
            }
        });
        
        // Get dropped items
        entityUtils.set("getDroppedItems", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both Entity.getDroppedItems() and Entity:getDroppedItems() syntax
                    int offset = args.narg() >= 2 && args.arg(1).istable() ? 1 : 0;
                    double range = args.narg() > offset ? args.arg(1 + offset).todouble() : 20.0;
                    
                    if (mod.getPlayer() == null) return new LuaTable();
                    
                    LuaTable items = new LuaTable();
                    int index = 1;
                    
                    java.util.List<net.minecraft.entity.ItemEntity> droppedItems = mod.getEntityTracker().getDroppedItems();
                    for (net.minecraft.entity.ItemEntity itemEntity : droppedItems) {
                        double distance = itemEntity.distanceTo(mod.getPlayer());
                        if (distance <= range) {
                            LuaTable itemInfo = createSimpleEntityTable(itemEntity, distance);
                            itemInfo.set("itemName", LuaValue.valueOf(itemEntity.getStack().getItem().toString()));
                            itemInfo.set("count", LuaValue.valueOf(itemEntity.getStack().getCount()));
                            items.set(index++, itemInfo);
                        }
                    }
                    
                    return items;
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Entity.getDroppedItems: " + e.getMessage());
                    return new LuaTable();
                }
            }
        });
        
        // Count entities of type
        entityUtils.set("countEntitiesOfType", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both Entity.countEntitiesOfType() and Entity:countEntitiesOfType() syntax
                    int offset = args.narg() >= 2 && args.arg(1).istable() ? 1 : 0;
                    if (args.narg() < 1 + offset) return LuaValue.valueOf(0);
                    
                    String entityType = args.arg(1 + offset).tojstring().toLowerCase();
                    
                    int count = 0;
                    for (net.minecraft.entity.Entity entity : mod.getEntityTracker().getCloseEntities()) {
                        String entityTypeName = entity.getType().toString().toLowerCase();
                        if (entityTypeName.contains(entityType) || entityType.equals("all") || entityType.equals("entity")) {
                            count++;
                        }
                    }
                    
                    return LuaValue.valueOf(count);
                } catch (Exception e) {
                    mod.logWarning("Error in Utils.Entity.countEntitiesOfType: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        return entityUtils;
    }
    
    /**
     * Creates a simple entity table for Utils API (simplified version)
     */
    private LuaTable createSimpleEntityTable(net.minecraft.entity.Entity entity, double distance) {
        LuaTable entityTable = new LuaTable();
        
        try {
            // Basic entity information
            entityTable.set("name", LuaValue.valueOf(entity.getName().getString()));
            entityTable.set("type", LuaValue.valueOf(entity.getType().toString()));
            entityTable.set("distance", LuaValue.valueOf(distance));
            entityTable.set("id", LuaValue.valueOf(entity.getId()));
            
            // Position
            net.minecraft.util.math.Vec3d pos = entity.getPos();
            LuaTable position = new LuaTable();
            position.set("x", LuaValue.valueOf(pos.x));
            position.set("y", LuaValue.valueOf(pos.y));
            position.set("z", LuaValue.valueOf(pos.z));
            entityTable.set("position", position);
            
            // Basic state
            entityTable.set("isAlive", LuaValue.valueOf(entity.isAlive()));
            
            // Type flags
            entityTable.set("isPlayer", LuaValue.valueOf(entity instanceof net.minecraft.entity.player.PlayerEntity));
            entityTable.set("isLiving", LuaValue.valueOf(entity instanceof net.minecraft.entity.LivingEntity));
            entityTable.set("isItem", LuaValue.valueOf(entity instanceof net.minecraft.entity.ItemEntity));
            
        } catch (Exception e) {
            mod.logWarning("Error creating simple entity table: " + e.getMessage());
        }
        
        return entityTable;
    }
    
    /**
     * Helper method to parse item names - copied from main API for consistency
     */
    private Item parseItemName(String itemName) {
        try {
            String trimmedName = itemName.toLowerCase().trim();
            if (!trimmedName.contains(":")) {
                trimmedName = "minecraft:" + trimmedName;
            }
            
            Identifier itemId = new Identifier(trimmedName);
            if (Registries.ITEM.containsId(itemId)) {
                return Registries.ITEM.get(itemId);
            }
            
            mod.logWarning("Unknown item: " + itemName);
            return null;
        } catch (Exception e) {
            mod.logWarning("Error parsing item name '" + itemName + "': " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Creates debug utility functions
     */
    private LuaTable createDebugUtils() {
        LuaTable debugUtils = new LuaTable();
        
        debugUtils.set("logTable", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Debug.logTable() and Debug:logTable() syntax
                int offset = args.narg() == 2 ? 1 : 0;
                if (args.narg() < 1 + offset) return LuaValue.NIL;
                
                LuaValue table = args.arg(1 + offset);
                
                if (!table.istable()) {
                    mod.log("[Debug] Not a table: " + table.tojstring());
                    return LuaValue.NIL;
                }
                
                mod.log("[Debug] Table contents:");
                LuaValue k = LuaValue.NIL;
                while (true) {
                    Varargs n = table.next(k);
                    if ((k = n.arg1()).isnil()) break;
                    LuaValue v = n.arg(2);
                    mod.log("  " + k.tojstring() + " = " + v.tojstring());
                }
                return LuaValue.NIL;
            }
        });
        
        debugUtils.set("logPosition", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Debug.logPosition() and Debug:logPosition() syntax
                int offset = args.narg() == 2 ? 1 : 0;
                if (args.narg() < 1 + offset) return LuaValue.NIL;
                
                LuaValue pos = args.arg(1 + offset);
                
                try {
                    double x = pos.get("x").todouble();
                    double y = pos.get("y").todouble();
                    double z = pos.get("z").todouble();
                    mod.log(String.format("[Debug] Position: (%.2f, %.2f, %.2f)", x, y, z));
                } catch (Exception e) {
                    mod.log("[Debug] Invalid position format");
                }
                return LuaValue.NIL;
            }
        });
        
        debugUtils.set("benchmark", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                // Handle both Debug.benchmark() and Debug:benchmark() syntax
                int offset = args.narg() == 3 ? 1 : 0;
                if (args.narg() < 2 + offset) return LuaValue.valueOf(-1);
                
                LuaValue name = args.arg(1 + offset);
                LuaValue func = args.arg(2 + offset);
                
                long start = System.nanoTime();
                try {
                    func.call();
                } catch (Exception e) {
                    mod.logWarning("[Debug] Benchmark '" + name.tojstring() + "' failed: " + e.getMessage());
                    return LuaValue.valueOf(-1);
                }
                long elapsed = System.nanoTime() - start;
                double milliseconds = elapsed / 1_000_000.0;
                mod.log(String.format("[Debug] Benchmark '%s': %.3f ms", name.tojstring(), milliseconds));
                return LuaValue.valueOf(milliseconds);
            }
        });
        
        return debugUtils;
    }
    
    /**
     * Creates data persistence utility functions
     */
    private LuaTable createDataUtils() {
        LuaTable dataUtils = new LuaTable();
        
        dataUtils.set("store", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both Data.store() and Data:store() syntax
                    int offset = args.narg() == 3 ? 1 : 0;
                    if (args.narg() < 2 + offset) return LuaValue.NIL;
                    
                    LuaValue key = args.arg(1 + offset);
                    LuaValue value = args.arg(2 + offset);
                    
                    if (persistenceManager != null && key != null) {
                        persistenceManager.storeScriptData(currentScriptName, key.tojstring(), value);
                    }
                    return LuaValue.NIL;
                } catch (Exception e) {
                    mod.logWarning("Error in Data.store: " + e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });
        
        dataUtils.set("retrieve", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both Data.retrieve() and Data:retrieve() syntax
                    int offset = args.narg() == 2 ? 1 : 0;
                    if (args.narg() < 1 + offset) return LuaValue.NIL;
                    
                    LuaValue key = args.arg(1 + offset);
                    
                    if (persistenceManager != null && key != null) {
                        LuaValue result = persistenceManager.retrieveScriptData(currentScriptName, key.tojstring());
                        return result != null ? result : LuaValue.NIL;
                    }
                    return LuaValue.NIL;
                } catch (Exception e) {
                    mod.logWarning("Error in Data.retrieve: " + e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });
        
        dataUtils.set("exists", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both Data.exists() and Data:exists() syntax
                    int offset = args.narg() == 2 ? 1 : 0;
                    if (args.narg() < 1 + offset) return LuaValue.FALSE;
                    
                    LuaValue key = args.arg(1 + offset);
                    
                    if (persistenceManager != null && key != null) {
                        return LuaValue.valueOf(persistenceManager.hasScriptData(currentScriptName, key.tojstring()));
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in Data.exists: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        dataUtils.set("remove", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both Data.remove() and Data:remove() syntax
                    int offset = args.narg() == 2 ? 1 : 0;
                    if (args.narg() < 1 + offset) return LuaValue.NIL;
                    
                    LuaValue key = args.arg(1 + offset);
                    
                    if (persistenceManager != null && key != null) {
                        LuaValue result = persistenceManager.removeScriptData(currentScriptName, key.tojstring());
                        return result != null ? result : LuaValue.NIL;
                    }
                    return LuaValue.NIL;
                } catch (Exception e) {
                    mod.logWarning("Error in Data.remove: " + e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });
        
        dataUtils.set("clear", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both Data.clear() and Data:clear() syntax
                    if (persistenceManager != null) {
                        persistenceManager.clearScriptData(currentScriptName);
                    }
                    return LuaValue.NIL;
                } catch (Exception e) {
                    mod.logWarning("Error in Data.clear: " + e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });
        
        dataUtils.set("keys", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both Data.keys() and Data:keys() syntax - no additional parameters needed
                    LuaTable keys = new LuaTable();
                    if (persistenceManager != null) {
                        Set<String> keySet = persistenceManager.getScriptDataKeys(currentScriptName);
                        if (keySet != null) {
                            int index = 1;
                            for (String key : keySet) {
                                if (key != null) {
                                    keys.set(index++, LuaValue.valueOf(key));
                                }
                            }
                        }
                    }
                    return keys;
                } catch (Exception e) {
                    mod.logWarning("Error in Data.keys: " + e.getMessage());
                    return new LuaTable();
                }
            }
        });
        
        return dataUtils;
    }
    
    /**
     * Cleanup utilities when scripts are unloaded
     */
    public void cleanup() {
        timers.clear();
        // Persistence data is managed by the persistence manager, no need to clear here
    }
} 