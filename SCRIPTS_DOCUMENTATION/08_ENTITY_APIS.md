# Entity Detection & Tracking APIs

**Phase 7 Implementation** - Added in v1.7.0  
Complete entity detection and tracking system with dual API access patterns.

## Overview

The Entity Detection & Tracking APIs provide comprehensive functionality for detecting, filtering, and tracking entities in the game world. These APIs give scripts the ability to monitor players, hostile entities, dropped items, and any other entities nearby.

### Key Features
- 🔍 **Entity Detection** - Find entities by range, type, or proximity
- 👥 **Player Tracking** - Monitor nearby players and their status
- ⚔️ **Hostile Monitoring** - Track threatening entities for safety
- 💎 **Item Detection** - Locate dropped items and loot
- 📊 **Detailed Information** - Get comprehensive entity data
- 🔄 **Dual API Access** - Both `AltoClef.*` and `Utils.Entity.*` patterns

## Dual API Access Pattern

All entity functions are available in two ways:
```lua
-- Primary API (AltoClef namespace)
local entities = AltoClef.getNearbyEntities(25)

-- Utils API (alternative access)
local entities = Utils.Entity.getNearbyEntities(25)
```

Both patterns provide identical functionality with the same parameters and return values.

---

## 🔍 Basic Entity Detection

### `getNearbyEntities(range)`
Get all entities within a specified range.

**Parameters:**
- `range` (number, optional): Detection range in blocks. Default: 20

**Returns:**
- `table`: Array of entity objects, or empty table if none found

**Example:**
```lua
-- Get entities within 25 blocks
local entities = AltoClef.getNearbyEntities(25)
AltoClef.log("Found " .. #entities .. " entities nearby")

for i, entity in ipairs(entities) do
    AltoClef.log("Entity: " .. entity.name .. " at distance " .. entity.distance)
end

-- Using Utils API
local entities = Utils.Entity.getNearbyEntities(25)
```

### `getClosestEntity()`
Get the single closest entity to the player.

**Returns:**
- `table`: Entity object of closest entity, or `nil` if none found

**Example:**
```lua
local closest = AltoClef.getClosestEntity()
if closest then
    AltoClef.log("Closest entity: " .. closest.name .. " at " .. closest.distance .. " blocks")
else
    AltoClef.log("No entities nearby")
end
```

### `countEntitiesOfType(entityType)`
Count entities of a specific type.

**Parameters:**
- `entityType` (string): Type of entity to count ("zombie", "player", "item", "all", etc.)

**Returns:**
- `number`: Count of entities of specified type

**Example:**
```lua
local totalEntities = AltoClef.countEntitiesOfType("all")
local zombies = AltoClef.countEntitiesOfType("zombie")
local players = AltoClef.countEntitiesOfType("player")

AltoClef.log("Total: " .. totalEntities .. ", Zombies: " .. zombies .. ", Players: " .. players)
```

---

## 🎯 Entity Type Filtering

### `getEntitiesOfType(entityType)`
Get all entities of a specific type.

**Parameters:**
- `entityType` (string): Type filter - supports many entity types

**Supported Entity Types:**
- **Players:** `"player"`, `"players"`
- **Hostile:** `"zombie"`, `"skeleton"`, `"creeper"`, `"spider"`, `"enderman"`, `"witch"`, `"slime"`
- **Passive:** `"cow"`, `"pig"`, `"sheep"`, `"chicken"`, `"horse"`, `"wolf"`, `"cat"`, `"villager"`
- **Items:** `"item"`, `"items"`, `"drop"`, `"drops"`
- **Categories:** `"hostile"`, `"passive"`, `"animal"`, `"mob"`, `"living"`, `"all"`

**Returns:**
- `table`: Array of matching entity objects

