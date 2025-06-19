package adris.altoclef.scripting;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for extracting bundled Lua scripts from JAR resources
 * to the user's script directory.
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class ScriptResourceExtractor {
    
    private static final String RESOURCE_SCRIPTS_PATH = "/scripts/";
    private static final Path USER_SCRIPTS_DIR = Paths.get("AltoClefLUA", "scripts");
    
    // List of bundled scripts to extract
    private static final String[] EXAMPLE_SCRIPTS = {
        "examples/auto_food.lua",
        "examples/basic_mining.lua"
    };
    
    private static final String[] LIBRARY_SCRIPTS = {
        "libraries/math_helpers.lua"
    };
    
    /**
     * Extract all bundled scripts to the user directory if they don't exist
     */
    public static void extractBundledScripts() {
        try {
            // Ensure the user scripts directory exists
            initializeDirectoryStructure();
            
            // Extract example scripts
            extractScriptCategory("examples", EXAMPLE_SCRIPTS);
            
            // Extract library scripts
            extractScriptCategory("libraries", LIBRARY_SCRIPTS);
            
            System.out.println("Script extraction completed successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to extract bundled scripts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize the directory structure for scripts
     */
    private static void initializeDirectoryStructure() throws IOException {
        Files.createDirectories(USER_SCRIPTS_DIR);
        Files.createDirectories(USER_SCRIPTS_DIR.resolve("examples"));
        Files.createDirectories(USER_SCRIPTS_DIR.resolve("libraries"));
        Files.createDirectories(USER_SCRIPTS_DIR.resolve("user_scripts"));
    }
    
    /**
     * Extract scripts from a specific category
     */
    private static void extractScriptCategory(String category, String[] scripts) {
        for (String scriptPath : scripts) {
            try {
                extractScript(scriptPath);
            } catch (Exception e) {
                System.err.println("Failed to extract script '" + scriptPath + "': " + e.getMessage());
            }
        }
    }
    
    /**
     * Extract a single script from resources to user directory
     */
    private static void extractScript(String scriptPath) throws IOException {
        String resourcePath = RESOURCE_SCRIPTS_PATH + scriptPath;
        Path targetPath = USER_SCRIPTS_DIR.resolve(scriptPath);
        
        // Skip if file already exists (don't overwrite user modifications)
        if (Files.exists(targetPath)) {
            return;
        }
        
        // Get the script from resources
        InputStream resourceStream = ScriptResourceExtractor.class.getResourceAsStream(resourcePath);
        
        if (resourceStream == null) {
            System.err.println("Bundled script not found in resources: " + resourcePath);
            return;
        }
        
        try {
            // Ensure parent directory exists
            Files.createDirectories(targetPath.getParent());
            
            // Copy the script to user directory
            Files.copy(resourceStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("Extracted script: " + scriptPath);
            
        } finally {
            resourceStream.close();
        }
    }
    
    /**
     * Check if bundled scripts need to be extracted
     */
    public static boolean needsExtraction() {
        // Check if any of the bundled scripts are missing
        for (String scriptPath : EXAMPLE_SCRIPTS) {
            if (!Files.exists(USER_SCRIPTS_DIR.resolve(scriptPath))) {
                return true;
            }
        }
        
        for (String scriptPath : LIBRARY_SCRIPTS) {
            if (!Files.exists(USER_SCRIPTS_DIR.resolve(scriptPath))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Force extraction of all bundled scripts (overwrites existing)
     */
    public static void forceExtractBundledScripts() {
        try {
            // Ensure the user scripts directory exists
            initializeDirectoryStructure();
            
            // Force extract all scripts
            for (String scriptPath : EXAMPLE_SCRIPTS) {
                forceExtractScript(scriptPath);
            }
            
            for (String scriptPath : LIBRARY_SCRIPTS) {
                forceExtractScript(scriptPath);
            }
            
            System.out.println("Force extraction completed successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to force extract bundled scripts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Force extract a single script (overwrites existing)
     */
    private static void forceExtractScript(String scriptPath) throws IOException {
        String resourcePath = RESOURCE_SCRIPTS_PATH + scriptPath;
        Path targetPath = USER_SCRIPTS_DIR.resolve(scriptPath);
        
        // Get the script from resources
        InputStream resourceStream = ScriptResourceExtractor.class.getResourceAsStream(resourcePath);
        
        if (resourceStream == null) {
            System.err.println("Bundled script not found in resources: " + resourcePath);
            return;
        }
        
        try {
            // Ensure parent directory exists
            Files.createDirectories(targetPath.getParent());
            
            // Copy the script to user directory (force overwrite)
            Files.copy(resourceStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("Force extracted script: " + scriptPath);
            
        } finally {
            resourceStream.close();
        }
    }
    
    /**
     * Get list of all bundled scripts
     */
    public static List<String> getBundledScripts() {
        List<String> scripts = new ArrayList<>();
        
        for (String script : EXAMPLE_SCRIPTS) {
            scripts.add(script);
        }
        
        for (String script : LIBRARY_SCRIPTS) {
            scripts.add(script);
        }
        
        return scripts;
    }
    
    /**
     * Create a default user script template in the user_scripts directory
     */
    public static void createDefaultUserScript() {
        try {
            Path userScriptsDir = USER_SCRIPTS_DIR.resolve("user_scripts");
            Path welcomeScript = userScriptsDir.resolve("welcome.lua");
            
            // Don't overwrite if it already exists
            if (Files.exists(welcomeScript)) {
                return;
            }
            
            String welcomeContent = """
                --[[
                @name Welcome Script
                @description Welcome to AltoClef Lua scripting!
                @version 1.0.0
                @author Player
                @category Tutorial
                @dependencies none
                ]]--
                
                local script = {}
                
                function onLoad()
                    AltoClef.log("Welcome to AltoClef Lua Scripting!")
                    AltoClef.log("This is a sample script to get you started.")
                    AltoClef.log("Check out the examples folder for more complex scripts.")
                end
                
                function onTick()
                    -- Your script logic goes here
                    -- This function is called every game tick when the script is enabled
                end
                
                function onEnable()
                    AltoClef.log("Welcome script enabled!")
                end
                
                function onDisable()
                    AltoClef.log("Welcome script disabled!")
                end
                
                function onCleanup()
                    AltoClef.log("Welcome script cleaning up")
                end
                
                -- Example: Get player health
                function getPlayerHealth()
                    return Utils.player.getHealth()
                end
                
                -- Example: Check if player has item
                function hasItem(itemName)
                    return Utils.inventory.hasItem(itemName)
                end
                
                return script
                """;
            
            Files.writeString(welcomeScript, welcomeContent);
            System.out.println("Created welcome script for new users");
            
        } catch (IOException e) {
            System.err.println("Failed to create welcome script: " + e.getMessage());
        }
    }
} 