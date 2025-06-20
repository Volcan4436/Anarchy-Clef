# 🚀 AltoClef Lua Scripting System Documentation

**Version:** 1.0.0  
**Author:** Hearty  
**Created:** 2025-01-10  

## 📖 Table of Contents

1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [Script Structure](#script-structure)
4. [Core APIs](#core-apis)
5. [AltoMenu Integration](#altomenu-integration)
6. [Example Scripts](#example-scripts)
7. [Debug Tools](#debug-tools)
8. [Best Practices](#best-practices)
9. [Troubleshooting](#troubleshooting)
10. [Advanced Features](#advanced-features)

---

## 🎯 Introduction

The AltoClef Lua Scripting System provides a powerful, enterprise-grade platform for creating custom automation scripts in Minecraft. Built with comprehensive APIs, debugging tools, and a modern management interface, it enables both beginners and advanced users to create sophisticated automation workflows.

### ✨ Key Features

- **Complete Lua 5.2 Environment** - Full scripting capabilities with sandboxed execution
- **Rich API Library** - Access to player data, world information, inventory management, and more
- **AltoMenu Integration** - Create custom modules with settings and UI elements
- **Visual Script Manager** - File browser, drag & drop, one-click enable/disable
- **Advanced Debug Tools** - Comprehensive debugging commands and real-time monitoring
- **Script Lifecycle Management** - Automatic state persistence and error recovery
- **Real-time Execution** - Scripts run on the game tick cycle for responsive automation

---

## 🚀 Getting Started

### Accessing the Script Manager

1. **Open AltoClef Menu** - Press your configured menu key (default: Right Shift)
2. **Navigate to Scripts Tab** - Click the "Scripts" tab in the top menu
3. **Browse Scripts** - View all available scripts in the file browser
4. **Enable Scripts** - Click on any script to enable/disable it

### Your First Script

Create a new file in `AltoClefLUA/scripts/user_scripts/` called `hello_world.lua`:

```lua
--[[
@name Hello World
@description My first AltoClef script
@version 1.0.0
@author YourName
@category Test
@dependencies none
]]--

function onLoad()
    AltoClef.log("Hello, AltoClef Scripting!")
end

function onTick()
    -- This runs every game tick (20 times per second)
    -- Add your automation logic here
end

function onEnable()
    AltoClef.log("Script enabled!")
end

function onDisable()
    AltoClef.log("Script disabled!")
end

function onCleanup()
    AltoClef.log("Script cleaning up...")
end
```

### File Structure

```
AltoClefLUA/
├── scripts/
│   ├── examples/           # Example scripts for learning
│   │   ├── auto_food.lua
│   │   ├── basic_mining.lua
│   │   └── debug_test.lua
│   ├── libraries/          # Shared utility libraries
│   │   └── math_helpers.lua
│   └── user_scripts/       # Your custom scripts
│       └── my_script.lua
```

---

## 📝 Script Structure

### Metadata Header

Every script should start with a metadata header:

```lua
--[[
@name Script Display Name
@description Brief description of what the script does
@version 1.0.0
@author Your Name
@category Utility|Mining|Combat|Movement|Debug
@dependencies library1.lua, library2.lua
]]--
```

### Lifecycle Functions

Scripts can implement these lifecycle functions:

| Function | When Called | Purpose |
|----------|-------------|---------|
| `onLoad()` | Script file loaded | Initialize variables, log startup |
| `onTick()` | Every game tick | Main automation logic |
| `onEnable()` | Script enabled | Setup when activated |
| `onDisable()` | Script disabled | Cleanup when deactivated |
| `onCleanup()` | Script unloaded | Final cleanup before removal |

### Example Implementation

```lua
-- Global variables
local tickCounter = 0
local lastActionTime = 0

function onLoad()
    AltoClef.log("Script initialized")
    tickCounter = 0
end

function onTick()
    tickCounter = tickCounter + 1
    
    -- Run every 5 seconds (100 ticks)
    if tickCounter % 100 == 0 then
        local currentTime = os.clock() * 1000
        
        -- Your automation logic here
        if currentTime - lastActionTime > 5000 then
            doSomeAction()
            lastActionTime = currentTime
        end
    end
end

function doSomeAction()
    AltoClef.log("Performing action...")
    -- Implementation here
end
```

---

## 🔧 Core APIs

### Player Information

#### Position & Movement
```lua
-- Get player position
local pos = AltoClef.getPlayerPos()  -- Returns {x, y, z}
local x = AltoClef.getPlayerX()      -- Individual coordinates
local y = AltoClef.getPlayerY()
local z = AltoClef.getPlayerZ()

-- Movement commands
AltoClef.moveTo(x, y, z)             -- Move to specific position
AltoClef.lookAt(x, y, z)             -- Look at position
```

#### Health & Hunger
```lua
-- Health information
local health = AltoClef.getHealth()           -- Current health (0-20)
local maxHealth = AltoClef.getMaxHealth()     -- Maximum health
local healthPercent = AltoClef.getHealthPercent()  -- Health percentage (0-100)
local lowHealth = AltoClef.isLowHealth()      -- True if health < 50%

-- Hunger information
local hunger = AltoClef.getHunger()           -- Hunger level (0-20)
local saturation = AltoClef.getSaturation()  -- Saturation level
local isHungry = AltoClef.isHungry()          -- True if hunger < 20
```

#### Food Management
```lua
-- Food operations
local hasFood = AltoClef.hasFood()            -- Check if player has any food
local needsFood = AltoClef.needsFood()        -- Check if should eat
AltoClef.eatFood()                            -- Trigger eating food

-- Food commands
AltoClef.runCommand("food 20")                -- Get food to reach 20 hunger
```

#### Jump & Velocity Control
```lua
-- Jump functionality
local isJumping = AltoClef.isJumping()        -- Check if player is currently jumping
AltoClef.jump()                               -- Make the player jump

-- Velocity operations
local velocity = AltoClef.getVelocity()       -- Get current velocity {x, y, z}
local x = velocity.x                          -- Individual velocity components
local y = velocity.y
local z = velocity.z

AltoClef.setVelocity(0.5, 1.0, -0.3)         -- Set velocity (x, y, z)
AltoClef.setVelocity(0, 0, 0)                 -- Stop player movement

-- Also available via Utils API:
Utils.Player.isJumping()                      -- Alternative access
Utils.Player.jump()                           -- Alternative access
Utils.Player.getVelocity()                    -- Alternative access
Utils.Player.setVelocity(x, y, z)             -- Alternative access
```

### Inventory Management

```lua
-- Item checking
local hasItem = AltoClef.hasItem("bread")     -- Check if player has item
local itemCount = AltoClef.getItemCount("iron_ingot")  -- Get count of item

-- Inventory information
local slots = AltoClef.getInventorySlots()    -- Get all inventory slots
local emptySlots = AltoClef.getEmptySlots()   -- Number of empty slots

-- Item operations
AltoClef.collectItem("iron_ore", 10)          -- Collect specific items
AltoClef.craftItem("iron_pickaxe", 1)         -- Craft items
AltoClef.dropItem("dirt", 32)                 -- Drop items
```

### World Interaction

```lua
-- World state
local inGame = AltoClef.isInGame()            -- Check if in game world
local singleplayer = AltoClef.isSingleplayer()  -- Check if singleplayer
local difficulty = AltoClef.getDifficulty()   -- Get world difficulty
local gameTime = AltoClef.getGameTime()       -- Get minecraft world time

-- Block operations
local block = AltoClef.getBlockAt(x, y, z)    -- Get block at position
local isBlock = AltoClef.isBlockAt("iron_ore", x, y, z)  -- Check specific block
local nearestPos = AltoClef.findNearestBlock("diamond_ore")  -- Find nearest block
AltoClef.mineBlock("coal_ore")                -- Mine specific block type
```

### Task Management

```lua
-- Task execution
AltoClef.runUserTask("MineAndCollectTask", {
    target = "iron_ore",
    count = 32
})

-- Task status
local isActive = AltoClef.getTaskRunner():isActive()  -- Check if task running
AltoClef.cancelUserTask()                     -- Cancel current task
```

### Utility Functions

```lua
-- Logging
AltoClef.log("Information message")           -- Log to chat and console
AltoClef.logWarning("Warning message")        -- Log warning

-- Commands
AltoClef.runCommand("give iron_sword 1")      -- Execute AltoClef commands

-- Timing
local currentTime = os.clock()                -- Get current time in seconds
AltoClef.wait(5)                              -- Wait for 5 seconds
```

---

## 🎮 AltoMenu Integration

Create custom modules with the AltoMenu API:

### Creating a Module

```lua
-- Create a new module
local myModule = AltoMenu:createModule("My Module", "Custom automation module", "Utility")

-- Add settings
local enableSetting = myModule:addBooleanSetting("Enabled", true)
local speedSetting = myModule:addNumberSetting("Speed", 1.0, 5.0, 2.5, 0.1)
local modeSetting = myModule:addModeSetting("Mode", "Auto", "Auto", "Manual", "Smart")

-- Set event handlers
myModule:onEnable(function()
    AltoClef.log("My module enabled!")
end)

myModule:onDisable(function()
    AltoClef.log("My module disabled!")
end)

myModule:onTick(function()
    if myModule.enabled then
        local speed = speedSetting:getValue()
        local mode = modeSetting:getValue()
        -- Module logic here
    end
end)
```

### Working with Settings

```lua
-- Boolean settings
local boolSetting = myModule:addBooleanSetting("Auto Mode", false)
boolSetting:toggle()                          -- Toggle value
boolSetting:setValue(true)                    -- Set specific value
local value = boolSetting:getValue()          -- Get current value

-- Number settings
local numSetting = myModule:addNumberSetting("Range", 1.0, 10.0, 5.0, 0.5)
numSetting:setValue(7.5)                      -- Set value
numSetting:increment(true)                    -- Increase value
local current = numSetting:getValue()         -- Get current value

-- Mode settings
local modeSetting = myModule:addModeSetting("Strategy", "Aggressive", "Passive", "Aggressive", "Balanced")
modeSetting:setMode("Balanced")               -- Set specific mode
modeSetting:cycle()                           -- Cycle to next mode
local currentMode = modeSetting:getValue()    -- Get current mode
```

### Module Management

```lua
-- Get existing modules
local module = AltoMenu:getModule("ModuleName")         -- Get specific module
local modules = AltoMenu:getModulesInCategory("Combat") -- Get modules by category
local categories = AltoMenu:getCategories()             -- Get all categories

-- Module control
module:toggle()                               -- Toggle module
module:setEnabled(true)                       -- Enable/disable module
local isEnabled = module.enabled              -- Check if enabled
```

---

## 📚 Example Scripts

### 1. Auto Food Monitor

```lua
--[[
@name Auto Food
@description Automatically monitors hunger and provides food management
@version 1.0.0
@author Hearty
@category Utility
@dependencies none
]]--

local HUNGER_WARNING_THRESHOLD = 10
local lastWarningTime = 0
local WARNING_COOLDOWN = 10000 -- 10 seconds

function onLoad()
    AltoClef.log("Auto Food Monitor loaded!")
end

function onTick()
    if not AltoClef.isInGame() then return end
    
    local currentTime = os.clock() * 1000
    if currentTime - lastWarningTime < WARNING_COOLDOWN then return end
    
    local hunger = AltoClef.getHunger()
    local hasFood = AltoClef.hasFood()
    local needsFood = AltoClef.needsFood()
    
    if hunger < HUNGER_WARNING_THRESHOLD then
        AltoClef.log("§c⚠ Low Hunger Warning!")
        AltoClef.log("  Current hunger: " .. hunger .. "/20")
        AltoClef.log("  Has food: " .. (hasFood and "Yes" or "No"))
        
        if not hasFood and needsFood then
            AltoClef.log("  §eRecommendation: Try @food 20 command")
        end
        
        lastWarningTime = currentTime
    end
end
```

### 2. Smart Mining

```lua
--[[
@name Basic Mining
@description Automatically mines valuable ores when found nearby
@version 1.2.0
@author Hearty
@category Mining
@dependencies none
]]--

local TARGET_BLOCKS = {"iron_ore", "coal_ore", "diamond_ore", "gold_ore"}
local SEARCH_RADIUS = 16
local lastScanTime = 0
local SCAN_INTERVAL = 3000

function onLoad()
    AltoClef.log("Basic Mining script loaded! Targeting: " .. table.concat(TARGET_BLOCKS, ", "))
end

function onTick()
    local currentTime = os.clock() * 1000
    
    if currentTime - lastScanTime < SCAN_INTERVAL then return end
    if AltoClef.getTaskRunner():isActive() then return end
    
    local foundBlocks = {}
    for _, blockType in ipairs(TARGET_BLOCKS) do
        local blocks = AltoClef.getBlockTracker():getNearbyBlocks(blockType, SEARCH_RADIUS)
        if #blocks > 0 then
            table.insert(foundBlocks, {type = blockType, count = #blocks})
        end
    end
    
    if #foundBlocks > 0 then
        local targetBlock = selectBestTarget(foundBlocks)
        if targetBlock then
            AltoClef.log("Found " .. targetBlock.count .. " " .. targetBlock.type .. " blocks, mining...")
            AltoClef.runUserTask("MineAndCollectTask", {
                target = targetBlock.type,
                count = math.min(targetBlock.count, 64)
            })
        end
    end
    
    lastScanTime = currentTime
end

function selectBestTarget(blocks)
    local priority = {diamond_ore = 4, gold_ore = 3, iron_ore = 2, coal_ore = 1}
    local bestBlock = nil
    local highestPriority = 0
    
    for _, block in ipairs(blocks) do
        local blockPriority = priority[block.type] or 0
        if blockPriority > highestPriority then
            highestPriority = blockPriority
            bestBlock = block
        end
    end
    
    return bestBlock
end
```

### 3. Custom AltoMenu Module

```lua
--[[
@name Advanced Auto Miner
@description Creates a custom AltoMenu module for advanced mining automation
@version 1.0.0
@author Hearty
@category Mining
@dependencies none
]]--

local minerModule = nil

function onLoad()
    -- Create the module
    minerModule = AltoMenu:createModule("Advanced Miner", "Smart mining with customizable settings", "Mining")
    
    -- Add settings
    minerModule:addBooleanSetting("Auto Mine", true)
    minerModule:addNumberSetting("Search Radius", 5, 50, 16, 1)
    minerModule:addModeSetting("Priority", "Balanced", "Speed", "Balanced", "Value")
    minerModule:addBooleanSetting("Auto Tools", true)
    
    -- Set up event handlers
    minerModule:onEnable(function()
        AltoClef.log("Advanced Miner enabled!")
    end)
    
    minerModule:onDisable(function()
        AltoClef.log("Advanced Miner disabled!")
        AltoClef.cancelUserTask()
    end)
    
    minerModule:onTick(function()
        if minerModule.enabled then
            performMining()
        end
    end)
    
    AltoClef.log("Advanced Miner module created!")
end

function performMining()
    local autoMineSetting = minerModule:getSetting("Auto Mine")
    if not autoMineSetting:getValue() then return end
    
    local radius = minerModule:getSetting("Search Radius"):getValue()
    local priority = minerModule:getSetting("Priority"):getValue()
    
    -- Mining logic using settings
    scanAndMine(radius, priority)
end

function scanAndMine(radius, priority)
    -- Implement mining logic based on settings
    -- This is where your advanced mining algorithm would go
end
```

### 4. Player Movement Control

```lua
--[[
@name Advanced Movement
@description Demonstrates jump and velocity control for advanced movement
@version 1.0.0
@author Hearty  
@category Movement
@dependencies none
]]--

local JUMP_COOLDOWN = 500 -- milliseconds
local lastJumpTime = 0
local movementState = "idle"
local targetVelocity = {x = 0, y = 0, z = 0}

function onLoad()
    AltoClef.log("Advanced Movement script loaded!")
    AltoClef.log("Commands: jump, boost, stop, hover")
end

function onTick()
    if not AltoClef.isInGame() then return end
    
    handleMovementControl()
    handleJumpLogic()
    monitorPlayerState()
end

function handleMovementControl()
    -- Example: Boost player forward when sprinting
    if movementState == "boost" then
        local currentVel = AltoClef.getVelocity()
        if currentVel then
            -- Apply forward boost while maintaining Y velocity
            AltoClef.setVelocity(targetVelocity.x, currentVel.y, targetVelocity.z)
        end
    elseif movementState == "hover" then
        -- Hover mode: maintain Y position
        local currentVel = AltoClef.getVelocity()
        if currentVel and currentVel.y < -0.1 then
            AltoClef.setVelocity(currentVel.x, 0.1, currentVel.z)
        end
    end
end

function handleJumpLogic()
    local currentTime = os.clock() * 1000
    
    -- Auto-jump when moving and on ground
    if movementState == "auto_jump" then
        if currentTime - lastJumpTime > JUMP_COOLDOWN then
            if not AltoClef.isJumping() then
                AltoClef.jump()
                lastJumpTime = currentTime
                AltoClef.log("Auto-jump activated!")
            end
        end
    end
end

function monitorPlayerState()
    local velocity = AltoClef.getVelocity()
    if velocity then
        local speed = math.sqrt(velocity.x^2 + velocity.z^2)
        
        -- Log interesting states
        if AltoClef.isJumping() and speed > 0.3 then
            -- Player is jumping while moving fast
            AltoClef.log(string.format("High-speed jump! Speed: %.2f", speed))
        end
        
        -- Safety check: reset if moving too fast
        if speed > 2.0 then
            AltoClef.log("Speed limit exceeded, applying brakes...")
            AltoClef.setVelocity(velocity.x * 0.5, velocity.y, velocity.z * 0.5)
        end
    end
end

-- Command handlers (would be triggered by chat commands or other events)
function setBoostMode(forwardSpeed, sidewaysSpeed)
    movementState = "boost"
    targetVelocity.x = sidewaysSpeed or 0
    targetVelocity.z = forwardSpeed or 0.8
    AltoClef.log("Boost mode activated!")
end

function setHoverMode()
    movementState = "hover"
    AltoClef.log("Hover mode activated!")
end

function setAutoJumpMode()
    movementState = "auto_jump"
    AltoClef.log("Auto-jump mode activated!")
end

function stopMovement()
    movementState = "idle"
    AltoClef.setVelocity(0, AltoClef.getVelocity().y, 0)
    AltoClef.log("Movement stopped!")
end

function emergencyStop()
    movementState = "idle"
    AltoClef.setVelocity(0, 0, 0)
    AltoClef.log("Emergency stop - all movement halted!")
end

-- Example usage functions
function performSuperJump()
    AltoClef.jump()
    -- Give a small upward boost after jumping
    local currentVel = AltoClef.getVelocity()
    if currentVel then
        AltoClef.setVelocity(currentVel.x, currentVel.y + 0.2, currentVel.z)
    end
    AltoClef.log("Super jump executed!")
end

function performDashAttack(direction)
    -- Quick dash in specified direction
    local dashPower = 1.2
    local x = direction == "forward" and 0 or (direction == "left" and -dashPower or dashPower)
    local z = direction == "forward" and dashPower or 0
    
    AltoClef.setVelocity(x, 0.1, z)
    AltoClef.log("Dash attack: " .. direction)
end
```

---

## 💬 Chat & Command System

AltoClef's Lua scripts can interact with Minecraft's chat system and create custom commands:

### Chat Functions

**Send Chat Messages**
```lua
-- Send a message to chat
AltoClef.chat("Hello everyone!")
AltoClef.chat("My position: " .. AltoClef.getPlayerX() .. ", " .. AltoClef.getPlayerY() .. ", " .. AltoClef.getPlayerZ())
```

**Listen for Chat Events**
```lua
-- Set up a chat event handler
AltoClef.onchat(function(chatInfo)
    local message = chatInfo.message    -- The chat message content
    local sender = chatInfo.sender      -- Username of sender
    local senderUUID = chatInfo.senderUUID  -- UUID of sender
    local isSelf = chatInfo.isSelf      -- true if message is from you
    local timestamp = chatInfo.timestamp -- Message timestamp
    
    AltoClef.log("Chat from " .. sender .. ": " .. message)
    
    -- Auto-respond to messages
    if not isSelf and string.lower(message):find("hello") then
        AltoClef.chat("Hello there, " .. sender .. "!")
    end
end)
```

### Custom Commands

**Create Custom Commands**
```lua
-- Create a simple command
AltoClef.createcommand("greet", "Says hello", function(args)
    AltoClef.chat("Hello World!")
end)

-- Create a command with arguments
AltoClef.createcommand("goto", "Go to coordinates", function(args)
    if args[1] and args[2] and args[3] then
        local x, y, z = tonumber(args[1]), tonumber(args[2]), tonumber(args[3])
        AltoClef.chat(string.format("Going to %d, %d, %d", x, y, z))
        -- Add movement logic here
    else
        AltoClef.chat("Usage: @goto <x> <y> <z>")
    end
end)
```

**Listen for Command Events**
```lua
-- Set up a command event handler
AltoClef.oncommand(function(cmdInfo)
    local command = cmdInfo.command  -- Command name (without @)
    local args = cmdInfo.args       -- Command arguments as string
    local timestamp = cmdInfo.timestamp -- Command timestamp
    
    AltoClef.log("Command executed: @" .. command .. " " .. args)
    
    -- React to specific commands
    if command == "status" then
        AltoClef.chat("Script is running smoothly!")
    end
end)
```

### Complete Chat Example

```lua
--[[
@name Chat Bot
@description Interactive chat bot with custom commands
@version 1.0.0
@author Hearty
@category Utility
]]--

local chatCount = 0
local commandCount = 0

function onLoad()
    AltoClef.log("Chat Bot loaded!")
    
    -- Create custom commands
    AltoClef.createcommand("stats", "Show chat statistics", function(args)
        local msg = string.format("I've seen %d chats and %d commands!", chatCount, commandCount)
        AltoClef.chat(msg)
    end)
    
    AltoClef.createcommand("position", "Report current position", function(args)
        local x, y, z = AltoClef.getPlayerX(), AltoClef.getPlayerY(), AltoClef.getPlayerZ()
        AltoClef.chat(string.format("Current position: %.1f, %.1f, %.1f", x, y, z))
    end)
    
    AltoClef.createcommand("health", "Report health status", function(args)
        local health = AltoClef.getHealth()
        local hunger = AltoClef.getHunger()
        AltoClef.chat(string.format("Health: %.1f/20, Hunger: %d/20", health, hunger))
    end)
    
    -- Listen for chat messages
    AltoClef.onchat(function(chatInfo)
        chatCount = chatCount + 1
        
        -- Don't respond to own messages
        if chatInfo.isSelf then return end
        
        local msg = string.lower(chatInfo.message)
        local sender = chatInfo.sender
        
        -- Auto-responses
        if msg:find("hello") or msg:find("hi") then
            AltoClef.chat("Hello there, " .. sender .. "!")
        elseif msg:find("how are you") then
            AltoClef.chat("I'm doing great, thanks for asking!")
        elseif msg:find("what time") then
            local gameTime = AltoClef.getGameTime()
            AltoClef.chat("Game time: " .. gameTime)
        elseif msg:find("help") then
            AltoClef.chat("Try: @stats, @position, @health")
        end
    end)
    
    -- Listen for command execution
    AltoClef.oncommand(function(cmdInfo)
        commandCount = commandCount + 1
        AltoClef.log("Command executed: @" .. cmdInfo.command .. " " .. cmdInfo.args)
    end)
end
```

---

## 🔍 Debug Tools

### Debug Commands

The scripting system includes comprehensive debugging tools:

#### Main Debug Command
```bash
@luadebug <type>
```

**API Debug Types:**
- `hunger` - Show hunger API information and current values
- `position` - Show position API information and coordinates  
- `inventory` - Show inventory API and item counts
- `health` - Show health API and current health status
- `food` - Show food API and food availability
- `blocks` - Show block API and nearby blocks
- `items` - Show item API and common item names
- `time` - Show time API and current time values
- `world` - Show world API and world information
- `player` - Show new jump and velocity control APIs

**Script Debug Types:**
- `errors` - Show recent script errors with detailed information
- `scripts` - Show loaded scripts status and performance
- `logs` - Show logging configuration and debugging tips
- `performance` - Show script performance metrics and warnings
- `help` - Show all available debug commands

#### Quick Shortcuts
```bash
@hunger    # Quick hunger debug
@pos       # Quick position debug  
@inv       # Quick inventory debug
```

### Debug Output Example

```bash
# @luadebug hunger
=== HUNGER DEBUG ===
Current Values:
  Hunger: 15/20
  Saturation: 5.2

Lua API Calls:
  AltoClef.getHunger() - returns hunger level (0-20)
  AltoClef.getSaturation() - returns saturation level
  AltoClef.isHungry() - returns true if hunger < 20

Example Usage:
  if AltoClef.getHunger() < 10 then
    AltoClef.log('Low hunger!')
  end
```

### Error Logging

The Lua scripting system includes comprehensive error logging with detailed debugging information:

#### Error Log Format

When a script error occurs, you'll see enhanced error information:

```
╔══════════════════════════════════════════════════════════════════════════════
║ 🔥 LUA SCRIPT ERROR #1 - 14:25:30
║ Script: 'my_script'
║ Context: Error in onTick
║ Error Type: LuaError
║ Message: attempt to index a nil value (global 'invalidVar')
╚══════════════════════════════════════════════════════════════════════════════
┌─ 🔍 LUA ERROR DETAILS:
│ 📍 Location: my_script:15
│ 🎯 Function: onTick
│ 📚 Lua Stack Trace:
│   LuaError: my_script:15: attempt to index a nil value
│   at LuaFunction.call(LuaFunction.java:142)
│ 📊 Variable Context: variable is nil
└─ End Lua Error Details
┌─ 🖥️ SYSTEM STATE:
│ 🏃 Player: Health=20.0, Hunger=18, InGame=true
│ 📍 Position: (123.5, 64.0, -456.2)
│ 💾 Memory: Used=245MB, Free=123MB, Max=2048MB
│ 📜 Scripts: 3 loaded
│ 🧵 Thread: Client thread (Priority: 5)
└─ End System State
════════════════════════════════════════════════════════════════════════════════
```

#### Debug Commands for Error Analysis

```bash
# View recent script errors
@luadebug errors

# Check script status and performance
@luadebug scripts

# View logging configuration
@luadebug logs

# Check performance metrics
@luadebug performance
```

#### Error Management Features

- **Rate Limiting**: Prevents error spam - duplicate errors are summarized
- **Auto-Disable**: Scripts are disabled after 10 errors to prevent infinite loops
- **Line Numbers**: Lua errors show exact line numbers when possible
- **Function Context**: Error messages include function names
- **System State**: Each error logs current player/system state for context
- **Performance Warnings**: Scripts taking >50ms per tick are flagged

### Script Debugging

```lua
function onTick()
    -- Add debug prints to understand script flow
    print("DEBUG: onTick called")
    
    -- Log current state
    AltoClef.log("Script state: " .. tostring(scriptEnabled))
    
    -- Conditional debugging
    if DEBUG_MODE then
        local hunger = AltoClef.getHunger()
        AltoClef.log("Current hunger: " .. hunger)
    end
end
```

---

## 📋 Best Practices

### Performance Optimization

```lua
-- ❌ Bad: Checking every tick
function onTick()
    local blocks = AltoClef.findNearestBlock("diamond_ore")
    -- This is expensive to run 20 times per second
end

-- ✅ Good: Throttled checking
local lastScanTime = 0
local SCAN_INTERVAL = 5000 -- 5 seconds

function onTick()
    local currentTime = os.clock() * 1000
    if currentTime - lastScanTime < SCAN_INTERVAL then
        return
    end
    
    local blocks = AltoClef.findNearestBlock("diamond_ore")
    lastScanTime = currentTime
end
```

### Error Handling

```lua
function safeFunction()
    local success, result = pcall(function()
        -- Your potentially failing code here
        return AltoClef.getHunger()
    end)
    
    if success then
        return result
    else
        AltoClef.logWarning("Error in safeFunction: " .. tostring(result))
        return nil
    end
end
```

### State Management

```lua
-- Global state variables
local isInitialized = false
local scriptState = {
    enabled = false,
    lastAction = 0,
    targetQueue = {}
}

function onEnable()
    if not isInitialized then
        initializeScript()
        isInitialized = true
    end
    scriptState.enabled = true
end

function initializeScript()
    -- One-time initialization
    AltoClef.log("Initializing script...")
    scriptState.targetQueue = {}
end
```

### Resource Management

```lua
function onDisable()
    -- Clean up resources
    AltoClef.cancelUserTask()
    
    -- Clear temporary data
    tempData = {}
    
    -- Reset state
    isRunning = false
end

function onCleanup()
    -- Final cleanup
    if timer then
        timer:cancel()
        timer = nil
    end
end
```

---

## 🚨 Troubleshooting

### Common Issues

#### 1. Script Not Running
**Problem:** Script enabled but `onTick()` not called
**Solution:**
- Use `@luadebug scripts` to check script status
- Check enhanced error logs with `@luadebug errors`
- Look for script errors in console (lines starting with 🔥)
- Ensure `onTick()` function is properly defined
- Verify script is actually enabled in UI

#### 2. API Functions Not Found
**Problem:** `attempt to call field 'getHunger' (a nil value)`
**Solution:**
- Ensure you're using `AltoClef.getHunger()` not `getHunger()`
- Check if you're in game (`AltoClef.isInGame()`)
- Use `@luadebug hunger` to verify API availability
- Check the enhanced error logs for exact error location

#### 3. Enhanced Debugging Workflow
**New Feature:** Use the comprehensive debugging system
**Steps:**
1. **Check Error Summary**: `@luadebug errors` shows recent errors
2. **Examine Full Logs**: Look in console for detailed error information
3. **Performance Check**: `@luadebug performance` identifies slow scripts
4. **Script Status**: `@luadebug scripts` shows which scripts are loaded/enabled
5. **Player State**: `@luadebug player` shows current jump/velocity status

#### 4. Performance Issues
**Problem:** Game lagging when script is enabled
**Solution:**
- Add throttling to `onTick()` function
- Avoid expensive operations every tick
- Use `pcall()` for error-prone operations

#### 4. Script Conflicts
**Problem:** Multiple scripts interfering with each other
**Solution:**
- Use unique variable names
- Implement proper cleanup in `onDisable()`
- Check for active tasks before starting new ones

### Debug Steps

1. **Enable Debug Logging**
   ```lua
   local DEBUG = true
   
   function debugLog(message)
       if DEBUG then
           AltoClef.log("[DEBUG] " .. message)
       end
   end
   ```

2. **Use Debug Commands**
   ```bash
   @luadebug help     # See all debug options
   @hunger            # Check hunger API
   @luadebug position # Check position API
   ```

3. **Check Console Output**
   - Look for Lua errors in the game console
   - Check for `ScriptEngine` related messages
   - Monitor for stack traces

4. **Isolate the Problem**
   - Disable other scripts
   - Test with minimal script
   - Add print statements to trace execution

---

## 🔬 Advanced Features

### Library System

Create reusable libraries in `AltoClefLUA/scripts/libraries/`:

```lua
-- utils.lua
local utils = {}

function utils.distance3D(x1, y1, z1, x2, y2, z2)
    local dx = x2 - x1
    local dy = y2 - y1  
    local dz = z2 - z1
    return math.sqrt(dx*dx + dy*dy + dz*dz)
end

function utils.waitUntil(condition, timeout)
    local startTime = os.clock()
    while not condition() do
        if os.clock() - startTime > timeout then
            return false
        end
        coroutine.yield()
    end
    return true
end

return utils
```

### Script Communication

Scripts can share data through global variables:

```lua
-- Script A
_G.SharedData = _G.SharedData or {}
_G.SharedData.minerActive = true

-- Script B  
if _G.SharedData and _G.SharedData.minerActive then
    -- Don't start farming while mining is active
    return
end
```

### Advanced Task Integration

```lua
-- Custom task with callbacks
function startCustomTask()
    AltoClef.runUserTask("ComplexTask", {
        onProgress = function(progress)
            AltoClef.log("Task progress: " .. progress .. "%")
        end,
        onComplete = function(result)
            AltoClef.log("Task completed: " .. result)
            handleTaskCompletion(result)
        end,
        onError = function(error)
            AltoClef.logWarning("Task failed: " .. error)
            handleTaskError(error)
        end
    })
end
```

### Dynamic Module Creation

```lua
-- Create modules based on configuration
local moduleConfig = {
    {name = "Auto Fisher", category = "Utility"},
    {name = "Tree Farm", category = "Farming"},
    {name = "Mob Grinder", category = "Combat"}
}

for _, config in ipairs(moduleConfig) do
    local module = AltoMenu:createModule(config.name, "Auto-generated module", config.category)
    setupModuleLogic(module, config)
end
```

---

## 📖 Reference

### Complete API Reference

For the most up-to-date API reference, use the debug commands:
- `@luadebug help` - Complete command list
- `@luadebug <api>` - Detailed API information with examples

### File Locations

- **Scripts:** `AltoClefLUA/scripts/`
- **User Scripts:** `AltoClefLUA/scripts/user_scripts/`
- **Libraries:** `AltoClefLUA/scripts/libraries/`
- **Examples:** `AltoClefLUA/scripts/examples/`

### Version History

- **v1.0.0** - Initial release with complete scripting system
- Core engine with script lifecycle management
- Comprehensive APIs for game interaction
- AltoMenu integration for custom modules
- Debug tools and script manager UI

---

## 🤝 Contributing

When creating scripts for the community:

1. **Use proper metadata headers**
2. **Include comprehensive error handling**
3. **Add helpful comments and documentation**
4. **Test thoroughly in different scenarios**
5. **Follow the established patterns and conventions**

### Script Template

```lua
--[[
@name Script Name
@description What this script does
@version 1.0.0
@author Your Name
@category Appropriate Category
@dependencies List any dependencies
]]--

-- Configuration constants
local CONFIG = {
    SETTING_ONE = 10,
    SETTING_TWO = true
}

-- State variables
local isInitialized = false
local lastActionTime = 0

function onLoad()
    AltoClef.log("Script loaded: " .. _ENV._SCRIPT_NAME)
    isInitialized = false
end

function onTick()
    if not AltoClef.isInGame() then return end
    if not isInitialized then
        initialize()
        return
    end
    
    -- Main script logic here
    performMainLogic()
end

function initialize()
    -- Initialization logic
    AltoClef.log("Initializing script...")
    isInitialized = true
end

function performMainLogic()
    -- Your automation logic here
end

function onEnable()
    AltoClef.log("Script enabled")
    lastActionTime = 0
end

function onDisable()
    AltoClef.log("Script disabled")
    cleanup()
end

function onCleanup()
    AltoClef.log("Script cleaning up")
    cleanup()
end

function cleanup()
    -- Cleanup resources
    AltoClef.cancelUserTask()
end
```

---

## 📞 Support

If you encounter issues or need help:

1. **Check this documentation** for common solutions
2. **Use debug commands** to diagnose API issues  
3. **Review example scripts** for implementation patterns
4. **Check console output** for error messages
5. **Test with minimal scripts** to isolate problems

---

**Happy Scripting! 🎉**

*Create powerful automation, build custom modules, and extend AltoClef with the full power of Lua scripting.* 