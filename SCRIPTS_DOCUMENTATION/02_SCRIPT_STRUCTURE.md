# Script Structure

**Version:** 1.0.0  
**Status:** ✅ Complete  
**Category:** Getting Started  

## Overview

AltoClef Lua scripts follow a standardized structure with metadata headers, lifecycle functions, and consistent patterns. Understanding this structure is essential for creating reliable, maintainable scripts.

---

## Table of Contents

1. [Quick Start](#-quick-start)
2. [Metadata Header](#-metadata-header)
3. [Lifecycle Functions](#-lifecycle-functions)
4. [Script Template](#️-script-template)
5. [Best Practices](#-best-practices)
6. [File Organization](#-file-organization)
7. [Development Tips](#-development-tips)

---

##  Quick Start

### Essential Template
Use this basic template for all new scripts:

```lua
--[[
@name Your Script Name
@description Brief description of functionality
@version 1.0.0
@author YourName
@category Utility|Mining|Combat|Movement|Debug|Analytics
@dependencies none
]]--

function onLoad()
    -- Called when script file is loaded
    AltoClef.log("Script loaded: " .. (_ENV._SCRIPT_NAME or "Unknown"))
end

function onTick()
    -- Called every game tick (20 times per second)
    if not AltoClef.isInGame() then return end
    -- Your main automation logic here
end

function onEnable()
    -- Called when script is enabled in UI
    AltoClef.log("Script enabled!")
end

function onDisable()
    -- Called when script is disabled in UI  
    AltoClef.log("Script disabled!")
end

function onCleanup()
    -- Called when script is being unloaded
    AltoClef.log("Script cleaning up...")
end
```

---

## Metadata Header

Every script should start with a metadata header that provides information about the script:

```lua
--[[
@name Script Display Name
@description Brief description of what the script does
@version 1.0.0
@author Your Name
@category Utility|Mining|Combat|Movement|Debug|Analytics
@dependencies library1.lua, library2.lua
]]--
```

### Metadata Fields

| Field | Required | Description | Example |
|-------|----------|-------------|---------|
| `@name` | Yes | Display name in script manager | `"Auto Mining Bot"` |
| `@description` | Yes | Brief description of functionality | `"Automatically mines valuable ores"` |
| `@version` | Yes | Version number (semantic versioning) | `"1.2.3"` |
| `@author` | Yes | Script author name | `"YourUsername"` |
| `@category` | Yes | Category for organization | `"Mining"` |
| `@dependencies` | No | Required library files | `"math_helpers.lua"` |

### Category Guidelines

- **Utility** - General purpose tools, monitors, helpers
- **Mining** - Mining automation, ore collection, cave exploration  
- **Combat** - PvP assistance, mob fighting, defense systems
- **Movement** - Advanced movement, parkour, navigation
- **Debug** - Testing tools, debugging utilities, diagnostics
- **Analytics** - Data collection, statistics, performance monitoring

---

## Lifecycle Functions

Scripts can implement these lifecycle functions to handle different events:

### Core Lifecycle Functions

```lua
function onLoad()
    -- Called when script file is loaded
    -- Use for: Variable initialization, setup logging
    AltoClef.log("Script loaded: " .. _ENV._SCRIPT_NAME)
end

function onTick()
    -- Called every game tick (20 times per second)
    -- Use for: Main automation logic, monitoring
    if not AltoClef.isInGame() then return end
    
    -- Your main script logic here
end

function onEnable()
    -- Called when script is enabled in UI
    -- Use for: Starting automation, resetting state
    AltoClef.log("Script enabled!")
end

function onDisable()
    -- Called when script is disabled in UI
    -- Use for: Stopping tasks, cleanup
    AltoClef.log("Script disabled!")
end

function onCleanup()
    -- Called when script is being unloaded
    -- Use for: Final cleanup, resource release
    AltoClef.log("Script cleaning up...")
end
```

### Lifecycle Function Details

#### `onLoad()`
- **When:** Script file is first loaded by the engine
- **Purpose:** One-time initialization, variable setup
- **Best Practice:** Keep it lightweight, just setup variables
- **Always Called:** Yes, regardless of enabled state

#### `onTick()`
- **When:** Every game tick while script is enabled (20 FPS)
- **Purpose:** Main automation logic, continuous monitoring
- **Best Practice:** Add performance throttling for expensive operations
- **Always Called:** Only when script is enabled

#### `onEnable()`
- **When:** Script is enabled in the UI
- **Purpose:** Start automation, reset counters, setup state
- **Best Practice:** Prepare for automation to begin
- **Always Called:** When enabled, even if already loaded

#### `onDisable()`
- **When:** Script is disabled in the UI
- **Purpose:** Stop ongoing tasks, pause automation
- **Best Practice:** Clean up temporary state, cancel tasks
- **Always Called:** When disabled, even if staying loaded

#### `onCleanup()`
- **When:** Script is being unloaded from memory
- **Purpose:** Final resource cleanup, save persistent data
- **Best Practice:** Release all resources, clear global variables
- **Always Called:** Before script is removed from engine

---

## Script Template

Here's a complete template for new scripts:

```lua
--[[
@name Script Template
@description Template for creating new AltoClef scripts
@version 1.0.0
@author YourName
@category Utility
@dependencies none
]]--

-- ============================================================================
-- CONFIGURATION
-- ============================================================================

local CONFIG = {
    ENABLE_DEBUG = true,
    CHECK_INTERVAL = 1000,  -- milliseconds
    MAX_RETRIES = 3
}

-- ============================================================================
-- STATE VARIABLES
-- ============================================================================

local scriptState = {
    initialized = false,
    enabled = false,
    lastCheckTime = 0,
    retryCount = 0,
    statistics = {
        tickCount = 0,
        actionCount = 0,
        errorCount = 0
    }
}

-- ============================================================================
-- UTILITY FUNCTIONS
-- ============================================================================

local function debugLog(message, level)
    if CONFIG.ENABLE_DEBUG then
        local prefix = level == "warn" and "⚠️" or level == "error" and "🚨" or "🔍"
        AltoClef.log(prefix .. " [" .. _ENV._SCRIPT_NAME .. "] " .. message)
    end
end

local function safeApiCall(func, ...)
    local success, result = pcall(func, ...)
    if success then
        return result
    else
        debugLog("API call failed: " .. tostring(result), "error")
        scriptState.statistics.errorCount = scriptState.statistics.errorCount + 1
        return nil
    end
end

local function shouldPerformCheck()
    local currentTime = os.clock() * 1000
    return currentTime - scriptState.lastCheckTime >= CONFIG.CHECK_INTERVAL
end

-- ============================================================================
-- MAIN LOGIC FUNCTIONS
-- ============================================================================

local function initialize()
    debugLog("Initializing script...")
    
    -- Initialize your script-specific state here
    scriptState.initialized = true
    
    debugLog("Script initialization complete")
end

local function performMainLogic()
    -- Add your main automation logic here
    debugLog("Performing main logic")
    
    -- Example: Check player health
    local health = safeApiCall(AltoClef.getHealth)
    if health and health < 10 then
        debugLog("Low health detected: " .. health, "warn")
        -- Handle low health
    end
    
    scriptState.statistics.actionCount = scriptState.statistics.actionCount + 1
end

local function cleanup()
    debugLog("Cleaning up script resources...")
    
    -- Cancel any ongoing tasks
    if safeApiCall(AltoClef.getTaskRunner) then
        AltoClef.cancelUserTask()
    end
    
    -- Reset state
    scriptState.enabled = false
end

-- ============================================================================
-- LIFECYCLE FUNCTIONS
-- ============================================================================

function onLoad()
    debugLog("Script loaded: " .. (_ENV._SCRIPT_NAME or "unknown"))
    
    -- Initialize configuration
    scriptState.lastCheckTime = os.clock() * 1000
    
    debugLog("Script ready for activation")
end

function onTick()
    -- Always check if in-game first
    if not AltoClef.isInGame() then return end
    
    scriptState.statistics.tickCount = scriptState.statistics.tickCount + 1
    
    -- Initialize on first tick if not already done
    if not scriptState.initialized then
        initialize()
        return
    end
    
    -- Only proceed if script is enabled
    if not scriptState.enabled then return end
    
    -- Throttle expensive operations
    if shouldPerformCheck() then
        performMainLogic()
        scriptState.lastCheckTime = os.clock() * 1000
    end
end

function onEnable()
    debugLog("Script enabled")
    scriptState.enabled = true
    scriptState.retryCount = 0
    
    -- Reset statistics
    scriptState.statistics.actionCount = 0
    scriptState.statistics.errorCount = 0
    
    debugLog("Script is now active")
end

function onDisable()
    debugLog("Script disabled")
    scriptState.enabled = false
    
    -- Perform immediate cleanup
    cleanup()
    
    -- Log statistics
    debugLog(string.format("Session stats: %d ticks, %d actions, %d errors", 
        scriptState.statistics.tickCount, 
        scriptState.statistics.actionCount, 
        scriptState.statistics.errorCount))
end

function onCleanup()
    debugLog("Script cleaning up for unload")
    
    -- Final cleanup
    cleanup()
    
    -- Clear global references
    _G[_ENV._SCRIPT_NAME .. "_State"] = nil
    
    debugLog("Script cleanup complete")
end

-- ============================================================================
-- OPTIONAL: CUSTOM COMMANDS
-- ============================================================================

-- Uncomment to add custom commands for your script
--[[
if AltoClef.createcommand then
    AltoClef.createcommand("mystats", "Show script statistics", function(args)
        AltoClef.chat("Script Stats:")
        AltoClef.chat(string.format("  Ticks: %d", scriptState.statistics.tickCount))
        AltoClef.chat(string.format("  Actions: %d", scriptState.statistics.actionCount))
        AltoClef.chat(string.format("  Errors: %d", scriptState.statistics.errorCount))
    end)
end
]]--
```

---

## Best Practices

### ✅ State Management

```lua
-- ✅ Good: Centralized state management
local scriptState = {
    mode = "idle",
    targetPosition = nil,
    lastAction = nil,
    startTime = os.clock()
}

-- ❌ Bad: Scattered global variables
mode = "idle"
targetX = nil
targetY = nil
lastActionTime = nil
```

### Error Handling

```lua
-- ✅ Good: Comprehensive error handling
function safeOperation()
    if not AltoClef.isInGame() then
        return false, "Not in game"
    end
    
    local success, result = pcall(function()
        return AltoClef.getHealth()
    end)
    
    if success then
        return true, result
    else
        AltoClef.logWarning("Health check failed: " .. tostring(result))
        return false, result
    end
end

-- ❌ Bad: No error handling
function unsafeOperation()
    local health = AltoClef.getHealth() -- Could crash if not in game
    return health
end
```

### Performance Optimization

```lua
-- ✅ Good: Throttled operations
local lastExpensiveCheck = 0
local EXPENSIVE_CHECK_INTERVAL = 5000 -- 5 seconds

function onTick()
    local currentTime = os.clock() * 1000
    
    -- Cheap operations every tick
    local health = AltoClef.getHealth()
    
    -- Expensive operations periodically
    if currentTime - lastExpensiveCheck > EXPENSIVE_CHECK_INTERVAL then
        performExpensiveOperation()
        lastExpensiveCheck = currentTime
    end
end

-- ❌ Bad: Expensive operations every tick
function onTick()
    local nearbyBlocks = AltoClef.findNearbyBlocks("diamond_ore", 50) -- Expensive!
    local nearbyPlayers = AltoClef.findNearbyPlayers(100) -- Expensive!
end
```

### Resource Cleanup

```lua
-- ✅ Good: Proper cleanup
local timer = nil
local eventHandler = nil

function onEnable()
    -- Setup resources
    timer = createTimer()
    eventHandler = registerEventHandler()
end

function onDisable()
    -- Clean up resources
    if timer then
        timer:cancel()
        timer = nil
    end
    
    if eventHandler then
        unregisterEventHandler(eventHandler)
        eventHandler = nil
    end
end

-- ❌ Bad: No cleanup
function onEnable()
    createTimer() -- Lost reference, can't clean up
    registerEventHandler() -- Keeps running after disable
end
```

### Configuration Management

```lua
-- Good: Centralized configuration
local CONFIG = {
    HEALTH_THRESHOLD = 10,
    HUNGER_THRESHOLD = 8,
    SCAN_RADIUS = 16,
    ENABLE_CHAT_ALERTS = true,
    DEBUG_MODE = false
}

-- Easy to modify behavior
if player.health < CONFIG.HEALTH_THRESHOLD then
    -- Handle low health
end

-- ❌ Bad: Magic numbers scattered throughout
if player.health < 10 then -- What does 10 represent?
    -- Handle low health
end

if hunger < 8 then -- Another magic number
    -- Handle hunger
end
```

---

## File Organization

### Single File Scripts
For simple scripts, keep everything in one file:

```
my_simple_script.lua
├── Metadata header
├── Configuration constants
├── State variables
├── Utility functions
├── Main logic functions
└── Lifecycle functions
```

### Multi-File Scripts
For complex scripts, consider splitting into modules:

```
my_complex_script/
├── main.lua              # Entry point with lifecycle functions
├── config.lua            # Configuration constants
├── state.lua             # State management
├── utils.lua             # Utility functions
└── logic/
    ├── mining.lua         # Mining-specific logic
    └── combat.lua         # Combat-specific logic
```

---

## Development Tips

### Start Small
Begin with a simple template and gradually add features:

```lua
-- Minimal starting point
function onLoad()
    AltoClef.log("Script loaded!")
end

function onTick()
    if not AltoClef.isInGame() then return end
    -- Add your logic here
end
```

### 🔍 Use Debug Logging
Add comprehensive logging during development:

```lua
local DEBUG = true

function debug(message)
    if DEBUG then
        AltoClef.log("[DEBUG] " .. message)
    end
end

function onTick()
    if not AltoClef.isInGame() then return end
    debug("onTick called, health: " .. AltoClef.getHealth())
end
```

### Test Incrementally
Test each function as you add it:

```lua
function onEnable()
    AltoClef.log("Testing health API...")
    local health = AltoClef.getHealth()
    AltoClef.log("Health: " .. health)
    
    AltoClef.log("Testing position API...")
    local pos = AltoClef.getPlayerPos()
    AltoClef.log("Position: " .. pos.x .. ", " .. pos.y .. ", " .. pos.z)
end
```

### Handle Edge Cases
Always consider what could go wrong:

```lua
function safeGetHealth()
    if not AltoClef.isInGame() then
        return 0 -- Default value
    end
    
    local health = AltoClef.getHealth()
    if not health or health < 0 then
        return 0 -- Sanitize invalid values
    end
    
    return health
end
```

---

## Related Topics

**Next Steps:**
- [Player APIs](03_PLAYER_APIS.md) - Start building functionality with player APIs
- [Examples](10_EXAMPLES.md) - Browse working example scripts

**See Also:**
- [Debug Tools](09_DEBUG_TOOLS.md) - Debugging and troubleshooting
- [API Reference](13_API_REFERENCE.md) - Complete function reference

---

## Troubleshooting

### Common Issues

**Script not loading**
- **Cause:** Syntax errors in metadata header or function definitions
- **Solution:** Check for missing `]]--` in header, proper function syntax
- **Debug:** `@luadebug scripts` to see script status

**Functions not running**
- **Cause:** Missing `onTick()` or incorrect function names
- **Solution:** Ensure lifecycle functions are properly named and defined
- **Debug:** `@luadebug errors` to see specific error messages

### Debug Commands
```bash
@luadebug scripts     # Show all loaded scripts and their status
@luadebug errors      # Display recent script errors with details
``` 