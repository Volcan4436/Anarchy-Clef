package adris.altoclef.scripting.dependency;

import java.util.*;

/**
 * Contains the result of dependency resolution for a script
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class DependencyResolution {
    private List<String> loadingOrder = new ArrayList<>();
    private Set<String> requiredScripts = new HashSet<>();
    private List<DependencyIssue> issues = new ArrayList<>();
    private boolean successful = true;
    
    public DependencyResolution() {
    }
    
    /**
     * Gets the order in which scripts should be loaded
     */
    public List<String> getLoadingOrder() {
        return new ArrayList<>(loadingOrder);
    }
    
    /**
     * Sets the loading order
     */
    public void setLoadingOrder(List<String> loadingOrder) {
        this.loadingOrder = new ArrayList<>(loadingOrder);
    }
    
    /**
     * Gets all scripts required for the operation
     */
    public Set<String> getRequiredScripts() {
        return new HashSet<>(requiredScripts);
    }
    
    /**
     * Sets the required scripts
     */
    public void setRequiredScripts(Set<String> requiredScripts) {
        this.requiredScripts = new HashSet<>(requiredScripts);
    }
    
    /**
     * Gets all dependency issues found
     */
    public List<DependencyIssue> getIssues() {
        return new ArrayList<>(issues);
    }
    
    /**
     * Sets the dependency issues
     */
    public void setIssues(List<DependencyIssue> issues) {
        this.issues = new ArrayList<>(issues);
        updateSuccessStatus();
    }
    
    /**
     * Adds a dependency issue
     */
    public void addIssue(DependencyIssue issue) {
        this.issues.add(issue);
        updateSuccessStatus();
    }
    
    /**
     * Checks if dependency resolution was successful
     */
    public boolean isSuccessful() {
        return successful;
    }
    
    /**
     * Checks if there are any critical issues
     */
    public boolean hasCriticalIssues() {
        return issues.stream().anyMatch(issue -> 
            issue.getType() == DependencyIssue.Type.MISSING_SCRIPT ||
            issue.getType() == DependencyIssue.Type.CIRCULAR_DEPENDENCY ||
            issue.getType() == DependencyIssue.Type.RESOLUTION_ERROR
        );
    }
    
    /**
     * Checks if there are any warnings
     */
    public boolean hasWarnings() {
        return issues.stream().anyMatch(issue -> 
            issue.getType() == DependencyIssue.Type.MISSING_DEPENDENCY ||
            issue.getType() == DependencyIssue.Type.VERSION_CONFLICT
        );
    }
    
    /**
     * Gets issues by type
     */
    public List<DependencyIssue> getIssuesByType(DependencyIssue.Type type) {
        return issues.stream()
                .filter(issue -> issue.getType() == type)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * Gets a summary of the resolution
     */
    public String getSummary() {
        if (successful) {
            return String.format("Dependency resolution successful. %d scripts to load in order: %s", 
                                loadingOrder.size(), String.join(" -> ", loadingOrder));
        } else {
            return String.format("Dependency resolution failed. %d issues found: %s", 
                                issues.size(), issues.toString());
        }
    }
    
    /**
     * Updates success status based on issues
     */
    private void updateSuccessStatus() {
        this.successful = !hasCriticalIssues();
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
} 