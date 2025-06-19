package adris.altoclef.scripting.persistence;

/**
 * Contains statistical information about a script
 * 
 * @author Hearty
 */
public class ScriptStatistics {
    private final String scriptName;
    private final long lastModified;
    private final boolean isRunning;
    private final int configurationCount;
    private final int dataItemCount;
    
    public ScriptStatistics(String scriptName, long lastModified, boolean isRunning, 
                          int configurationCount, int dataItemCount) {
        this.scriptName = scriptName;
        this.lastModified = lastModified;
        this.isRunning = isRunning;
        this.configurationCount = configurationCount;
        this.dataItemCount = dataItemCount;
    }
    
    public String getScriptName() {
        return scriptName;
    }
    
    public long getLastModified() {
        return lastModified;
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public int getConfigurationCount() {
        return configurationCount;
    }
    
    public int getDataItemCount() {
        return dataItemCount;
    }
    
    @Override
    public String toString() {
        return "ScriptStatistics{" +
                "scriptName='" + scriptName + '\'' +
                ", lastModified=" + lastModified +
                ", isRunning=" + isRunning +
                ", configurationCount=" + configurationCount +
                ", dataItemCount=" + dataItemCount +
                '}';
    }
} 