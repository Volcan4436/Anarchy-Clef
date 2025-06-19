--[[
@name Auto Food
@description Automatically eats food when hunger is low
@version 1.0.0
@author Hearty
@category Utility
@dependencies none
]]--

local script = {}

-- Configuration
local HUNGER_THRESHOLD = 10 -- Eat when hunger drops below this value
local lastFoodTime = 0
local FOOD_COOLDOWN = 5000 -- 5 seconds between food checks

function onLoad()
    AltoClef.log("Auto Food script loaded! Will eat when hunger < " .. HUNGER_THRESHOLD)
end

function onTick()
    local currentTime = Utils.time.getCurrentTime()
    
    -- Check if enough time has passed since last food check
    if currentTime - lastFoodTime < FOOD_COOLDOWN then
        return
    end
    
    -- Check hunger level
    local hunger = Utils.player.getHunger()
    
    if hunger < HUNGER_THRESHOLD then
        -- Try to eat food
        local hasFood = Utils.inventory.hasFood()
        
        if hasFood then
            AltoClef.log("Hunger low (" .. hunger .. "), attempting to eat food...")
            
            -- Use AltoClef's food chain to handle eating
            AltoClef.runUserTask("EatFoodTask")
            
            lastFoodTime = currentTime
        else
            AltoClef.log("Hunger low but no food available!")
        end
    end
end

function onEnable()
    AltoClef.log("Auto Food script enabled")
    lastFoodTime = 0
end

function onDisable()
    AltoClef.log("Auto Food script disabled")
end

function onCleanup()
    AltoClef.log("Auto Food script cleaning up")
end

return script 