package adris.altoclef.scripting.persistence;

import adris.altoclef.AltoClef;
import com.google.gson.*;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages script persistence across game sessions
 * Handles saving/loading script states, configurations, and data
 * 
 * Directory structure:
 * AltoClefLUA/scripts/
 *   ├── script_states.json           - Script running states
 *   ├── script_data/                 - Per-script persistent data
 *   │   ├── script1.json
 *   │   └── script2.json
 *   └── script_configs/              - Per-script configurations
 *       ├── script1.json
 *       └── script2.json
 * 
 * @author Hearty
 */
public class ScriptPersistenceManager {
    private final AltoClef mod;
    private final Path persistenceDir;
    private final Path scriptsStateFile;
    private final Path scriptDataDir;
    private final Path scriptConfigDir;
    
    private final Gson gson;
    private final Map<String, ScriptState> scriptStates;
    private final Map<String, Map<String, Object>> scriptConfigurations;
    private final Map<String, Map<String, LuaValue>> scriptPersistentData;
    
    // File-backed persistent data storage
    private static final String SCRIPT_STATE_FILE = "script_states.json";
    private static final String SCRIPT_DATA_DIR = "script_data";
    private static final String SCRIPT_CONFIG_DIR = "script_configs";
    private static final String DATA_FILE_EXTENSION = ".json";
    private static final String CONFIG_FILE_EXTENSION = ".json";
    
    public ScriptPersistenceManager(AltoClef mod) {
        this.mod = mod;
        this.persistenceDir = Paths.get("AltoClefLUA", "scripts");
        this.scriptsStateFile = persistenceDir.resolve(SCRIPT_STATE_FILE);
        this.scriptDataDir = persistenceDir.resolve(SCRIPT_DATA_DIR);
        this.scriptConfigDir = persistenceDir.resolve(SCRIPT_CONFIG_DIR);
        
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
            
        this.scriptStates = new ConcurrentHashMap<>();
        this.scriptConfigurations = new ConcurrentHashMap<>();
        this.scriptPersistentData = new ConcurrentHashMap<>();
        
        initializePersistenceDirectories();
        loadPersistedData();
    }
    
    /**
     * Initialize persistence directories
     */
    private void initializePersistenceDirectories() {
        try {
            Files.createDirectories(persistenceDir);
            Files.createDirectories(scriptDataDir);
            Files.createDirectories(scriptConfigDir);
            // Use System.out during initialization to avoid butler dependency
            System.out.println("ALTO CLEF: Script persistence directories initialized: " + persistenceDir);
        } catch (IOException e) {
            System.err.println("ALTO CLEF: Failed to create script persistence directories: " + e.getMessage());
        }
    }
    
    /**
     * Load all persisted data on startup
     */
    private void loadPersistedData() {
        loadScriptStates();
        loadScriptConfigurations();
        loadScriptPersistentData();
    }
    
    /**
     * Save script state when script starts/stops
     */
    public void saveScriptState(String scriptName, boolean isRunning, Map<String, Object> metadata) {
        ScriptState state = new ScriptState(
            scriptName,
            isRunning,
            System.currentTimeMillis(),
            metadata
        );
        
        scriptStates.put(scriptName, state);
        saveScriptStatesToFile();
        
        mod.log("Saved state for script: " + scriptName + " (running: " + isRunning + ")");
    }
    
    /**
     * Get script state
     */
    public ScriptState getScriptState(String scriptName) {
        return scriptStates.get(scriptName);
    }
    
    /**
     * Get all scripts that were running when last saved
     */
    public List<String> getRunningScripts() {
        return scriptStates.values().stream()
            .filter(state -> state.isRunning())
            .map(ScriptState::getScriptName)
            .toList();
    }
    
    /**
     * Save script configuration
     */
    public void saveScriptConfiguration(String scriptName, String key, Object value) {
        scriptConfigurations.computeIfAbsent(scriptName, k -> new ConcurrentHashMap<>())
            .put(key, value);
        saveScriptConfigurationToFile(scriptName);
        
        mod.log("Saved configuration for script " + scriptName + ": " + key + " = " + value);
    }
    
