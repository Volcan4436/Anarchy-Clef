package adris.altoclef.scripting.security;

import adris.altoclef.AltoClef;
import adris.altoclef.ui.MessagePriority;
import org.luaj.vm2.LuaError;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles and reports script errors with rate limiting and categorization
 * Prevents error spam while providing useful debugging information
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class ScriptErrorHandler {
    private final AltoClef mod;
    private final ConcurrentHashMap<String, ScriptErrorInfo> scriptErrors;
    private final ConcurrentHashMap<String, AtomicInteger> errorCounts;
    private final List<ErrorRecord> recentErrors;
    private final int maxRecentErrors = 100;
    private final int maxErrorsPerScript = 10; // Max errors before disabling script
    
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public ScriptErrorHandler(AltoClef mod) {
        this.mod = mod;
        this.scriptErrors = new ConcurrentHashMap<>();
        this.errorCounts = new ConcurrentHashMap<>();
        this.recentErrors = new ArrayList<>();
    }
    
    /**
     * Handles a script error with context and rate limiting
     */
    public void handleScriptError(String scriptName, String context, Throwable error) {
        // Get or create error count for this script
        AtomicInteger count = errorCounts.computeIfAbsent(scriptName, k -> new AtomicInteger(0));
        int errorCount = count.incrementAndGet();
        
        // Create error record
        ErrorRecord record = new ErrorRecord(scriptName, context, error, errorCount);
        addErrorRecord(record);
        
        // Rate limiting - don't spam console with same errors
        String errorKey = scriptName + ":" + error.getClass().getSimpleName();
        ScriptErrorInfo errorInfo = scriptErrors.get(errorKey);
        
        if (errorInfo == null) {
            // First occurrence of this error type
            errorInfo = new ScriptErrorInfo(error, System.currentTimeMillis());
            scriptErrors.put(errorKey, errorInfo);
            
            // Log detailed error info
            logDetailedError(scriptName, context, error, errorCount);
            
        } else {
            // Update existing error info
            errorInfo.incrementCount();
            errorInfo.setLastOccurrence(System.currentTimeMillis());
            
            // Only log if enough time has passed or if it's a different error
            long timeSinceLastLog = System.currentTimeMillis() - errorInfo.getLastLogTime();
            if (timeSinceLastLog > 30000) { // 30 seconds
                logSummaryError(scriptName, context, error, errorInfo.getCount());
                errorInfo.setLastLogTime(System.currentTimeMillis());
            }
        }
        
        // Disable script if too many errors
        if (errorCount >= maxErrorsPerScript) {
            mod.logWarning(String.format(
                "Script '%s' disabled due to excessive errors (%d)", 
                scriptName, errorCount), MessagePriority.ASAP);
        }
    }
    
    /**
     * Handles syntax errors specifically
     */
    public void handleSyntaxError(String scriptName, String message) {
        String fullMessage = String.format("Syntax error in script '%s': %s", scriptName, message);
        mod.logWarning(fullMessage, MessagePriority.TIMELY);
        
        // Add to error records
        ErrorRecord record = new ErrorRecord(scriptName, "Syntax Validation", 
                                           new RuntimeException(message), 1);
        addErrorRecord(record);
    }
    
    /**
     * Handles performance warnings
     */
    public void handlePerformanceWarning(String scriptName, String message) {
        String fullMessage = String.format("Performance warning for script '%s': %s", scriptName, message);
        mod.logWarning(fullMessage, MessagePriority.OPTIONAL);
        
        // Could implement performance tracking here
    }
    
    /**
     * Logs detailed error information
     */
    private void logDetailedError(String scriptName, String context, Throwable error, int errorCount) {
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        String errorType = error.getClass().getSimpleName();
        String message = error.getMessage() != null ? error.getMessage() : "No message";
        
        // Log main error
        String mainLog = String.format(
            "[%s] Script Error #%d in '%s' (%s): %s - %s", 
            timestamp, errorCount, scriptName, context, errorType, message
        );
        mod.logWarning(mainLog, MessagePriority.TIMELY);
        
        // Log stack trace for Lua errors (filtered for readability)
        if (error instanceof LuaError) {
            String stackTrace = getFilteredStackTrace(error);
            if (!stackTrace.isEmpty()) {
                mod.log("[Script] Stack trace: " + stackTrace);
            }
        }
    }
    
    /**
     * Logs summary error information for repeated errors
     */
    private void logSummaryError(String scriptName, String context, Throwable error, int count) {
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        String errorType = error.getClass().getSimpleName();
        
        String summaryLog = String.format(
            "[%s] Script '%s' error (%s) occurred %d times in last 30s", 
            timestamp, scriptName, errorType, count
        );
        mod.logWarning(summaryLog, MessagePriority.OPTIONAL);
    }
    
    /**
     * Adds an error record to the recent errors list
     */
    private synchronized void addErrorRecord(ErrorRecord record) {
        recentErrors.add(record);
        
        // Keep only recent errors
        while (recentErrors.size() > maxRecentErrors) {
            recentErrors.remove(0);
        }
    }
    
    /**
     * Gets filtered stack trace that's useful for script debugging
     */
    private String getFilteredStackTrace(Throwable error) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);
        String fullTrace = sw.toString();
        
        // Filter out irrelevant parts of the stack trace
        String[] lines = fullTrace.split("\n");
        StringBuilder filtered = new StringBuilder();
        
        for (String line : lines) {
            // Keep Lua-related stack traces and our script classes
            if (line.contains("luaj") || 
                line.contains("altoclef.scripting") || 
                line.contains("LuaError") ||
                (line.contains("at ") && line.contains(".lua:"))) {
                
                if (filtered.length() > 0) filtered.append(" | ");
                filtered.append(line.trim());
            }
        }
        
        return filtered.toString();
    }
    
    /**
     * Gets recent errors for a specific script
     */
    public List<ErrorRecord> getRecentErrors(String scriptName) {
        List<ErrorRecord> scriptErrors = new ArrayList<>();
        synchronized (this) {
            for (ErrorRecord record : recentErrors) {
                if (record.getScriptName().equals(scriptName)) {
                    scriptErrors.add(record);
                }
            }
        }
        return scriptErrors;
    }
    
    /**
     * Gets all recent errors
     */
    public List<ErrorRecord> getAllRecentErrors() {
        synchronized (this) {
            return new ArrayList<>(recentErrors);
        }
    }
    
    /**
     * Gets error count for a script
     */
    public int getErrorCount(String scriptName) {
        AtomicInteger count = errorCounts.get(scriptName);
        return count != null ? count.get() : 0;
    }
    
    /**
     * Resets error count for a script (when script is reloaded)
     */
    public void resetErrorCount(String scriptName) {
        errorCounts.remove(scriptName);
        scriptErrors.entrySet().removeIf(entry -> entry.getKey().startsWith(scriptName + ":"));
    }
    
    /**
     * Checks if a script should be disabled due to errors
     */
    public boolean shouldDisableScript(String scriptName) {
        return getErrorCount(scriptName) >= maxErrorsPerScript;
    }
    
    /**
     * Clears old error records
     */
    public void clearOldErrors() {
        long cutoffTime = System.currentTimeMillis() - (60 * 60 * 1000); // 1 hour ago
        
        synchronized (this) {
            recentErrors.removeIf(record -> record.getTimestamp() < cutoffTime);
        }
        
        // Clear old error info
        scriptErrors.entrySet().removeIf(entry -> 
            entry.getValue().getLastOccurrence() < cutoffTime);
    }
    
    /**
     * Information about a specific type of error for a script
     */
    private static class ScriptErrorInfo {
        private final Throwable error;
        private final long firstOccurrence;
        private long lastOccurrence;
        private long lastLogTime;
        private int count = 1;
        
        public ScriptErrorInfo(Throwable error, long timestamp) {
            this.error = error;
            this.firstOccurrence = timestamp;
            this.lastOccurrence = timestamp;
            this.lastLogTime = timestamp;
        }
        
        public void incrementCount() { count++; }
        public int getCount() { return count; }
        public long getLastOccurrence() { return lastOccurrence; }
        public void setLastOccurrence(long timestamp) { this.lastOccurrence = timestamp; }
        public long getLastLogTime() { return lastLogTime; }
        public void setLastLogTime(long timestamp) { this.lastLogTime = timestamp; }
    }
    
    /**
     * Record of a specific error occurrence
     */
    public static class ErrorRecord {
        private final String scriptName;
        private final String context;
        private final String errorType;
        private final String message;
        private final String stackTrace;
        private final long timestamp;
        private final int errorNumber;
        
        public ErrorRecord(String scriptName, String context, Throwable error, int errorNumber) {
            this.scriptName = scriptName;
            this.context = context;
            this.errorType = error.getClass().getSimpleName();
            this.message = error.getMessage() != null ? error.getMessage() : "No message";
            this.stackTrace = getStackTraceString(error);
            this.timestamp = System.currentTimeMillis();
            this.errorNumber = errorNumber;
        }
        
        private String getStackTraceString(Throwable error) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            error.printStackTrace(pw);
            return sw.toString();
        }
        
        // Getters
        public String getScriptName() { return scriptName; }
        public String getContext() { return context; }
        public String getErrorType() { return errorType; }
        public String getMessage() { return message; }
        public String getStackTrace() { return stackTrace; }
        public long getTimestamp() { return timestamp; }
        public int getErrorNumber() { return errorNumber; }
        
        public String getFormattedTime() {
            return LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(timestamp), 
                java.time.ZoneId.systemDefault()
            ).format(TIME_FORMAT);
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s in %s (%s): %s", 
                                getFormattedTime(), errorType, scriptName, context, message);
        }
    }
} 