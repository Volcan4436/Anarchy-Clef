# Introduction & Setup

**Version:** 1.0.0  
**Status:** ✅ Complete  
**Category:** Getting Started  

## Overview

The AltoClef Lua Scripting System provides a powerful, enterprise-grade platform for creating custom automation scripts in Minecraft. Built with comprehensive APIs, debugging tools, and a modern management interface, it enables both beginners and advanced users to create sophisticated automation workflows.

---

## 📚 Table of Contents

1. [🚀 Quick Start](#-quick-start)
2. [✨ Key Features](#-key-features)
3. [⚙️ Getting Started](#️-getting-started)
4. [🎯 What You Can Build](#-what-you-can-build)
5. [⚡ Quick Examples](#-quick-examples)
6. [📈 Current Capabilities](#-current-capabilities)
7. [🔗 Next Steps](#-next-steps)

---

## 🚀 Quick Start

### Essential Commands
- **Open Script Manager:** Press menu key → Scripts tab
- **Enable Script:** Click script file in browser
- **Debug Issues:** `@luadebug scripts` or `@luadebug errors`

### Your First Script
Create `AltoClefLUA/scripts/user_scripts/hello_world.lua`:

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
    if not AltoClef.isInGame() then return end
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

---

## Key Features

###  Core Capabilities
- **Complete Lua 5.2 Environment** - Full scripting capabilities with sandboxed execution
- **Rich API Library** - Access to player data, world information, inventory management, and more
- **Real-time Execution** - Scripts run on the game tick cycle for responsive automation
- **Script Lifecycle Management** - Automatic state persistence and error recovery

###  User Experience
- **Visual Script Manager** - File browser, drag & drop, one-click enable/disable
- **Advanced Debug Tools** - Comprehensive debugging commands and real-time monitoring
- **AltoMenu Integration** - Create custom modules with settings and UI elements

---

## ⚙️ Getting Started

### File Structure
```
AltoClefLUA/
├── scripts/
│   ├── examples/           # Example scripts for learning
│   │   ├── auto_food.lua
│   │   ├── basic_mining.lua
│   │   └── chat_demo.lua
│   ├── libraries/          # Shared utility libraries
│   │   └── math_helpers.lua
│   └── user_scripts/       # Your custom scripts
│       └── my_script.lua
```

### Script Manager Access
1. **Open AltoClef Menu** - Press your configured menu key (default: Right Shift)
2. **Navigate to Scripts Tab** - Click the "Scripts" tab in the top menu
3. **Browse Scripts** - View all available scripts in the file browser
4. **Enable Scripts** - Click on any script to enable/disable it

---

## What You Can Build

### **Automation Bots**
- **Auto-Mining** - Automatically find and mine valuable ores
- **Smart Farming** - Plant, harvest, and manage crops
- **Resource Collection** - Gather specific materials efficiently
- **Base Building** - Automated construction and infrastructure

### n*Interactive Systems**
- **Chat Bots** - Respond to player messages with custom logic
- **Command Systems** - Create custom @ commands for users
- **Monitoring Tools** - Track health, hunger, resources, performance
- **Alert Systems** - Notify about important game events

### **Custom Modules**
- **AltoMenu Integration** - Create modules with settings UI
- **Movement Enhancements** - Advanced jump and velocity control
- **Combat Assistance** - Smart fighting and defense
- **Utility Tools** - Quality of life improvements

### **Advanced Automation**
- **Multi-Task Coordination** - Complex workflows and task chains
- **Decision Making** - AI-like behavior based on game state
- **Event-Driven Actions** - React to specific game events
- **Performance Optimization** - Efficient resource utilization

---

---

## Quick Examples

### Simple Health Monitor
```lua
function onTick()
    if not AltoClef.isInGame() then return end
    
    if AltoClef.getHealth() < 10 then
        AltoClef.log("⚠️ Low health warning!")
    end
end
```

### Auto Jumper
```lua
function onTick()
    if not AltoClef.isInGame() then return end
    
    if not AltoClef.isJumping() then
        AltoClef.jump()
    end
end
```

### Chat Responder
```lua
function onLoad()
    AltoClef.onchat(function(chatInfo)
        if not chatInfo.isSelf and chatInfo.message:lower():find("hello") then
            AltoClef.chat("Hello there, " .. chatInfo.sender .. "!")
        end
    end)
end
```

---

##  Current Capabilities

###  Fully Implemented
- ** Player APIs** - Health, hunger, position, movement, physics
- ** World APIs** - Time, weather, dimensions, biomes, blocks, light levels
- ** Inventory APIs** - Item management, equipment, containers, furnaces
- ** Control APIs** - Input simulation, camera control, block breaking
- ** Chat & Commands** - Messages, custom commands, event handling
- ** Entity APIs** - Player/mob detection, tracking, hostile monitoring
- ** Debug Tools** - Comprehensive error logging and debugging

###  Coming Soon
- ** AltoMenu Integration** - Custom modules with settings UI
- ** Advanced Analytics** - Performance monitoring and statistics
- ** Task System** - Integration with AltoClef's powerful task system
- ** More Examples** - Comprehensive example library

---

## 🔗 Next Steps

**Get Started:**
- [Script Structure](02_SCRIPT_STRUCTURE.md) - Learn the script template and lifecycle
- [Examples](10_EXAMPLES.md) - Browse working example scripts

**Larn APIs:**
- [Player APIs](03_PLAYER_APIS.md) - Movement, health, status information
- [World APIs](04_WORLD_APIS.md) - Time, weather, blocks, light levels
- [Chat & Commands](07_CHAT_COMMANDS.md) - Interactive chat and command systems

**Advanced Topics:**
- [Debug Tools](09_DEBUG_TOOLS.md) - Troubleshooting and optimization
- [Entity APIs](08_ENTITY_APIS.md) - Player and mob interaction
- [API Reference](13_API_REFERENCE.md) - Complete function reference

---

## 🚨 Troubleshooting

### Common Issues

**Script not running**
- **Check:** Script enabled in UI and no syntax errors
- **Debug:** `@luadebug scripts` and `@luadebug errors`

**API functions not working**
- **Check:** Using correct function names and `AltoClef.isInGame()`
- **Debug:** `@luladebug player` or specific API category

### Debug Commands
```bash
@luadebug scripts     # Show all loaded scripts and status
@luadebug errors      # Display recent script errors
@luadebug help        # List all available debug commands
```

---