**Example:**
```lua
-- Get all zombies
local zombies = AltoClef.getEntitiesOfType("zombie")
for i, zombie in ipairs(zombies) do
    AltoClef.log("Zombie at distance: " .. zombie.distance)
end

-- Get all hostile entities
local hostiles = AltoClef.getEntitiesOfType("hostile")
AltoClef.log("Found " .. #hostiles .. " hostile entities")

-- Get all animals
local animals = AltoClef.getEntitiesOfType("animal")
```

---

## 👥 Player Detection & Tracking

### `getPlayersNearby(range)`
Get all players within range.

**Parameters:**
- `range` (number, optional): Detection range in blocks. Default: 50

**Returns:**
- `table`: Array of player entity objects with additional player data

**Example:**
```lua
local players = AltoClef.getPlayersNearby(100)
for i, player in ipairs(players) do
    AltoClef.log("Player: " .. player.username .. " at " .. player.distance .. " blocks")
    AltoClef.log("  UUID: " .. player.uuid)
    AltoClef.log("  Position: (" .. player.position.x .. ", " .. player.position.y .. ", " .. player.position.z .. ")")
end
```

### `isPlayerLoaded(username)`
Check if a specific player is currently loaded.

**Parameters:**
- `username` (string): Player's username to check

**Returns:**
- `boolean`: `true` if player is loaded, `false` otherwise

**Example:**
```lua
if AltoClef.isPlayerLoaded("Steve") then
    AltoClef.log("Steve is online and loaded")
else
    AltoClef.log("Steve is not loaded")
end
```

### `getPlayerByName(username)`
Get a specific player entity by username.

**Parameters:**
- `username` (string): Player's username

**Returns:**
- `table`: Player entity object, or `nil` if not found

**Example:**
```lua
local steve = AltoClef.getPlayerByName("Steve")
if steve then
    AltoClef.log("Found Steve at distance: " .. steve.distance)
    AltoClef.log("Position: (" .. steve.position.x .. ", " .. steve.position.y .. ", " .. steve.position.z .. ")")
else
    AltoClef.log("Steve not found")
end
```

---

## ⚔️ Hostile Entity Monitoring

### `getHostileEntities()`
Get all hostile/dangerous entities.

**Returns:**
- `table`: Array of hostile entity objects

**Example:**
```lua
local hostiles = AltoClef.getHostileEntities()
if #hostiles > 0 then
    AltoClef.log("⚠️ " .. #hostiles .. " hostile entities detected!")
    
    for i, hostile in ipairs(hostiles) do
        AltoClef.log("Hostile: " .. hostile.name .. " at " .. hostile.distance .. " blocks")
        
        -- Check if it's a living entity and get health
        if hostile.isLiving and hostile.health then
            AltoClef.log("  Health: " .. hostile.health .. "/" .. hostile.maxHealth)
        end
    end
else
    AltoClef.log("No hostile entities nearby")
end
```

---

## 💎 Item Drop Detection

### `getDroppedItems(range)`
Get all dropped item entities within range.

**Parameters:**
- `range` (number, optional): Detection range in blocks. Default: 20

**Returns:**
- `table`: Array of item entity objects with item data

**Example:**
```lua
local items = AltoClef.getDroppedItems(30)
AltoClef.log("Found " .. #items .. " dropped items")

local itemCounts = {}
for i, item in ipairs(items) do
    local itemName = item.itemName
    local count = item.count or 1
    
    itemCounts[itemName] = (itemCounts[itemName] or 0) + count
    
    AltoClef.log("Item: " .. itemName .. " x" .. count .. " at " .. item.distance .. " blocks")
end

-- Show summary
for itemName, totalCount in pairs(itemCounts) do
    AltoClef.log("Total " .. itemName .. ": " .. totalCount)
end
```

---

## 📊 Advanced Entity Information

### Entity Object Structure

All entity objects returned by the APIs contain the following information:

