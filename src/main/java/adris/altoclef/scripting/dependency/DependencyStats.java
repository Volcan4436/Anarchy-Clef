package adris.altoclef.scripting.dependency;

/**
 * Contains statistics about the dependency management system
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class DependencyStats {
    private final int totalScripts;
    private final int loadedScripts;
    private final int scriptsWithDependencies;
    
    public DependencyStats(int totalScripts, int loadedScripts, int scriptsWithDependencies) {
        this.totalScripts = totalScripts;
        this.loadedScripts = loadedScripts;
        this.scriptsWithDependencies = scriptsWithDependencies;
    }
    
    /**
     * Gets the total number of registered scripts
     */
    public int getTotalScripts() {
        return totalScripts;
    }
    
    /**
     * Gets the number of currently loaded scripts
     */
    public int getLoadedScripts() {
        return loadedScripts;
    }
    
    /**
     * Gets the number of scripts that have dependencies
     */
    public int getScriptsWithDependencies() {
        return scriptsWithDependencies;
    }
    
    /**
     * Gets the number of unloaded scripts
     */
    public int getUnloadedScripts() {
        return totalScripts - loadedScripts;
    }
    
    /**
     * Gets the percentage of scripts that are loaded
     */
    public double getLoadedPercentage() {
        if (totalScripts == 0) return 0.0;
        return (double) loadedScripts / totalScripts * 100.0;
    }
    
    /**
     * Gets the percentage of scripts that have dependencies
     */
    public double getDependencyPercentage() {
        if (totalScripts == 0) return 0.0;
        return (double) scriptsWithDependencies / totalScripts * 100.0;
    }
    
    /**
     * Gets a formatted summary of the statistics
     */
    public String getSummary() {
        return String.format(
            "Scripts: %d total, %d loaded (%.1f%%), %d with dependencies (%.1f%%)",
            totalScripts, loadedScripts, getLoadedPercentage(), 
            scriptsWithDependencies, getDependencyPercentage()
        );
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
} 