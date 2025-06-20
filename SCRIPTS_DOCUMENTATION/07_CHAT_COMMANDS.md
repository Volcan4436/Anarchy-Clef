# 💬 Chat & Commands

**Version:** 1.0.0  
**Status:** ✅ Complete  
**Category:** Core APIs  

## 📖 Overview

The Chat & Commands system allows Lua scripts to interact with Minecraft's chat, create custom commands, and respond to chat events in real-time. This enables creating interactive bots, command systems, and automated chat responses.

---

## 📚 Table of Contents

1. [🚀 Quick Start](#-quick-start)
2. [💬 Chat System](#-chat-system)
3. [⚡ Custom Commands](#-custom-commands)
4. [🚀 Advanced Features](#-advanced-features)
5. [🧪 Examples](#-examples)
6. [💡 Best Practices](#-best-practices)
7. [🔗 Related Topics](#-related-topics)

---

## 🚀 Quick Start

### Essential Functions
```lua
-- Basic chat
AltoClef.chat("Hello everyone!")

-- Chat event handling
AltoClef.onchat(function(chatInfo)
    if not chatInfo.isSelf then
        AltoClef.log("Chat from " .. chatInfo.sender .. ": " .. chatInfo.message)
    end
end)

-- Create custom commands
AltoClef.createcommand("hello", "Say hello", function(args)
    AltoClef.chat("Hello World!")
end)

-- Alternative Utils API access
Utils.Chat.whisper("PlayerName", "Private message")
```

---

## 💬 Chat System

### 💬 **Chat System**

#### Send Chat Messages
```lua
-- Send a message to chat
AltoClef.chat("Hello everyone!")
AltoClef.chat("My position: " .. AltoClef.getPlayerX() .. ", " .. AltoClef.getPlayerY() .. ", " .. AltoClef.getPlayerZ())

-- Send formatted messages
local health = AltoClef.getHealth()
AltoClef.chat(string.format("Current health: %.1f/20", health))
```

#### Listen for Chat Events
```lua
-- Set up a chat event handler
AltoClef.onchat(function(chatInfo)
    local message = chatInfo.message      -- The chat message content
    local sender = chatInfo.sender        -- Username of sender
    local senderUUID = chatInfo.senderUUID -- UUID of sender
    local isSelf = chatInfo.isSelf        -- true if message is from you
    local timestamp = chatInfo.timestamp  -- Message timestamp
    
    AltoClef.log("Chat from " .. sender .. ": " .. message)
    
    -- Auto-respond to messages
    if not isSelf and string.lower(message):find("hello") then
        AltoClef.chat("Hello there, " .. sender .. "!")
    end
end)
```

**Chat Event Properties:**
- `message` - The actual chat message text
- `sender` - Username of the message sender
- `senderUUID` - Unique identifier of the sender
- `isSelf` - Boolean indicating if you sent the message
- `timestamp` - When the message was received

### ⚡ **Custom Commands**

#### Create Custom Commands
```lua
-- Create a simple command
AltoClef.createcommand("hello", "Says hello to everyone", function(args)
    AltoClef.chat("Hello World!")
end)

-- Create a command with arguments
AltoClef.createcommand("goto", "Go to coordinates", function(args)
    if #args >= 3 then
        local x, y, z = tonumber(args[1]), tonumber(args[2]), tonumber(args[3])
        if x and y and z then
            AltoClef.chat(string.format("Going to %.1f, %.1f, %.1f", x, y, z))
            -- Add movement logic here
        else
            AltoClef.chat("Invalid coordinates! Use numbers only.")
        end
    else
        AltoClef.chat("Usage: @goto <x> <y> <z>")
    end
end)

-- Create a status command
AltoClef.createcommand("status", "Show player status", function(args)
    local health = AltoClef.getHealth()
    local hunger = AltoClef.getHunger()
    local pos = AltoClef.getPlayerPos()
    
    AltoClef.chat(string.format("Status: %.1f❤️ %d🍖 (%.1f, %.1f, %.1f)", 
        health, hunger, pos.x, pos.y, pos.z))
end)
```

#### Listen for Command Events
```lua
-- Set up a command event handler
AltoClef.oncommand(function(cmdInfo)
    local command = cmdInfo.command    -- Command name (without @)
    local args = cmdInfo.args         -- Command arguments as string
    local timestamp = cmdInfo.timestamp -- Command timestamp
    
    AltoClef.log("Command executed: @" .. command .. " " .. args)
    
    -- React to specific commands
    if command == "emergency" then
        AltoClef.chat("🚨 Emergency protocol activated!")
        -- Add emergency logic
    end
end)
```

---

## 🚀 **Advanced Features (Phase 6)**

### 💌 **Whisper System**

Send private messages to specific players using the whisper functionality:

```lua
-- Send a basic whisper
AltoClef.whisper("PlayerName", "This is a private message!")

-- Send whisper with priority levels
AltoClef.whisperPriority("PlayerName", "Urgent message!", "WARNING")  -- High priority
AltoClef.whisperPriority("PlayerName", "Important info", "IMPORTANT")
AltoClef.whisperPriority("PlayerName", "Regular message", "NORMAL")
AltoClef.whisperPriority("PlayerName", "Low priority note", "LOW")

-- Using Utils API (alternative access)
Utils.Chat.whisper("PlayerName", "Message via Utils API")
Utils.Chat.whisperPriority("PlayerName", "Priority message", "TIMELY")
```

**Priority Levels:**
- `LOW` - Optional messages, lowest queue priority
- `NORMAL` / `INFO` - Standard informational messages  
- `IMPORTANT` - Important notifications, higher priority
- `TIMELY` - Time-sensitive messages, high priority
- `WARNING` - Urgent warnings, highest priority
- `UNAUTHORIZED` - Security-related messages

### 📢 **Priority Chat Messages**

Send chat messages with different priority levels for better message management:

```lua
-- Send messages with different priorities
AltoClef.chatPriority("Regular announcement", "NORMAL")
AltoClef.chatPriority("Important server notice!", "IMPORTANT") 
AltoClef.chatPriority("URGENT: Server restart in 5 minutes!", "WARNING")

-- Using Utils API
Utils.Chat.chatPriority("Low priority update", "LOW")
```

The priority system ensures important messages are sent first and helps prevent server kicks from message flooding.

### 🔐 **Butler System Integration**

Access AltoClef's built-in Butler system for user authentication and management:

```lua
-- Check if a user is authorized for butler commands
local isAuthorized = AltoClef.isUserAuthorized("PlayerName")
if isAuthorized then
    AltoClef.chat("Player is authorized for butler commands")
else
    AltoClef.chat("Player is not authorized")
end

-- Get current butler user (returns nil if none)
local currentUser = AltoClef.getCurrentUser()
if currentUser then
    AltoClef.chat("Current butler user: " .. currentUser)
    AltoClef.whisper(currentUser, "Command completed successfully!")
end

-- Check if butler has an active user
local hasUser = AltoClef.hasCurrentUser()
AltoClef.log("Butler has active user: " .. tostring(hasUser))

-- Using Utils API (alternative access)
local authorized = Utils.Chat.isUserAuthorized("PlayerName")
local user = Utils.Chat.getCurrentUser()
local active = Utils.Chat.hasCurrentUser()
```

### ⚙️ **Command Management**

Advanced command management features for dynamic command systems:

```lua
-- Get list of all registered commands
local commands = AltoClef.getRegisteredCommands()
for i = 1, #commands do
    local cmd = commands[i]
    AltoClef.log("Command: " .. cmd.name .. " - " .. cmd.description)
end

-- Check if a specific command exists
if AltoClef.hasCommand("mycommand") then
    AltoClef.log("Command 'mycommand' is available")
end

-- Remove a command dynamically
local removed = AltoClef.removeCommand("oldcommand")
if removed then
    AltoClef.log("Successfully removed 'oldcommand'")
end

-- Create a help command that lists all available commands
AltoClef.createcommand("help", "Show all available commands", function(args)
    local commands = AltoClef.getRegisteredCommands()
    AltoClef.chat("📋 Available Commands:")
    
    for i = 1, #commands do
        local cmd = commands[i]
        AltoClef.chat("  @" .. cmd.name .. " - " .. cmd.description)
    end
end)
```

### 🎧 **Enhanced Event Handling**

Additional event handlers for specialized chat monitoring:

```lua
-- Enhanced chat handler with detailed event information
AltoClef.onchat(function(chatInfo)
    AltoClef.log("Chat Event Details:")
    AltoClef.log("  Message: " .. (chatInfo.message or "unknown"))
    AltoClef.log("  Sender: " .. (chatInfo.sender or "unknown"))
    AltoClef.log("  UUID: " .. (chatInfo.senderUUID or "unknown"))
    AltoClef.log("  Is Self: " .. tostring(chatInfo.isSelf))
    AltoClef.log("  Timestamp: " .. (chatInfo.timestamp or "unknown"))
end)

-- Whisper-specific event handler (future feature)
AltoClef.onwhisper(function(whisperInfo)
    AltoClef.log("Received whisper from: " .. whisperInfo.sender)
    AltoClef.log("Whisper content: " .. whisperInfo.message)
    
    -- Auto-respond to whispers
    AltoClef.whisper(whisperInfo.sender, "Thanks for the whisper!")
end)

-- User join/leave event handler (future feature)
AltoClef.onuserevent(function(userInfo)
    if userInfo.event == "join" then
        AltoClef.chat("Welcome " .. userInfo.username .. "! 👋")
    elseif userInfo.event == "leave" then
        AltoClef.log("Player " .. userInfo.username .. " left the server")
    end
end)
```

### 🔄 **Dual API Access**

All chat and command features are available through both API patterns:

```lua
-- Main AltoClef API (recommended)
AltoClef.chat("Hello via main API")
AltoClef.whisper("Player", "Message via main API")
AltoClef.createcommand("test", "Test command", function(args) end)
AltoClef.isUserAuthorized("Player")

-- Utils API (alternative access pattern)
Utils.Chat.whisper("Player", "Message via Utils API")
Utils.Chat.chatPriority("Priority message", "IMPORTANT")
Utils.Chat.isUserAuthorized("Player")
Utils.Chat.getCurrentUser()
```

---

## 🧪 **Complete Examples**

### Interactive Chat Bot
```lua
--[[
@name Interactive Chat Bot
@description Responds to chat messages and provides helpful commands
@version 1.0.0
@author Hearty
@category Utility
]]--

local chatCount = 0
local commandCount = 0
local startTime = os.clock()

function onLoad()
    AltoClef.log("🤖 Interactive Chat Bot loaded!")
    
    -- Create helpful commands
    createBotCommands()
    
    -- Set up chat event handling
    setupChatHandling()
    
    -- Set up command monitoring
    setupCommandMonitoring()
    
    -- Announce bot availability
    AltoClef.chat("🤖 Chat Bot online! Type 'help' for commands.")
end

function createBotCommands()
    -- Statistics command
    AltoClef.createcommand("stats", "Show bot statistics", function(args)
        local uptime = os.clock() - startTime
        local msg = string.format("📊 Bot Stats: %d chats, %d commands, %.1fs uptime", 
            chatCount, commandCount, uptime)
        AltoClef.chat(msg)
    end)
    
    -- Position command
    AltoClef.createcommand("whereami", "Show current position", function(args)
        local pos = AltoClef.getPlayerPos()
        local dimension = AltoClef.getCurrentDimension()
        AltoClef.chat(string.format("📍 You are at %.1f, %.1f, %.1f in %s", 
            pos.x, pos.y, pos.z, dimension))
    end)
    
    -- Health command
    AltoClef.createcommand("health", "Show health and hunger status", function(args)
        local health = AltoClef.getHealth()
        local hunger = AltoClef.getHunger()
        local hasFood = AltoClef.hasFood()
        
        AltoClef.chat(string.format("💚 Health: %.1f/20, 🍖 Hunger: %d/20, 🍞 Has Food: %s", 
            health, hunger, hasFood and "Yes" or "No"))
    end)
    
    -- Time command
    AltoClef.createcommand("time", "Show game time", function(args)
        local gameTime = AltoClef.getGameTime()
        local realTime = os.date("%H:%M:%S")
        AltoClef.chat(string.format("⏰ Game Time: %d, Real Time: %s", gameTime, realTime))
    end)
    
    -- Help command
    AltoClef.createcommand("help", "Show available commands", function(args)
        AltoClef.chat("🆘 Available commands:")
        AltoClef.chat("  @stats - Bot statistics")
        AltoClef.chat("  @whereami - Current position")
        AltoClef.chat("  @health - Health and hunger status")
        AltoClef.chat("  @time - Game and real time")
    end)
end

function setupChatHandling()
    AltoClef.onchat(function(chatInfo)
        chatCount = chatCount + 1
        
        -- Don't respond to own messages
        if chatInfo.isSelf then return end
        
        local msg = string.lower(chatInfo.message)
        local sender = chatInfo.sender
        
        -- Greeting responses
        if msg:find("hello") or msg:find("hi") then
            local responses = {
                "Hello there, " .. sender .. "! 👋",
                "Hi " .. sender .. "! How can I help?",
                "Greetings, " .. sender .. "! 🤖"
            }
            AltoClef.chat(responses[math.random(#responses)])
            
        -- Help requests
        elseif msg:find("help") then
            AltoClef.chat("I can help! Try @help for commands, or just chat with me! 🤖")
            
        -- Status requests
        elseif msg:find("how are you") or msg:find("status") then
            local health = AltoClef.getHealth()
            if health > 15 then
                AltoClef.chat("I'm doing great! Health is " .. health .. "/20 ✨")
            elseif health > 10 then
                AltoClef.chat("I'm okay, health is " .. health .. "/20 😊")
            else
                AltoClef.chat("Not feeling great... health is only " .. health .. "/20 😞")
            end
            
        -- Time requests
        elseif msg:find("what time") or msg:find("time") then
            local gameTime = AltoClef.getGameTime()
            AltoClef.chat("Game time is " .. gameTime .. " ⏰")
            
        -- Location requests
        elseif msg:find("where are you") or msg:find("position") then
            local pos = AltoClef.getPlayerPos()
            AltoClef.chat(string.format("I'm at %.1f, %.1f, %.1f 📍", pos.x, pos.y, pos.z))
            
        -- Jokes
        elseif msg:find("joke") or msg:find("funny") then
            local jokes = {
                "Why don't creepers ever get invited to parties? They always blow up! 💥",
                "What do you call a sleeping bull in Minecraft? A bulldozer! 😴",
                "Why don't Endermen ever win at hide and seek? They always teleport! ⚡"
            }
            AltoClef.chat(jokes[math.random(#jokes)])
            
        -- Thanks
        elseif msg:find("thank") then
            AltoClef.chat("You're welcome, " .. sender .. "! Happy to help! 😊")
            
        -- Generic conversation
        elseif msg:find("bot") or msg:find("script") then
            AltoClef.chat("Yes, I'm a Lua script bot! I can chat and run commands. Type @help! 🤖")
        end
    end)
end

function setupCommandMonitoring()
    AltoClef.oncommand(function(cmdInfo)
        commandCount = commandCount + 1
        AltoClef.log("📝 Command executed: @" .. cmdInfo.command .. " " .. cmdInfo.args)
        
        -- React to specific commands from other sources
        if cmdInfo.command == "emergency" then
            AltoClef.chat("🚨 I detected an emergency command! Stay safe!")
        elseif cmdInfo.command == "food" then
            AltoClef.chat("🍞 Someone's getting food! Bon appétit!")
        end
    end)
end

function onTick()
    -- Periodic status updates (every 5 minutes)
    local uptime = os.clock() - startTime
    if uptime > 0 and uptime % 300 < 0.05 then -- Every 300 seconds
        AltoClef.chat(string.format("🤖 Bot active for %.1f minutes. %d chats processed.", 
            uptime / 60, chatCount))
    end
end

function onDisable()
    AltoClef.chat("🤖 Chat Bot going offline. Goodbye! 👋")
end
```

### Command-Based Automation
```lua
--[[
@name Command Automation
@description Provides utility commands for common tasks
@version 1.0.0
@author Hearty
@category Utility
]]--

function onLoad()
    AltoClef.log("🔧 Command Automation loaded!")
    
    -- Movement commands
    AltoClef.createcommand("jump", "Make the player jump", function(args)
        if AltoClef.isInGame() then
            AltoClef.jump()
            AltoClef.chat("🦘 Jumping!")
        else
            AltoClef.chat("❌ Not in game!")
        end
    end)
    
    AltoClef.createcommand("boost", "Apply velocity boost", function(args)
        if not AltoClef.isInGame() then
            AltoClef.chat("❌ Not in game!")
            return
        end
        
        local multiplier = 1.5
        if args[1] then
            local mult = tonumber(args[1])
            if mult and mult > 0 and mult <= 3 then
                multiplier = mult
            end
        end
        
        local vel = AltoClef.getVelocity()
        if vel then
            AltoClef.setVelocity(vel.x * multiplier, vel.y, vel.z * multiplier)
            AltoClef.chat(string.format("⚡ Velocity boost: %.1fx", multiplier))
        end
    end)
    
    AltoClef.createcommand("stop", "Stop all movement", function(args)
        if AltoClef.isInGame() then
            AltoClef.setVelocity(0, 0, 0)
            AltoClef.chat("🛑 Movement stopped!")
        else
            AltoClef.chat("❌ Not in game!")
        end
    end)
    
    -- Information commands
    AltoClef.createcommand("info", "Show detailed player information", function(args)
        if not AltoClef.isInGame() then
            AltoClef.chat("❌ Not in game!")
            return
        end
        
        local health = AltoClef.getHealth()
        local hunger = AltoClef.getHunger()
        local pos = AltoClef.getPlayerPos()
        local vel = AltoClef.getVelocity()
        local dimension = AltoClef.getCurrentDimension()
        local gameTime = AltoClef.getGameTime()
        
        AltoClef.chat("📋 Player Information:")
        AltoClef.chat(string.format("  💚 Health: %.1f/20", health))
        AltoClef.chat(string.format("  🍖 Hunger: %d/20", hunger))
        AltoClef.chat(string.format("  📍 Position: %.1f, %.1f, %.1f", pos.x, pos.y, pos.z))
        AltoClef.chat(string.format("  ⚡ Velocity: %.2f, %.2f, %.2f", vel.x, vel.y, vel.z))
        AltoClef.chat(string.format("  🌍 Dimension: %s", dimension))
        AltoClef.chat(string.format("  ⏰ Game Time: %d", gameTime))
    end)
    
    -- Utility commands
    AltoClef.createcommand("monitor", "Toggle health/hunger monitoring", function(args)
        if monitoringEnabled then
            monitoringEnabled = false
            AltoClef.chat("📊 Monitoring disabled")
        else
            monitoringEnabled = true
            AltoClef.chat("📊 Monitoring enabled")
        end
    end)
    
    AltoClef.createcommand("alert", "Set health/hunger alert thresholds", function(args)
        if #args >= 2 then
            local healthThreshold = tonumber(args[1])
            local hungerThreshold = tonumber(args[2])
            
            if healthThreshold and hungerThreshold then
                healthAlertThreshold = healthThreshold
                hungerAlertThreshold = hungerThreshold
                AltoClef.chat(string.format("🚨 Alert thresholds: Health < %.1f, Hunger < %d", 
                    healthThreshold, hungerThreshold))
            else
                AltoClef.chat("❌ Invalid numbers!")
            end
        else
            AltoClef.chat("Usage: @alert <health_threshold> <hunger_threshold>")
        end
    end)
end

-- Monitoring variables
local monitoringEnabled = false
local healthAlertThreshold = 10
local hungerAlertThreshold = 8
local lastAlertTime = 0
local ALERT_COOLDOWN = 10000 -- 10 seconds

function onTick()
    if not monitoringEnabled or not AltoClef.isInGame() then return end
    
    local currentTime = os.clock() * 1000
    if currentTime - lastAlertTime < ALERT_COOLDOWN then return end
    
    local health = AltoClef.getHealth()
    local hunger = AltoClef.getHunger()
    
    if health < healthAlertThreshold then
        AltoClef.chat(string.format("🚨 LOW HEALTH ALERT: %.1f/20", health))
        lastAlertTime = currentTime
    elseif hunger < hungerAlertThreshold then
        AltoClef.chat(string.format("🍖 LOW HUNGER ALERT: %d/20", hunger))
        lastAlertTime = currentTime
    end
end
```

### Advanced Chat Analytics
```lua
--[[
@name Chat Analytics
@description Tracks and analyzes chat patterns
@version 1.0.0
@author Hearty
@category Analytics
]]--

local chatStats = {
    totalMessages = 0,
    playerMessages = {},
    commonWords = {},
    sessionsStartTime = os.clock()
}

function onLoad()
    AltoClef.log("📊 Chat Analytics loaded!")
    
    -- Analytics commands
    AltoClef.createcommand("chatstats", "Show chat statistics", function(args)
        showChatStatistics()
    end)
    
    AltoClef.createcommand("topwords", "Show most common words", function(args)
        showTopWords(args[1] and tonumber(args[1]) or 10)
    end)
    
    AltoClef.createcommand("topplayers", "Show most active players", function(args)
        showTopPlayers(args[1] and tonumber(args[1]) or 10)
    end)
    
    -- Set up chat tracking
    AltoClef.onchat(function(chatInfo)
        if not chatInfo.isSelf then
            trackChatMessage(chatInfo)
        end
    end)
end

function trackChatMessage(chatInfo)
    chatStats.totalMessages = chatStats.totalMessages + 1
    
    -- Track player activity
    local sender = chatInfo.sender
    if not chatStats.playerMessages[sender] then
        chatStats.playerMessages[sender] = {
            count = 0,
            firstSeen = os.clock(),
            lastSeen = os.clock()
        }
    end
    
    chatStats.playerMessages[sender].count = chatStats.playerMessages[sender].count + 1
    chatStats.playerMessages[sender].lastSeen = os.clock()
    
    -- Track word frequency
    local words = {}
    for word in chatInfo.message:gmatch("%w+") do
        word = word:lower()
        if #word > 3 then -- Ignore short words
            words[word] = (words[word] or 0) + 1
        end
    end
    
    for word, count in pairs(words) do
        chatStats.commonWords[word] = (chatStats.commonWords[word] or 0) + count
    end
end

function showChatStatistics()
    local uptime = os.clock() - chatStats.sessionsStartTime
    local playerCount = 0
    
    for _ in pairs(chatStats.playerMessages) do
        playerCount = playerCount + 1
    end
    
    AltoClef.chat("📊 Chat Statistics:")
    AltoClef.chat(string.format("  💬 Total Messages: %d", chatStats.totalMessages))
    AltoClef.chat(string.format("  👥 Unique Players: %d", playerCount))
    AltoClef.chat(string.format("  ⏱️ Session Time: %.1f minutes", uptime / 60))
    AltoClef.chat(string.format("  📈 Messages/Minute: %.1f", chatStats.totalMessages / (uptime / 60)))
end

function showTopWords(limit)
    local wordList = {}
    for word, count in pairs(chatStats.commonWords) do
        table.insert(wordList, {word = word, count = count})
    end
    
    table.sort(wordList, function(a, b) return a.count > b.count end)
    
    AltoClef.chat(string.format("🔤 Top %d Words:", math.min(limit, #wordList)))
    for i = 1, math.min(limit, #wordList) do
        AltoClef.chat(string.format("  %d. %s (%d times)", i, wordList[i].word, wordList[i].count))
    end
end

function showTopPlayers(limit)
    local playerList = {}
    for player, data in pairs(chatStats.playerMessages) do
        table.insert(playerList, {player = player, count = data.count, lastSeen = data.lastSeen})
    end
    
    table.sort(playerList, function(a, b) return a.count > b.count end)
    
    AltoClef.chat(string.format("👥 Top %d Players:", math.min(limit, #playerList)))
    for i = 1, math.min(limit, #playerList) do
        local timeSince = os.clock() - playerList[i].lastSeen
        local timeStr = timeSince < 60 and "just now" or string.format("%.0fm ago", timeSince / 60)
        AltoClef.chat(string.format("  %d. %s (%d msgs, %s)", 
            i, playerList[i].player, playerList[i].count, timeStr))
    end
end
```

---

## 🔍 **Event System Integration**

The chat and command system integrates fully with AltoClef's EventBus:

### Event Types
- **ChatMessageEvent** - Fired when any chat message is received
- **SendChatEvent** - Fired when a chat message is sent (including by scripts)

### Event Properties
Both events provide comprehensive information about the chat activity, allowing for sophisticated chat bot behavior and monitoring.

---

## 💡 **Best Practices**

### Performance Considerations
```lua
-- ❌ Bad: Expensive operations in chat handlers
AltoClef.onchat(function(chatInfo)
    -- Heavy processing for every message
    local result = doExpensiveCalculation()
end)

-- ✅ Good: Lightweight handlers, defer heavy work
local pendingWork = {}

AltoClef.onchat(function(chatInfo)
    -- Quick check, queue work if needed
    if chatInfo.message:find("calculate") then
        table.insert(pendingWork, chatInfo)
    end
end)

function onTick()
    -- Process queued work gradually
    if #pendingWork > 0 then
        local work = table.remove(pendingWork, 1)
        processCalculation(work)
    end
end
```

### Error Handling
```lua
AltoClef.onchat(function(chatInfo)
    local success, result = pcall(function()
        -- Your chat handling logic
        handleChatMessage(chatInfo)
    end)
    
    if not success then
        AltoClef.logWarning("Chat handler error: " .. tostring(result))
    end
end)
```

### Rate Limiting
```lua
local chatCooldown = {}
local CHAT_COOLDOWN = 1000 -- 1 second

function safeChatResponse(message)
    local currentTime = os.clock() * 1000
    local lastChatTime = chatCooldown[message] or 0
    
    if currentTime - lastChatTime > CHAT_COOLDOWN then
        AltoClef.chat(message)
        chatCooldown[message] = currentTime
    end
end
```

---

## 🔍 **Debug Commands**

Test chat and command functionality:

```bash
@luadebug chat        # Show chat system information
```

---

## 🚀 **Advanced Use Cases**

- **Multi-Player Coordination** - Coordinate actions between multiple players
- **Server Administration** - Create admin commands and monitoring
- **Interactive Tutorials** - Guide new players with chat responses
- **Game Analytics** - Track player behavior and chat patterns
- **Community Features** - Build social features and mini-games
- **Alert Systems** - Notify about important events via chat

---

**Next: Learn about [AltoMenu Integration](08_ALTOMENU.md) for creating custom modules! 🏗️** 