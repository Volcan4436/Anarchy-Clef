# Control APIs

**Status**: Complete  
**Version**: 1.0  
**Created**: 2025-01-10

Control APIs provide comprehensive input control, actions, and automation capabilities for Lua scripts. These APIs allow scripts to simulate keyboard inputs, control camera movement, monitor player actions, and implement complex automation sequences.

## Overview

The Control APIs offer two access patterns:
- **Main API**: `AltoClef.functionName()`
- **Utils API**: `Utils.Control.functionName()`

Both patterns provide identical functionality with full error handling and thread safety.

## Basic Key Controls

### Input Simulation

```lua
-- Press a key once (single press)
AltoClef.pressKey(keyName)
Utils.Control.pressKey(keyName)

-- Hold a key down continuously
AltoClef.holdKey(keyName)
Utils.Control.holdKey(keyName)

-- Release a held key
AltoClef.releaseKey(keyName)
Utils.Control.releaseKey(keyName)

-- Check if a key is currently being held
local isHeld = AltoClef.isHoldingKey(keyName)
local isHeld = Utils.Control.isHoldingKey(keyName)
```

### Supported Keys

All key functions support the following key names:

- **Movement**: `"forward"`, `"back"`, `"left"`, `"right"`, `"w"`, `"a"`, `"s"`, `"d"`
- **Actions**: `"attack"`, `"use"`, `"left_click"`, `"right_click"`
- **Character**: `"jump"`, `"sneak"`, `"sprint"`

### Example: Basic Movement Control

```lua
-- Simple forward movement
AltoClef.holdKey("forward")
Utils.Time.sleep(2000)  -- Move forward for 2 seconds
AltoClef.releaseKey("forward")

-- Sprint jumping
AltoClef.holdKey("sprint")
AltoClef.holdKey("forward")
AltoClef.pressKey("jump")
Utils.Time.sleep(1000)
AltoClef.releaseKey("forward")
AltoClef.releaseKey("sprint")
```

## Special Actions

### Item and Interface Control

```lua
-- Drop currently held item (Q key)
local success = AltoClef.dropItem()
local success = Utils.Control.dropItem()

-- Open player inventory (E key)
local success = AltoClef.openInventory()
local success = Utils.Control.openInventory()

-- Open chat interface (T key)  
local success = AltoClef.openChat()
local success = Utils.Control.openChat()
```

### Example: Inventory Management

```lua
-- Auto-drop unwanted items
local unwantedItems = {"dirt", "cobblestone", "gravel"}

for _, itemName in ipairs(unwantedItems) do
   if AltoClef.hasItem(itemName) then
       -- Select item in hotbar first
       local slot = AltoClef.getItemSlot(itemName)
       if slot then
           AltoClef.selectHotbarSlot(slot)
           Utils.Time.sleep(100)
           AltoClef.dropItem()
           AltoClef.log("Dropped " .. itemName)
       end
   end
end
```

## Look Control

### Camera Movement

```lua
-- Look at specific yaw/pitch angles
AltoClef.lookAt(yaw, pitch)
Utils.Control.lookAt(yaw, pitch)

-- Instantly set look direction (alias for lookAt)
AltoClef.setLook(yaw, pitch)  
Utils.Control.setLook(yaw, pitch)
```

**Parameters:**
- `yaw`: Horizontal rotation (-180 to 180, 0 = North, 90 = East)
- `pitch`: Vertical rotation (-90 to 90, -90 = up, 90 = down)

### Example: Automated Scanning

```lua
-- 360-degree horizontal scan
local scanAngles = {0, 45, 90, 135, 180, 225, 270, 315}

AltoClef.log("Starting area scan...")
for i, angle in ipairs(scanAngles) do
   AltoClef.lookAt(angle, 0)
   Utils.Time.sleep(500)
   
   -- Check for entities or blocks at this angle
   local entities = AltoClef.getEntitiesInRange(20)
   if #entities > 0 then
       AltoClef.log("Found " .. #entities .. " entities at angle " .. angle)
   end
end

-- Return to north
AltoClef.lookAt(0, 0)
```

