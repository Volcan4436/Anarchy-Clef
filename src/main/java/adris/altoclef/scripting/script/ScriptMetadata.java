package adris.altoclef.scripting.script;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses and stores metadata from Lua script comments
 * Supports metadata format like:
 * --[[
 * @name Script Name
 * @description Script description
 * @version 1.0.0
 * @author Author Name
 * @category Category
 * @dependencies [dep1, dep2]
 * @permissions [perm1, perm2]
 * ]]--
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class ScriptMetadata {
    private String name = "Unknown Script";
    private String description = "No description provided";
    private String version = "1.0.0";
    private String author = "Unknown";
    private String category = "Utility";
    private List<String> dependencies = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();
    private long lastModified = System.currentTimeMillis();
    
    // Regex patterns for parsing metadata
    private static final Pattern METADATA_BLOCK = Pattern.compile("--\\[\\[(.*?)\\]\\]--", Pattern.DOTALL);
    private static final Pattern NAME_PATTERN = Pattern.compile("@name\\s+(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("@description\\s+(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern VERSION_PATTERN = Pattern.compile("@version\\s+(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("@author\\s+(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("@category\\s+(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DEPENDENCIES_PATTERN = Pattern.compile("@dependencies\\s+\\[([^\\]]+)\\]", Pattern.CASE_INSENSITIVE);
    private static final Pattern PERMISSIONS_PATTERN = Pattern.compile("@permissions\\s+\\[([^\\]]+)\\]", Pattern.CASE_INSENSITIVE);
    
    private ScriptMetadata() {
        // Private constructor - use parseFromSource
    }
    
    /**
     * Parses metadata from Lua source code
     */
    public static ScriptMetadata parseFromSource(String sourceCode) {
        ScriptMetadata metadata = new ScriptMetadata();
        
        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            return metadata;
        }
        
        try {
            // Find metadata block
            Matcher blockMatcher = METADATA_BLOCK.matcher(sourceCode);
            if (blockMatcher.find()) {
                String metadataBlock = blockMatcher.group(1);
                
                // Parse individual fields
                metadata.name = extractField(metadataBlock, NAME_PATTERN, metadata.name);
                metadata.description = extractField(metadataBlock, DESCRIPTION_PATTERN, metadata.description);
                metadata.version = extractField(metadataBlock, VERSION_PATTERN, metadata.version);
                metadata.author = extractField(metadataBlock, AUTHOR_PATTERN, metadata.author);
                metadata.category = extractField(metadataBlock, CATEGORY_PATTERN, metadata.category);
                
                // Parse dependencies list
                metadata.dependencies = extractList(metadataBlock, DEPENDENCIES_PATTERN);
                
                // Parse permissions list
                metadata.permissions = extractList(metadataBlock, PERMISSIONS_PATTERN);
            }
        } catch (Exception e) {
            // If metadata parsing fails, just use defaults
            System.err.println("Failed to parse script metadata: " + e.getMessage());
        }
        
        return metadata;
    }
    
    /**
     * Extracts a single field from metadata block
     */
    private static String extractField(String metadataBlock, Pattern pattern, String defaultValue) {
        Matcher matcher = pattern.matcher(metadataBlock);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return defaultValue;
    }
    
    /**
     * Extracts a list field from metadata block
     */
    private static List<String> extractList(String metadataBlock, Pattern pattern) {
        List<String> result = new ArrayList<>();
        Matcher matcher = pattern.matcher(metadataBlock);
        if (matcher.find()) {
            String listContent = matcher.group(1);
            String[] items = listContent.split(",");
            for (String item : items) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
        }
        return result;
    }
    
    /**
     * Creates a default metadata object
     */
    public static ScriptMetadata createDefault(String scriptName) {
        ScriptMetadata metadata = new ScriptMetadata();
        metadata.name = scriptName != null ? scriptName : "Unknown Script";
        return metadata;
    }
    
    /**
     * Validates the metadata for any issues
     */
    public List<String> validate() {
        List<String> issues = new ArrayList<>();
        
        if (name == null || name.trim().isEmpty()) {
            issues.add("Script name is missing or empty");
        }
        
        if (version != null && !version.matches("\\d+\\.\\d+\\.\\d+")) {
            issues.add("Version should follow semantic versioning (x.y.z)");
        }
        
        // Check for valid category
        List<String> validCategories = List.of("Combat", "Movement", "Player", "Render", 
                                               "Utility", "Anarchy", "Mining", "Farming", "Other");
        if (!validCategories.contains(category)) {
            issues.add("Category '" + category + "' is not recognized");
        }
        
        return issues;
    }
    
    /**
     * Checks if this script requires specific permissions
     */
    public boolean requiresPermission(String permission) {
        return permissions.contains(permission);
    }
    
    /**
     * Checks if this script depends on another script
     */
    public boolean dependsOn(String dependency) {
        return dependencies.contains(dependency);
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getVersion() { return version; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public List<String> getDependencies() { return new ArrayList<>(dependencies); }
    public List<String> getPermissions() { return new ArrayList<>(permissions); }
    public long getLastModified() { return lastModified; }
    
    // Setters (for programmatic creation)
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setVersion(String version) { this.version = version; }
    public void setAuthor(String author) { this.author = author; }
    public void setCategory(String category) { this.category = category; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }
    
    /**
     * Generates metadata comment block for insertion into script
     */
    public String generateMetadataComment() {
        StringBuilder sb = new StringBuilder();
        sb.append("--[[\n");
        sb.append("@name ").append(name).append("\n");
        sb.append("@description ").append(description).append("\n");
        sb.append("@version ").append(version).append("\n");
        sb.append("@author ").append(author).append("\n");
        sb.append("@category ").append(category).append("\n");
        
        if (!dependencies.isEmpty()) {
            sb.append("@dependencies [").append(String.join(", ", dependencies)).append("]\n");
        }
        
        if (!permissions.isEmpty()) {
            sb.append("@permissions [").append(String.join(", ", permissions)).append("]\n");
        }
        
        sb.append("]]--\n");
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("ScriptMetadata{name='%s', version='%s', author='%s', category='%s'}", 
                           name, version, author, category);
    }
} 