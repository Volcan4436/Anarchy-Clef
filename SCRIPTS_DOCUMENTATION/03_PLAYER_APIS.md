# 👤 Player APIs

**Version:** 1.0.0  
**Status:** ✅ Complete  
**Category:** Core APIs  

## 📖 Overview

Player APIs provide access to player state, movement, health, and physics. These are the most fundamental APIs for creating automation scripts and form the foundation for most automation workflows.

---

## 📚 Table of Contents

1. [🚀 Quick Start](#-quick-start)
2. [🏃 Movement & Physics](#-movement--physics)
3. [💚 Health & Hunger](#-health--hunger)
4. [📍 Position & Location](#-position--location)
5. [⚡ Input Controls](#-input-controls)
6. [🎮 Enhanced Player Status](#-enhanced-player-status)
7. [🧪 Examples](#-examples)
8. [💡 Best Practices](#-best-practices)
9. [🔗 Related Topics](#-related-topics)

---

## 🚀 Quick Start

### Essential Functions
```lua
-- Health and safety
local health = AltoClef.getHealth()
local hunger = AltoClef.getHunger()

-- Position and movement
local pos = AltoClef.getPlayerPos()
local velocity = AltoClef.getVelocity()

-- Basic controls
AltoClef.jump()
AltoClef.setVelocity(0, 1, 0)  -- Jump upward

-- Alternative Utils API access
local health = Utils.Player.getHealth()
local pos = Utils.Player.getPlayerPos()
```

### Quick Health Monitor
```lua
function onTick()
    if not AltoClef.isInGame() then return end
    
    local health = AltoClef.getHealth()
    local hunger = AltoClef.getHunger()
    
    if health < 10 then
        AltoClef.log("⚠️ Low health: " .. health)
    end
    
    if hunger < 8 and AltoClef.hasFood() then
        AltoClef.eatFood()
    end
end
```

---

## 🏃 Movement & Physics

### 🦘 Jump Control
```lua
-- Check if player is currently jumping
local isJumping = AltoClef.isJumping()        -- Returns true/false

-- Make the player jump
AltoClef.jump()                               -- Triggers jump action
```

**Implementation Details:**
- Jump detection: `!player.isOnGround() && player.getVelocity().y > 0`
- Jump execution: Uses `InputControls.tryPress(Input.JUMP)`
- Works with both standing and moving jumps

### ⚡ Velocity Control
```lua
-- Get current velocity
local velocity = AltoClef.getVelocity()       -- Returns {x, y, z} table
local x = velocity.x                          -- Individual components  
local y = velocity.y
local z = velocity.z

-- Set player velocity
AltoClef.setVelocity(0.5, 1.0, -0.3)         -- Set velocity (x, y, z)
AltoClef.setVelocity(0, 0, 0)                 -- Stop player movement
```

**Usage Examples:**
```lua
-- Boost player forward
local currentVel = AltoClef.getVelocity()
AltoClef.setVelocity(currentVel.x * 1.5, currentVel.y, currentVel.z * 1.5)

-- Emergency stop
AltoClef.setVelocity(0, 0, 0)

-- Hover mode (maintain Y position)
local vel = AltoClef.getVelocity()
if vel.y < -0.1 then
    AltoClef.setVelocity(vel.x, 0.1, vel.z)
end
```

### 🔄 Alternative API Access
```lua
-- Also available via Utils API for consistency
Utils.Player.isJumping()                      -- Alternative access
Utils.Player.jump()                           -- Alternative access
Utils.Player.getVelocity()                    -- Alternative access  
Utils.Player.setVelocity(x, y, z)             -- Alternative access
```

---

## 💚 Health & Hunger

### ❤️ Health Information  
```lua
-- Current health (0-20)
local health = AltoClef.getHealth()           -- Returns current health value

-- Health percentage (0-100)
local healthPercent = AltoClef.getHealthPercent()  

-- Check if health is critically low
local lowHealth = AltoClef.isLowHealth()      -- True if health < 50%

-- Maximum health value
local maxHealth = AltoClef.getMaxHealth()     -- Usually 20
```

### 🍖 Hunger System
```lua
-- Hunger level (0-20)
local hunger = AltoClef.getHunger()           -- Current hunger level

-- Saturation level (hidden hunger buffer)
local saturation = AltoClef.getSaturation()  -- Current saturation

-- Check if player needs food
local isHungry = AltoClef.isHungry()          -- True if hunger < 20
local needsFood = AltoClef.needsFood()        -- True if should eat

-- Check if player has food available
local hasFood = AltoClef.hasFood()            -- True if any food in inventory

-- Trigger eating food
AltoClef.eatFood()                            -- Eats available food
```

---

## 📍 Position & Location

### 🗺️ Position Access
```lua
-- Get complete position as table
local pos = AltoClef.getPlayerPos()           -- Returns {x, y, z}

-- Get individual coordinates
local x = AltoClef.getPlayerX()               -- Individual X coordinate
local y = AltoClef.getPlayerY()               -- Individual Y coordinate  
local z = AltoClef.getPlayerZ()               -- Individual Z coordinate
```

### 🎯 Movement Commands
```lua
-- Move to specific position (if available)
AltoClef.moveTo(x, y, z)                      -- Move to coordinates

-- Look at specific position (if available)
AltoClef.lookAt(x, y, z)                      -- Look at coordinates
```

### 🌍 Game State
```lua
-- Check if player is in-game (not in menu)
local inGame = AltoClef.isInGame()            -- True if in world

-- Check if singleplayer world
local singleplayer = AltoClef.isSingleplayer()  -- True if singleplayer

-- Get world difficulty
local difficulty = AltoClef.getDifficulty()   -- World difficulty setting
```

---

### 🎮 Attack & Use Controls
```lua
-- Attack/punch (left click)
AltoClef.attack()                             -- Triggers attack action
Utils.Player.attack()                         -- Alternative access

-- Use/interact (right click)  
AltoClef.use()                                -- Triggers use/interact action
Utils.Player.use()                            -- Alternative access
```

### 🏃 Movement Controls
```lua
-- Sneak control
AltoClef.sneak(true)                          -- Hold sneak
AltoClef.sneak(false)                         -- Release sneak
Utils.Player.sneak(true/false)                -- Alternative access

-- Sprint control
AltoClef.sprint(true)                         -- Hold sprint
AltoClef.sprint(false)                        -- Release sprint  
Utils.Player.sprint(true/false)               -- Alternative access
```

### 🔍 Key Detection
```lua
-- Check if specific keys are being held
local isSneaking = AltoClef.isHoldingKey("sneak")
local isSprinting = AltoClef.isHoldingKey("sprint")
local isJumping = AltoClef.isHoldingKey("jump")

-- Supported keys: "sneak", "sprint", "jump", "attack", "use", 
--                 "left_click", "right_click", "w", "a", "s", "d"
```

### 🎯 Hotbar Management
```lua
-- Select hotbar slot (1-9)
AltoClef.selectHotbarSlot(1)                  -- Select slot 1
Utils.Player.selectHotbarSlot(slot)           -- Alternative access

-- Get current selected slot
local currentSlot = AltoClef.getSelectedHotbarSlot()  -- Returns 1-9
```

---

## 🎮 Enhanced Player Status

### 🌟 Experience System
```lua
-- Experience points and levels
local totalXP = AltoClef.getXP()              -- Total experience points
local level = AltoClef.getLevel()             -- Current experience level
local progress = AltoClef.getXPProgress()     -- Progress to next level (0.0-1.0)
```

### ⚗️ Status Effects & Potions
```lua
-- Get all active effects
local effects = AltoClef.getActiveEffects()
for i = 1, #effects do
    local effect = effects[i]
    local name = effect.name                   -- Effect name
    local amplifier = effect.amplifier         -- Effect level (0-based)
    local duration = effect.duration           -- Duration in ticks
end

-- Check for specific effects
local hasSpeed = AltoClef.hasEffect("speed")
local hasPoison = AltoClef.hasEffect("poison")
```

### 🎮 Gamemode & Abilities
```lua
-- Gamemode detection
local gameMode = AltoClef.getGameMode()       -- "CREATIVE", "SURVIVAL", "SPECTATOR"
local isCreative = AltoClef.isCreative()      -- Boolean
local canFly = AltoClef.canFly()              -- Can player fly?
local isFlying = AltoClef.isFlying()          -- Is player currently flying?
```

---

## 🧪 Examples

### Example 1: Advanced Movement Control
```lua
--[[
Advanced Movement Script - Demonstrates jump and velocity control
]]--

local JUMP_COOLDOWN = 500 -- milliseconds
local lastJumpTime = 0
local movementMode = "normal"

function onTick()
    if not AltoClef.isInGame() then return end
    
    handleMovementLogic()
    monitorPlayerState()
end

function handleMovementLogic()
    local currentTime = os.clock() * 1000
    
    if movementMode == "auto_jump" then
        -- Auto-jump every 500ms when not already jumping
        if currentTime - lastJumpTime > JUMP_COOLDOWN then
            if not AltoClef.isJumping() then
                AltoClef.jump()
                lastJumpTime = currentTime
                AltoClef.log("Auto-jump activated!")
            end
        end
    elseif movementMode == "speed_boost" then
        -- Apply forward speed boost
        local velocity = AltoClef.getVelocity()
        if velocity then
            AltoClef.setVelocity(velocity.x * 1.2, velocity.y, velocity.z * 1.2)
        end
    elseif movementMode == "hover" then
        -- Maintain altitude
        local velocity = AltoClef.getVelocity()
        if velocity and velocity.y < -0.1 then
            AltoClef.setVelocity(velocity.x, 0.05, velocity.z)
        end
    end
end

function monitorPlayerState()
    local velocity = AltoClef.getVelocity()
    if velocity then
        local speed = math.sqrt(velocity.x^2 + velocity.z^2)
        
        -- Safety check for excessive speed
        if speed > 2.0 then
            AltoClef.log("⚠️ Speed limit exceeded, applying brakes...")
            AltoClef.setVelocity(velocity.x * 0.5, velocity.y, velocity.z * 0.5)
        end
        
        -- Log interesting movement states
        if AltoClef.isJumping() and speed > 0.5 then
            AltoClef.log(string.format("High-speed jump! Speed: %.2f", speed))
        end
    end
end

-- Mode switching functions
function setAutoJumpMode()
    movementMode = "auto_jump"
    AltoClef.log("🦘 Auto-jump mode activated!")
end

function setSpeedBoostMode()
    movementMode = "speed_boost"
    AltoClef.log("⚡ Speed boost mode activated!")
end

function setHoverMode()
    movementMode = "hover"
    AltoClef.log("🚁 Hover mode activated!")
end

function setNormalMode()
    movementMode = "normal"
    AltoClef.log("👤 Normal movement mode")
end
```

### Health & Hunger Monitor
```lua
--[[
Health & Hunger Monitoring Script
]]--

local HEALTH_WARNING_THRESHOLD = 10
local HUNGER_WARNING_THRESHOLD = 8
local lastWarningTime = 0
local WARNING_COOLDOWN = 10000 -- 10 seconds

function onTick()
    if not AltoClef.isInGame() then return end
    
    local currentTime = os.clock() * 1000
    if currentTime - lastWarningTime < WARNING_COOLDOWN then return end
    
    checkHealthStatus()
    checkHungerStatus()
end

function checkHealthStatus()
    local health = AltoClef.getHealth()
    local healthPercent = AltoClef.getHealthPercent()
    
    if health < HEALTH_WARNING_THRESHOLD then
        AltoClef.log("🚨 CRITICAL HEALTH WARNING!")
        AltoClef.log(string.format("  Health: %.1f/20 (%.0f%%)", health, healthPercent))
        
        if AltoClef.isLowHealth() then
            AltoClef.log("  Status: CRITICALLY LOW - SEEK SAFETY!")
        end
        
        lastWarningTime = os.clock() * 1000
    end
end

function checkHungerStatus()
    local hunger = AltoClef.getHunger()
    local saturation = AltoClef.getSaturation()
    local hasFood = AltoClef.hasFood()
    local needsFood = AltoClef.needsFood()
    
    if hunger < HUNGER_WARNING_THRESHOLD then
        AltoClef.log("🍖 HUNGER WARNING!")
        AltoClef.log(string.format("  Hunger: %d/20", hunger))
        AltoClef.log(string.format("  Saturation: %.1f", saturation))
        AltoClef.log("  Has food: " .. (hasFood and "Yes" or "No"))
        
        if needsFood and hasFood then
            AltoClef.log("  🍞 Auto-eating available food...")
            AltoClef.eatFood()
        elseif needsFood and not hasFood then
            AltoClef.log("  ⚠️ NO FOOD AVAILABLE - Try @food command")
        end
        
        lastWarningTime = os.clock() * 1000
    end
end

function onEnable()
    AltoClef.log("🔍 Health & Hunger Monitor enabled!")
    AltoClef.log(string.format("  Health warning at: %d HP", HEALTH_WARNING_THRESHOLD))
    AltoClef.log(string.format("  Hunger warning at: %d points", HUNGER_WARNING_THRESHOLD))
end
```

### Position Tracking
```lua
--[[
Position Tracking & Movement Analysis
]]--

local previousPos = nil
local positionHistory = {}
local MAX_HISTORY = 20

function onTick()
    if not AltoClef.isInGame() then return end
    
    trackPosition()
    analyzeMovement()
end

function trackPosition()
    local currentPos = AltoClef.getPlayerPos()
    
    -- Store position in history
    table.insert(positionHistory, {
        pos = currentPos,
        time = os.clock(),
        velocity = AltoClef.getVelocity()
    })
    
    -- Limit history size
    if #positionHistory > MAX_HISTORY then
        table.remove(positionHistory, 1)
    end
    
    previousPos = currentPos
end

function analyzeMovement()
    if #positionHistory < 2 then return end
    
    local current = positionHistory[#positionHistory]
    local previous = positionHistory[#positionHistory - 1]
    
    -- Calculate distance moved
    local dx = current.pos.x - previous.pos.x
    local dy = current.pos.y - previous.pos.y
    local dz = current.pos.z - previous.pos.z
    local distance = math.sqrt(dx*dx + dy*dy + dz*dz)
    
    -- Calculate speed (blocks per second)
    local timeDiff = current.time - previous.time
    local speed = distance / timeDiff
    
    -- Log interesting movement
    if speed > 10 then -- Very fast movement
        AltoClef.log(string.format("🚀 High speed detected: %.2f blocks/sec", speed))
    end
    
    -- Detect teleportation (instant long-distance movement)
    if distance > 50 and timeDiff < 0.1 then
        AltoClef.log(string.format("⚡ Teleportation detected: %.1f blocks", distance))
    end
    
    -- Track vertical movement
    if math.abs(dy) > 5 then
        if dy > 0 then
            AltoClef.log(string.format("⬆️ Ascending: +%.1f blocks", dy))
        else
            AltoClef.log(string.format("⬇️ Descending: %.1f blocks", dy))
        end
    end
end

function getCurrentSpeed()
    if #positionHistory < 2 then return 0 end
    
    local current = positionHistory[#positionHistory]
    local previous = positionHistory[#positionHistory - 1]
    
    local dx = current.pos.x - previous.pos.x
    local dz = current.pos.z - previous.pos.z
    local distance = math.sqrt(dx*dx + dz*dz)
    local timeDiff = current.time - previous.time
    
    return distance / timeDiff
end
```

---

### ⚡ **Input Controls**

#### Attack & Use Controls
```lua
-- Attack/punch (left click)
AltoClef.attack()                             -- Triggers attack action
Utils.Player.attack()                         -- Alternative access

-- Use/interact (right click)  
AltoClef.use()                                -- Triggers use/interact action
Utils.Player.use()                            -- Alternative access
```

#### Movement Controls
```lua
-- Sneak control
AltoClef.sneak(true)                          -- Hold sneak
AltoClef.sneak(false)                         -- Release sneak
AltoClef.sneak()                              -- Single sneak press
Utils.Player.sneak(true/false)                -- Alternative access

-- Sprint control
AltoClef.sprint(true)                         -- Hold sprint
AltoClef.sprint(false)                        -- Release sprint  
AltoClef.sprint()                             -- Single sprint press
Utils.Player.sprint(true/false)               -- Alternative access
```

#### Key Detection
```lua
-- Check if specific keys are being held
local isSneaking = AltoClef.isHoldingKey("sneak")
local isSprinting = AltoClef.isHoldingKey("sprint")
local isJumping = AltoClef.isHoldingKey("jump")
local isAttacking = AltoClef.isHoldingKey("attack")  -- or "left_click"
local isUsing = AltoClef.isHoldingKey("use")         -- or "right_click"
local isMoving = AltoClef.isHoldingKey("w")          -- WASD movement keys

-- Supported key names: "sneak", "sprint", "jump", "attack", "use", 
--                      "left_click", "right_click", "w", "a", "s", "d",
--                      "forward", "back", "left", "right"
```

#### Hotbar Management
```lua
-- Select hotbar slot (1-9)
AltoClef.selectHotbarSlot(1)                  -- Select slot 1
AltoClef.selectHotbarSlot(9)                  -- Select slot 9
Utils.Player.selectHotbarSlot(slot)           -- Alternative access

-- Get current selected slot
local currentSlot = AltoClef.getSelectedHotbarSlot()  -- Returns 1-9
local currentSlot = Utils.Player.getSelectedHotbarSlot()  -- Alternative
```

### 🎯 **Enhanced Player Status & Effects**

#### Experience System
```lua
-- Experience points and levels
local totalXP = AltoClef.getXP()              -- Total experience points
local level = AltoClef.getLevel()             -- Current experience level
local progress = AltoClef.getXPProgress()     -- Progress to next level (0.0-1.0)

-- Alternative access
local totalXP = Utils.Player.getXP()
local level = Utils.Player.getLevel()
```

#### Status Effects & Potions
```lua
-- Get all active effects
local effects = AltoClef.getActiveEffects()
for i = 1, #effects do
    local effect = effects[i]
    local name = effect.name                   -- Effect name
    local amplifier = effect.amplifier         -- Effect level (0-based)
    local duration = effect.duration           -- Duration in ticks
    local infinite = effect.infinite           -- Is effect infinite?
    local visible = effect.visible             -- Is effect visible?
end

-- Check for specific effects
local hasSpeed = AltoClef.hasEffect("speed")
local hasPoison = AltoClef.hasEffect("poison")  
local hasStrength = AltoClef.hasEffect("strength")

-- Get specific effect details
local speedEffect = AltoClef.getEffect("speed")
if speedEffect then
    local level = speedEffect.amplifier + 1    -- Effect level (1-based)
    local timeLeft = speedEffect.duration      -- Ticks remaining
end

-- Supported effect names: "speed", "slowness", "haste", "mining_fatigue",
--                         "strength", "instant_health", "instant_damage",
--                         "jump_boost", "nausea", "regeneration", "resistance",
--                         "fire_resistance", "water_breathing", "invisibility",
--                         "blindness", "night_vision", "hunger", "weakness",
--                         "poison", "wither", "absorption", "saturation",
--                         "levitation", "luck", "unluck"
```

#### Gamemode & Abilities
```lua
-- Gamemode detection
local gameMode = AltoClef.getGameMode()       -- "CREATIVE", "SURVIVAL", "SPECTATOR", "UNKNOWN"
local isCreative = AltoClef.isCreative()      -- Boolean
local isSpectator = AltoClef.isSpectator()    -- Boolean

-- Alternative access
local gameMode = Utils.Player.getGameMode()
local isCreative = Utils.Player.isCreative()

-- Flight abilities
local canFly = AltoClef.canFly()              -- Can player fly?
local isFlying = AltoClef.isFlying()          -- Is player currently flying?
local flySpeed = AltoClef.getFlySpeed()       -- Flight speed
local walkSpeed = AltoClef.getWalkSpeed()     -- Walking speed

-- Alternative access
local canFly = Utils.Player.canFly()
```

#### Enhanced Player State
```lua
-- Player state checks
local onGround = AltoClef.isOnGround()         -- Is player on ground?
local inWater = AltoClef.isInWater()           -- Is player in water?
local inLava = AltoClef.isInLava()             -- Is player in lava?
local sneaking = AltoClef.isSneaking()         -- Is player sneaking?
local sprinting = AltoClef.isSprinting()       -- Is player sprinting?

-- Physical stats
local fallDistance = AltoClef.getFallDistance()  -- Current fall distance
local armor = AltoClef.getArmor()              -- Armor points (0-20)
local absorption = AltoClef.getAbsorption()    -- Absorption hearts

-- Look direction
local yaw = AltoClef.getYaw()                  -- Horizontal rotation (-180 to 180)
local pitch = AltoClef.getPitch()              -- Vertical rotation (-90 to 90)

-- Set look direction
AltoClef.setLook(yaw, pitch)                   -- Set camera direction
```

## 🚧 **Planned Features** 

### 🎮 **Advanced Input Controls** (Coming Soon)
```lua
-- These will be implemented in future phases
AltoClef.dropItem()                           -- Drop items (Q key)
AltoClef.openInventory()                      -- Open inventory (E key)
AltoClef.openChat()                           -- Open chat (T key)
```

### 🔄 **Respawn & Death System** (Coming Soon)
```lua
-- Death detection and respawn handling
AltoClef.isDead()                             -- Check if player is dead
AltoClef.getRespawnPoint()                    -- Get respawn location
AltoClef.setBedSpawn(x, y, z)                 -- Set bed as spawn point
```

---

## 🔍 **Debug Commands**

Use these commands to test and debug Player APIs:

```bash
@luadebug player      # Show jump and velocity control APIs
@luladebug health     # Show health API information
@luladebug hunger     # Show hunger API information  
@luladebug position   # Show position API information
```

---

## 💡 **Best Practices**

### Performance Optimization
```lua
-- ❌ Bad: Checking expensive operations every tick
function onTick()
    local pos = AltoClef.getPlayerPos()
    -- Expensive calculation every tick
end

-- ✅ Good: Throttled checking
local lastCheck = 0
local CHECK_INTERVAL = 1000 -- 1 second

function onTick()
    local currentTime = os.clock() * 1000
    if currentTime - lastCheck < CHECK_INTERVAL then return end
    
    local pos = AltoClef.getPlayerPos()
    -- Do work here
    lastCheck = currentTime
end
```

### Error Handling
```lua
function safeGetHealth()
    local success, health = pcall(function()
        return AltoClef.getHealth()
    end)
    
    if success then
        return health
    else
        AltoClef.logWarning("Failed to get health: " .. tostring(health))
        return 0
    end
end
```

### State Validation
```lua
function onTick()
    -- Always check if in-game before using player APIs
    if not AltoClef.isInGame() then return end
    
    -- Your player API calls here
    local health = AltoClef.getHealth()
    -- ...
end
```

---

## 💡 Best Practices

### ✅ Performance Optimization
```lua
-- ✅ Good: Throttled checking
local lastCheck = 0
local CHECK_INTERVAL = 1000 -- 1 second

function onTick()
    if not AltoClef.isInGame() then return end
    
    local currentTime = os.clock() * 1000
    if currentTime - lastCheck < CHECK_INTERVAL then return end
    
    local health = AltoClef.getHealth()
    -- Do work here
    lastCheck = currentTime
end

-- ❌ Bad: Expensive operations every tick
function onTick()
    local health = AltoClef.getHealth() -- Called 20 times per second
end
```

### ✅ Error Handling
```lua
function safeGetHealth()
    if not AltoClef.isInGame() then
        return 0, "Not in game"
    end
    
    local success, health = pcall(function()
        return AltoClef.getHealth()
    end)
    
    if success then
        return health
    else
        AltoClef.logWarning("Failed to get health: " .. tostring(health))
        return 0
    end
end
```

### ✅ State Validation
```lua
function onTick()
    -- Always check if in-game before using player APIs
    if not AltoClef.isInGame() then return end
    
    -- Your player API calls here
    local health = AltoClef.getHealth()
    -- ...
end
```

---

## 🔗 Related Topics

**Next Steps:**
- [🌍 World APIs](04_WORLD_APIS.md) - Interact with blocks, time, weather, and dimensions
- [🎒 Inventory APIs](05_INVENTORY_APIS.md) - Manage items, equipment, and containers

**See Also:**
- [⚡ Control APIs](06_CONTROL_APIS.md) - Advanced input controls and automation
- [🔍 Debug Tools](09_DEBUG_TOOLS.md) - Debug player-related issues
- [📖 Examples](10_EXAMPLES.md) - More complete script examples

---

## 🚨 Troubleshooting

### Common Issues

**API functions returning nil/error**
- **Cause:** Called when not in game or player not loaded
- **Solution:** Always check `AltoClef.isInGame()` first
- **Debug:** `@luadebug player` to check player state

**Movement/physics not working**
- **Cause:** Player not on ground or in creative mode
- **Solution:** Check `AltoClef.isOnGround()` and game mode
- **Debug:** `@luadebug position` to see current state

**Input controls not responding**
- **Cause:** Key names incorrect or conflicting inputs
- **Solution:** Use exact key names from documentation
- **Debug:** `@luadebug player` to verify key detection

### Debug Commands
```bash
@luadebug player      # Show player movement and physics info
@luadebug health      # Display health and hunger status  
@luadebug position    # Show position and velocity data
``` 