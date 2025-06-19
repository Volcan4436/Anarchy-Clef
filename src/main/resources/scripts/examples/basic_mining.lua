--[[
@name Basic Mining
@description Automatically mines specific blocks when nearby
@version 1.2.0
@author Hearty
@category Mining
@dependencies none
]]--

local script = {}

-- Configuration
local TARGET_BLOCKS = {"iron_ore", "coal_ore", "diamond_ore", "gold_ore"}
local SEARCH_RADIUS = 16
local lastScanTime = 0
local SCAN_INTERVAL = 3000 -- 3 seconds between scans

function onLoad()
    AltoClef.log("Basic Mining script loaded! Targeting: " .. table.concat(TARGET_BLOCKS, ", "))
end

function onTick()
    local currentTime = Utils.time.getCurrentTime()
    
    -- Only scan periodically to avoid lag
    if currentTime - lastScanTime < SCAN_INTERVAL then
        return
    end
    
    -- Check if player is already busy with a task
    if AltoClef.getTaskRunner():isActive() then
        return
    end
    
    -- Scan for target blocks
    local foundBlocks = {}
    for _, blockType in ipairs(TARGET_BLOCKS) do
        local blocks = AltoClef.getBlockTracker():getNearbyBlocks(blockType, SEARCH_RADIUS)
        if #blocks > 0 then
            table.insert(foundBlocks, {type = blockType, count = #blocks})
        end
    end
    
    if #foundBlocks > 0 then
        -- Find the most valuable block to mine
        local targetBlock = selectBestTarget(foundBlocks)
        
        if targetBlock then
            AltoClef.log("Found " .. targetBlock.count .. " " .. targetBlock.type .. " blocks, starting to mine...")
            
            -- Start mining task
            AltoClef.runUserTask("MineAndCollectTask", {
                target = targetBlock.type,
                count = math.min(targetBlock.count, 64) -- Don't mine more than a stack
            })
        end
    end
    
    lastScanTime = currentTime
end

function selectBestTarget(blocks)
    -- Prioritize by value: diamond > gold > iron > coal
    local priority = {
        diamond_ore = 4,
        gold_ore = 3,
        iron_ore = 2,
        coal_ore = 1
    }
    
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

function onEnable()
    AltoClef.log("Basic Mining script enabled - Ready to mine!")
    lastScanTime = 0
end

function onDisable()
    AltoClef.log("Basic Mining script disabled")
    -- Cancel any active mining task
    AltoClef.cancelUserTask()
end

function onCleanup()
    AltoClef.log("Basic Mining script cleaning up")
end

return script 