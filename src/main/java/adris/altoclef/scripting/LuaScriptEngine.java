package adris.altoclef.scripting;

import adris.altoclef.AltoClef;
import adris.altoclef.eventbus.ClefEventBus;
import adris.altoclef.scripting.api.LuaAltoClefAPI;
import adris.altoclef.scripting.api.LuaAltoMenuAPI;
import adris.altoclef.scripting.api.LuaUtilsAPI;
import adris.altoclef.scripting.dependency.DependencyManager;
import adris.altoclef.scripting.dependency.DependencyResolution;
import adris.altoclef.scripting.dependency.DependencyIssue;
import adris.altoclef.scripting.persistence.ScriptPersistenceManager;
import adris.altoclef.scripting.script.LuaScript;
import adris.altoclef.scripting.script.ScriptMetadata;
import adris.altoclef.scripting.security.ScriptSandbox;
import adris.altoclef.scripting.security.ScriptErrorHandler;
import adris.altoclef.eventbus.Subscription;
import adris.altoclef.eventbus.events.ChatMessageEvent;
import adris.altoclef.eventbus.events.SendChatEvent;
import net.minecraft.client.MinecraftClient;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.Map;

/**
 * Core Lua scripting engine for Anarchy-Clef
 * Manages the lifecycle of Lua scripts, provides API bindings, and ensures security
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class LuaScriptEngine {
    private final AltoClef mod;
    private final Globals globals;
    private final ConcurrentHashMap<String, LuaScript> loadedScripts;
    private final ScriptSandbox sandbox;
    private final ScriptErrorHandler errorHandler;
    private final ScriptPersistenceManager persistenceManager;
    private final DependencyManager dependencyManager;
    private boolean enabled = true;
    
    // Event subscriptions
    private Subscription<ChatMessageEvent> chatEventSubscription;
    private Subscription<SendChatEvent> sendChatEventSubscription;
    
    public LuaScriptEngine(AltoClef mod) {
        System.out.println("🔧 LuaScriptEngine constructor started");
        
        try {
            System.out.println("Setting mod instance...");
            this.mod = mod;
            
            System.out.println("Creating loadedScripts map...");
            this.loadedScripts = new ConcurrentHashMap<>();
            
            System.out.println("Creating ScriptErrorHandler...");
            this.errorHandler = new ScriptErrorHandler(mod);
            
            System.out.println("Creating ScriptSandbox...");
            this.sandbox = new ScriptSandbox();
            
            System.out.println("Creating ScriptPersistenceManager...");
            this.persistenceManager = new ScriptPersistenceManager(mod);
            
            System.out.println("Creating DependencyManager...");
            this.dependencyManager = new DependencyManager(mod);
            
            System.out.println("Initializing Lua environment with standard libraries...");
            // Initialize Lua environment with standard libraries
            this.globals = JsePlatform.standardGlobals();
            System.out.println("✅ JsePlatform.standardGlobals() completed");
            
            System.out.println("Registering AltoClef APIs...");
            // Register AltoClef APIs
            registerAltoClefAPIs();
            System.out.println("✅ API registration completed");
            
            System.out.println("Applying security sandbox restrictions...");
            // Apply security sandbox restrictions
            sandbox.applySecurityRestrictions(globals);
            System.out.println("✅ Security sandbox applied");
            
            System.out.println("Registering event listeners...");
            // Register event listeners for chat and commands
            try {
                registerEventListeners();
                System.out.println("✅ Event listeners registered");
            } catch (Exception e) {
                System.err.println("❌ Event listener registration failed, but continuing initialization");
                System.err.println("Event listener error: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("ALTO CLEF: Lua scripting engine initialized successfully");
            System.out.println("✅ LuaScriptEngine constructor completed successfully");
            
        } catch (Exception e) {
            System.err.println("❌ EXCEPTION in LuaScriptEngine constructor!");
            System.err.println("Exception type: " + e.getClass().getSimpleName());
            System.err.println("Exception message: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            throw e; // Re-throw to be caught by AltoClef
        }
    }
    
    /**
     * Registers all AltoClef-specific APIs that scripts can use
     */
    private void registerAltoClefAPIs() {
        try {
            // Core AltoClef API - provides access to bot functionality
            globals.set("AltoClef", new LuaAltoClefAPI(mod));
            
            // AltoMenu API - allows creating custom modules
            globals.set("AltoMenu", new LuaAltoMenuAPI(mod));
            
            // TODO: Add other APIs in future phases
            // globals.set("TaskSystem", new LuaTaskSystemAPI(mod));
            // globals.set("Utils", new LuaUtilsAPI(mod));
            // globals.set("Events", new LuaEventAPI(mod));
            
            // Script engine control
            globals.set("ScriptEngine", new LuaTable() {{
                set("reload", new ZeroArgFunction() {
                    @Override
                    public LuaValue call() {
                        // TODO: Implement script reloading
                        return LuaValue.NIL;
                    }
                });
                
                set("version", LuaValue.valueOf("1.0.0"));
            }});
            
            System.out.println("ALTO CLEF: Registered Lua APIs: AltoClef, AltoMenu, ScriptEngine");
            
        } catch (Exception e) {
            System.err.println("ALTO CLEF: Failed to register Lua APIs: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Registers event listeners for chat and command events
     */
    private void registerEventListeners() {
        try {
            // Listen for chat messages
            chatEventSubscription = ClefEventBus.subscribe(ChatMessageEvent.class, this::handleChatEvent);
            
            // Listen for command execution - we'll need to hook into the command system differently
            // For now, we'll listen for SendChatEvent to catch @commands
            sendChatEventSubscription = ClefEventBus.subscribe(SendChatEvent.class, this::handleSendChatEvent);
            
            System.out.println("ALTO CLEF: Registered Lua script engine event listeners");
        } catch (Exception e) {
            System.err.println("ALTO CLEF: Failed to register event listeners: " + e.getMessage());
            e.printStackTrace();
            // Don't throw the exception, just log it - event registration failure shouldn't stop script engine
        }
    }
    
    /**
     * Handle incoming chat messages and dispatch to script handlers
     */
    private void handleChatEvent(ChatMessageEvent event) {
        if (!enabled) return;
        
        try {
            String message = event.messageContent();
            String sender = event.senderName();
            String senderUUID = event.senderUUID();
            boolean isSelf = false;
            
            // Check if message is from self (current player)
            if (MinecraftClient.getInstance().player != null) {
                isSelf = senderUUID.equals(MinecraftClient.getInstance().player.getUuid().toString());
            }
            
            // Dispatch to all scripts with chat handlers
            for (LuaScript script : loadedScripts.values()) {
                if (script.isEnabled()) {
                    try {
                        // Get the script's AltoClef API instance
                        LuaValue altoClefAPI = script.getGlobal("AltoClef");
                        if (altoClefAPI instanceof LuaAltoClefAPI) {
                            ((LuaAltoClefAPI) altoClefAPI).handleChatEvent(message, sender, senderUUID, isSelf);
                        }
                    } catch (Exception e) {
                        mod.logWarning("Error dispatching chat event to script '" + script.getName() + "': " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            mod.logWarning("Error handling chat event: " + e.getMessage());
        }
    }
    
    /**
     * Handle outgoing chat messages to detect commands
     */
    private void handleSendChatEvent(SendChatEvent event) {
        if (!enabled) return;
        
        try {
            String message = event.message;
            
            // Check if it's a command (starts with command prefix)
            if (message.startsWith(mod.getModSettings().getCommandPrefix())) {
                String commandLine = message.substring(mod.getModSettings().getCommandPrefix().length());
                String[] parts = commandLine.split(" ", 2);
                String command = parts[0];
                String args = parts.length > 1 ? parts[1] : "";
                
                // Dispatch to all scripts with command handlers
                for (LuaScript script : loadedScripts.values()) {
                    if (script.isEnabled()) {
                        try {
                            // Get the script's AltoClef API instance
                            LuaValue altoClefAPI = script.getGlobal("AltoClef");
                            if (altoClefAPI instanceof LuaAltoClefAPI) {
                                ((LuaAltoClefAPI) altoClefAPI).handleCommandEvent(command, args);
                            }
                        } catch (Exception e) {
                            mod.logWarning("Error dispatching command event to script '" + script.getName() + "': " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            mod.logWarning("Error handling send chat event: " + e.getMessage());
        }
    }
    
    /**
     * Loads and executes a Lua script with dependency resolution
     * @param name Unique name for the script
     * @param sourceCode The Lua source code
     * @return true if script loaded successfully, false otherwise
     */
    public boolean loadScript(String name, String sourceCode) {
        if (!enabled) {
            mod.logWarning("Script engine is disabled");
            return false;
        }
        
        if (name == null || name.trim().isEmpty()) {
            mod.logWarning("Script name cannot be empty");
            return false;
        }
        
        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            mod.logWarning("Script source code cannot be empty");
            return false;
        }
        
        try {
            // Pre-validate script syntax
            if (!validateScriptSyntax(sourceCode)) {
                errorHandler.handleSyntaxError(name, "Invalid Lua syntax");
                return false;
            }
            
            // Parse script metadata for dependency information
            ScriptMetadata metadata = ScriptMetadata.parseFromSource(sourceCode);
            mod.log("[Dependency] Parsed metadata for " + name + ": " + metadata.getDependencies().size() + " dependencies");
            
            // Register script with dependency manager
            dependencyManager.registerScript(name, metadata);
            
            // Resolve dependencies and loading order
            DependencyResolution resolution = dependencyManager.resolveLoadingOrder(name);
            
            if (!resolution.isSuccessful()) {
                mod.logWarning("[Dependency] Failed to resolve dependencies for " + name);
                for (DependencyIssue issue : resolution.getIssues()) {
                    mod.logWarning("[Dependency] " + issue.toString());
                }
                
                // Don't load script if there are critical dependency issues
                if (resolution.hasCriticalIssues()) {
                    dependencyManager.unregisterScript(name);
                    return false;
                }
            }
            
            // Load dependencies first (if they're not already loaded)
            for (String depName : resolution.getLoadingOrder()) {
                if (!depName.equals(name) && !isScriptLoaded(depName)) {
                    mod.log("[Dependency] Dependency " + depName + " is required but not loaded for " + name);
                    // In a real implementation, we would load the dependency script here
                    // For now, we'll just log the requirement
                }
            }
            
            // Create isolated globals for this script
            Globals scriptGlobals = createScriptGlobals(name);
            
            // Compile the script
            LuaValue chunk = scriptGlobals.load(sourceCode, name);
            
            // Create script wrapper with metadata
            LuaScript script = new LuaScript(name, sourceCode, chunk, scriptGlobals, this);
            
            // Execute script initialization in sandbox with timeout
            sandbox.executeWithTimeout(() -> {
                chunk.call();
                
                // Cache script functions AFTER execution when they actually exist
                script.cacheScriptFunctionsAfterExecution();
                
                // Call script's onLoad function if it exists
                LuaValue onLoad = scriptGlobals.get("onLoad");
                if (!onLoad.isnil()) {
                    onLoad.call();
                }
                
                return null;
            }, 5000); // 5 second timeout for script loading
            
            // Store the loaded script (replace if exists)
            LuaScript oldScript = loadedScripts.put(name, script);
            if (oldScript != null) {
                oldScript.cleanup();
                dependencyManager.markScriptUnloaded(name);
                persistenceManager.saveScriptState(name, false, Map.of("replaced", true));
                mod.log("Replaced existing script: " + name);
            } else {
                mod.log("Loaded new script: " + name);
            }
            
            // Mark script as loaded in dependency manager
            dependencyManager.markScriptLoaded(name);
            
            // Save script state as loaded and running
            persistenceManager.saveScriptState(name, true, Map.of(
                "loaded_at", System.currentTimeMillis(),
                "source_length", sourceCode.length(),
                "auto_loaded", false,
                "dependencies", metadata.getDependencies().size()
            ));
            
            mod.log("Script '" + name + "' is now in loadedScripts map. Total scripts: " + loadedScripts.size());
            
            return true;
            
        } catch (LuaError e) {
            errorHandler.handleScriptError(name, "Lua error during loading", e);
            dependencyManager.unregisterScript(name);
            return false;
        } catch (TimeoutException e) {
            errorHandler.handleScriptError(name, "Script loading timeout", e);
            dependencyManager.unregisterScript(name);
            return false;
        } catch (Exception e) {
            errorHandler.handleScriptError(name, "Unexpected error during loading", e);
            dependencyManager.unregisterScript(name);
            return false;
        }
    }
    
    /**
     * Creates isolated Lua globals for a script
     */
    private Globals createScriptGlobals(String scriptName) {
        // Create new isolated globals for each script
        Globals scriptGlobals = JsePlatform.standardGlobals();
        
        // Copy other APIs to the script environment
        LuaValue altoMenuAPI = globals.get("AltoMenu");
        LuaValue scriptEngineAPI = globals.get("ScriptEngine");
        
        scriptGlobals.set("AltoMenu", altoMenuAPI);
        scriptGlobals.set("ScriptEngine", scriptEngineAPI);
        
        // Create script-specific Utils API with persistence support
        LuaUtilsAPI utilsAPI = new LuaUtilsAPI(mod, persistenceManager, scriptName);
        
        // Create script-specific AltoClef API with chat and command event handling
        LuaAltoClefAPI scriptAltoClefAPI = new LuaAltoClefAPI(mod, scriptName);
        
        // Add Utils API to AltoClef API
        scriptAltoClefAPI.set("Utils", utilsAPI);
        
        scriptGlobals.set("AltoClef", scriptAltoClefAPI);
        
        // Also make Utils available as a global for easier access (Utils.Player.isJumping())
        scriptGlobals.set("Utils", utilsAPI);
        
        // Apply sandbox restrictions to this script's environment
        sandbox.applySecurityRestrictions(scriptGlobals);
        
        return scriptGlobals;
    }
    
    /**
     * Basic syntax validation for Lua scripts
     */
    private boolean validateScriptSyntax(String sourceCode) {
        try {
            // Try to compile the script to check for syntax errors
            Globals testGlobals = JsePlatform.standardGlobals();
            testGlobals.load(sourceCode, "syntax_test");
            return true;
        } catch (LuaError e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Unloads a script and cleans up its resources
     */
    public void unloadScript(String name) {
        LuaScript script = loadedScripts.remove(name);
        if (script != null) {
            try {
                // Check if other scripts depend on this one
                java.util.List<String> dependents = dependencyManager.validateUnload(name);
                if (!dependents.isEmpty()) {
                    mod.logWarning("[Dependency] Warning: Unloading " + name + " but these loaded scripts depend on it: " + dependents);
                }
                
                script.cleanup();
                
                // Mark script as unloaded in dependency manager
                dependencyManager.markScriptUnloaded(name);
                
                // Save script state as not running
                persistenceManager.saveScriptState(name, false, Map.of(
                    "unloaded_at", System.currentTimeMillis(),
                    "was_enabled", script.isEnabled(),
                    "had_dependents", dependents.size()
                ));
                
                mod.log("Unloaded script: " + name + ". Remaining scripts: " + loadedScripts.size());
            } catch (Exception e) {
                errorHandler.handleScriptError(name, "Error during script cleanup", e);
            }
        } else {
            mod.log("Attempted to unload script '" + name + "' but it was not found in loadedScripts");
        }
    }
    
    /**
     * Reloads a script with its current source code
     */
    public void reloadScript(String name) {
        LuaScript script = loadedScripts.get(name);
        if (script != null) {
            String sourceCode = script.getSourceCode();
            unloadScript(name);
            loadScript(name, sourceCode);
        }
    }
    
    /**
     * Called every game tick to update all loaded scripts
     */
    public void tickAllScripts() {
        if (!enabled) return;
        
        loadedScripts.values().parallelStream().forEach(script -> {
            if (script.isEnabled()) {
                try {
                    script.tick();
                } catch (Exception e) {
                    errorHandler.handleScriptError(script.getName(), "Error during script tick", e);
                    // Disable script after error to prevent spam
                    script.setEnabled(false);
                }
            }
        });
    }
    
    /**
     * Disables/enables the entire script engine
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            mod.log("Lua script engine disabled");
        } else {
            mod.log("Lua script engine enabled");
        }
    }
    
    // Getters and utility methods
    public Map<String, LuaScript> getLoadedScripts() { 
        return new ConcurrentHashMap<>(loadedScripts); 
    }
    
    public boolean isScriptLoaded(String name) { 
        return loadedScripts.containsKey(name); 
    }
    
    public ScriptErrorHandler getErrorHandler() { 
        return errorHandler; 
    }
    
    public boolean isEnabled() { 
        return enabled; 
    }
    
    public int getLoadedScriptCount() {
        return loadedScripts.size();
    }
    
    /**
     * Auto-restore scripts that were running when last saved
     */
    public void autoRestoreScripts() {
        if (!enabled) {
            mod.log("Cannot auto-restore scripts: engine is disabled");
            return;
        }
        
        try {
            persistenceManager.autoRestoreScripts();
            // Note: The actual restoration would require loading script files from disk
            // This is a placeholder for the restoration logic
        } catch (Exception e) {
            mod.logWarning("Error during script auto-restoration: " + e.getMessage());
        }
    }
    
    /**
     * Get the persistence manager for external access
     */
    public ScriptPersistenceManager getPersistenceManager() {
        return persistenceManager;
    }
    
    /**
     * Get the dependency manager for external access
     */
    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }
    
    /**
     * Cleanup all scripts and shutdown the engine
     */
    public void shutdown() {
        mod.log("Shutting down Lua script engine...");
        
        // Unsubscribe from events
        if (chatEventSubscription != null) {
            ClefEventBus.unsubscribe(chatEventSubscription);
            chatEventSubscription = null;
        }
        if (sendChatEventSubscription != null) {
            ClefEventBus.unsubscribe(sendChatEventSubscription);
            sendChatEventSubscription = null;
        }
        
        // Unload all scripts
        for (String scriptName : loadedScripts.keySet()) {
            unloadScript(scriptName);
        }
        
        // Cleanup dependency manager
        dependencyManager.clear();
        
        // Cleanup persistence manager
        persistenceManager.cleanup();
        
        enabled = false;
        mod.log("Lua script engine shutdown complete");
    }
} 