package adris.altoclef.scripting.dependency;

/**
 * Represents an issue with script dependencies
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class DependencyIssue {
    public enum Type {
        MISSING_SCRIPT,
        MISSING_DEPENDENCY, 
        CIRCULAR_DEPENDENCY,
        VERSION_CONFLICT,
        RESOLUTION_ERROR
    }
    
    private final String scriptName;
    private final String message;
    private final Type type;
    private final long timestamp;
    
    public DependencyIssue(String scriptName, String message, Type type) {
        this.scriptName = scriptName;
        this.message = message;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getScriptName() {
        return scriptName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Type getType() {
        return type;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getSeverity() {
        switch (type) {
            case MISSING_SCRIPT:
            case CIRCULAR_DEPENDENCY:
                return "ERROR";
            case MISSING_DEPENDENCY:
            case VERSION_CONFLICT:
                return "WARNING";
            case RESOLUTION_ERROR:
                return "CRITICAL";
            default:
                return "INFO";
        }
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", getSeverity(), scriptName, message);
    }
} 