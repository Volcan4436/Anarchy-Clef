package adris.altoclef.scripting.security;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Security sandbox for Lua scripts to prevent malicious or resource-intensive operations
 * Removes dangerous functions, limits resource usage, and enforces execution timeouts
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class ScriptSandbox {
    private final ExecutorService executorService;
    private final long maxMemoryMB = 64;
    private final MemoryMonitor memoryMonitor;
    
    public ScriptSandbox() {
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "LuaScript-Thread");
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1); // Lower priority than main game thread
            return t;
        });
        this.memoryMonitor = new MemoryMonitor();
    }
    
    /**
     * Applies security restrictions to Lua globals
     */
    public void applySecurityRestrictions(Globals globals) {
        // Remove potentially dangerous functions
        removeUnsafeFunctions(globals);
        
        // Limit resource access
        limitResourceAccess(globals);
        
        // Install memory monitoring
        installMemoryMonitor(globals);
        
        // Add custom security functions
        addSecurityFunctions(globals);
    }
    
    /**
     * Removes functions that could be used maliciously or cause security issues
     */
    private void removeUnsafeFunctions(Globals globals) {
        // File system access
        globals.set("dofile", LuaValue.NIL);
        globals.set("loadfile", LuaValue.NIL);
        globals.set("require", LuaValue.NIL);
        
        // Dynamic code loading
        globals.set("load", LuaValue.NIL);
        globals.set("loadstring", LuaValue.NIL);
        
        // I/O operations
        LuaValue io = globals.get("io");
        if (!io.isnil() && io.istable()) {
            LuaTable ioTable = io.checktable();
            ioTable.set("open", LuaValue.NIL);
            ioTable.set("popen", LuaValue.NIL);
            ioTable.set("tmpfile", LuaValue.NIL);
            ioTable.set("input", LuaValue.NIL);
            ioTable.set("output", LuaValue.NIL);
        }
        
        // OS operations
        LuaValue os = globals.get("os");
        if (!os.isnil() && os.istable()) {
            LuaTable osTable = os.checktable();
            osTable.set("execute", LuaValue.NIL);
            osTable.set("exit", LuaValue.NIL);
            osTable.set("remove", LuaValue.NIL);
            osTable.set("rename", LuaValue.NIL);
            osTable.set("tmpname", LuaValue.NIL);
            osTable.set("getenv", LuaValue.NIL);
            osTable.set("setlocale", LuaValue.NIL);
        }
        
        // Debug access (can be used for introspection attacks)
        globals.set("debug", LuaValue.NIL);
        
        // Package system (loading external modules)
        LuaValue packageLib = globals.get("package");
        if (!packageLib.isnil()) {
            globals.set("package", LuaValue.NIL);
        }
        
        // Coroutines that could be used for infinite loops
        LuaValue coroutine = globals.get("coroutine");
        if (!coroutine.isnil() && coroutine.istable()) {
            LuaTable coroutineTable = coroutine.checktable();
            // Keep basic coroutine functions but monitor them
            // Could add monitoring here if needed
        }
    }
    
    /**
     * Limits access to resource-intensive operations
     */
    private void limitResourceAccess(Globals globals) {
        // Override string operations to prevent excessive memory usage
        LuaValue string = globals.get("string");
        if (!string.isnil() && string.istable()) {
            LuaTable stringTable = string.checktable();
            
            // Wrap rep function to limit repetitions
            LuaValue originalRep = stringTable.get("rep");
            stringTable.set("rep", new ThreeArgFunction() {
                @Override
                public LuaValue call(LuaValue str, LuaValue count, LuaValue sep) {
                    int n = count.checkint();
                    if (n > 10000) { // Limit string repetitions
                        throw new LuaError("String repetition limit exceeded (max: 10000)");
                    }
                    if (str.tojstring().length() * n > 1000000) { // Limit total string size
                        throw new LuaError("String size limit exceeded (max: 1MB)");
                    }
                    return originalRep.call(str, count, sep);
                }
            });
        }
        
        // Limit table operations
        LuaValue table = globals.get("table");
        if (!table.isnil() && table.istable()) {
            // Could add monitoring for large table operations here
        }
    }
    
    /**
     * Installs memory monitoring functions
     */
    private void installMemoryMonitor(Globals globals) {
        // Add memory checking function that scripts can call
        globals.set("__checkMemory", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                if (memoryMonitor.isMemoryExceeded()) {
                    throw new LuaError("Script memory limit exceeded");
                }
                return LuaValue.NIL;
            }
        });
        
        // Add function to get current memory usage
        globals.set("__getMemoryUsage", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(memoryMonitor.getCurrentMemoryUsage());
            }
        });
    }
    
    /**
     * Adds custom security functions
     */
    private void addSecurityFunctions(Globals globals) {
        // Add sandbox info function
        globals.set("__sandboxInfo", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                LuaTable info = new LuaTable();
                info.set("maxMemoryMB", LuaValue.valueOf(maxMemoryMB));
                info.set("currentMemoryMB", LuaValue.valueOf(memoryMonitor.getCurrentMemoryUsage() / (1024 * 1024)));
                info.set("sandboxed", LuaValue.TRUE);
                return info;
            }
        });
        
        // Add safe sleep function (uses yield instead of actual sleep)
        globals.set("safeSleep", new org.luaj.vm2.lib.OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                // Instead of actually sleeping, just yield
                // This prevents scripts from blocking the main thread
                return LuaValue.valueOf(0);
            }
        });
    }
    
    /**
     * Executes a function with timeout protection
     */
    public <T> T executeWithTimeout(Supplier<T> operation, long timeoutMs) throws TimeoutException {
        Future<T> future = executorService.submit(() -> {
            long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            
            try {
                T result = operation.get();
                
                // Check memory usage after execution
                long currentMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                long usedMemory = currentMemory - startMemory;
                
                if (usedMemory > maxMemoryMB * 1024 * 1024) {
                    throw new SecurityException("Script memory usage exceeded limit: " + 
                                              usedMemory / (1024*1024) + "MB (max: " + maxMemoryMB + "MB)");
                }
                
                return result;
                
            } finally {
                // Suggest garbage collection after script execution
                if (Runtime.getRuntime().freeMemory() < Runtime.getRuntime().totalMemory() * 0.3) {
                    System.gc();
                }
            }
        });
        
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Script execution interrupted", e);
        } catch (ExecutionException e) {
            future.cancel(true);
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                throw new RuntimeException("Script execution failed", cause);
            }
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new TimeoutException("Script execution timeout after " + timeoutMs + "ms");
        }
    }
    
    /**
     * Validates that a script doesn't contain obviously malicious patterns
     */
    public boolean validateScript(String sourceCode) {
        if (sourceCode == null) return false;
        
        // Check for suspicious patterns
        String[] suspiciousPatterns = {
            "Runtime.getRuntime()",
            "System.exit",
            "ProcessBuilder",
            "java.io.File",
            "java.net.",
            "javax.net.",
            "java.lang.reflect",
            "sun\\.", 
            "com\\.sun\\.",
            "getClass\\(\\)",
            "forName\\(",
        };
        
        String lowerCode = sourceCode.toLowerCase();
        for (String pattern : suspiciousPatterns) {
            if (lowerCode.contains(pattern.toLowerCase())) {
                return false;
            }
        }
        
        // Check for infinite loop patterns
        if (lowerCode.contains("while true") && !lowerCode.contains("break")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Shuts down the sandbox executor
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Memory monitoring utility
     */
    private static class MemoryMonitor {
        private final long maxMemoryBytes = 64 * 1024 * 1024; // 64MB
        
        public boolean isMemoryExceeded() {
            long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            return usedMemory > maxMemoryBytes;
        }
        
        public long getCurrentMemoryUsage() {
            return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        }
        
        public double getMemoryUsagePercentage() {
            long usedMemory = getCurrentMemoryUsage();
            return (double) usedMemory / maxMemoryBytes * 100.0;
        }
    }
} 