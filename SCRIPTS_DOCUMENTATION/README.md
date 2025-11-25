# ? AltoClef Lua Scripting Documentation

**Version:** 1.0.0  
**Status:** ? Complete  
**Category:** Getting Started

## ? Overview

Welcome to the comprehensive documentation for the AltoClef Lua Scripting System. This documentation covers all aspects of creating, managing, and debugging Lua scripts for AltoClef automation with 125+ APIs across 7 major categories.

---

## ? Table of Contents

### ? Getting Started
1. [? Introduction & Setup](01_INTRODUCTION.md) - Get started with the scripting system
2. [? Script Structure](02_SCRIPT_STRUCTURE.md) - Learn the standard script format and lifecycle

### ? Core APIs
3. [? Player APIs](03_PLAYER_APIS.md) - Movement, health, physics, input controls
4. [? World APIs](04_WORLD_APIS.md) - Time, weather, dimensions, blocks, light levels
5. [? Inventory APIs](05_INVENTORY_APIS.md) - Item management, equipment, containers
6. [? Control APIs](06_CONTROL_APIS.md) - Input simulation, camera control, actions
7. [? Chat & Commands](07_CHAT_COMMANDS.md) - Interactive chat and custom commands
8. [? Entity APIs](08_ENTITY_APIS.md) - Player/mob detection and tracking

### ? Development Tools
9. [? Debug Tools](09_DEBUG_TOOLS.md) - Troubleshooting and optimization tools

### ? Reference Materials
10. [? Examples](10_EXAMPLES.md) - Complete example scripts *(coming soon)*
11. [? AltoMenu Integration](11_ALTOMENU.md) - Custom UI modules *(coming soon)*
12. [? Performance Guide](12_PERFORMANCE.md) - Optimization techniques *(coming soon)*
13. [? API Reference](13_API_REFERENCE.md) - Complete function reference *(coming soon)*

---

## ? Quick Start

### ? New to Scripting?
```bash
1. Read Introduction & Setup -> Learn what's possible
2. Study Script Structure -> Understand the template
3. Try Player APIs -> Start with basic automation
4. Use Debug Tools -> Troubleshoot any issues
```

### ? Need Specific Features?
- **? Player Automation** -> [Player APIs](03_PLAYER_APIS.md)
- **? World Interaction** -> [World APIs](04_WORLD_APIS.md)
- **? Item Management** -> [Inventory APIs](05_INVENTORY_APIS.md)
- **? Chat Systems** -> [Chat & Commands](07_CHAT_COMMANDS.md)
- **? Entity Detection** -> [Entity APIs](08_ENTITY_APIS.md)

### ? Having Issues?
```bash
@luadebug errors      # Check for script errors
@luadebug scripts     # See all loaded scripts
@luadebug help        # List debug commands
```

---

## ? What's Available

### ? **Fully Implemented (125+ APIs)**
- **? Player APIs** - Movement, health, physics, input controls, status
- **? World APIs** - Time, weather, dimensions, biomes, blocks, light levels
- **? Inventory APIs** - Items, equipment, containers, furnaces, crafting
- **? Control APIs** - Input simulation, camera control, block interaction
- **? Chat & Commands** - Messages, custom commands, event handling
- **? Entity APIs** - Player/mob detection, tracking, hostile monitoring
- **? Debug Tools** - Error logging, performance monitoring, diagnostics

### ? **Coming Soon**
- **? AltoMenu Integration** - Custom modules with settings UI
- **? Performance Guide** - Advanced optimization techniques
- **? More Examples** - Comprehensive script library
- **? Complete API Reference** - Searchable function database

---

## ? Key Features

### ? **Dual API Access**
```lua
-- Main API (recommended)
local health = AltoClef.getHealth()
AltoClef.chat("Hello World!")

-- Utils API (alternative)  
local health = Utils.Player.getHealth()
Utils.Chat.whisper("PlayerName", "Message")
```

### ? **Advanced Error Handling**
- Visual error identification with stack traces
- Automatic script disable after repeated errors
- Performance monitoring and warnings
- Comprehensive debug command suite

### ? **Complete Automation**
- **Real-time execution** - Scripts run on game tick cycle
- **Event-driven** - React to chat, player actions, world changes
- **Safe execution** - Sandboxed environment with resource limits
- **Easy debugging** - Rich diagnostic tools and logging

---

## ? Documentation Standards

### ? **Consistent Format**
- **? Overview** - Clear explanation of purpose
- **? Quick Start** - Essential functions and examples
- **? API Reference** - Complete function documentation
- **? Examples** - Practical, working code samples
- **? Best Practices** - Performance tips and patterns
- **? Troubleshooting** - Common issues and solutions

### ? **Quality Assurance**
- **Code examples** for every API function
- **Cross-references** between related topics
- **Error handling** patterns throughout
- **Performance considerations** noted where relevant

---

## ? Quick Navigation

**? Most Popular:**
- [? Player APIs](03_PLAYER_APIS.md) - Essential for any automation
- [? Chat & Commands](07_CHAT_COMMANDS.md) - Create interactive bots
- [? Debug Tools](09_DEBUG_TOOLS.md) - Essential troubleshooting

**? Advanced Topics:**
- [? Inventory APIs](05_INVENTORY_APIS.md) - Complex item management
- [? Entity APIs](08_ENTITY_APIS.md) - Player and mob interaction
- [? Control APIs](06_CONTROL_APIS.md) - Sophisticated input automation

---

## ? Troubleshooting

### Common Issues
**Script not loading**
- Check for syntax errors in metadata header
- Ensure proper `]]--` closing in header
- Use `@luadebug scripts` to see status

**API functions not working**
- Always check `AltoClef.isInGame()` first
- Verify correct function names and parameters
- Use `@luadebug errors` to see specific issues

**Performance problems**
- Add throttling to expensive operations
- Check `@luadebug performance` for metrics
- Use `pcall()` for error-prone code

### Debug Commands
```bash
@luadebug help        # List all debug commands
@luadebug scripts     # Show script status
@luadebug errors      # Display recent errors
@luadebug performance # Show performance metrics
```

---

## ? Contributing

Found an error or want to improve the documentation?
- Follow the established template format from [00_TEMPLATE.md](00_TEMPLATE.md)
- Include practical code examples
- Add troubleshooting sections
- Test all code samples before submitting

---

**Ready to build amazing automation? Let's get started!** 