```lua
{
    -- Basic Information
    name = "Entity Name",           -- Display name
    type = "minecraft:entity_type", -- Entity type identifier
    id = 12345,                     -- Unique entity ID
    distance = 15.3,                -- Distance from player in blocks
    
    -- Position
    position = {
        x = 100.5,
        y = 64.0,
        z = -50.2
    },
    
    -- Velocity
    velocity = {
        x = 0.0,
        y = 0.0,
        z = 0.1
    },
    
    -- State Flags
    isAlive = true,                 -- Is entity alive
    isOnGround = true,              -- Is touching ground
    isInWater = false,              -- Is in water
    isInLava = false,               -- Is in lava
    
    -- Type Flags
    isLiving = true,                -- Is a living entity
    isPlayer = false,               -- Is a player
    isItem = false,                 -- Is a dropped item
    
    -- Living Entity Data (if isLiving = true)
    health = 20.0,                  -- Current health
    maxHealth = 20.0,               -- Maximum health
    canSeePlayer = false,           -- Can this entity see the player
    
    -- Player Data (if isPlayer = true)
    username = "PlayerName",        -- Player's username
    uuid = "550e8400-e29b-...",     -- Player's UUID
    
    -- Item Data (if isItem = true)
    itemName = "minecraft:diamond", -- Item type
    count = 5                       -- Stack size
}
```

### `getEntityHealth(entityName)`
Get the health of a specific entity.

**Parameters:**
- `entityName` (string): Name or ID of the entity

**Returns:**
- `number`: Entity's current health, or 0 if not found/not living

**Example:**
```lua
local hostiles = AltoClef.getHostileEntities()
for i, hostile in ipairs(hostiles) do
    local health = AltoClef.getEntityHealth(hostile.name)
    AltoClef.log(hostile.name .. " health: " .. health)
end
```

### `getEntityPosition(entityName)`
Get the position of a specific entity.

**Parameters:**
- `entityName` (string): Name or ID of the entity

**Returns:**
- `table`: Position table `{x, y, z}`, or `nil` if not found

**Example:**
```lua
local pos = AltoClef.getEntityPosition("Zombie")
if pos then
    AltoClef.log("Zombie at: (" .. pos.x .. ", " .. pos.y .. ", " .. pos.z .. ")")
end
```

### `isEntityInSight(entityName)`
Check if a specific entity is visible to the player.

**Parameters:**
- `entityName` (string): Name or ID of the entity

**Returns:**
- `boolean`: `true` if entity is in line of sight, `false` otherwise

**Example:**
```lua
local hostiles = AltoClef.getHostileEntities()
for i, hostile in ipairs(hostiles) do
    local inSight = AltoClef.isEntityInSight(hostile.name)
    if inSight then
        AltoClef.log("⚠️ " .. hostile.name .. " can see you!")
    end
end
```

---

## 🎯 Practical Examples

### Combat Awareness System
```lua
function checkThreats()
    local hostiles = AltoClef.getHostileEntities()
    local immediateThreats = 0
    
    for i, hostile in ipairs(hostiles) do
        if hostile.distance < 10 then
            immediateThreats = immediateThreats + 1
            
            if hostile.canSeePlayer then
                AltoClef.log("⚠️ THREAT: " .. hostile.name .. " sees you! Distance: " .. hostile.distance)
            end
        end
    end
    
    return immediateThreats
end
```

### Loot Collection System
```lua
function collectNearbyLoot()
    local items = AltoClef.getDroppedItems(15)
    local valuableItems = {}
    
    local valuable = {"diamond", "emerald", "gold", "iron", "enchanted"}
    
    for i, item in ipairs(items) do
        for j, valuable_name in ipairs(valuable) do
            if string.find(item.itemName:lower(), valuable_name) then
                table.insert(valuableItems, item)
                break
            end
        end
    end
    
    if #valuableItems > 0 then
        AltoClef.log("💎 Found " .. #valuableItems .. " valuable items nearby!")
        return valuableItems
    end
    
    return {}
end
```

