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
     * Logs detailed error information with comprehensive debugging info
     */
    private void logDetailedError(String scriptName, String context, Throwable error, int errorCount) {
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        String errorType = error.getClass().getSimpleName();
        String message = error.getMessage() != null ? error.getMessage() : "No message";
        
        // Enhanced main error log with more context
        String mainLog = String.format(
            "╔══════════════════════════════════════════════════════════════════════════════\n" +
            "║ 🔥 LUA SCRIPT ERROR #%d - %s\n" +
            "║ Script: '%s'\n" +
            "║ Context: %s\n" +
            "║ Error Type: %s\n" +
            "║ Message: %s\n" +
            "╚══════════════════════════════════════════════════════════════════════════════", 
            errorCount, timestamp, scriptName, context, errorType, message
        );
        mod.logWarning(mainLog, MessagePriority.TIMELY);
        
        // Enhanced Lua error handling with line numbers and function context
        if (error instanceof LuaError) {
            logEnhancedLuaError(scriptName, (LuaError) error);
        }
        
        // Log full Java stack trace for non-Lua errors (but formatted nicely)
        if (!(error instanceof LuaError)) {
            logEnhancedJavaError(scriptName, error);
        }
        
        // Log system state for debugging
        logSystemState(scriptName, context);
        
        // Separator for readability
        mod.log("════════════════════════════════════════════════════════════════════════════════");
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
     * Logs enhanced Lua error information with line numbers and context
     */
    private void logEnhancedLuaError(String scriptName, LuaError luaError) {
        mod.log("┌─ 🔍 LUA ERROR DETAILS:");
        
        // Extract line number and function information from Lua error
        String errorMessage = luaError.getMessage();
        String lineInfo = extractLineInfo(errorMessage);
        String functionInfo = extractFunctionInfo(errorMessage);
        
        if (!lineInfo.isEmpty()) {
            mod.log("│ 📍 Location: " + lineInfo);
        }
        
        if (!functionInfo.isEmpty()) {
            mod.log("│ 🎯 Function: " + functionInfo);
        }
        
        // Get detailed Lua stack trace
        String detailedTrace = getEnhancedLuaStackTrace(luaError);
        if (!detailedTrace.isEmpty()) {
            mod.log("│ 📚 Lua Stack Trace:");
            String[] traceLines = detailedTrace.split("\n");
            for (String line : traceLines) {
                if (!line.trim().isEmpty()) {
                    mod.log("│   " + line.trim());
                }
            }
        }
        
        // Try to extract variable context from error
        String variableContext = extractVariableContext(errorMessage);
        if (!variableContext.isEmpty()) {
            mod.log("│ 📊 Variable Context: " + variableContext);
        }
        
        mod.log("└─ End Lua Error Details");
    }
    
    /**
     * Logs enhanced Java error information
     */
    private void logEnhancedJavaError(String scriptName, Throwable error) {
        mod.log("┌─ ☕ JAVA ERROR DETAILS:");
        
        // Get the filtered stack trace
        String filteredTrace = getFilteredStackTrace(error);
        if (!filteredTrace.isEmpty()) {
            mod.log("│ 📚 Java Stack Trace:");
            String[] traceLines = filteredTrace.split("\\|");
            for (String line : traceLines) {
                if (!line.trim().isEmpty()) {
                    mod.log("│   " + line.trim());
                }
            }
        }
        
        // Show cause chain if present
        Throwable cause = error.getCause();
        int causeLevel = 1;
        while (cause != null && causeLevel <= 3) {
            mod.log("│ 🔗 Caused by (#" + causeLevel + "): " + cause.getClass().getSimpleName() + 
                   " - " + (cause.getMessage() != null ? cause.getMessage() : "No message"));
            cause = cause.getCause();
            causeLevel++;
        }
        
        mod.log("└─ End Java Error Details");
    }
    
    /**
     * Logs current system state for debugging context
     */
    private void logSystemState(String scriptName, String context) {
        mod.log("┌─ 🖥️ SYSTEM STATE:");
        
        try {
            // Player state
            if (mod.getPlayer() != null) {
                mod.log("│ 🏃 Player: " +
                       "Health=" + String.format("%.1f", mod.getPlayer().getHealth()) +
                       ", Hunger=" + mod.getPlayer().getHungerManager().getFoodLevel() +
                       ", InGame=" + AltoClef.inGame());
                
                mod.log("│ 📍 Position: " +
                       String.format("(%.1f, %.1f, %.1f)", 
                                   mod.getPlayer().getX(), 
                                   mod.getPlayer().getY(), 
                                   mod.getPlayer().getZ()));
            } else {
                mod.log("│ 🏃 Player: NULL");
            }
            
            // Memory usage
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory() / 1024 / 1024; // MB
            long totalMemory = runtime.totalMemory() / 1024 / 1024; // MB
            long freeMemory = runtime.freeMemory() / 1024 / 1024; // MB
            long usedMemory = totalMemory - freeMemory;
            
            mod.log("│ 💾 Memory: " +
                   "Used=" + usedMemory + "MB" +
                   ", Free=" + freeMemory + "MB" +
                   ", Max=" + maxMemory + "MB");
            
            // Script engine state
            if (mod.getScriptEngine() != null) {
                int loadedScripts = mod.getScriptEngine().getLoadedScriptCount();
                mod.log("│ 📜 Scripts: " + loadedScripts + " loaded");
            }
            
            // Thread information
            Thread currentThread = Thread.currentThread();
            mod.log("│ 🧵 Thread: " + currentThread.getName() + 
                   " (Priority: " + currentThread.getPriority() + ")");
            
        } catch (Exception e) {
            mod.log("│ ❌ Error getting system state: " + e.getMessage());
        }
        
        mod.log("└─ End System State");
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
     * Gets enhanced Lua stack trace with better formatting
     */
    private String getEnhancedLuaStackTrace(LuaError luaError) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        luaError.printStackTrace(pw);
        String fullTrace = sw.toString();
        
        StringBuilder enhanced = new StringBuilder();
        String[] lines = fullTrace.split("\n");
        
        for (String line : lines) {
            // Format Lua-specific stack trace lines
            if (line.contains("luaj") || line.contains("LuaError") || line.contains(".lua:")) {
                // Clean up the line for better readability
                String cleaned = line.trim()
                    .replace("org.luaj.vm2.", "")
                    .replace("adris.altoclef.scripting.", "");
                
                enhanced.append(cleaned).append("\n");
            }
        }
        
        return enhanced.toString();
    }
    
    /**
     * Extracts line number information from Lua error message
     */
    private String extractLineInfo(String errorMessage) {
        // Look for patterns like "script_name:line_number:" or "line line_number"
        if (errorMessage.contains(":")) {
            String[] parts = errorMessage.split(":");
            for (int i = 0; i < parts.length - 1; i++) {
                try {
                    Integer.parseInt(parts[i + 1].trim().split("\\s+")[0]);
                    return parts[i] + ":" + parts[i + 1].trim().split("\\s+")[0];
                } catch (NumberFormatException e) {
                    // Continue searching
                }
            }
        }
        
        // Look for "line X" pattern
        if (errorMessage.toLowerCase().contains("line ")) {
            String[] words = errorMessage.split("\\s+");
            for (int i = 0; i < words.length - 1; i++) {
                if (words[i].toLowerCase().equals("line")) {
                    try {
                        Integer.parseInt(words[i + 1]);
                        return "Line " + words[i + 1];
                    } catch (NumberFormatException e) {
                        // Continue searching
                    }
                }
            }
        }
        
        return "";
    }
    
    /**
     * Extracts function information from Lua error message
     */
    private String extractFunctionInfo(String errorMessage) {
        // Look for function names in common error patterns
        if (errorMessage.contains("in function")) {
            String[] parts = errorMessage.split("in function");
            if (parts.length > 1) {
                String funcPart = parts[1].trim();
                // Extract function name (remove quotes and extra text)
                funcPart = funcPart.replaceAll("['\"<>]", "").split("\\s+")[0];
                return funcPart;
            }
        }
        
        // Look for "attempt to call" errors
        if (errorMessage.contains("attempt to call")) {
            String[] parts = errorMessage.split("attempt to call");
            if (parts.length > 1) {
                String callPart = parts[1].trim();
                return "attempted to call: " + callPart.split("\\s+")[0];
            }
        }
        
        return "";
    }
    
    /**
     * Extracts variable context from Lua error message
     */
    private String extractVariableContext(String errorMessage) {
        // Look for variable names in common error patterns
        if (errorMessage.contains("nil value")) {
            return "variable is nil";
        }
        
        if (errorMessage.contains("attempt to index")) {
            String[] parts = errorMessage.split("attempt to index");
            if (parts.length > 1) {
                return "indexing issue: " + parts[1].trim();
            }
        }
        
        if (errorMessage.contains("attempt to perform")) {
            String[] parts = errorMessage.split("attempt to perform");
            if (parts.length > 1) {
                return "operation issue: " + parts[1].trim();
            }
        }
        
        return "";
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