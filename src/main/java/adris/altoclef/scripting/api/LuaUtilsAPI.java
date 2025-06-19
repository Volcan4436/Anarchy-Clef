package adris.altoclef.scripting.api;

import adris.altoclef.AltoClef;
import adris.altoclef.scripting.persistence.ScriptPersistenceManager;
import adris.altoclef.util.helpers.WorldHelper;
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
        
        // Inventory utilities
        set("Inventory", createInventoryUtils());
        
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
        
        return playerUtils;
    }
    
    /**
     * Creates world utility functions
     */
    private LuaTable createWorldUtils() {
        LuaTable worldUtils = new LuaTable();
        
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
        
        worldUtils.set("isNight", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                return LuaValue.valueOf(mod.getWorld().isNight());
            }
        });
        
        worldUtils.set("isDay", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                return LuaValue.valueOf(mod.getWorld().isDay());
            }
        });
        
        worldUtils.set("getTimeOfDay", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue self) {
                if (mod.getWorld() == null) return LuaValue.valueOf(0);
                return LuaValue.valueOf(mod.getWorld().getTimeOfDay());
            }
        });
        
        return worldUtils;
    }
    
    /**
     * Creates inventory utility functions
     */
    private LuaTable createInventoryUtils() {
        LuaTable invUtils = new LuaTable();
        
        invUtils.set("getTotalItemCount", new OneArgFunction() {
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
        
        invUtils.set("getUniqueItemCount", new OneArgFunction() {
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
        
        return invUtils;
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