## Block Breaking Monitor

### Breaking Status Detection

```lua
-- Check if currently breaking a block
local isBreaking = AltoClef.isBreakingBlock()
local isBreaking = Utils.Control.isBreakingBlock()

-- Get position of block being broken
local pos = AltoClef.getBreakingBlockPos()
local pos = Utils.Control.getBreakingBlockPos()
-- Returns: {x = number, y = number, z = number} or nil

-- Get breaking progress (0.0 to 1.0)
local progress = AltoClef.getBreakingProgress()
local progress = Utils.Control.getBreakingProgress()
```

### Example: Mining Progress Monitor

```lua
-- Monitor mining progress with feedback
local function monitorMining()
   while true do
       local isBreaking = AltoClef.isBreakingBlock()
       
       if isBreaking then
           local pos = AltoClef.getBreakingBlockPos()
           local progress = AltoClef.getBreakingProgress()
           
           if pos then
               local progressPercent = math.floor(progress * 100)
               AltoClef.log(string.format("Mining block at (%d, %d, %d) - %d%% complete",
                          pos.x, pos.y, pos.z, progressPercent))
               
               -- Play sound at certain progress milestones
               if progressPercent >= 50 and progressPercent < 55 then
                   AltoClef.log("Mining halfway complete!")
               elseif progressPercent >= 90 then
                   AltoClef.log("Almost done mining!")
               end
           end
       end
       
       Utils.Time.sleep(250)  -- Check 4 times per second
   end
end
```

## Advanced Control Patterns

### Key Combination Automation

```lua
-- Complex movement patterns
local function performParkourSequence()
   -- Sprint jump forward
   AltoClef.holdKey("sprint")
   AltoClef.holdKey("forward")
   AltoClef.pressKey("jump")
   Utils.Time.sleep(800)
   
   -- Mid-air strafe
   AltoClef.releaseKey("forward")
   AltoClef.holdKey("right")
   Utils.Time.sleep(300)
   
   -- Land and continue
   AltoClef.releaseKey("right")
   AltoClef.holdKey("forward")
   Utils.Time.sleep(500)
   
   -- Stop
   AltoClef.releaseKey("forward")
   AltoClef.releaseKey("sprint")
end
```

### Combat Automation

```lua
-- Simple combat sequence
local function basicCombat()
   -- Face the target (assumes target position is known)
   local targetPos = AltoClef.getClosestEntity("zombie")
   if targetPos then
       local yaw = Utils.Math.angleTo(AltoClef.getPlayerPos(), targetPos)
       AltoClef.lookAt(yaw, 0)
       
       -- Attack pattern: strike, back up, strike
       AltoClef.pressKey("attack")
       Utils.Time.sleep(100)
       
       AltoClef.holdKey("back")
       Utils.Time.sleep(300)
       AltoClef.releaseKey("back")
       
       AltoClef.pressKey("attack")
       Utils.Time.sleep(600)  -- Wait for attack cooldown
   end
end
```

## Error Handling

### Input Validation

All Control APIs include comprehensive error handling:

```lua
-- Safe key operations with validation
local function safeKeyPress(keyName)
   if not keyName or type(keyName) ~= "string" then
       AltoClef.log("Error: Invalid key name")
       return false
   end
   
   local result = AltoClef.pressKey(keyName)
   if not result then
       AltoClef.log("Warning: Key press failed for " .. keyName)
   end
   
   return result
end

-- Safe look control with bounds checking
local function safeLookAt(yaw, pitch)
   -- Clamp values to valid ranges
   yaw = Utils.Math.clamp(yaw, -180, 180)
   pitch = Utils.Math.clamp(pitch, -90, 90)
   
   return AltoClef.lookAt(yaw, pitch)
end
```

## Practical Examples