    /**
     * Get script configuration value
     */
    public Object getScriptConfiguration(String scriptName, String key) {
        Map<String, Object> config = scriptConfigurations.get(scriptName);
        return config != null ? config.get(key) : null;
    }
    
    /**
     * Get all configurations for a script
     */
    public Map<String, Object> getScriptConfigurations(String scriptName) {
        return scriptConfigurations.getOrDefault(scriptName, new HashMap<>());
    }
    
    /**
     * Store persistent data for a script
     */
    public void storeScriptData(String scriptName, String key, LuaValue value) {
        scriptPersistentData.computeIfAbsent(scriptName, k -> new ConcurrentHashMap<>())
            .put(key, value);
        saveScriptDataToFile(scriptName);
    }
    
    /**
     * Retrieve persistent data for a script
     */
    public LuaValue retrieveScriptData(String scriptName, String key) {
        Map<String, LuaValue> data = scriptPersistentData.get(scriptName);
        return data != null ? data.get(key) : LuaValue.NIL;
    }
    
    /**
     * Check if script data key exists
     */
    public boolean hasScriptData(String scriptName, String key) {
        Map<String, LuaValue> data = scriptPersistentData.get(scriptName);
        return data != null && data.containsKey(key);
    }
    
    /**
     * Get all data keys for a script
     */
    public Set<String> getScriptDataKeys(String scriptName) {
        Map<String, LuaValue> data = scriptPersistentData.get(scriptName);
        return data != null ? new HashSet<>(data.keySet()) : new HashSet<>();
    }
    
    /**
     * Remove a specific data key for a script
     */
    public LuaValue removeScriptData(String scriptName, String key) {
        Map<String, LuaValue> data = scriptPersistentData.get(scriptName);
        if (data != null) {
            LuaValue removed = data.remove(key);
            saveScriptDataToFile(scriptName);
            return removed != null ? removed : LuaValue.NIL;
        }
        return LuaValue.NIL;
    }
    
    /**
     * Clear all data for a script
     */
    public void clearScriptData(String scriptName) {
        scriptPersistentData.remove(scriptName);
        deleteScriptDataFile(scriptName);
        mod.log("Cleared all persistent data for script: " + scriptName);
    }
    
    /**
     * Remove script from persistence (when uninstalled)
     */
    public void removeScript(String scriptName) {
        scriptStates.remove(scriptName);
        scriptConfigurations.remove(scriptName);
        scriptPersistentData.remove(scriptName);
        
        saveScriptStatesToFile();
        deleteScriptConfigFile(scriptName);
        deleteScriptDataFile(scriptName);
        
        mod.log("Removed script from persistence: " + scriptName);
    }
    
    /**
     * Auto-restore scripts that were running
     */
    public void autoRestoreScripts() {
        List<String> runningScripts = getRunningScripts();
        
        if (runningScripts.isEmpty()) {
            mod.log("No scripts to auto-restore");
            return;
        }
        
        mod.log("Auto-restoring " + runningScripts.size() + " scripts...");
        
        for (String scriptName : runningScripts) {
            try {
                // Note: This would need to be called by the script engine
                // mod.getScriptEngine().loadScript(scriptName);
                mod.log("Queued script for restoration: " + scriptName);
            } catch (Exception e) {
                mod.logWarning("Failed to restore script " + scriptName + ": " + e.getMessage());
                // Mark as not running since restoration failed
                saveScriptState(scriptName, false, Map.of("restore_failed", true));
            }
        }
    }
    
    /**
     * Get script statistics
     */
    public ScriptStatistics getScriptStatistics(String scriptName) {
        ScriptState state = scriptStates.get(scriptName);
        Map<String, Object> config = scriptConfigurations.get(scriptName);
        Map<String, LuaValue> data = scriptPersistentData.get(scriptName);
        
        return new ScriptStatistics(
            scriptName,
            state != null ? state.getLastModified() : 0,
            state != null && state.isRunning(),
            config != null ? config.size() : 0,
            data != null ? data.size() : 0
        );
    }
    
