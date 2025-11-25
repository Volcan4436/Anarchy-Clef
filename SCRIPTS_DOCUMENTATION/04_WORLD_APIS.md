# World APIs

**Version:** 1.0.0  
**Status:** Complete  
**Category:** Core APIs

## Overview

World APIs provide access to environmental information including time, weather, dimensions, biomes, blocks, and light levels. These APIs are essential for creating context-aware automation scripts that respond to environmental conditions.

---

## Table of Contents

1. [Quick Start](#-quick-start)
2. [Time & Weather](#-time--weather)
3. [Dimensions & Biomes](#-dimensions--biomes)
4. [Block Information](#-block-information)
5. [Light Levels](#-light-levels)
6. [World Interaction](#-world-interaction)
7. [Examples](#-examples)
8. [Best Practices](#-best-practices)
9. [Related Topics](#-related-topics)

---

## Quick Start

### Essential Functions
```lua
-- Time and weather
local time = AltoClef.getTimeOfDay()
local isRaining = AltoClef.isRaining()

-- Location info
local dimension = AltoClef.getCurrentDimension()
local biome = AltoClef.getCurrentBiome()

-- Block analysis
local block = AltoClef.getBlockAt(x, y, z)
local lightLevel = AltoClef.getLightLevelAt(x, y, z)

-- Alternative Utils API access
local time = Utils.World.getTimeOfDay()
local block = Utils.World.getBlockAt(x, y, z)
```

### Environmental Monitor
```lua
function onTick()
   if not AltoClef.isInGame() then return end
   
   local time = AltoClef.getTimeOfDay()
   if AltoClef.isNight() then
       AltoClef.log("Night time - hostile mobs spawning")
   end
   
   if AltoClef.isRaining() then
       AltoClef.log("Rainy weather detected")
   end
end
```

---

---

## Time & Weather

### Time Functions

```lua
-- Get absolute world time (total ticks since world creation)
local worldTime = AltoClef.getWorldTime()

-- Get time of day (0-24000 scale, where 0=dawn, 6000=noon, 12000=dusk, 18000=midnight)
local timeOfDay = AltoClef.getTimeOfDay()

-- Check if it's day time
local isDay = AltoClef.isDay()

-- Check if it's night time
local isNight = AltoClef.isNight()
```

### Weather Functions

```lua
-- Check weather conditions
local isRaining = AltoClef.isRaining()
local isThundering = AltoClef.isThundering()

-- Check if player can sleep (night time or thunderstorm)
local canSleep = AltoClef.canSleep()
```

### Example: Time-Based Actions

```lua
function onTick()
   if AltoClef.isNight() and AltoClef.canSleep() then
       AltoClef.log("It's nighttime! Time to sleep.")
   end
   
   if AltoClef.isRaining() then
       AltoClef.log("It's raining - seeking shelter!")
   end
   
   local timeOfDay = AltoClef.getTimeOfDay()
   if timeOfDay > 12000 and timeOfDay < 13000 then
       AltoClef.log("Sunset time - beautiful!")
   end
end
```

---

---

## Dimensions & Biomes

### Dimension Detection

### Current Dimension

```lua
-- Get current dimension as string
local dimension = AltoClef.getCurrentDimension()
-- Returns: "OVERWORLD", "NETHER", or "END"

-- Check specific dimensions
local isOverworld = AltoClef.isInOverworld()
local isNether = AltoClef.isInNether()
local isEnd = AltoClef.isInEnd()
```

### Example: Dimension-Specific Behavior

```lua
function onLoad()
   local dimension = AltoClef.getCurrentDimension()
   AltoClef.log("Currently in: " .. dimension)
   
   if AltoClef.isInNether() then
       AltoClef.log("In the Nether - watch out for lava!")
   elseif AltoClef.isInEnd() then
       AltoClef.log("In the End - beware the void!")
   else
       AltoClef.log("In the Overworld - home sweet home!")
   end
end
```

### Biome Information

### Current Biome Detection

```lua
-- Get current biome at player position
local biome = AltoClef.getCurrentBiome()
-- Returns: Full biome identifier like "minecraft:plains"

-- Check if in ocean biome
local isInOcean = AltoClef.isInOcean()

-- Get biome at specific coordinates
local biomeAt = AltoClef.getBiomeAt(x, y, z)
```

### Example: Biome-Based Actions

```lua
function analyzeSurroundings()
   local biome = AltoClef.getCurrentBiome()
   AltoClef.log("Current biome: " .. biome)
   
   if AltoClef.isInOcean() then
       AltoClef.log("In ocean biome - water everywhere!")
   end
   
   -- Check biomes in nearby chunks
   local pos = AltoClef.getPlayerPos()
   if pos then
       for dx = -16, 16, 16 do
           for dz = -16, 16, 16 do
               local nearbyBiome = AltoClef.getBiomeAt(pos.x + dx, pos.y, pos.z + dz)
               AltoClef.log("Biome at offset (" .. dx .. ", " .. dz .. "): " .. nearbyBiome)
           end
       end
   end
end
```

---

## Block Information

### Basic Block Detection

```lua
-- Get block type at coordinates
local blockName = AltoClef.getBlockAt(x, y, z)
-- Returns: Full block identifier like "minecraft:stone"

-- Check if specific block type at coordinates
local isStone = AltoClef.isBlockAt(x, y, z, "stone")
local isDirt = AltoClef.isBlockAt(x, y, z, "minecraft:dirt")

-- Check basic block properties
local isAir = AltoClef.isAirAt(x, y, z)
local isSolid = AltoClef.isSolidAt(x, y, z)
```

### Example: Block Analysis

```lua
function analyzeBlocksAroundPlayer()
   local pos = AltoClef.getPlayerPos()
   if not pos then return end
   
   local x, y, z = math.floor(pos.x), math.floor(pos.y), math.floor(pos.z)
   
   -- Check blocks around player
   for dx = -1, 1 do
       for dy = -1, 1 do
           for dz = -1, 1 do
               local blockX, blockY, blockZ = x + dx, y + dy, z + dz
               local block = AltoClef.getBlockAt(blockX, blockY, blockZ)
               local isSolid = AltoClef.isSolidAt(blockX, blockY, blockZ)
               
               AltoClef.log("Block at (" .. blockX .. ", " .. blockY .. ", " .. blockZ .. "): " .. block .. " (solid: " .. tostring(isSolid) .. ")")
           end
       end
   end
end
```

---

## Light Level Detection

### Light Level Functions

```lua
-- Get total light level (combines block and sky light)
local totalLight = AltoClef.getLightLevelAt(x, y, z)

-- Get block light level (from torches, lava, etc.)
local blockLight = AltoClef.getBlockLightAt(x, y, z)

-- Get sky light level (from sun/moon)
local skyLight = AltoClef.getSkyLightAt(x, y, z)
```

### Example: Lighting Analysis

```lua
function findDarkSpots()
   local pos = AltoClef.getPlayerPos()
   if not pos then return end
   
   local darkSpots = {}
   local radius = 5
   
   for dx = -radius, radius do
       for dy = -2, 2 do
           for dz = -radius, radius do
               local x, y, z = pos.x + dx, pos.y + dy, pos.z + dz
               local lightLevel = AltoClef.getLightLevelAt(x, y, z)
               
               -- Light level 7 or below allows mob spawning
               if lightLevel <= 7 then
                   table.insert(darkSpots, {x = x, y = y, z = z, light = lightLevel})
               end
           end
       end
   end
   
   AltoClef.log("Found " .. #darkSpots .. " dark spots where mobs can spawn:")
   for i, spot in ipairs(darkSpots) do
       AltoClef.log("  (" .. spot.x .. ", " .. spot.y .. ", " .. spot.z .. ") - Light: " .. spot.light)
   end
end
```

---

## Block Properties

### Hardness and Breaking

```lua
-- Get block hardness (-1 if unbreakable)
local hardness = AltoClef.getBlockHardnessAt(x, y, z)

-- Example: Find hardest nearby blocks
function findHardestBlocks()
   local pos = AltoClef.getPlayerPos()
   if not pos then return end
   
   local hardestBlock = {hardness = -1, name = "none", pos = nil}
   
   for dx = -3, 3 do
       for dy = -3, 3 do
           for dz = -3, 3 do
               local x, y, z = pos.x + dx, pos.y + dy, pos.z + dz
               local hardness = AltoClef.getBlockHardnessAt(x, y, z)
               local blockName = AltoClef.getBlockAt(x, y, z)
               
               if hardness > hardestBlock.hardness then
                   hardestBlock = {
                       hardness = hardness,
                       name = blockName,
                       pos = {x = x, y = y, z = z}
                   }
               end
           end
       end
   end
   
   if hardestBlock.pos then
       AltoClef.log("Hardest block: " .. hardestBlock.name .. " (hardness: " .. hardestBlock.hardness .. ")")
   end
end
```

---

## Special Block Detection

### Container and Interactive Blocks

```lua
-- Check for specific block types
local isChest = AltoClef.isChestAt(x, y, z)
local isInteractable = AltoClef.isInteractableAt(x, y, z)

-- Example: Find nearby containers
function findNearbyContainers()
   local pos = AltoClef.getPlayerPos()
   if not pos then return end
   
   local chests = {}
   local interactables = {}
   
   for dx = -10, 10 do
       for dy = -5, 5 do
           for dz = -10, 10 do
               local x, y, z = pos.x + dx, pos.y + dy, pos.z + dz
               
               if AltoClef.isChestAt(x, y, z) then
                   table.insert(chests, {x = x, y = y, z = z})
               elseif AltoClef.isInteractableAt(x, y, z) then
                   local blockName = AltoClef.getBlockAt(x, y, z)
                   table.insert(interactables, {x = x, y = y, z = z, block = blockName})
               end
           end
       end
   end
   
   AltoClef.log("Found " .. #chests .. " chests")
   AltoClef.log("Found " .. #interactables .. " other interactable blocks")
end
```

---

## World Interaction

### Position Validation

```lua
-- Check if position can be reached by the bot
local canReach = AltoClef.canReach(x, y, z)

-- Check if block can be broken
local canBreak = AltoClef.canBreak(x, y, z)

-- Check if block can be placed at position
local canPlace = AltoClef.canPlace(x, y, z)
```

### Example: Safe Building Location

```lua
function findSafeBuildingSpot()
   local pos = AltoClef.getPlayerPos()
   if not pos then return end
   
   local baseX, baseY, baseZ = math.floor(pos.x), math.floor(pos.y), math.floor(pos.z)
   
   for dx = -5, 5 do
       for dz = -5, 5 do
           for dy = 0, 3 do
               local x, y, z = baseX + dx, baseY + dy, baseZ + dz
               
               local canReach = AltoClef.canReach(x, y, z)
               local canPlace = AltoClef.canPlace(x, y, z)
               local isAir = AltoClef.isAirAt(x, y, z)
               local groundBelow = AltoClef.isSolidAt(x, y - 1, z)
               
               if canReach and canPlace and isAir and groundBelow then
                   AltoClef.log("Good building spot found at (" .. x .. ", " .. y .. ", " .. z .. ")")
                   return {x = x, y = y, z = z}
               end
           end
       end
   end
   
   AltoClef.log("No suitable building spots found nearby")
end
```

---

## Utility Functions

### Ground Height Calculation

```lua
-- Find ground level at X,Z coordinates
local groundY = AltoClef.getGroundHeight(x, z)

-- Example: Map terrain height
function mapTerrain()
   local pos = AltoClef.getPlayerPos()
   if not pos then return end
   
   local centerX, centerZ = math.floor(pos.x), math.floor(pos.z)
   
   AltoClef.log("Terrain height map around player:")
   for dz = -5, 5 do
       local line = ""
       for dx = -5, 5 do
           local height = AltoClef.getGroundHeight(centerX + dx, centerZ + dz)
           line = line .. string.format("%3d ", height)
       end
       AltoClef.log(line)
   end
end
```

---

## API Access Methods

All World APIs are available through two access patterns:

### 1. Main AltoClef API (Recommended)
```lua
-- Direct access - recommended for consistency
local timeOfDay = AltoClef.getTimeOfDay()
local blockName = AltoClef.getBlockAt(x, y, z)
local lightLevel = AltoClef.getLightLevelAt(x, y, z)
```

### 2. Utils API (Alternative)
```lua
-- Through Utils namespace
local timeOfDay = Utils.World.getTimeOfDay()
local blockName = Utils.World.getBlockAt(x, y, z)
local lightLevel = Utils.World.getLightLevelAt(x, y, z)
```

Both APIs provide identical functionality. Choose based on your preference for code organization.

---

## Example Scripts

### Complete World Scanner Script

```lua
-- Complete world analysis script
function analyzeWorld()
   AltoClef.log("=== WORLD ANALYSIS ===")
   
   -- Time and weather
   local timeOfDay = AltoClef.getTimeOfDay()
   local isDay = AltoClef.isDay()
   local isRaining = AltoClef.isRaining()
   
   AltoClef.log("Time: " .. timeOfDay .. " (Day: " .. tostring(isDay) .. ")")
   AltoClef.log("Weather: " .. (isRaining and "Raining" or "Clear"))
   
   -- Dimension and biome
   local dimension = AltoClef.getCurrentDimension()
   local biome = AltoClef.getCurrentBiome()
   
   AltoClef.log("Dimension: " .. dimension)
   AltoClef.log("Biome: " .. biome)
   
   -- Player surroundings
   local pos = AltoClef.getPlayerPos()
   if pos then
       local x, y, z = math.floor(pos.x), math.floor(pos.y), math.floor(pos.z)
       
       -- Block below
       local blockBelow = AltoClef.getBlockAt(x, y - 1, z)
       local lightLevel = AltoClef.getLightLevelAt(x, y, z)
       
       AltoClef.log("Standing on: " .. blockBelow)
       AltoClef.log("Light level: " .. lightLevel)
       
       -- Ground height
       local groundHeight = AltoClef.getGroundHeight(x, z)
       AltoClef.log("Ground height: " .. groundHeight)
   end
end

function onLoad()
   analyzeWorld()
end
```

### Light-Based Security System

```lua
-- Security system that monitors light levels
local secureArea = {
   minX = 100, maxX = 120,
   minY = 60, maxY = 80,
   minZ = 200, maxZ = 220
}

function checkSecurity()
   local darkSpots = 0
   local totalSpots = 0
   
   for x = secureArea.minX, secureArea.maxX do
       for y = secureArea.minY, secureArea.maxY do
           for z = secureArea.minZ, secureArea.maxZ do
               local lightLevel = AltoClef.getLightLevelAt(x, y, z)
               totalSpots = totalSpots + 1
               
               if lightLevel <= 7 then
                   darkSpots = darkSpots + 1
               end
           end
       end
   end
   
   local securityLevel = ((totalSpots - darkSpots) / totalSpots) * 100
   AltoClef.log("Security level: " .. string.format("%.1f", securityLevel) .. "%")
   
   if securityLevel < 90 then
       AltoClef.log("WARNING: Security compromised! " .. darkSpots .. " dark spots found!")
   end
end

function onTick()
   -- Check security every 10 seconds
   if AltoClef.getWorldTime() % 200 == 0 then
       checkSecurity()
   end
end
```

---

## Complete Reference

### Time & Weather
- `AltoClef.getWorldTime()` - Total world time in ticks
- `AltoClef.getTimeOfDay()` - Time of day (0-24000)
- `AltoClef.isDay()` - Returns true if daytime
- `AltoClef.isNight()` - Returns true if nighttime
- `AltoClef.isRaining()` - Returns true if raining
- `AltoClef.isThundering()` - Returns true if thunderstorm
- `AltoClef.canSleep()` - Returns true if player can sleep

### Dimensions
- `AltoClef.getCurrentDimension()` - Current dimension name
- `AltoClef.isInOverworld()` - True if in Overworld
- `AltoClef.isInNether()` - True if in Nether
- `AltoClef.isInEnd()` - True if in End

### Biomes
- `AltoClef.getCurrentBiome()` - Current biome identifier
- `AltoClef.getBiomeAt(x, y, z)` - Biome at coordinates
- `AltoClef.isInOcean()` - True if in ocean biome

### Blocks
- `AltoClef.getBlockAt(x, y, z)` - Block type at coordinates
- `AltoClef.isBlockAt(x, y, z, blockName)` - Check specific block
- `AltoClef.isAirAt(x, y, z)` - True if air block
- `AltoClef.isSolidAt(x, y, z)` - True if solid block
- `AltoClef.getBlockHardnessAt(x, y, z)` - Block hardness value

### Light Levels
- `AltoClef.getLightLevelAt(x, y, z)` - Total light level
- `AltoClef.getBlockLightAt(x, y, z)` - Block light only
- `AltoClef.getSkyLightAt(x, y, z)` - Sky light only

### Special Blocks
- `AltoClef.isChestAt(x, y, z)` - True if chest block
- `AltoClef.isInteractableAt(x, y, z)` - True if interactive block

### World Interaction
- `AltoClef.canReach(x, y, z)` - True if position reachable
- `AltoClef.canBreak(x, y, z)` - True if block can be broken
- `AltoClef.canPlace(x, y, z)` - True if block can be placed
- `AltoClef.getGroundHeight(x, z)` - Ground level at coordinates

All functions return appropriate default values (0, false, "air", etc.) if the world is not available or an error occurs.

---

## Testing

Use the provided test scripts to verify World API functionality:

- `world_info_test.lua` - Tests time, weather, dimension, and biome APIs
- `block_scanner_test.lua` - Tests block detection and light level APIs

Load these scripts to ensure all World APIs are working correctly in your environment. 