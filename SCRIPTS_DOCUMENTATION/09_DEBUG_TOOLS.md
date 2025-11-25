# Debug Tools

**Version:** 1.0.0  
**Status:** Complete  
**Category:** Reference

## Overview

The AltoClef Lua Scripting System includes a comprehensive debugging suite with detailed error logging, performance monitoring, and interactive debugging commands. These tools help you develop, test, and troubleshoot your scripts efficiently.

---

## Table of Contents

1. [Quick Start](#-quick-start)
2. [Enhanced Error Logging](#-enhanced-error-logging)
3. [Debug Commands](#-debug-commands)
4. [Interactive Debugging](#-interactive-debugging)
5. [Development Workflow](#-development-workflow)
6. [Common Issues](#-common-issues)
7. [Best Practices](#-best-practices)

---

## Quick Start

### Essential Debug Commands
```bash
@luadebug help            # Show all available debug commands
@luadebug scripts         # List all loaded scripts and their status
@luadebug errors          # Display recent script errors
@luladebug player         # Show player-specific debug info
@luadebug performance     # Show performance metrics
```

### Quick Error Check
```bash
# Check for script issues
@luadebug errors
@luadebug scripts

# Monitor specific API categories
@luadebug player
@luadebug inventory
@luadebug world
```

---

## Enhanced Error Logging

### **Visual Error Identification**

When a script error occurs, you'll see enhanced error information with visual markers:

```
???????????????????????????????????????????????????????????????????????????????
LUA SCRIPT ERROR #1 - 14:25:30
Script: 'my_script'
Context: Error in onTick
Error Type: LuaError
Message: attempt to index a nil value (global 'invalidVar')
???????????????????????????????????????????????????????????????????????????????
+- LUA ERROR DETAILS:
| Location: my_script:15
| Function: onTick
| Lua Stack Trace:
|   LuaError: my_script:15: attempt to index a nil value
|   at LuaFunction.call(LuaFunction.java:142)
| Variable Context: variable is nil
+- End Lua Error Details
+- SYSTEM STATE:
| Player: Health=20.0, Hunger=18, InGame=true
| Position: (123.5, 64.0, -456.2)
| Memory: Used=245MB, Free=123MB, Max=2048MB
| Scripts: 3 loaded
| Thread: Client thread (Priority: 5)
+- End System State
????????????????????????????????????????????????????????????????????????????????
```

### **Error Management Features**

- **Rate Limiting** - Prevents error spam by summarizing duplicate errors
- **Auto-Disable** - Scripts are disabled after 10 errors to prevent infinite loops
- **Line Numbers** - Lua errors show exact line numbers when possible
- **Function Context** - Error messages include function names where they occurred
- **System State** - Each error logs current player/system state for context
- **Performance Warnings** - Scripts taking >50ms per tick are flagged

---

## **Debug Commands**

### Main Debug Command
```bash
@luadebug <type>
```

### **API Debug Types**

#### Player & Game State
```bash
@luadebug player      # Show jump and velocity control APIs with examples
@luadebug health      # Show health API information and current values
@luadebug hunger      # Show hunger API information and current values
@luadebug position    # Show position API information and coordinates
@luadebug time        # Show time API and current time values
@luadebug world       # Show world API and world information
```

**Example Output:**
```
=== PLAYER DEBUG ===
Jump & Velocity Control:
 AltoClef.isJumping() - returns true/false
 AltoClef.jump() - triggers jump action
 AltoClef.getVelocity() - returns {x, y, z} table
 AltoClef.setVelocity(x, y, z) - set player velocity

Alternative Utils API:
 Utils.Player.isJumping()
 Utils.Player.jump()
 Utils.Player.getVelocity()
 Utils.Player.setVelocity(x, y, z)

Current Status:
 Is Jumping: false
 Velocity: {x: 0.0, y: -0.078, z: 0.0}
 On Ground: true

Example Usage:
 if not AltoClef.isJumping() then
   AltoClef.jump()
 end
```

#### Inventory & Items
```bash
@luadebug inventory   # Show inventory API and item counts
@luadebug items       # Show item API and common item names
@luadebug food        # Show food API and food availability
```

#### World & Blocks
```bash
@luadebug blocks      # Show block API and nearby blocks
```

### **Script Debug Types**

#### Error Analysis
```bash
@luadebug errors      # Show recent script errors with detailed information
```

**Example Output:**
```
=== RECENT SCRIPT ERRORS ===
Last 5 Errors:

Error #1 (2 minutes ago):
 Script: velocity_test
 Function: onTick
 Message: attempt to index a nil value
 Location: line 25
 System: Health=20.0, Position=(45.2, 64.0, -123.8)

Error #2 (5 minutes ago):
 Script: chat_bot  
 Function: handleMessage
 Message: attempt to call field 'chat' (a nil value)
 Location: line 12
 Note: Script auto-disabled after 10 errors

Performance Warnings (Scripts >50ms per tick):
 - mining_script: 67ms average (FLAGGED)
 - auto_farm: 23ms average (OK)

Error Prevention Tips:
 1. Always check AltoClef.isInGame() before API calls
 2. Use pcall() for potentially failing operations
 3. Validate function parameters before use
 4. Add nil checks for optional values
```

#### Script Management
```bash
@luadebug scripts     # Show loaded scripts status and performance
```

**Example Output:**
```
=== LOADED SCRIPTS STATUS ===
Total Scripts: 4
Active Scripts: 3
Disabled Scripts: 1

Script Details:
chat_demo.lua
  Status: Running, 1.2MB memory, 15ms avg tick time
  Lifecycle: onLoad ?, onTick ?, onEnable ?
 
velocity_test.lua
  Status: Running, 0.8MB memory, 8ms avg tick time
  Lifecycle: onLoad ?, onTick ?
 
broken_script.lua
  Status: Disabled (10 errors), last error: 5 minutes ago
  Error: attempt to index a nil value
 
health_monitor.lua
  Status: Running, 0.5MB memory, 3ms avg tick time
  Lifecycle: onLoad ?, onTick ?

Performance Summary:
 Total Memory: 2.5MB
 Average Tick Time: 8.7ms
 Scripts >50ms: 0 (GOOD)
```

#### Performance Monitoring
```bash
@luadebug performance # Show script performance metrics and warnings
```

**Example Output:**
```
=== SCRIPT PERFORMANCE ===
Performance Metrics (Last 60 seconds):

Individual Scripts:
 chat_demo.lua:      avg=12ms, max=45ms, min=2ms
 velocity_test.lua:  avg=8ms,  max=23ms, min=1ms
 health_monitor.lua: avg=3ms,  max=8ms,  min=1ms

System Performance:
 Total Script Time: 23ms/tick (GOOD - <50ms)
 Memory Usage: 2.5MB total, 0.1MB per script average
 Garbage Collection: 15 cycles, 23ms total time
 
Warnings:
 chat_demo.lua occasionally spikes to 45ms
 All other scripts performing well

Recommendations:
 1. Consider throttling heavy operations in chat_demo.lua
 2. Overall performance is good - no action needed
 3. Monitor memory usage if adding more scripts
```

#### Logging Configuration
```bash
@luadebug logs        # Show logging configuration and debugging tips
```

### **Quick Shortcuts**
```bash
@hunger              # Quick hunger debug (shortcut for @luadebug hunger)
@pos                 # Quick position debug (shortcut for @luadebug position)
@inv                 # Quick inventory debug (shortcut for @luadebug inventory)
```

---

## **Interactive Debugging**

### Script Testing Commands
```lua
-- In your scripts, add debug logging
function debugLog(message, level)
   if DEBUG_MODE then
       local prefix = level == "warn" and "?" or level == "error" and "?" or "?"
       AltoClef.log(prefix .. " [DEBUG] " .. message)
   end
end

-- Use in your code
debugLog("Player health: " .. AltoClef.getHealth())
debugLog("Velocity check failed", "warn")
debugLog("Critical error in movement", "error")
```

### Performance Profiling
```lua
-- Profile function execution time
function profileFunction(funcName, func, ...)
   local startTime = os.clock()
   local results = {func(...)}
   local endTime = os.clock()
   local duration = (endTime - startTime) * 1000 -- Convert to milliseconds
   
   if duration > 10 then -- Log if function takes >10ms
       AltoClef.log(string.format("%s took %.2fms", funcName, duration))
   end
   
   return table.unpack(results)
end

-- Usage
local health = profileFunction("getHealth", AltoClef.getHealth)
```

### Memory Usage Tracking
```lua
-- Track memory usage in scripts
local function getMemoryUsage()
   collectgarbage("collect") -- Force garbage collection
   return collectgarbage("count") -- Returns KB used
end

local startMemory = getMemoryUsage()

-- Your script logic here

local endMemory = getMemoryUsage()
local memoryUsed = endMemory - startMemory

if memoryUsed > 100 then -- Alert if >100KB used
   AltoClef.log(string.format("High memory usage: %.1f KB", memoryUsed))
end
```

---

## **Development Workflow**

### 1. **Development Phase**
```bash
# Enable debug mode in your scripts
local DEBUG_MODE = true

# Use comprehensive error handling
function safeApiCall(apiFunc, ...)
   local success, result = pcall(apiFunc, ...)
   if success then
       return result
   else
       AltoClef.logWarning("API call failed: " .. tostring(result))
       return nil
   end
end

# Example usage
local health = safeApiCall(AltoClef.getHealth)
if health then
   -- Use health value
else
   -- Handle error case
end
```

### 2. **Testing Phase**
```bash
# Test all script functions
@luadebug scripts     # Check script status
@luadebug errors      # Look for any errors
@luadebug performance # Check performance impact
```

### 3. **Production Phase**
```bash
# Monitor ongoing performance
@luadebug performance # Regular performance checks
@luadebug errors      # Monitor for new errors

# Disable debug mode for production
local DEBUG_MODE = false
```

---

## **Common Issues & Solutions**

### Issue 1: Script Not Running
**Symptoms:** Script enabled but `onTick()` not called

**Debug Steps:**
```bash
@luadebug scripts     # Check script status
@luadebug errors      # Look for loading errors
```

**Common Solutions:**
- Check for syntax errors in script
- Ensure `onTick()` function is properly defined
- Verify script is actually enabled in UI
- Check enhanced error logs for specific issues

### Issue 2: API Functions Not Found
**Symptoms:** `attempt to call field 'getHealth' (a nil value)`

**Debug Steps:**
```bash
@luadebug health      # Verify API availability
@luadebug player      # Check player API status
```

**Common Solutions:**
- Ensure you're using `AltoClef.getHealth()` not `getHealth()`
- Check if you're in game with `AltoClef.isInGame()`
- Verify API function names are correct

### Issue 3: Performance Issues
**Symptoms:** Game lagging when script is enabled

**Debug Steps:**
```bash
@luadebug performance # Check script performance
@luadebug scripts     # Look for slow scripts
```

**Common Solutions:**
- Add throttling to `onTick()` function
- Avoid expensive operations every tick
- Use `pcall()` for error-prone operations
- Profile function execution times

### Issue 4: Memory Leaks
**Symptoms:** Increasing memory usage over time

**Debug Steps:**
```bash
@luadebug performance # Monitor memory usage
```

**Common Solutions:**
- Clear large tables periodically
- Remove event listeners in `onDisable()`
- Use `collectgarbage()` if needed
- Limit size of data structures

---

## **Best Practices**

### Error Handling
```lua
function robustFunction()
   if not AltoClef.isInGame() then
       return false, "Not in game"
   end
   
   local success, result = pcall(function()
       -- Your potentially failing code here
       return AltoClef.getHealth()
   end)
   
   if success then
       return true, result
   else
       AltoClef.logWarning("Error in robustFunction: " .. tostring(result))
       return false, result
   end
end
```

### Debug Logging
```lua
local DEBUG = true  -- Set to false for production

function debug(message, level)
   if DEBUG then
       local timestamp = os.date("%H:%M:%S")
       local prefix = level == "warn" and "?" or level == "error" and "?" or "?"
       AltoClef.log(string.format("[%s] %s %s", timestamp, prefix, message))
   end
end

-- Usage
debug("Script started")
debug("Low health detected", "warn")  
debug("Critical error occurred", "error")
```

### Performance Monitoring
```lua
local performanceTracker = {
   tickTimes = {},
   maxSamples = 60 -- Track last 60 ticks
}

function onTick()
   local startTime = os.clock()
   
   -- Your script logic here
   
   local tickTime = (os.clock() - startTime) * 1000
   
   -- Track performance
   table.insert(performanceTracker.tickTimes, tickTime)
   if #performanceTracker.tickTimes > performanceTracker.maxSamples then
       table.remove(performanceTracker.tickTimes, 1)
   end
   
   -- Warn if consistently slow
   if tickTime > 50 then
       AltoClef.logWarning(string.format("Slow tick: %.2fms", tickTime))
   end
end

function getAverageTickTime()
   if #performanceTracker.tickTimes == 0 then return 0 end
   
   local total = 0
   for _, time in ipairs(performanceTracker.tickTimes) do
       total = total + time
   end
   
   return total / #performanceTracker.tickTimes
end
```

---

## **Custom Debug Tools**

### Create Custom Debug Commands
```lua
-- Add to your scripts for custom debugging
function onLoad()
   -- Create custom debug command for your script
   AltoClef.createcommand("mydebug", "Show script debug info", function(args)
       AltoClef.chat("=== MY SCRIPT DEBUG ===")
       AltoClef.chat("State: " .. tostring(scriptState))
       AltoClef.chat("Last action: " .. tostring(lastAction))
       AltoClef.chat("Tick count: " .. tostring(tickCount))
       AltoClef.chat("Average performance: " .. string.format("%.2fms", getAverageTickTime()))
   end)
end
```

### Script State Inspection
```lua
local scriptState = {
   initialized = false,
   lastActionTime = 0,
   errorCount = 0,
   tickCount = 0
}

function getScriptDebugInfo()
   return {
       state = scriptState,
       memory = collectgarbage("count"),
       uptime = os.clock() - startTime,
       health = AltoClef.getHealth(),
       position = AltoClef.getPlayerPos()
   }
end
```

---

## **Help Command**

Get help with debugging:

```bash
@luadebug help        # Show all available debug commands and their usage
```

---

## **Advanced Debugging**

### Remote Debugging
```lua
-- Log debug info to external file (when available)
function logToFile(message)
   -- Implementation depends on file system access
   -- For now, use enhanced console logging
   AltoClef.log("" .. message)
end
```

### Script Communication Debugging
```lua
-- Debug script-to-script communication
_G.DebugInfo = _G.DebugInfo or {}
_G.DebugInfo[scriptName] = {
   status = "running",
   lastUpdate = os.clock(),
   data = scriptState
}

-- Check other scripts' debug info
function checkOtherScripts()
   if _G.DebugInfo then
       for script, info in pairs(_G.DebugInfo) do
           AltoClef.log(string.format("Script %s: %s (%.1fs ago)",
               script, info.status, os.clock() - info.lastUpdate))
       end
   end
end
```

---

**The debugging system provides comprehensive tools for developing reliable, high-performance Lua scripts. Use these tools throughout your development process for the best results!** 