### Example 1: Auto-Walk with Obstacle Avoidance

```lua
local function autoWalkWithAvoidance(distance)
   local startPos = AltoClef.getPlayerPos()
   local targetDistance = distance or 50
   
   AltoClef.holdKey("forward")
   
   while true do
       local currentPos = AltoClef.getPlayerPos()
       local distanceTraveled = Utils.Math.distance2D(startPos, currentPos)
       
       if distanceTraveled >= targetDistance then
           break
       end
       
       -- Check for obstacles ahead
       local frontPos = {
           x = currentPos.x + math.sin(math.rad(AltoClef.getYaw())),
           y = currentPos.y,
           z = currentPos.z - math.cos(math.rad(AltoClef.getYaw()))
       }
       
       local blockAhead = AltoClef.getBlock(frontPos.x, frontPos.y, frontPos.z)
       if blockAhead ~= "air" then
           -- Try to jump over obstacle
           AltoClef.pressKey("jump")
           Utils.Time.sleep(200)
       end
       
       Utils.Time.sleep(100)
   end
   
   AltoClef.releaseKey("forward")
   AltoClef.log("Auto-walk completed: " .. distanceTraveled .. " blocks")
end
```

### Example 2: Resource Collection Automation

```lua
local function autoCollectResources()
   local collectionRadius = 10
   local itemsToCollect = {"diamond", "iron_ingot", "gold_ingot", "emerald"}
   
   -- Scan in a circle
   for angle = 0, 350, 45 do
       AltoClef.lookAt(angle, -15)  -- Look slightly down
       Utils.Time.sleep(200)
       
       -- Check for items on ground
       local nearbyItems = AltoClef.getItemsInRange(collectionRadius)
       
       for _, item in ipairs(nearbyItems) do
           if Utils.Table.contains(itemsToCollect, item.name) then
               AltoClef.log("Found " .. item.name .. " at distance " .. item.distance)
               
               -- Navigate to item
               local yawToItem = Utils.Math.angleTo(AltoClef.getPlayerPos(), item.pos)
               AltoClef.lookAt(yawToItem, 0)
               
               -- Walk towards item
               AltoClef.holdKey("forward")
               while Utils.Math.distance2D(AltoClef.getPlayerPos(), item.pos) > 2 do
                   Utils.Time.sleep(100)
               end
               AltoClef.releaseKey("forward")
               
               -- Item should be auto-collected
               Utils.Time.sleep(500)
           end
       end
   end
end
```

### Example 3: Building Block Placer

```lua
local function autoBuildWall(length, height)
   local buildMaterial = "cobblestone"
   
   if not AltoClef.hasItem(buildMaterial) then
       AltoClef.log("Error: No " .. buildMaterial .. " available")
       return false
   end
   
   -- Select building material
   AltoClef.equipItem(buildMaterial)
   
   for y = 1, height do
       for x = 1, length do
           -- Look at placement position
           AltoClef.lookAt(90, 45)  -- Look right and down
           
           -- Place block
           AltoClef.pressKey("use")
           Utils.Time.sleep(100)
           
           -- Move forward
           if x < length then
               AltoClef.holdKey("forward")
               Utils.Time.sleep(300)
               AltoClef.releaseKey("forward")
           end
       end
       
       -- Move up for next layer
       if y < height then
           AltoClef.pressKey("jump")
           Utils.Time.sleep(400)
           
           -- Move back to start position
           AltoClef.lookAt(270, 0)  -- Look left
           AltoClef.holdKey("forward")
           Utils.Time.sleep(length * 300)
           AltoClef.releaseKey("forward")
       end
   end
   
   AltoClef.log("Wall construction completed: " .. length .. "x" .. height)
end
```

## Performance Considerations

### Timing and Delays

- **Key Press Timing**: Allow 50-100ms between rapid key presses
- **Movement Delays**: Use 100-500ms delays for movement operations
- **Look Control**: Allow 200-500ms for look direction changes to settle
- **Complex Sequences**: Add appropriate delays between automation steps