### Player Monitoring System
```lua
function monitorPlayers()
    local players = AltoClef.getPlayersNearby(50)
    
    for i, player in ipairs(players) do
        local status = "online"
        
        if player.distance < 10 then
            status = "very close"
        elseif player.distance < 25 then
            status = "nearby"
        else
            status = "distant"
        end
        
        AltoClef.log("Player " .. player.username .. " is " .. status .. " (" .. player.distance .. " blocks)")
    end
    
    return #players
end
```

### Entity Radar System
```lua
function entityRadar()
    local totalEntities = AltoClef.countEntitiesOfType("all")
    local hostiles = AltoClef.countEntitiesOfType("hostile")
    local players = AltoClef.countEntitiesOfType("player")
    local items = AltoClef.countEntitiesOfType("item")
    
    AltoClef.log("📡 Entity Radar:")
    AltoClef.log("  Total: " .. totalEntities)
    AltoClef.log("  Hostiles: " .. hostiles)
    AltoClef.log("  Players: " .. players)
    AltoClef.log("  Items: " .. items)
    
    -- Get closest of each type
    local closestHostile = nil
    local minHostileDistance = math.huge
    
    local hostileEntities = AltoClef.getHostileEntities()
    for i, hostile in ipairs(hostileEntities) do
        if hostile.distance < minHostileDistance then
            minHostileDistance = hostile.distance
            closestHostile = hostile
        end
    end
    
    if closestHostile then
        AltoClef.log("  Closest threat: " .. closestHostile.name .. " at " .. closestHostile.distance .. " blocks")
    end
end
```

---

## ⚡ Performance Considerations

### Optimization Tips

1. **Use appropriate ranges** - Smaller ranges are faster
```lua
-- Good for frequent checks
local nearbyThreats = AltoClef.getHostileEntities() -- Uses default range

-- Good for specific needs
local distantPlayers = AltoClef.getPlayersNearby(100) -- Larger range when needed
```

2. **Cache results when possible**
```lua
local lastScanTime = 0
local cachedEntities = {}

function getEntitiesWithCache()
    local currentTime = os.clock() * 1000
    
    if currentTime - lastScanTime > 1000 then -- Cache for 1 second
        cachedEntities = AltoClef.getNearbyEntities(25)
        lastScanTime = currentTime
    end
    
    return cachedEntities
end
```

3. **Use counting for simple checks**
```lua
-- Faster for simple checks
if AltoClef.countEntitiesOfType("hostile") > 0 then
    -- Handle threats
end

-- Instead of getting full list when you just need count
local hostiles = AltoClef.getHostileEntities()
if #hostiles > 0 then
    -- Handle threats
end
```

---

## 🔄 Utils API Reference

All functions are also available through `Utils.Entity.*`:

```lua
-- All primary functions have Utils equivalents
Utils.Entity.getNearbyEntities(range)
Utils.Entity.getClosestEntity()
Utils.Entity.getEntitiesOfType(entityType)
Utils.Entity.getPlayersNearby(range)
Utils.Entity.getHostileEntities()
Utils.Entity.getDroppedItems(range)
Utils.Entity.countEntitiesOfType(entityType)
Utils.Entity.getEntityHealth(entityName)
Utils.Entity.getEntityPosition(entityName)
Utils.Entity.isEntityInSight(entityName)
Utils.Entity.isPlayerLoaded(username)
Utils.Entity.getPlayerByName(username)
```

Both API patterns provide identical functionality and can be used interchangeably based on your preference or coding style.

---

## Test Script

Use the comprehensive test script to verify entity detection functionality:

```lua
-- Load the entity detection demo script
@loadscript entity_detection_demo.lua
```

The test script validates:
- ✅ Basic entity detection and counting
- ✅ Player detection and tracking
- ✅ Hostile entity monitoring
- ✅ Item drop scanning
- ✅ Entity type filtering
- ✅ Advanced entity information
- ✅ Dual API validation
- ✅ Performance testing

---

*Phase 7 Complete: Entity Detection & Tracking APIs provide comprehensive entity monitoring and tracking capabilities for advanced script functionality.* 