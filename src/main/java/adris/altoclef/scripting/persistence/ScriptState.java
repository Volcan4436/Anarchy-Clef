package adris.altoclef.scripting.persistence;

import java.util.Map;

/**
 * Represents the persisted state of a script
 * 
 * @author Hearty
 */
public class ScriptState {
    private final String scriptName;
    private final boolean running;
    private final long lastModified;
    private final Map<String, Object> metadata;
    
    public ScriptState(String scriptName, boolean running, long lastModified, Map<String, Object> metadata) {
        this.scriptName = scriptName;
        this.running = running;
        this.lastModified = lastModified;
        this.metadata = metadata;
    }
    
    public String getScriptName() {
        return scriptName;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public long getLastModified() {
        return lastModified;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    @Override
    public String toString() {
        return "ScriptState{" +
                "scriptName='" + scriptName + '\'' +
                ", running=" + running +
                ", lastModified=" + lastModified +
                ", metadata=" + metadata +
                '}';
    }
} 