    /**
     * Save script states to file
     */
    private void saveScriptStatesToFile() {
        try {
            String json = gson.toJson(scriptStates);
            Files.writeString(scriptsStateFile, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            mod.logWarning("Failed to save script states: " + e.getMessage());
        }
    }
    
    /**
     * Load script states from file
     */
    private void loadScriptStates() {
        if (!Files.exists(scriptsStateFile)) return;
        
        try {
            String json = Files.readString(scriptsStateFile);
            Map<String, ScriptState> loaded = gson.fromJson(json, 
                new com.google.gson.reflect.TypeToken<Map<String, ScriptState>>(){}.getType());
            
            if (loaded != null) {
                scriptStates.putAll(loaded);
                System.out.println("ALTO CLEF: Loaded " + loaded.size() + " script states");
            }
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("ALTO CLEF: Failed to load script states: " + e.getMessage());
        }
    }
    
    /**
     * Save script configuration to file
     */
    private void saveScriptConfigurationToFile(String scriptName) {
        Map<String, Object> config = scriptConfigurations.get(scriptName);
        if (config == null) return;
        
        Path configFile = scriptConfigDir.resolve(scriptName + CONFIG_FILE_EXTENSION);
        try {
            String json = gson.toJson(config);
            Files.writeString(configFile, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            mod.logWarning("Failed to save script configuration: " + e.getMessage());
        }
    }
    
    /**
     * Load script configurations
     */
    private void loadScriptConfigurations() {
        if (!Files.exists(scriptConfigDir)) return;
        
        try {
            Files.list(scriptConfigDir)
                .filter(path -> path.toString().endsWith(CONFIG_FILE_EXTENSION))
                .forEach(this::loadScriptConfigurationFromFile);
        } catch (IOException e) {
            System.err.println("ALTO CLEF: Failed to load script configurations: " + e.getMessage());
        }
    }
    
    /**
     * Load single script configuration
     */
    private void loadScriptConfigurationFromFile(Path configFile) {
        try {
            String scriptName = configFile.getFileName().toString()
                .replace(CONFIG_FILE_EXTENSION, "");
            String json = Files.readString(configFile);
            Map<String, Object> config = gson.fromJson(json, 
                new com.google.gson.reflect.TypeToken<Map<String, Object>>(){}.getType());
            
            if (config != null) {
                scriptConfigurations.put(scriptName, config);
            }
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("ALTO CLEF: Failed to load script configuration from " + configFile + ": " + e.getMessage());
        }
    }
    
    /**
     * Save script data to file
     */
    private void saveScriptDataToFile(String scriptName) {
        Map<String, LuaValue> data = scriptPersistentData.get(scriptName);
        if (data == null || data.isEmpty()) return;
        
        Path dataFile = scriptDataDir.resolve(scriptName + DATA_FILE_EXTENSION);
        try {
            Map<String, Object> exportableData = convertLuaDataToExportable(data);
            String json = gson.toJson(exportableData);
            Files.writeString(dataFile, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            mod.logWarning("Failed to save script data: " + e.getMessage());
        }
    }
    
    /**
     * Load script persistent data
     */
    private void loadScriptPersistentData() {
        if (!Files.exists(scriptDataDir)) return;
        
        try {
            Files.list(scriptDataDir)
                .filter(path -> path.toString().endsWith(DATA_FILE_EXTENSION))
                .forEach(this::loadScriptDataFromFile);
        } catch (IOException e) {
            System.err.println("ALTO CLEF: Failed to load script data: " + e.getMessage());
        }
    }
    
    /**
     * Load single script data file
     */
    private void loadScriptDataFromFile(Path dataFile) {
        try {
            String scriptName = dataFile.getFileName().toString()
                .replace(DATA_FILE_EXTENSION, "");
            String json = Files.readString(dataFile);
            Map<String, Object> data = gson.fromJson(json, 
                new com.google.gson.reflect.TypeToken<Map<String, Object>>(){}.getType());
            
            if (data != null) {
                Map<String, LuaValue> luaData = convertExportableToLuaData(data);
                scriptPersistentData.put(scriptName, luaData);
            }
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("ALTO CLEF: Failed to load script data from " + dataFile + ": " + e.getMessage());
        }
    }
    
    /**
     * Convert LuaValue data to JSON-serializable format
     */
    private Map<String, Object> convertLuaDataToExportable(Map<String, LuaValue> luaData) {
        if (luaData == null) return new HashMap<>();
        
        Map<String, Object> exportable = new HashMap<>();
        for (Map.Entry<String, LuaValue> entry : luaData.entrySet()) {
            exportable.put(entry.getKey(), luaValueToObject(entry.getValue()));
        }
        return exportable;
    }
    
    /**
     * Convert JSON data back to LuaValue format
     */
    private Map<String, LuaValue> convertExportableToLuaData(Map<String, Object> exportable) {
        Map<String, LuaValue> luaData = new HashMap<>();
        for (Map.Entry<String, Object> entry : exportable.entrySet()) {
            luaData.put(entry.getKey(), objectToLuaValue(entry.getValue()));
        }
        return luaData;
    }
    
    /**
     * Convert LuaValue to serializable object
     */
    private Object luaValueToObject(LuaValue value) {
        if (value.isnil()) return null;
        if (value.isboolean()) return value.toboolean();
        if (value.isnumber()) return value.todouble();
        if (value.isstring()) return value.tojstring();
        if (value.istable()) {
            Map<String, Object> table = new HashMap<>();
            LuaValue k = LuaValue.NIL;
            while (true) {
                org.luaj.vm2.Varargs n = value.next(k);
                if ((k = n.arg1()).isnil()) break;
                table.put(k.tojstring(), luaValueToObject(n.arg(2)));
            }
            return table;
        }
        return value.tojstring(); // Fallback to string representation
    }
    
    /**
     * Convert object back to LuaValue
     */
    private LuaValue objectToLuaValue(Object obj) {
        if (obj == null) return LuaValue.NIL;
        if (obj instanceof Boolean) return LuaValue.valueOf((Boolean) obj);
        if (obj instanceof Number) return LuaValue.valueOf(((Number) obj).doubleValue());
        if (obj instanceof String) return LuaValue.valueOf((String) obj);
        if (obj instanceof Map) {
            LuaTable table = new LuaTable();
            Map<?, ?> map = (Map<?, ?>) obj;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                table.set(entry.getKey().toString(), objectToLuaValue(entry.getValue()));
            }
            return table;
        }
        return LuaValue.valueOf(obj.toString()); // Fallback
    }
    
    /**
     * Delete script configuration file
     */
    private void deleteScriptConfigFile(String scriptName) {
        Path configFile = scriptConfigDir.resolve(scriptName + CONFIG_FILE_EXTENSION);
        try {
            Files.deleteIfExists(configFile);
        } catch (IOException e) {
            mod.logWarning("Failed to delete script config file: " + e.getMessage());
        }
    }
    
    /**
     * Delete script data file
     */
    private void deleteScriptDataFile(String scriptName) {
        Path dataFile = scriptDataDir.resolve(scriptName + DATA_FILE_EXTENSION);
        try {
            Files.deleteIfExists(dataFile);
        } catch (IOException e) {
            mod.logWarning("Failed to delete script data file: " + e.getMessage());
        }
    }
    
    /**
     * Cleanup persistence manager
     */
    public void cleanup() {
        // Save all current states before shutdown
        saveScriptStatesToFile();
        
        // Save all configurations
        for (String scriptName : scriptConfigurations.keySet()) {
            saveScriptConfigurationToFile(scriptName);
        }
        
        // Save all persistent data
        for (String scriptName : scriptPersistentData.keySet()) {
            saveScriptDataToFile(scriptName);
        }
        
        mod.log("Script persistence cleanup completed");
    }
} 