### Resource Management

```lua
-- Efficient key state management
local activeKeys = {}

local function startKeyHold(key)
   if not activeKeys[key] then
       AltoClef.holdKey(key)
       activeKeys[key] = true
   end
end

local function stopKeyHold(key)
   if activeKeys[key] then
       AltoClef.releaseKey(key)
       activeKeys[key] = false
   end
end

-- Cleanup function
local function releaseAllKeys()
   for key, _ in pairs(activeKeys) do
       AltoClef.releaseKey(key)
   end
   activeKeys = {}
end
```

## Integration with Other APIs

### Combining Control with Player APIs

```lua
-- Health-aware movement
local function smartMovement()
   local health = AltoClef.getHealth()
   local hunger = AltoClef.getHunger()
   
   if health < 6 then
       -- Low health: sneak and move carefully
       AltoClef.holdKey("sneak")
       AltoClef.log("Low health detected - sneaking mode")
   elseif hunger > 6 then
       -- Good hunger: can sprint
       AltoClef.holdKey("sprint")
   end
   
   AltoClef.holdKey("forward")
   Utils.Time.sleep(3000)
   
   -- Clean up
   AltoClef.releaseKey("forward")
   AltoClef.releaseKey("sprint")
   AltoClef.releaseKey("sneak")
end
```

### Combining Control with Inventory APIs

```lua
-- Auto-equip tools while mining
local function smartMining()
   while AltoClef.isBreakingBlock() do
       local pos = AltoClef.getBreakingBlockPos()
       if pos then
           local blockType = AltoClef.getBlock(pos.x, pos.y, pos.z)
           
           -- Auto-equip appropriate tool
           if Utils.String.contains(blockType, "ore") then
               AltoClef.equipItem("iron_pickaxe")
           elseif Utils.String.contains(blockType, "log") then
               AltoClef.equipItem("iron_axe")
           end
       end
       
       Utils.Time.sleep(250)
   end
end
```

## Testing and Validation

### Control API Test Script

The comprehensive test script `control_demo.lua` validates all Control APIs:

```lua
-- Run comprehensive control tests
/run control_demo
```

**Test Categories:**
1. **Basic Key Controls** - Press, hold, release operations
2. **Advanced Input Testing** - All supported keys and combinations
3. **Look Control Testing** - Camera movement and positioning
4. **Block Breaking Monitor** - Breaking status and progress tracking
5. **Dual API Validation** - Consistency between AltoClef.* and Utils.Control.*
6. **Complex Control Automation** - Multi-step automation sequences
7. **Error Handling Test** - Invalid inputs and edge cases

## API Reference Summary

### Key Control Functions
- `pressKey(keyName)` - Single key press
- `holdKey(keyName)` - Continuous key hold
- `releaseKey(keyName)` - Release held key
- `isHoldingKey(keyName)` - Check key hold status

### Special Actions
- `dropItem()` - Drop held item (Q key)
- `openInventory()` - Open inventory (E key)
- `openChat()` - Open chat (T key)

### Look Control
- `lookAt(yaw, pitch)` - Set camera direction
- `setLook(yaw, pitch)` - Alias for lookAt

### Block Breaking Monitor
- `isBreakingBlock()` - Check if breaking a block
- `getBreakingBlockPos()` - Get breaking block position
- `getBreakingProgress()` - Get breaking progress (0.0-1.0)

## Best Practices

1. **Always release held keys** when automation completes
2. **Use appropriate delays** between rapid operations
3. **Validate inputs** before calling control functions
4. **Handle errors gracefully** with try-catch blocks
5. **Clean up resources** with cleanup functions
6. **Test thoroughly** with the provided test scripts
7. **Combine APIs effectively** for complex automation

The Control APIs provide the foundation for sophisticated automation systems, enabling scripts to interact with Minecraft through simulated player inputs while maintaining safety and reliability. 