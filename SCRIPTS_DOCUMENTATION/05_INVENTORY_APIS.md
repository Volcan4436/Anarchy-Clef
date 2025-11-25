# Inventory Management APIs

**Version:** 1.0.0  
**Status:** Complete  
**Category:** Core APIs

## Overview

Inventory APIs provide comprehensive access to item management, equipment handling, container interactions, and crafting systems. These APIs enable sophisticated automation for resource management and item processing.

---

## Table of Contents

1. [Quick Start](#-quick-start)
2. [Basic Inventory](#-basic-inventory)
3. [Equipment Management](#-equipment-management)
4. [Container Operations](#-container-operations)
5. [Furnace Operations](#-furnace-operations)
6. [Examples](#-examples)
7. [Best Practices](#-best-practices)
8. [Related Topics](#-related-topics)

---

## Quick Start

```lua
-- Basic inventory operations
local dirtCount = AltoClef.getItemCount("dirt")
local hasTools = AltoClef.hasItem("diamond_pickaxe")
local emptySlots = AltoClef.hasEmptySlot()

-- Equipment management
if AltoClef.hasItem("diamond_sword") then
   AltoClef.equipItem("diamond_sword")
end

-- Alternative Utils API
local ironCount = Utils.Inventory.getItemCount("iron_ingot")
local isInventoryFull = not Utils.Inventory.hasEmptySlot()
```

## **Complete API Reference**

### **Item Counting & Detection**

#### `getItemCount(itemName)` / `Utils.Inventory.getItemCount(itemName)`
Get total count of an item (inventory + conversion slots).
```lua
local coalCount = AltoClef.getItemCount("coal")          -- Total coal
local altCount = Utils.Inventory.getItemCount("coal")    -- Same result
print("Coal count: " .. coalCount)
```

#### `getItemCountInventory(itemName)` / `Utils.Inventory.getItemCountInventory(itemName)`
Get count strictly in player inventory (excludes crafting/furnace slots).
```lua
local inventoryDirt = AltoClef.getItemCountInventory("dirt")
local totalDirt = AltoClef.getItemCount("dirt")
print("Dirt in inventory: " .. inventoryDirt .. " / " .. totalDirt)
```

#### `hasItem(itemName)` / `Utils.Inventory.hasItem(itemName)`
Check if item exists anywhere accessible.
```lua
if AltoClef.hasItem("diamond") then
   print("We have diamonds!")
end

-- Also works with Utils API
if Utils.Inventory.hasItem("emerald") then
   print("We have emeralds!")
end
```

#### `hasItemInventory(itemName)` / `Utils.Inventory.hasItemInventory(itemName)`
Check if item is strictly in inventory.
```lua
local hasInInventory = AltoClef.hasItemInventory("bread")
local hasAnywhere = AltoClef.hasItem("bread")
print("Bread - Inventory: " .. tostring(hasInInventory) .. ", Total: " .. tostring(hasAnywhere))
```

### **Equipment Management**

#### `isEquipped(itemName)` / `Utils.Inventory.isEquipped(itemName)`
Check if item is currently equipped in hotbar.
```lua
if AltoClef.isEquipped("diamond_sword") then
   print("Diamond sword is ready!")
else
   print("No sword equipped")
end
```

#### `equipItem(itemName)` / `Utils.Inventory.equipItem(itemName)`
Force equip item to hotbar slot. Returns `true` if successful.
```lua
if AltoClef.hasItem("iron_pickaxe") then
   if AltoClef.equipItem("iron_pickaxe") then
       print("Iron pickaxe equipped!")
   else
       print("Failed to equip pickaxe")
   end
end
```

#### `equipItemToOffhand(itemName)` / `Utils.Inventory.equipItemToOffhand(itemName)`
Equip item to offhand slot. Returns `true` if successful.
```lua
if AltoClef.hasItem("shield") then
   AltoClef.equipItemToOffhand("shield")
   print("Shield equipped to offhand")
end

-- Works with torches, food, etc.
Utils.Inventory.equipItemToOffhand("torch")
```

#### `isArmorEquipped(itemName)` / `Utils.Inventory.isArmorEquipped(itemName)`
Check if armor piece is currently worn.
```lua
local armorPieces = {"diamond_helmet", "iron_chestplate", "leather_leggings", "chainmail_boots"}
for _, armor in ipairs(armorPieces) do
   if AltoClef.isArmorEquipped(armor) then
       print("Wearing: " .. armor)
   end
end
```

### **Inventory State**

#### `hasEmptySlot()` / `Utils.Inventory.hasEmptySlot()`
Check if inventory has empty slots.
```lua
if not AltoClef.hasEmptySlot() then
   print("Inventory is full!")
else
   print("Inventory has space")
end
```

#### `getInventoryItemCount()` (AltoClef only)
Get total number of inventory slots (usually 36).
```lua
local totalSlots = AltoClef.getInventoryItemCount()
print("Total inventory slots: " .. totalSlots)
```

#### `getTotalSlots()` (Utils only)
Get total inventory slot count.
```lua
local slots = Utils.Inventory.getTotalSlots()
print("Inventory slots: " .. slots)
```

#### `getFoodCount()` / `Utils.Inventory.getFoodCount()`
Calculate total food value score in inventory.
```lua
local foodScore = AltoClef.getFoodCount()
if foodScore < 20 then
   print("Low food supplies: " .. foodScore)
else
   print("Food adequate: " .. foodScore)
end
```

#### `getBuildingMaterialCount()` / `Utils.Inventory.getBuildingMaterialCount()`
Get count of building materials (blocks).
```lua
local blocks = AltoClef.getBuildingMaterialCount()
if blocks < 64 then
   print("Need more building materials: " .. blocks)
else
   print("Building materials sufficient: " .. blocks)
end
```

### **Screen & Container Detection**

#### `isInventoryOpen()` / `Utils.Inventory.isInventoryOpen()`
Check if player inventory screen is open.
```lua
if AltoClef.isInventoryOpen() then
   print("Inventory screen is open")
end
```

#### `isCraftingTableOpen()` / `Utils.Inventory.isCraftingTableOpen()`
Check if crafting table interface is open.
```lua
if AltoClef.isCraftingTableOpen() then
   print("Crafting table is open")
end
```

#### `isFurnaceOpen()` / `Utils.Inventory.isFurnaceOpen()`
#### `isSmokerOpen()` / `Utils.Inventory.isSmokerOpen()`
#### `isBlastFurnaceOpen()` / `Utils.Inventory.isBlastFurnaceOpen()`
Check if furnace interfaces are open.
```lua
local furnaceTypes = {
   {name = "Furnace", check = AltoClef.isFurnaceOpen},
   {name = "Smoker", check = AltoClef.isSmokerOpen},
   {name = "Blast Furnace", check = AltoClef.isBlastFurnaceOpen}
}

for _, furnace in ipairs(furnaceTypes) do
   if furnace.check() then
       print("" .. furnace.name .. " interface is open")
   end
end
```

#### `closeScreen()` / `Utils.Inventory.closeScreen()`
Close any open screen/interface.
```lua
if AltoClef.isInventoryOpen() then
   AltoClef.closeScreen()
   print("Closed inventory screen")
end
```

### **Furnace Operations**

#### `getFurnaceFuel()` / `Utils.Inventory.getFurnaceFuel()`
Get furnace fuel level (0.0 to 1.0).
```lua
local fuel = AltoClef.getFurnaceFuel()
if fuel > 0 then
   print("Furnace fuel: " .. string.format("%.1f%%", fuel * 100))
end
```

#### `getFurnaceCookProgress()` / `Utils.Inventory.getFurnaceCookProgress()`
Get furnace cooking progress (0.0 to 1.0).
```lua
local progress = AltoClef.getFurnaceCookProgress()
if progress > 0.9 then
   print("Furnace almost done: " .. string.format("%.1f%%", progress * 100))
end
```

#### Smoker & Blast Furnace Variants
```lua
-- Smoker operations
local smokerFuel = AltoClef.getSmokerFuel()
local smokerProgress = AltoClef.getSmokerCookProgress()

-- Blast furnace operations  
local blastFuel = AltoClef.getBlastFurnaceFuel()
local blastProgress = AltoClef.getBlastFurnaceCookProgress()

print(string.format("Smoker: %.1f%% fuel, %.1f%% done", smokerFuel * 100, smokerProgress * 100))
print(string.format("Blast Furnace: %.1f%% fuel, %.1f%% done", blastFuel * 100, blastProgress * 100))
```

### **Container Operations**

#### `getContainerItemCount(itemName)` / `Utils.Inventory.getContainerItemCount(itemName)`
Get item count in open container (excluding inventory).
```lua
local chestDiamonds = AltoClef.getContainerItemCount("diamond")
if chestDiamonds > 0 then
   print("Found " .. chestDiamonds .. " diamonds in container")
end
```

#### `hasItemInContainer(itemName)` / `Utils.Inventory.hasItemInContainer(itemName)`
Check if item exists in any cached container.
```lua
if AltoClef.hasItemInContainer("enchanted_book") then
   print("Enchanted books available in storage")
end
```

### **Utility Functions**

#### `refreshInventory()` / `Utils.Inventory.refreshInventory()`
Force refresh inventory state. Returns `true` if successful.
```lua
print("Refreshing inventory...")
if AltoClef.refreshInventory() then
   print("Inventory refreshed")
end
```

## **Practical Examples**

### **Auto-Equipment Manager**
```lua
function autoEquipBestWeapon()
   local weapons = {"netherite_sword", "diamond_sword", "iron_sword", "stone_sword", "wooden_sword"}
   
   for _, weapon in ipairs(weapons) do
       if AltoClef.hasItem(weapon) and not AltoClef.isEquipped(weapon) then
           if AltoClef.equipItem(weapon) then
               print("Equipped " .. weapon)
               return true
           end
       end
   end
   
   print("No weapons available")
   return false
end
```

### **Inventory Status Monitor**
```lua
function checkInventoryStatus()
   print("=== INVENTORY STATUS ===")
   
   -- Basic stats
   local totalSlots = AltoClef.getInventoryItemCount()
   local hasEmpty = AltoClef.hasEmptySlot()
   local foodScore = AltoClef.getFoodCount()
   local buildingMats = AltoClef.getBuildingMaterialCount()
   
   print("Slots: " .. totalSlots .. " | Empty: " .. tostring(hasEmpty))
   print("Food: " .. foodScore .. " | Building: " .. buildingMats)
   
   -- Resource check
   local resources = {"diamond", "iron_ingot", "coal", "gold_ingot"}
   for _, item in ipairs(resources) do
       local count = AltoClef.getItemCount(item)
       if count > 0 then
           print("o " .. item .. ": " .. count)
       end
   end
   
   -- Warnings
   if not hasEmpty then print("INVENTORY FULL") end
   if foodScore < 20 then print("LOW FOOD") end
   if buildingMats < 64 then print("LOW BLOCKS") end
end
```

### **Furnace Monitoring System**
```lua
function monitorAllFurnaces()
   local furnaces = {
       {name = "Furnace", open = AltoClef.isFurnaceOpen, fuel = AltoClef.getFurnaceFuel, progress = AltoClef.getFurnaceCookProgress},
       {name = "Smoker", open = AltoClef.isSmokerOpen, fuel = AltoClef.getSmokerFuel, progress = AltoClef.getSmokerCookProgress},
       {name = "Blast Furnace", open = AltoClef.isBlastFurnaceOpen, fuel = AltoClef.getBlastFurnaceFuel, progress = AltoClef.getBlastFurnaceCookProgress}
   }
   
   for _, furnace in ipairs(furnaces) do
       if furnace.open() then
           local fuel = furnace.fuel()
           local progress = furnace.progress()
           
           print(string.format("%s: %.1f%% fuel, %.1f%% done",
               furnace.name, fuel * 100, progress * 100))
           
           if fuel < 0.2 then
               print("  " .. furnace.name .. " needs fuel!")
           end
           
           if progress > 0.95 then
               print("  " .. furnace.name .. " finished!")
           end
       end
   end
end
```

### **Smart Storage Scanner**
```lua
function scanStorageForValuables()
   local valuables = {"diamond", "emerald", "netherite_ingot", "enchanted_book", "golden_apple"}
   local found = {}
   
   print("=== VALUABLE ITEMS SCAN ===")
   
   for _, item in ipairs(valuables) do
       local inventoryCount = AltoClef.getItemCountInventory(item)
       local containerCount = AltoClef.getContainerItemCount(item)
       local totalCount = AltoClef.getItemCount(item)
       
       if totalCount > 0 then
           found[item] = {inventory = inventoryCount, container = containerCount, total = totalCount}
           print(string.format("o %s: %d total (%d inventory, %d containers)",
               item, totalCount, inventoryCount, containerCount))
       end
   end
   
   if next(found) == nil then
       print("No valuable items found")
   else
       print("Found " .. table.maxn(found) .. " types of valuable items")
   end
   
   return found
end
```

### **Dual API Consistency Checker**
```lua
function validateDualAPIs()
   print("=== DUAL API VALIDATION ===")
   
   local testItems = {"dirt", "stone", "iron_ingot", "diamond"}
   local allMatch = true
   
   for _, item in ipairs(testItems) do
       local altoCount = AltoClef.getItemCount(item)
       local utilsCount = Utils.Inventory.getItemCount(item)
       
       if altoCount == utilsCount then
           print("" .. item .. ": " .. altoCount)
       else
           print("" .. item .. " MISMATCH: AltoClef=" .. altoCount .. ", Utils=" .. utilsCount)
           allMatch = false
       end
   end
   
   -- Test state functions
   local altoEmpty = AltoClef.hasEmptySlot()
   local utilsEmpty = Utils.Inventory.hasEmptySlot()
   
   if altoEmpty == utilsEmpty then
       print("hasEmptySlot: " .. tostring(altoEmpty))
   else
       print("hasEmptySlot MISMATCH")
       allMatch = false
   end
   
   return allMatch
end
```

## **Advanced Patterns**

### **Inventory Organization Helper**
```lua
function organizeInventory()
   print("Organizing inventory...")
   
   -- Check critical items
   local criticalItems = {
       {name = "food", items = {"bread", "apple", "cooked_beef"}, minCount = 10},
       {name = "tools", items = {"diamond_pickaxe", "iron_pickaxe"}, minCount = 1},
       {name = "weapons", items = {"diamond_sword", "iron_sword"}, minCount = 1}
   }
   
   for _, category in ipairs(criticalItems) do
       local totalCount = 0
       for _, item in ipairs(category.items) do
           totalCount = totalCount + AltoClef.getItemCount(item)
       end
       
       if totalCount < category.minCount then
           print("Low " .. category.name .. ": " .. totalCount .. "/" .. category.minCount)
       else
           print("" .. category.name .. " adequate: " .. totalCount)
       end
   end
   
   -- Auto-equip best items
   local bestWeapons = {"netherite_sword", "diamond_sword", "iron_sword"}
   for _, weapon in ipairs(bestWeapons) do
       if AltoClef.hasItem(weapon) then
           AltoClef.equipItem(weapon)
           print("Equipped " .. weapon)
           break
       end
   end
end
```

### **Resource Management Dashboard**
```lua
function resourceDashboard()
   print("=== RESOURCE DASHBOARD ===")
   
   local resources = {
       ["Mining"] = {"diamond", "iron_ingot", "gold_ingot", "coal", "redstone"},
       ["Building"] = {"stone", "cobblestone", "dirt", "wood", "sand"},
       ["Food"] = {"bread", "apple", "cooked_beef", "cooked_chicken"},
       ["Combat"] = {"arrow", "bow", "diamond_sword", "iron_sword"}
   }
   
   for category, items in pairs(resources) do
       print("\n" .. category .. ":")
       local categoryTotal = 0
       
       for _, item in ipairs(items) do
           local count = AltoClef.getItemCount(item)
           if count > 0 then
               print("  o " .. item .. ": " .. count)
               categoryTotal = categoryTotal + count
           end
       end
       
       if categoryTotal == 0 then
           print("  No " .. category:lower() .. " items found")
       else
           print("  Total " .. category:lower() .. " items: " .. categoryTotal)
       end
   end
   
   -- Overall status
   print("\n=== OVERALL STATUS ===")
   print("Inventory full: " .. tostring(not AltoClef.hasEmptySlot()))
   print("Food score: " .. AltoClef.getFoodCount())
   print("Building materials: " .. AltoClef.getBuildingMaterialCount())
end
```

## **Use Cases**

### **1. Automated Base Management**
- Monitor storage containers for resource levels
- Auto-organize inventory by priority
- Track furnace operations and fuel levels
- Manage equipment durability and upgrades

### **2. Resource Collection Optimization**
- Check inventory space before mining
- Prioritize valuable items when inventory is full
- Monitor food levels during exploration
- Auto-equip appropriate tools for tasks

### **3. Combat Preparation System**
- Auto-equip best available weapons/armor
- Check arrow count for ranged combat
- Monitor potion supplies and effects
- Prepare healing items in hotbar

### **4. Crafting & Production Automation**
- Monitor ingredient availability
- Track furnace progress for batch processing
- Auto-organize crafting materials
- Manage fuel supplies for smelting

## **Integration with Other APIs**

```lua
-- Combine with Player APIs
if AltoClef.getHealth() < 10 and AltoClef.hasItem("golden_apple") then
   AltoClef.equipItem("golden_apple")
   AltoClef.use()  -- From player input API
end

-- Combine with World APIs
if AltoClef.isDay() and AltoClef.hasItem("diamond_pickaxe") then
   AltoClef.equipItem("diamond_pickaxe")
   -- Ready for mining during day
end

-- Combine with Chat APIs
if not AltoClef.hasEmptySlot() then
   AltoClef.sendChat("Inventory full! Returning to base...")
end
```

## **Important Notes**

1. **Item Names**: Use standard Minecraft item IDs (`"diamond_sword"` not `"Diamond Sword"`)
2. **Dual API Support**: Both `AltoClef.*` and `Utils.Inventory.*` provide identical functionality
3. **Container Access**: Container functions require nearby chests/storage to be cached
4. **Equipment Timing**: Equipment changes may take a few ticks to register
5. **Furnace Detection**: Furnace functions only work when furnace interface is open
6. **Error Handling**: All functions include try-catch blocks and return safe defaults

---

**Next**: Explore [Entity Detection APIs](06_ENTITY_APIS.md) for player and mob interaction systems.

**Previous**: [World Information APIs](04_WORLD_APIS.md) 