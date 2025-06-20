-- Chat and Command Demo Script
-- Demonstrates the new chat and command API features

local chatCount = 0
local commandCount = 0

function onLoad()
    AltoClef.log("Chat Demo script loaded!")
    
    -- Register a custom command
    AltoClef.createcommand("hello", "Says hello with optional name", function(args)
        local name = "World"
        if args and args[1] then
            name = args[1]
        end
        AltoClef.chat("Hello " .. name .. "!")
        AltoClef.log("Said hello to " .. name)
    end)
    
    -- Register another command with multiple arguments
    AltoClef.createcommand("position", "Reports current position", function(args)
        local x = AltoClef.getPlayerX()
        local y = AltoClef.getPlayerY() 
        local z = AltoClef.getPlayerZ()
        local msg = string.format("Current position: %.1f, %.1f, %.1f", x, y, z)
        AltoClef.chat(msg)
    end)
    
    -- Set up chat event handler
    AltoClef.onchat(function(chatInfo)
        chatCount = chatCount + 1
        
        local message = chatInfo.message
        local sender = chatInfo.sender
        local isSelf = chatInfo.isSelf
        
        AltoClef.log(string.format("Chat #%d - %s: %s (self: %s)", 
            chatCount, sender, message, tostring(isSelf)))
        
        -- Respond to specific messages
        if not isSelf then
            if string.lower(message):find("hello") then
                AltoClef.chat("Hello there, " .. sender .. "!")
            elseif string.lower(message):find("bot") then
                AltoClef.chat("Yes, I'm a bot! AltoClef Lua script running.")
            end
        end
    end)
    
    -- Set up command event handler
    AltoClef.oncommand(function(cmdInfo)
        commandCount = commandCount + 1
        
        local command = cmdInfo.command
        local args = cmdInfo.args
        
        AltoClef.log(string.format("Command #%d executed: @%s %s", 
            commandCount, command, args))
        
        -- React to specific commands
        if command == "status" then
            local msg = string.format("Script stats: %d chats, %d commands processed", 
                chatCount, commandCount)
            AltoClef.chat(msg)
        end
    end)
end

function onTick()
    -- Nothing needed for this demo
end

function onCleanup()
    AltoClef.log("Chat Demo script cleaned up. Stats: " .. chatCount .. " chats, " .. commandCount .. " commands")
end 