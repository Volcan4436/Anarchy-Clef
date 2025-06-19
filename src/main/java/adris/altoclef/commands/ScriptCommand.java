package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.managers.ModuleManager;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.scripting.ScriptResourceExtractor;

/**
 * Command for testing Lua scripts
 * Usage: @script [test|menu]
 */
public class ScriptCommand extends Command {
    
    public ScriptCommand() throws CommandException {
        super("script", "Test Lua scripting functionality",
                new Arg<>(String.class, "type", "test", 0, false));
    }
    
    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        if (mod.getScriptEngine() == null) {
            mod.logWarning("Lua scripting engine is not available");
            return;
        }
        
        String testType = "test";
        try {
            testType = parser.get(String.class);
        } catch (Exception e) {
            // Default to basic test
        }
        
        switch (testType.toLowerCase()) {
            case "test":
            case "basic":
                runBasicTestScript(mod);
                break;
            case "menu":
            case "altomenu":
                runAltoMenuTestScript(mod);
                break;
            case "task":
            case "tasks":
                runTaskSystemTestScript(mod);
                break;
            case "utils":
            case "utilities":
                runUtilsTestScript(mod);
                break;
            case "persistence":
            case "persist":
                runPersistenceTestScript(mod);
                break;
            case "dependency":
            case "deps":
                runDependencyTestScript(mod);
                break;
            case "debug":
                debugModules(mod);
                break;
            case "restore":
            case "extract":
                restoreBundledScripts(mod);
                break;
            default:
                mod.log("Usage: @script [test|menu|task|utils|persistence|debug|restore]");
                mod.log("  test        - Basic AltoClef API test (default)");
                mod.log("  menu        - AltoMenu module creation test");
                mod.log("  task        - Task System API test");
                mod.log("  utils       - Utility Functions API test");
                mod.log("  persistence - Script Persistence System test");
                mod.log("  dependency  - Dependency Management System test");
                mod.log("  debug       - Debug module registration");
                mod.log("  restore     - Restore/extract bundled example scripts");
        }
    }
    
    private void runBasicTestScript(AltoClef mod) {
        String testScriptName = "basic_test";
        String testScriptCode = """
            --[[
            @name Basic Test Script
            @description Simple test script to demonstrate Lua scripting functionality
            @version 1.0.0
            @author Hearty
            @category Test
            ]]--
            
            local tickCount = 0
            
            function onLoad()
                AltoClef.log("✓ Basic Test Script loaded successfully!")
                AltoClef.log("✓ Lua scripting engine is working!")
                
                -- Test basic API functions
                testBasicAPIs()
            end
            
            function onTick()
                tickCount = tickCount + 1
                
                -- Log every 10 seconds (200 ticks) to avoid spam
                if tickCount % 200 == 0 then
                    AltoClef.log("Test script has been running for " .. (tickCount / 20) .. " seconds")
                    testGameState()
                end
            end
            
            function onEnable()
                AltoClef.log("✓ Test script enabled")
                tickCount = 0
            end
            
            function onDisable()
                AltoClef.log("✓ Test script disabled after " .. tickCount .. " ticks")
            end
            
            function onCleanup()
                AltoClef.log("✓ Test script cleaning up...")
            end
            
            function testBasicAPIs()
                AltoClef.log("Testing basic API functions...")
                
                -- Test in-game detection
                local inGame = AltoClef.isInGame()
                AltoClef.log("In game: " .. tostring(inGame))
                
                if inGame then
                    testPlayerInfo()
                    testWorldInfo()
                    testItemStorage()
                else
                    AltoClef.log("Not in game - limited testing available")
                end
            end
            
            function testPlayerInfo()
                local player = AltoClef.getPlayer()
                if player then
                    AltoClef.log("✓ Player API working - Name: " .. player.name)
                    AltoClef.log("  Health: " .. player.health .. "/" .. player.maxHealth)
                    AltoClef.log("  Position: (" .. string.format("%.1f, %.1f, %.1f", player.position.x, player.position.y, player.position.z) .. ")")
                else
                    AltoClef.log("✗ Could not get player data")
                end
            end
            
            function testWorldInfo()
                local world = AltoClef.getWorld()
                if world then
                    AltoClef.log("✓ World API working")
                    AltoClef.log("  Time: " .. world.time)
                    AltoClef.log("  Dimension: " .. AltoClef.getCurrentDimension())
                    AltoClef.log("  Weather: " .. (world.isRaining and "Raining" or world.isThundering and "Thundering" or "Clear"))
                else
                    AltoClef.log("✗ Could not get world data")
                end
            end
            
            function testItemStorage()
                local storage = AltoClef.getItemStorage()
                if storage then
                    AltoClef.log("✓ Item Storage API working")
                    
                    -- Test common items
                    local testItems = {"dirt", "stone", "wood", "bread", "apple"}
                    for _, item in ipairs(testItems) do
                        if storage.hasItem(item) then
                            AltoClef.log("  Has " .. storage.getItemCount(item) .. " " .. item)
                        end
                    end
                else
                    AltoClef.log("✗ Could not access item storage")
                end
            end
            
            function testGameState()
                if AltoClef.isInGame() then
                    local player = AltoClef.getPlayer()
                    if player and player.health then
                        if player.health < 10 then
                            AltoClef.logWarning("Player health is low: " .. player.health)
                        end
                        if player.hunger and player.hunger < 10 then
                            AltoClef.logWarning("Player hunger is low: " .. player.hunger)
                        end
                    end
                end
            end
            """;
            
        boolean success = mod.getScriptEngine().loadScript(testScriptName, testScriptCode);
        
        if (success) {
            mod.log("✓ Basic test script loaded successfully!");
            mod.log("The script will now run in the background and log information periodically.");
        } else {
            mod.logWarning("✗ Failed to load basic test script");
        }
    }
    
    private void runAltoMenuTestScript(AltoClef mod) {
        String testScriptName = "altomenu_test";
        String testScriptCode = """
            --[[
            @name AltoMenu Test Script
            @description Demonstrates creating custom AltoMenu modules from Lua scripts
            @version 1.0.0
            @author Hearty
            @category Utility
            ]]--
            
            local testModule = nil
            local tickCount = 0
            
            function onLoad()
                AltoClef.log("🚀 AltoMenu Test Script loaded!")
                
                if AltoMenu then
                    AltoClef.log("✓ AltoMenu API is available")
                    testAltoMenuAPI()
                else
                    AltoClef.logWarning("✗ AltoMenu API not available")
                end
            end
            
            function testAltoMenuAPI()
                AltoClef.log("Testing AltoMenu API...")
                
                -- Test getting categories
                local categories = AltoMenu:getCategories()
                AltoClef.log("Available categories: " .. #categories)
                
                -- Create a test module
                createTestModule()
                
                -- Test module retrieval
                testModuleRetrieval()
            end
            
            function createTestModule()
                AltoClef.log("Creating custom test module...")
                
                testModule = AltoMenu:createModule(
                    "LuaTestModule",
                    "A test module created from Lua script",
                    "Development"
                )
                
                if testModule and testModule.name then
                    AltoClef.log("✓ Successfully created module: " .. testModule.name)
                    addModuleSettings()
                    addEventHandlers()
                else
                    AltoClef.logWarning("✗ Failed to create test module")
                end
            end
            
            function addModuleSettings()
                if not testModule then return end
                
                AltoClef.log("Adding settings to test module...")
                
                -- Boolean setting
                local enabledSetting = testModule:addBooleanSetting("Enabled", true)
                if enabledSetting then
                    AltoClef.log("✓ Added boolean setting: " .. enabledSetting.name)
                end
                
                -- Number setting
                local speedSetting = testModule:addNumberSetting("Speed", 1.0, 10.0, 5.0, 0.5)
                if speedSetting then
                    AltoClef.log("✓ Added number setting: " .. speedSetting.name)
                end
                
                -- Mode setting
                local modeSetting = testModule:addModeSetting("Mode", "Normal", "Slow", "Normal", "Fast")
                if modeSetting then
                    AltoClef.log("✓ Added mode setting: " .. modeSetting.name)
                end
            end
            
            function addEventHandlers()
                if not testModule then return end
                
                testModule:onEnable(function()
                    AltoClef.log("🔥 LuaTestModule enabled!")
                end)
                
                testModule:onDisable(function()
                    AltoClef.log("❄️ LuaTestModule disabled!")
                end)
                
                testModule:onTick(function()
                    tickCount = tickCount + 1
                    if tickCount % 200 == 0 then
                        local enabledSetting = testModule:getSetting("Enabled")
                        if enabledSetting and enabledSetting:getValue() then
                            AltoClef.log("LuaTestModule is active! Tick: " .. tickCount)
                        end
                    end
                end)
                
                AltoClef.log("✓ Event handlers added successfully!")
            end
            
                         function testModuleRetrieval()
                 -- Test getting our created module
                 local retrievedModule = AltoMenu:getModule("LuaTestModule")
                 if retrievedModule and retrievedModule.name then
                     AltoClef.log("✓ Successfully retrieved module: " .. retrievedModule.name)
                 else
                     AltoClef.logWarning("✗ Could not retrieve LuaTestModule!")
                 end
                 
                 -- Test getting modules by category
                 local devModules = AltoMenu:getModulesInCategory("Development")
                 AltoClef.log("Development modules found: " .. #devModules)
                 
                 -- Module retrieval test complete
             end
            
                         function onTick()
                 -- Keep script alive with a simple tick function
                 -- This ensures the script doesn't get garbage collected
             end
             
             function onEnable()
                 AltoClef.log("AltoMenu test script enabled")
                 if testModule then
                     testModule:setEnabled(true)
                     AltoClef.log("Enabled LuaTestModule - check the AltoMenu ClickGUI!")
                 end
             end
            
            function onDisable()
                AltoClef.log("AltoMenu test script disabled")
                if testModule then
                    testModule:setEnabled(false)
                end
            end
            
            function onCleanup()
                AltoClef.log("AltoMenu test script cleaning up...")
                if testModule then
                    testModule:setEnabled(false)
                end
            end
            """;
            
        boolean success = mod.getScriptEngine().loadScript(testScriptName, testScriptCode);
        
        if (success) {
            mod.log("✓ AltoMenu test script loaded successfully!");
            mod.log("This script creates a custom 'LuaTestModule' in the Development category.");
            mod.log("Check the AltoMenu ClickGUI to see the new module with settings!");
        } else {
                         mod.logWarning("✗ Failed to load AltoMenu test script");
         }
     }
     
     private void debugModules(AltoClef mod) {
         mod.log("=== Module Debug Information ===");
         
         // Check total modules
         var allModules = ModuleManager.INSTANCE.getModules();
         mod.log("Total modules registered: " + allModules.size());
         
         // Check Development category specifically
         var devModules = ModuleManager.INSTANCE.getModulesInCategory(Mod.Category.DEVELOPMENT);
         mod.log("Development category modules: " + devModules.size());
         
         for (Mod module : devModules) {
             String status = module.isEnabled() ? "enabled" : "disabled";
             mod.log("  - " + module.getName() + " (" + status + ") - " + module.getClass().getSimpleName());
         }
         
         // Check if LuaTestModule exists
         Mod luaModule = ModuleManager.INSTANCE.getModuleByName("LuaTestModule");
         if (luaModule != null) {
             mod.log("✓ LuaTestModule found:");
             mod.log("  Name: " + luaModule.getName());
             mod.log("  Description: " + luaModule.getDescription());
             mod.log("  Category: " + luaModule.getCategory().name);
             mod.log("  Class: " + luaModule.getClass().getSimpleName());
             mod.log("  Settings count: " + luaModule.getSettings().size());
             
             for (var setting : luaModule.getSettings()) {
                 mod.log("    Setting: " + setting.getName() + " (" + setting.getClass().getSimpleName() + ")");
             }
         } else {
             mod.logWarning("✗ LuaTestModule not found in ModuleManager");
         }
         
         // Check script engine status
         if (mod.getScriptEngine() != null) {
             var scripts = mod.getScriptEngine().getLoadedScripts();
             mod.log("Loaded scripts: " + scripts.size());
             for (String scriptName : scripts.keySet()) {
                 mod.log("  - " + scriptName);
             }
         }
     }
     
     private void runTaskSystemTestScript(AltoClef mod) {
         String testScriptName = "task_system_test";
         String testScriptCode = """
             --[[
             @name TaskSystemTest
             @description Test script for AltoClef Task System API
             @version 1.0.0
             @author Hearty
             @category Testing
             ]]--
             
             local tickCount = 0
             local testPhase = 1
             local testStartTime = 0
             local customTask = nil
             
             function onLoad()
                 AltoClef.log("🚀 Task System Test Script loaded!")
                 if AltoClef.TaskSystem then
                     AltoClef.log("✓ TaskSystem API is available")
                     testStartTime = os.time()
                     startTaskSystemTests()
                 else
                     AltoClef.logWarning("✗ TaskSystem API not available")
                 end
             end
             
             function startTaskSystemTests()
                 AltoClef.log("=== Task System API Test ===")
                 
                 -- Phase 1: Test Task Catalogue
                 testTaskCatalogue()
                 
                 -- Phase 2: Test Task Status Functions
                 testTaskStatus()
                 
                 -- Phase 3: Create Custom Task
                 createSimpleTask()
                 
                 -- Phase 4: Test Item Tasks
                 testItemTasks()
                 
                 AltoClef.log("Task System tests completed! ✅")
             end
             
             function testTaskCatalogue()
                 AltoClef.log("📋 Testing Task Catalogue...")
                 
                 -- Get available tasks
                 local tasks = AltoClef.TaskSystem:getAvailableTasks()
                 AltoClef.log("Available tasks in catalogue: " .. #tasks)
                 
                 -- Show first few tasks as examples
                 AltoClef.log("Sample tasks:")
                 for i = 1, math.min(5, #tasks) do
                     if tasks[i] then
                         AltoClef.log("  " .. i .. ". " .. tasks[i])
                     end
                 end
                 
                 -- Test task existence
                 local testTasks = {"stone", "wood", "iron_ingot", "invalid_task"}
                 AltoClef.log("Testing task existence:")
                 for _, task in ipairs(testTasks) do
                     local exists = AltoClef.TaskSystem:taskExists(task)
                     AltoClef.log("  " .. task .. ": " .. (exists and "✓" or "✗"))
                 end
             end
             
             function testTaskStatus()
                 AltoClef.log("📊 Testing Task Status Functions...")
                 
                 -- Check if any task is running
                 local isRunning = AltoClef.TaskSystem:isTaskRunning()
                 AltoClef.log("Task runner active: " .. (isRunning and "✓ YES" or "✗ NO"))
                 
                 -- Get current task info
                 local currentTask = AltoClef.TaskSystem:getCurrentTask()
                 if currentTask and currentTask.name then
                     AltoClef.log("Current task: " .. currentTask.name)
                     AltoClef.log("  Active: " .. tostring(currentTask.active))
                     AltoClef.log("  Finished: " .. tostring(currentTask.finished))
                 else
                     AltoClef.log("No current task running")
                 end
             end
             
             function createSimpleTask()
                 AltoClef.log("🔧 Creating Simple Custom Task...")
                 
                 local simpleTaskLogic = {
                     tickCount = 0,
                     maxTicks = 100,
                     
                     onStart = function(self)
                         AltoClef.log("Simple task started!")
                         self.tickCount = 0
                     end,
                     
                     onTick = function(self)
                         self.tickCount = self.tickCount + 1
                         
                         if self.tickCount >= self.maxTicks then
                             AltoClef.log("Simple task completed after " .. self.tickCount .. " ticks!")
                             return false -- Finished
                         end
                         
                         -- Log progress every 20 ticks
                         if self.tickCount % 20 == 0 then
                             AltoClef.log("Simple task progress: " .. self.tickCount .. "/" .. self.maxTicks)
                         end
                         
                         return true -- Continue
                     end,
                     
                     onStop = function(self, reason)
                         AltoClef.log("Simple task stopped after " .. self.tickCount .. " ticks")
                         if reason then
                             AltoClef.log("Reason: " .. reason)
                         end
                     end,
                     
                     isFinished = function(self)
                         return self.tickCount >= self.maxTicks
                     end,
                     
                     isEqual = function(self, other)
                         return other == "SimpleTestTask"
                     end,
                     
                     toDebugString = function(self)
                         return "SimpleTestTask[" .. self.tickCount .. "/" .. self.maxTicks .. "]"
                     end
                 }
                 
                 customTask = AltoClef.TaskSystem:createTask("SimpleTestTask", simpleTaskLogic)
                 
                 if customTask and customTask.name then
                     AltoClef.log("✓ Custom task created: " .. customTask.name)
                     AltoClef.log("Task can be run with: customTask:run()")
                 else
                     AltoClef.log("✗ Failed to create custom task")
                 end
             end
             
             function testItemTasks()
                 AltoClef.log("🎯 Testing Item Tasks...")
                 
                 -- Test inventory checking
                 local testItems = {"dirt", "stone", "wood"}
                 AltoClef.log("Current inventory status:")
                 
                 for _, item in ipairs(testItems) do
                     local hasItem = AltoClef.TaskSystem:hasItem(item, 1)
                     local count = hasItem and "some" or "none"
                     AltoClef.log("  " .. item .. ": " .. count)
                 end
                 
                 AltoClef.log("Item task functions available:")
                 AltoClef.log("  • AltoClef.TaskSystem:getItem(item, count)")
                 AltoClef.log("  • AltoClef.TaskSystem:runTask(task, count)")
                 AltoClef.log("  • AltoClef.TaskSystem:hasItem(item, count)")
             end
             
             function onTick()
                 tickCount = tickCount + 1
                 
                 -- Provide status update every 10 seconds
                 if tickCount % 200 == 0 then
                     local elapsed = os.time() - testStartTime
                     AltoClef.log("Task System test running for " .. elapsed .. " seconds")
                     
                     if customTask then
                         AltoClef.log("Custom task available - use: customTask:run()")
                     end
                 end
             end
             
             function onCleanup()
                 AltoClef.log("Task System test script cleaning up...")
                 if AltoClef.TaskSystem:isTaskRunning() then
                     AltoClef.TaskSystem:stopCurrentTask()
                 end
             end
             
             -- Export the custom task for manual testing
             _G.testTask = customTask
             
             AltoClef.log("Task System test script ready!")
             """;
             
         boolean success = mod.getScriptEngine().loadScript(testScriptName, testScriptCode);
         
         if (success) {
             mod.log("✓ Task System test script loaded successfully!");
             mod.log("The script demonstrates all Task System API functions.");
             mod.log("Check the logs for test results and available functions.");
         } else {
             mod.logWarning("✗ Failed to load Task System test script");
         }
     }
     
     private void runUtilsTestScript(AltoClef mod) {
         String testScriptName = "utils_test";
         String testScriptCode = """
             --[[
             @name UtilsTest
             @description Test script for AltoClef Utility Functions API
             @version 1.0.0
             @author Hearty
             @category Testing
             ]]--
             
             local tickCount = 0
             local testStartTime = 0
             
             function onLoad()
                 AltoClef.log("🧰 Utils Test Script loaded!")
                 if AltoClef.Utils then
                     AltoClef.log("✓ Utils API is available")
                     testStartTime = AltoClef.Utils.Time:getCurrentTime()
                     startUtilsTests()
                 else
                     AltoClef.logWarning("✗ Utils API not available")
                 end
             end
             
             function startUtilsTests()
                 AltoClef.log("=== Utility Functions API Test ===")
                 
                 -- Test Math utilities
                 testMathUtils()
                 
                 -- Test String utilities
                 testStringUtils()
                 
                 -- Test Table utilities
                 testTableUtils()
                 
                 -- Test Time utilities
                 testTimeUtils()
                 
                 -- Test Position utilities
                 testPositionUtils()
                 
                 -- Test Item utilities
                 testItemUtils()
                 
                 -- Test Player utilities (if in game)
                 if AltoClef.isInGame() then
                     testPlayerUtils()
                     testWorldUtils()
                     testInventoryUtils()
                 else
                     AltoClef.log("🚫 Skipping player/world/inventory tests (not in game)")
                 end
                 
                 -- Test Debug utilities
                 testDebugUtils()
                 
                 -- Test Data persistence
                 testDataUtils()
                 
                 AltoClef.log("Utils API tests completed! ✅")
             end
             
                           function testMathUtils()
                  AltoClef.log("🔢 Testing Math Utilities...")
                  
                  local pos1 = {x = 0, y = 0, z = 0}
                  local pos2 = {x = 3, y = 4, z = 0}
                  
                  -- Use dot syntax to avoid colon self-parameter issues
                  local dist2D = AltoClef.Utils.Math.distance2D(AltoClef.Utils.Math, pos1, pos2)
                  local dist3D = AltoClef.Utils.Math.distance3D(AltoClef.Utils.Math, pos1, pos2)
                  local angle = AltoClef.Utils.Math.angleTo(AltoClef.Utils.Math, pos1, pos2)
                  
                  AltoClef.log("  2D Distance (0,0) to (3,0): " .. tostring(dist2D))
                  AltoClef.log("  3D Distance (0,0,0) to (3,4,0): " .. tostring(dist3D))
                  AltoClef.log("  Angle from (0,0) to (3,0): " .. tostring(angle))
                  
                  -- Test utility functions
                  local clamped = AltoClef.Utils.Math.clamp(AltoClef.Utils.Math, 15, 0, 10)
                  local lerped = AltoClef.Utils.Math.lerp(AltoClef.Utils.Math, 0, 100, 0.5)
                  local rounded = AltoClef.Utils.Math.round(AltoClef.Utils.Math, 3.14159, 2)
                  
                  AltoClef.log("  Clamp(15, 0, 10): " .. tostring(clamped))
                  AltoClef.log("  Lerp(0, 100, 0.5): " .. tostring(lerped))
                  AltoClef.log("  Round(3.14159, 2): " .. tostring(rounded))
              end
             
                           function testStringUtils()
                  AltoClef.log("📝 Testing String Utilities...")
                  
                  local testStr = "  Hello,World,Test  "
                  
                  -- Use dot syntax to avoid colon self-parameter issues
                  local parts = AltoClef.Utils.String.split(AltoClef.Utils.String, testStr, ",")
                  local trimmed = AltoClef.Utils.String.trim(AltoClef.Utils.String, testStr)
                  local capitalized = AltoClef.Utils.String.capitalize(AltoClef.Utils.String, "hello world")
                  
                  AltoClef.log("  Split '" .. testStr .. "' by ',': " .. #parts .. " parts")
                  AltoClef.log("  Trimmed: '" .. tostring(trimmed) .. "'")
                  AltoClef.log("  Capitalized 'hello world': '" .. tostring(capitalized) .. "'")
                  
                  local startsWith = AltoClef.Utils.String.startsWith(AltoClef.Utils.String, "Hello World", "Hello")
                  local endsWith = AltoClef.Utils.String.endsWith(AltoClef.Utils.String, "Hello World", "World")
                  local contains = AltoClef.Utils.String.contains(AltoClef.Utils.String, "Hello World", "lo Wo")
                  
                  AltoClef.log("  'Hello World' starts with 'Hello': " .. tostring(startsWith))
                  AltoClef.log("  'Hello World' ends with 'World': " .. tostring(endsWith))
                  AltoClef.log("  'Hello World' contains 'lo Wo': " .. tostring(contains))
              end
             
                           function testTableUtils()
                  AltoClef.log("📊 Testing Table Utilities...")
                  
                  local testTable = {"apple", "banana", "cherry", "date"}
                  
                  -- Use dot syntax to avoid colon self-parameter issues
                  local length = AltoClef.Utils.Table.length(AltoClef.Utils.Table, testTable)
                  local isEmpty = AltoClef.Utils.Table.isEmpty(AltoClef.Utils.Table, testTable)
                  local contains = AltoClef.Utils.Table.contains(AltoClef.Utils.Table, testTable, "banana")
                  local indexOf = AltoClef.Utils.Table.indexOf(AltoClef.Utils.Table, testTable, "cherry")
                  
                  AltoClef.log("  Table length: " .. tostring(length))
                  AltoClef.log("  Table is empty: " .. tostring(isEmpty))
                  AltoClef.log("  Contains 'banana': " .. tostring(contains))
                  AltoClef.log("  Index of 'cherry': " .. tostring(indexOf))
                  
                  local reversed = AltoClef.Utils.Table.reverse(AltoClef.Utils.Table, testTable)
                  AltoClef.log("  Reversed table first item: " .. tostring(reversed[1]))
              end
             
             function testTimeUtils()
                 AltoClef.log("⏰ Testing Time Utilities...")
                 
                 -- Start a timer
                 AltoClef.Utils.Time:startTimer("test_timer")
                 
                 -- Simulate some time passing (we can't actually wait)
                 local currentTime = AltoClef.Utils.Time:getCurrentTime()
                 AltoClef.log("  Current time: " .. currentTime)
                 
                 -- Check elapsed time (should be very small)
                 local elapsed = AltoClef.Utils.Time:getElapsed("test_timer")
                 AltoClef.log("  Elapsed time for test_timer: " .. elapsed .. "ms")
                 
                 -- Test hasElapsed (should be false for 1000ms)
                 local hasElapsed = AltoClef.Utils.Time:hasElapsed("test_timer", 1000)
                 AltoClef.log("  Has 1000ms elapsed: " .. tostring(hasElapsed))
             end
             
                           function testPositionUtils()
                  AltoClef.log("📍 Testing Position Utilities...")
                  
                  -- Use dot syntax to avoid colon self-parameter issues
                  local pos1 = AltoClef.Utils.Position.create(AltoClef.Utils.Position, 1.5, 2.7, 3.9)
                  local pos2 = AltoClef.Utils.Position.create(AltoClef.Utils.Position, 0.5, 1.2, 0.8)
                  
                  local added = AltoClef.Utils.Position.add(AltoClef.Utils.Position, pos1, pos2)
                  local subtracted = AltoClef.Utils.Position.subtract(AltoClef.Utils.Position, pos1, pos2)
                  local multiplied = AltoClef.Utils.Position.multiply(AltoClef.Utils.Position, pos1, 2)
                  local blockPos = AltoClef.Utils.Position.toBlockPos(AltoClef.Utils.Position, pos1)
                  
                  AltoClef.log("  Position 1: (" .. tostring(pos1.x) .. ", " .. tostring(pos1.y) .. ", " .. tostring(pos1.z) .. ")")
                  AltoClef.log("  Position 2: (" .. tostring(pos2.x) .. ", " .. tostring(pos2.y) .. ", " .. tostring(pos2.z) .. ")")
                  AltoClef.log("  Added: (" .. tostring(added.x) .. ", " .. tostring(added.y) .. ", " .. tostring(added.z) .. ")")
                  AltoClef.log("  Multiplied by 2: (" .. tostring(multiplied.x) .. ", " .. tostring(multiplied.y) .. ", " .. tostring(multiplied.z) .. ")")
                  AltoClef.log("  Block position: (" .. tostring(blockPos.x) .. ", " .. tostring(blockPos.y) .. ", " .. tostring(blockPos.z) .. ")")
              end
             
             function testItemUtils()
                 AltoClef.log("🎒 Testing Item Utilities...")
                 
                 -- Use dot syntax to avoid colon self-parameter issues
                 local stoneExists = AltoClef.Utils.Item.exists(AltoClef.Utils.Item, "minecraft:stone")
                 local diamondExists = AltoClef.Utils.Item.exists(AltoClef.Utils.Item, "minecraft:diamond")
                 local invalidExists = AltoClef.Utils.Item.exists(AltoClef.Utils.Item, "invalid_item")
                 
                 if stoneExists then
                     local stoneName = AltoClef.Utils.Item.getDisplayName(AltoClef.Utils.Item, "minecraft:stone")
                     AltoClef.log("  stone exists: " .. tostring(stoneName))
                 else
                     AltoClef.log("  stone does not exist")
                 end
                 
                 if diamondExists then
                     local diamondName = AltoClef.Utils.Item.getDisplayName(AltoClef.Utils.Item, "minecraft:diamond")
                     AltoClef.log("  diamond exists: " .. tostring(diamondName))
                 else
                     AltoClef.log("  diamond does not exist")
                 end
                 
                 if not invalidExists then
                     AltoClef.log("  invalid_item does not exist")
                 end
             end
             
             function testPlayerUtils()
                 AltoClef.log("👤 Testing Player Utilities...")
                 
                 local healthLow = AltoClef.Utils.Player:isHealthLow(10)
                 local hungerLow = AltoClef.Utils.Player:isHungerLow(10)
                 local distanceToSpawn = AltoClef.Utils.Player:getDistanceToSpawn()
                 
                 AltoClef.log("  Health below 10: " .. tostring(healthLow))
                 AltoClef.log("  Hunger below 10: " .. tostring(hungerLow))
                                   if distanceToSpawn >= 0 then
                      AltoClef.log("  Distance to spawn: " .. tostring(distanceToSpawn))
                  else
                      AltoClef.log("  Could not calculate distance to spawn")
                  end
             end
             
             function testWorldUtils()
                 AltoClef.log("🌍 Testing World Utilities...")
                 
                 local dimension = AltoClef.Utils.World:getDimensionName()
                 local isNight = AltoClef.Utils.World:isNight()
                 local isDay = AltoClef.Utils.World:isDay()
                 local timeOfDay = AltoClef.Utils.World:getTimeOfDay()
                 
                 AltoClef.log("  Current dimension: " .. dimension)
                 AltoClef.log("  Is night: " .. tostring(isNight))
                 AltoClef.log("  Is day: " .. tostring(isDay))
                 AltoClef.log("  Time of day: " .. timeOfDay)
             end
             
             function testInventoryUtils()
                 AltoClef.log("🎒 Testing Inventory Utilities...")
                 
                 local totalItems = AltoClef.Utils.Inventory:getTotalItemCount()
                 local uniqueItems = AltoClef.Utils.Inventory:getUniqueItemCount()
                 
                 AltoClef.log("  Total items in inventory: " .. totalItems)
                 AltoClef.log("  Unique item types: " .. uniqueItems)
             end
             
             function testDebugUtils()
                 AltoClef.log("🔍 Testing Debug Utilities...")
                 
                 local testTable = {name = "Test", value = 42, active = true}
                 AltoClef.Utils.Debug.logTable(AltoClef.Utils.Debug, testTable)
                 
                 local testPos = {x = 100.5, y = 64.0, z = -200.3}
                 AltoClef.Utils.Debug.logPosition(AltoClef.Utils.Debug, testPos)
                 
                 -- Benchmark a simple function
                 local benchmarkResult = AltoClef.Utils.Debug.benchmark(AltoClef.Utils.Debug, "simple_loop", function()
                     local sum = 0
                     for i = 1, 1000 do
                         sum = sum + i
                     end
                     return sum
                 end)
                 
                 if benchmarkResult and benchmarkResult > 0 then
                     AltoClef.log("  Benchmark completed in " .. tostring(benchmarkResult) .. "ms")
                 end
             end
             
             function testDataUtils()
                 AltoClef.log("💾 Testing Data Persistence...")
                 
                 -- Store some data using dot syntax
                 AltoClef.Utils.Data.store(AltoClef.Utils.Data, "test_key", "test_value")
                 AltoClef.Utils.Data.store(AltoClef.Utils.Data, "number_key", 42)
                 AltoClef.Utils.Data.store(AltoClef.Utils.Data, "table_key", {a = 1, b = 2})
                 
                 -- Retrieve and check
                 local stringVal = AltoClef.Utils.Data.retrieve(AltoClef.Utils.Data, "test_key")
                 local numberVal = AltoClef.Utils.Data.retrieve(AltoClef.Utils.Data, "number_key")
                 local tableVal = AltoClef.Utils.Data.retrieve(AltoClef.Utils.Data, "table_key")
                 local missingVal = AltoClef.Utils.Data.retrieve(AltoClef.Utils.Data, "missing_key")
                 
                 AltoClef.log("  Retrieved string: " .. tostring(stringVal))
                 AltoClef.log("  Retrieved number: " .. tostring(numberVal))
                 AltoClef.log("  Retrieved table.a: " .. tostring(tableVal and tableVal.a or "nil"))
                 AltoClef.log("  Missing key result: " .. tostring(missingVal))
                 
                 -- Check existence
                 local exists = AltoClef.Utils.Data.exists(AltoClef.Utils.Data, "test_key")
                 local notExists = AltoClef.Utils.Data.exists(AltoClef.Utils.Data, "missing_key")
                 AltoClef.log("  'test_key' exists: " .. tostring(exists))
                 AltoClef.log("  'missing_key' exists: " .. tostring(notExists))
                 
                 -- List all keys
                 local keys = AltoClef.Utils.Data.keys(AltoClef.Utils.Data)
                 AltoClef.log("  Stored keys count: " .. tostring(#keys))
             end
             
             function onTick()
                 tickCount = tickCount + 1
                 
                 -- Provide status update every 10 seconds
                                   if tickCount % 200 == 0 then
                      local elapsed = AltoClef.Utils.Time:getCurrentTime() - testStartTime
                      AltoClef.log("Utils test running for " .. tostring(elapsed / 1000) .. " seconds")
                  end
             end
             
             function onCleanup()
                 AltoClef.log("Utils test script cleaning up...")
                 AltoClef.Utils.Time:removeTimer("test_timer")
                 AltoClef.Utils.Data:clear()
             end
             
             AltoClef.log("Utils test script ready!")
             """;
             
         boolean success = mod.getScriptEngine().loadScript(testScriptName, testScriptCode);
         
         if (success) {
             mod.log("✓ Utils test script loaded successfully!");
             mod.log("The script demonstrates all Utility Functions API capabilities.");
             mod.log("Check the logs for comprehensive test results.");
         } else {
             mod.logWarning("✗ Failed to load Utils test script");
         }
     }
     
     private void runPersistenceTestScript(AltoClef mod) {
         String testScriptName = "persistence_test";
         String testScriptCode = """
             --[[
             @name PersistenceTest
             @description Test script for AltoClef Script Persistence System
             @version 1.0.0
             @author Hearty
             @category Testing
             ]]--
             
             local tickCount = 0
             local testStartTime = 0
             local persistenceData = {}
             
             function onLoad()
                 AltoClef.log("💾 Script Persistence Test Script loaded!")
                 if AltoClef.Utils and AltoClef.Utils.Data then
                     AltoClef.log("✓ Data persistence API is available")
                     testStartTime = AltoClef.Utils.Time:getCurrentTime()
                     startPersistenceTests()
                 else
                     AltoClef.logWarning("✗ Data persistence API not available")
                 end
             end
             
             function startPersistenceTests()
                 AltoClef.log("=== Script Persistence System Test ===")
                 
                 -- Test 1: Basic data storage and retrieval
                 testBasicPersistence()
                 
                 -- Test 2: Data types persistence
                 testDataTypes()
                 
                 -- Test 3: Key management
                 testKeyManagement()
                 
                 -- Test 4: Data persistence across script operations
                 testPersistenceReliability()
                 
                 AltoClef.log("Script Persistence tests completed! ✅")
             end
             
             function testBasicPersistence()
                 AltoClef.log("📝 Testing Basic Data Persistence...")
                 
                 -- Store some basic data
                 AltoClef.Utils.Data:store("test_string", "Hello, Persistence!")
                 AltoClef.Utils.Data:store("test_number", 42)
                 AltoClef.Utils.Data:store("test_boolean", true)
                 
                 -- Retrieve and verify
                 local retrievedString = AltoClef.Utils.Data:retrieve("test_string")
                 local retrievedNumber = AltoClef.Utils.Data:retrieve("test_number")
                 local retrievedBoolean = AltoClef.Utils.Data:retrieve("test_boolean")
                 
                 AltoClef.log("  Stored string: 'Hello, Persistence!' -> Retrieved: '" .. tostring(retrievedString) .. "'")
                 AltoClef.log("  Stored number: 42 -> Retrieved: " .. tostring(retrievedNumber))
                 AltoClef.log("  Stored boolean: true -> Retrieved: " .. tostring(retrievedBoolean))
                 
                 -- Test existence checking
                 local exists = AltoClef.Utils.Data:exists("test_string")
                 local notExists = AltoClef.Utils.Data:exists("nonexistent_key")
                 AltoClef.log("  Key 'test_string' exists: " .. tostring(exists))
                 AltoClef.log("  Key 'nonexistent_key' exists: " .. tostring(notExists))
             end
             
             function testDataTypes()
                 AltoClef.log("🗂️ Testing Different Data Types...")
                 
                 -- Test table storage
                 local testTable = {
                     name = "TestTable",
                     values = {1, 2, 3, 4, 5},
                     nested = {
                         deep = {
                             value = "Deep nested value"
                         }
                     },
                     count = 10
                 }
                 
                 AltoClef.Utils.Data:store("test_table", testTable)
                 local retrievedTable = AltoClef.Utils.Data:retrieve("test_table")
                 
                 if retrievedTable and retrievedTable.name then
                     AltoClef.log("  Table persistence: ✓ SUCCESS")
                     AltoClef.log("    Name: " .. tostring(retrievedTable.name))
                     AltoClef.log("    Count: " .. tostring(retrievedTable.count))
                     if retrievedTable.nested and retrievedTable.nested.deep then
                         AltoClef.log("    Deep nested: " .. tostring(retrievedTable.nested.deep.value))
                     end
                 else
                     AltoClef.log("  Table persistence: ✗ FAILED")
                 end
                 
                 -- Test nil values
                 AltoClef.Utils.Data:store("test_nil", nil)
                 local retrievedNil = AltoClef.Utils.Data:retrieve("test_nil")
                 AltoClef.log("  Nil value persistence: " .. (retrievedNil == nil and "✓ SUCCESS" or "✗ FAILED"))
             end
             
             function testKeyManagement()
                 AltoClef.log("🔑 Testing Key Management...")
                 
                 -- Store multiple keys
                 for i = 1, 5 do
                     AltoClef.Utils.Data:store("key_" .. i, "value_" .. i)
                 end
                 
                 -- Get all keys
                 local allKeys = AltoClef.Utils.Data:keys()
                 AltoClef.log("  Total stored keys: " .. #allKeys)
                 
                 -- List some keys
                 AltoClef.log("  Sample keys:")
                 for i = 1, math.min(3, #allKeys) do
                     if allKeys[i] then
                         local value = AltoClef.Utils.Data:retrieve(allKeys[i])
                         AltoClef.log("    " .. allKeys[i] .. " = " .. tostring(value))
                     end
                 end
                 
                 -- Test key removal
                 local removedValue = AltoClef.Utils.Data:remove("key_1")
                 AltoClef.log("  Removed key_1, got value: " .. tostring(removedValue))
                 
                 local stillExists = AltoClef.Utils.Data:exists("key_1")
                 AltoClef.log("  key_1 still exists after removal: " .. tostring(stillExists))
             end
             
             function testPersistenceReliability()
                 AltoClef.log("🔄 Testing Persistence Reliability...")
                 
                 -- Store session data
                 local sessionData = {
                     sessionStart = testStartTime,
                     tickCount = tickCount,
                     testRunCount = (AltoClef.Utils.Data:retrieve("test_run_count") or 0) + 1,
                     lastTestDate = os.date("%Y-%m-%d %H:%M:%S")
                 }
                 
                 AltoClef.Utils.Data:store("session_data", sessionData)
                 AltoClef.Utils.Data:store("test_run_count", sessionData.testRunCount)
                 
                 AltoClef.log("  This is test run #" .. sessionData.testRunCount)
                 AltoClef.log("  Session started at: " .. sessionData.lastTestDate)
                 
                 -- Store script statistics
                 local stats = AltoClef.Utils.Data:retrieve("script_stats") or {}
                 stats.totalTests = (stats.totalTests or 0) + 1
                 stats.lastTest = sessionData.lastTestDate
                 stats.averageTickCount = ((stats.averageTickCount or 0) + tickCount) / 2
                 
                 AltoClef.Utils.Data:store("script_stats", stats)
                 
                 AltoClef.log("  Total tests run: " .. stats.totalTests)
                 AltoClef.log("  Average tick count: " .. string.format("%.1f", stats.averageTickCount))
             end
             
             function onTick()
                 tickCount = tickCount + 1
                 
                 -- Update live data every 5 seconds
                 if tickCount % 100 == 0 then
                     AltoClef.Utils.Data:store("live_tick_count", tickCount)
                     AltoClef.Utils.Data:store("live_timestamp", AltoClef.Utils.Time:getCurrentTime())
                 end
                 
                 -- Status update every 10 seconds
                 if tickCount % 200 == 0 then
                     local elapsed = AltoClef.Utils.Time:getCurrentTime() - testStartTime
                     AltoClef.log("Persistence test running for " .. string.format("%.1f", elapsed / 1000) .. " seconds")
                     
                     -- Show some persisted data
                     local storedKeys = AltoClef.Utils.Data:keys()
                     AltoClef.log("Currently storing " .. #storedKeys .. " persistent keys")
                 end
             end
             
             function onEnable()
                 AltoClef.log("Persistence test script enabled")
                 AltoClef.Utils.Data:store("script_state", "enabled")
                 AltoClef.Utils.Data:store("last_enable_time", AltoClef.Utils.Time:getCurrentTime())
             end
             
             function onDisable()
                 AltoClef.log("Persistence test script disabled")
                 AltoClef.Utils.Data:store("script_state", "disabled")
                 AltoClef.Utils.Data:store("last_disable_time", AltoClef.Utils.Time:getCurrentTime())
                 AltoClef.Utils.Data:store("final_tick_count", tickCount)
             end
             
             function onCleanup()
                 AltoClef.log("Persistence test script cleaning up...")
                 
                 -- Store cleanup data
                 AltoClef.Utils.Data:store("cleanup_time", AltoClef.Utils.Time:getCurrentTime())
                 AltoClef.Utils.Data:store("cleanup_tick_count", tickCount)
                 
                 -- Note: We don't clear all data here so it persists for next run
                 AltoClef.log("Persistence data will survive script cleanup!")
             end
             
             AltoClef.log("Script Persistence test script ready!")
             """;
             
         boolean success = mod.getScriptEngine().loadScript(testScriptName, testScriptCode);
         
         if (success) {
             mod.log("✓ Script Persistence test script loaded successfully!");
             mod.log("This script tests data persistence, storage, and retrieval across script lifecycle.");
             mod.log("Data will persist between script reloads and game sessions.");
             mod.log("Check the logs for detailed persistence test results.");
                 } else {
            mod.logWarning("✗ Failed to load Script Persistence test script");
        }
    }
    
    private void runDependencyTestScript(AltoClef mod) {
        String testScriptName = "dependency_test";
        String testScriptCode = """
            --[[
            @name DependencyTest
            @description Test script for AltoClef Dependency Management System
            @version 1.0.0
            @author Hearty
            @category Testing
            @dependencies [test_lib, math_utils]
            ]]--
            
            local tickCount = 0
            
            function onLoad()
                AltoClef.log("🔗 Dependency Management Test Script loaded!")
                AltoClef.log("✓ Dependency Manager is working!")
                testDependencySystem()
            end
            
            function testDependencySystem()
                AltoClef.log("=== Dependency Management System Test ===")
                
                -- This script declares dependencies on 'test_lib' and 'math_utils'
                -- These are fictional dependencies for testing purposes
                
                AltoClef.log("📋 This script depends on:")
                AltoClef.log("  - test_lib (fictional utility library)")
                AltoClef.log("  - math_utils (fictional math helper)")
                
                AltoClef.log("🔍 The dependency manager should have:")
                AltoClef.log("  1. Parsed this script's metadata")
                AltoClef.log("  2. Identified missing dependencies")
                AltoClef.log("  3. Logged warnings about unresolved dependencies")
                AltoClef.log("  4. Still allowed the script to load (with warnings)")
                
                AltoClef.log("✓ If you see this message, the dependency system is working!")
                AltoClef.log("  The script loaded despite missing dependencies")
                AltoClef.log("  Check the console for dependency resolution messages")
            end
            
            function onTick()
                tickCount = tickCount + 1
                
                -- Status update every 10 seconds
                if tickCount % 200 == 0 then
                    AltoClef.log("Dependency test script running (tick " .. tickCount .. ")")
                    AltoClef.log("Dependencies: test_lib, math_utils (both missing - this is expected)")
                end
            end
            
            function onEnable()
                AltoClef.log("Dependency test script enabled")
            end
            
            function onDisable()
                AltoClef.log("Dependency test script disabled after " .. tickCount .. " ticks")
            end
            
            function onCleanup()
                AltoClef.log("Dependency test script cleaning up...")
            end
            
            AltoClef.log("Dependency test script ready!")
            """;
            
        boolean success = mod.getScriptEngine().loadScript(testScriptName, testScriptCode);
        
        if (success) {
            mod.log("✓ Dependency test script loaded successfully!");
            mod.log("This script declares fictional dependencies to test the dependency system.");
            mod.log("Check the console for dependency resolution messages and warnings.");
            
            // Display dependency manager statistics
            var depManager = mod.getScriptEngine().getDependencyManager();
            if (depManager != null) {
                var stats = depManager.getStats();
                mod.log("📊 Dependency Manager Statistics:");
                mod.log("  " + stats.getSummary());
                
                // Show available scripts
                var availableScripts = depManager.getAvailableScripts();
                mod.log("📋 Scripts registered with dependency manager: " + availableScripts.size());
                for (String scriptName : availableScripts) {
                    var deps = depManager.getScriptDependencies(scriptName);
                    var dependents = depManager.getScriptDependents(scriptName);
                    boolean loaded = depManager.isScriptLoaded(scriptName);
                    mod.log("  - " + scriptName + " (deps: " + deps.size() + ", dependents: " + 
                           dependents.size() + ", loaded: " + loaded + ")");
                }
            }
        } else {
            mod.logWarning("✗ Failed to load dependency test script");
        }
    }
    
    private void restoreBundledScripts(AltoClef mod) {
        mod.log("🚀 Restoring bundled example scripts...");
        
        try {
            // Force extract all bundled scripts (overwrites existing)
            ScriptResourceExtractor.forceExtractBundledScripts();
            ScriptResourceExtractor.createDefaultUserScript();
            
            mod.log("✅ Successfully restored bundled scripts!");
            mod.log("📁 Scripts extracted to: AltoClefLUA/scripts/");
            
            // List what was extracted
            var extractedScripts = ScriptResourceExtractor.getBundledScripts();
            mod.log("📝 Extracted " + extractedScripts.size() + " bundled scripts:");
            
            for (String script : extractedScripts) {
                mod.log("  ✓ " + script);
            }
            
            mod.log("➕ Also created: user_scripts/welcome.lua");
            mod.log("🔄 Refresh the Scripts tab in AltoMenu to see the changes!");
            
        } catch (Exception e) {
            mod.logWarning("❌ Failed to restore bundled scripts: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 