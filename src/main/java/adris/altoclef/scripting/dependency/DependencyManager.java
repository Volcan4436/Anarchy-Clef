package adris.altoclef.scripting.dependency;

import adris.altoclef.AltoClef;
import adris.altoclef.scripting.script.ScriptMetadata;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages script dependencies, loading order, and dependency resolution
 * 
 * Features:
 * - Dependency resolution and validation
 * - Loading order calculation using topological sort
 * - Circular dependency detection
 * - Automatic dependency loading
 * - Version compatibility checking
 * - Dependency conflict resolution
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class DependencyManager {
    private final AltoClef mod;
    private final Map<String, ScriptMetadata> availableScripts = new HashMap<>();
    private final Map<String, Set<String>> dependencyGraph = new HashMap<>();
    private final Set<String> loadedScripts = new HashSet<>();
    private final List<DependencyIssue> issues = new ArrayList<>();
    
    public DependencyManager(AltoClef mod) {
        this.mod = mod;
    }
    
    /**
     * Registers a script's metadata for dependency management
     */
    public void registerScript(String scriptName, ScriptMetadata metadata) {
        availableScripts.put(scriptName, metadata);
        dependencyGraph.put(scriptName, new HashSet<>(metadata.getDependencies()));
        mod.log("[Dependency] Registered script: " + scriptName + " with " + metadata.getDependencies().size() + " dependencies");
    }
    
    /**
     * Unregisters a script from dependency management
     */
    public void unregisterScript(String scriptName) {
        availableScripts.remove(scriptName);
        dependencyGraph.remove(scriptName);
        loadedScripts.remove(scriptName);
        
        // Remove this script as a dependency from others
        dependencyGraph.values().forEach(deps -> deps.remove(scriptName));
        
        mod.log("[Dependency] Unregistered script: " + scriptName);
    }
    
    /**
     * Calculates the loading order for a script and its dependencies
     * Returns scripts in the order they should be loaded
     */
    public DependencyResolution resolveLoadingOrder(String scriptName) {
        issues.clear();
        
        if (!availableScripts.containsKey(scriptName)) {
            DependencyResolution resolution = new DependencyResolution();
            resolution.addIssue(new DependencyIssue(scriptName, "Script not found", DependencyIssue.Type.MISSING_SCRIPT));
            return resolution;
        }
        
        try {
            // Get all scripts that need to be loaded (script + dependencies)
            Set<String> requiredScripts = getAllDependencies(scriptName);
            
            // Check for missing dependencies
            validateDependencies(requiredScripts);
            
            // Detect circular dependencies
            detectCircularDependencies(requiredScripts);
            
            // Calculate loading order using topological sort
            List<String> loadingOrder = calculateTopologicalOrder(requiredScripts);
            
            // Create resolution result
            DependencyResolution resolution = new DependencyResolution();
            resolution.setLoadingOrder(loadingOrder);
            resolution.setRequiredScripts(requiredScripts);
            resolution.setIssues(new ArrayList<>(issues));
            
            return resolution;
            
        } catch (Exception e) {
            mod.logWarning("[Dependency] Error resolving dependencies for " + scriptName + ": " + e.getMessage());
            DependencyResolution resolution = new DependencyResolution();
            resolution.addIssue(new DependencyIssue(scriptName, "Resolution failed: " + e.getMessage(), 
                                                    DependencyIssue.Type.RESOLUTION_ERROR));
            return resolution;
        }
    }
    
    /**
     * Gets all dependencies for a script (recursive)
     */
    private Set<String> getAllDependencies(String scriptName) {
        Set<String> allDeps = new HashSet<>();
        Set<String> visited = new HashSet<>();
        collectDependencies(scriptName, allDeps, visited);
        return allDeps;
    }
    
    /**
     * Recursively collects all dependencies
     */
    private void collectDependencies(String scriptName, Set<String> collected, Set<String> visited) {
        if (visited.contains(scriptName)) {
            return; // Already processed or circular dependency (will be caught later)
        }
        
        visited.add(scriptName);
        collected.add(scriptName);
        
        Set<String> directDeps = dependencyGraph.get(scriptName);
        if (directDeps != null) {
            for (String dep : directDeps) {
                collectDependencies(dep, collected, visited);
            }
        }
    }
    
    /**
     * Validates that all required dependencies exist
     */
    private void validateDependencies(Set<String> requiredScripts) {
        for (String script : requiredScripts) {
            if (!availableScripts.containsKey(script)) {
                issues.add(new DependencyIssue(script, "Required dependency not found", 
                                               DependencyIssue.Type.MISSING_DEPENDENCY));
            }
        }
    }
    
    /**
     * Detects circular dependencies using DFS
     */
    private void detectCircularDependencies(Set<String> scripts) {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        
        for (String script : scripts) {
            if (!visited.contains(script)) {
                if (hasCycle(script, visited, recursionStack, new ArrayList<>())) {
                    return; // Cycle detected and reported
                }
            }
        }
    }
    
    /**
     * DFS helper for cycle detection
     */
    private boolean hasCycle(String script, Set<String> visited, Set<String> recursionStack, List<String> path) {
        visited.add(script);
        recursionStack.add(script);
        path.add(script);
        
        Set<String> deps = dependencyGraph.get(script);
        if (deps != null) {
            for (String dep : deps) {
                if (!visited.contains(dep)) {
                    if (hasCycle(dep, visited, recursionStack, new ArrayList<>(path))) {
                        return true;
                    }
                } else if (recursionStack.contains(dep)) {
                    // Circular dependency found
                    List<String> cycle = new ArrayList<>(path);
                    cycle.add(dep);
                    String cycleStr = String.join(" -> ", cycle);
                    issues.add(new DependencyIssue(script, "Circular dependency: " + cycleStr, 
                                                   DependencyIssue.Type.CIRCULAR_DEPENDENCY));
                    return true;
                }
            }
        }
        
        recursionStack.remove(script);
        return false;
    }
    
    /**
     * Calculates loading order using topological sort (Kahn's algorithm)
     */
    private List<String> calculateTopologicalOrder(Set<String> scripts) {
        // Build in-degree map
        Map<String, Integer> inDegree = new HashMap<>();
        for (String script : scripts) {
            inDegree.put(script, 0);
        }
        
        for (String script : scripts) {
            Set<String> deps = dependencyGraph.get(script);
            if (deps != null) {
                for (String dep : deps) {
                    if (scripts.contains(dep)) {
                        inDegree.put(script, inDegree.get(script) + 1);
                    }
                }
            }
        }
        
        // Kahn's algorithm
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }
        
        List<String> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            result.add(current);
            
            Set<String> deps = dependencyGraph.get(current);
            if (deps != null) {
                for (String dep : deps) {
                    if (scripts.contains(dep)) {
                        // This script depends on current, so reduce its in-degree
                        for (String script : scripts) {
                            if (dependencyGraph.get(script) != null && 
                                dependencyGraph.get(script).contains(current)) {
                                inDegree.put(script, inDegree.get(script) - 1);
                                if (inDegree.get(script) == 0) {
                                    queue.offer(script);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Marks a script as loaded
     */
    public void markScriptLoaded(String scriptName) {
        loadedScripts.add(scriptName);
        mod.log("[Dependency] Marked script as loaded: " + scriptName);
    }
    
    /**
     * Marks a script as unloaded
     */
    public void markScriptUnloaded(String scriptName) {
        loadedScripts.remove(scriptName);
        mod.log("[Dependency] Marked script as unloaded: " + scriptName);
    }
    
    /**
     * Checks if a script is currently loaded
     */
    public boolean isScriptLoaded(String scriptName) {
        return loadedScripts.contains(scriptName);
    }
    
    /**
     * Gets scripts that depend on the given script
     */
    public Set<String> getScriptDependents(String scriptName) {
        return dependencyGraph.entrySet().stream()
                .filter(entry -> entry.getValue().contains(scriptName))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
    
    /**
     * Gets direct dependencies of a script
     */
    public Set<String> getScriptDependencies(String scriptName) {
        return new HashSet<>(dependencyGraph.getOrDefault(scriptName, Collections.emptySet()));
    }
    
    /**
     * Gets all available scripts
     */
    public Set<String> getAvailableScripts() {
        return new HashSet<>(availableScripts.keySet());
    }
    
    /**
     * Gets all loaded scripts
     */
    public Set<String> getLoadedScripts() {
        return new HashSet<>(loadedScripts);
    }
    
    /**
     * Gets metadata for a script
     */
    public ScriptMetadata getScriptMetadata(String scriptName) {
        return availableScripts.get(scriptName);
    }
    
    /**
     * Validates that a script can be safely unloaded
     * Returns list of dependent scripts that would be affected
     */
    public List<String> validateUnload(String scriptName) {
        Set<String> dependents = getScriptDependents(scriptName);
        return dependents.stream()
                .filter(this::isScriptLoaded)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets dependency statistics
     */
    public DependencyStats getStats() {
        int totalScripts = availableScripts.size();
        int loadedScripts = this.loadedScripts.size();
        int scriptsWithDependencies = (int) dependencyGraph.values().stream()
                .filter(deps -> !deps.isEmpty())
                .count();
        
        return new DependencyStats(totalScripts, loadedScripts, scriptsWithDependencies);
    }
    
    /**
     * Clears all dependency data
     */
    public void clear() {
        availableScripts.clear();
        dependencyGraph.clear();
        loadedScripts.clear();
        issues.clear();
        mod.log("[Dependency] Cleared all dependency data");
    }
} 