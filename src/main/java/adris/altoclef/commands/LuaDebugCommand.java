package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * Debug command for Lua scripting API
 * Provides information about available API calls and current values
 * Usage: @hunger, @position, @inventory, etc.
 * 
 * @author Hearty
 */
public class LuaDebugCommand extends Command {
    
    public LuaDebugCommand() throws CommandException {
        super("luadebug", "Show Lua API debug information", new Arg(String.class, "type"));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String debugType = parser.get(String.class).toLowerCase();
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            mod.log("§cNot in game - cannot get debug values");
            return;
        }
        
        PlayerEntity player = client.player;
        
        switch (debugType) {
            case "hunger" -> showHungerDebug(mod, player);
            case "position" -> showPositionDebug(mod, player);
            case "inventory" -> showInventoryDebug(mod, player);
            case "health" -> showHealthDebug(mod, player);
            case "food" -> showFoodDebug(mod, player);
            case "blocks" -> showBlocksDebug(mod, player);
            case "items" -> showItemsDebug(mod, player);
            case "time" -> showTimeDebug(mod);
            case "world" -> showWorldDebug(mod, player);
            case "errors" -> showErrorsDebug(mod);
            case "scripts" -> showScriptsDebug(mod);
            case "logs" -> showRecentLogsDebug(mod);
            case "performance" -> showPerformanceDebug(mod);
            case "player" -> showPlayerUtilsDebug(mod, player);
            case "help" -> showHelpDebug(mod);
            default -> {
                mod.log("§eUnknown debug type: " + debugType);
                mod.log("§eAvailable types: hunger, position, inventory, health, food, blocks, items, time, world, errors, scripts, logs, performance, player, help");
            }
        }
    }
    
    private void showHungerDebug(AltoClef mod, PlayerEntity player) {
        int hunger = player.getHungerManager().getFoodLevel();
        float saturation = player.getHungerManager().getSaturationLevel();
        
        mod.log("§6=== HUNGER DEBUG ===");
        mod.log("§eCurrent Values:");
        mod.log("  §fHunger: §a" + hunger + "§f/20");
        mod.log("  §fSaturation: §a" + String.format("%.1f", saturation));
        mod.log("");
        mod.log("§eLua API Calls:");
        mod.log("  §bAltoClef.getHunger()§f - returns hunger level (0-20)");
        mod.log("  §bAltoClef.getSaturation()§f - returns saturation level");
        mod.log("  §bAltoClef.isHungry()§f - returns true if hunger < 20");
        mod.log("");
        mod.log("§eExample Usage:");
        mod.log("  §7if AltoClef.getHunger() < 10 then");
        mod.log("  §7  AltoClef.log('Low hunger!')");
        mod.log("  §7end");
    }
    
    private void showPositionDebug(AltoClef mod, PlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        
        mod.log("§6=== POSITION DEBUG ===");
        mod.log("§eCurrent Values:");
        mod.log("  §fX: §a" + pos.getX());
        mod.log("  §fY: §a" + pos.getY());
        mod.log("  §fZ: §a" + pos.getZ());
        mod.log("  §fDimension: §a" + player.getWorld().getRegistryKey().getValue());
        mod.log("");
        mod.log("§eLua API Calls:");
        mod.log("  §bAltoClef.getPlayerPos()§f - returns {x, y, z} table");
        mod.log("  §bAltoClef.getPlayerX()§f - returns X coordinate");
        mod.log("  §bAltoClef.getPlayerY()§f - returns Y coordinate");
        mod.log("  §bAltoClef.getPlayerZ()§f - returns Z coordinate");
        mod.log("  §bAltoClef.getDimension()§f - returns dimension name");
        mod.log("");
        mod.log("§eExample Usage:");
        mod.log("  §7local pos = AltoClef.getPlayerPos()");
        mod.log("  §7AltoClef.log('At: ' .. pos.x .. ', ' .. pos.y .. ', ' .. pos.z)");
    }
    
    private void showInventoryDebug(AltoClef mod, PlayerEntity player) {
        int totalItems = 0;
        int emptySlots = 0;
        
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isEmpty()) {
                emptySlots++;
            } else {
                totalItems++;
            }
        }
        
        mod.log("§6=== INVENTORY DEBUG ===");
        mod.log("§eCurrent Values:");
        mod.log("  §fTotal Items: §a" + totalItems);
        mod.log("  §fEmpty Slots: §a" + emptySlots);
        mod.log("  §fInventory Size: §a" + player.getInventory().size());
        mod.log("");
        mod.log("§eLua API Calls:");
        mod.log("  §bAltoClef.hasItem('item_name')§f - check if player has item");
        mod.log("  §bAltoClef.getItemCount('item_name')§f - get count of item");
        mod.log("  §bAltoClef.getInventorySlots()§f - get all inventory slots");
        mod.log("  §bAltoClef.getEmptySlots()§f - get number of empty slots");
        mod.log("");
        mod.log("§eExample Usage:");
        mod.log("  §7if AltoClef.hasItem('bread') then");
        mod.log("  §7  local count = AltoClef.getItemCount('bread')");
        mod.log("  §7  AltoClef.log('Have ' .. count .. ' bread')");
        mod.log("  §7end");
    }
    
    private void showHealthDebug(AltoClef mod, PlayerEntity player) {
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        
        mod.log("§6=== HEALTH DEBUG ===");
        mod.log("§eCurrent Values:");
        mod.log("  §fHealth: §a" + String.format("%.1f", health) + "§f/§a" + String.format("%.1f", maxHealth));
        mod.log("  §fHealth %: §a" + String.format("%.1f", (health/maxHealth)*100) + "%");
        mod.log("");
        mod.log("§eLua API Calls:");
        mod.log("  §bAltoClef.getHealth()§f - returns current health");
        mod.log("  §bAltoClef.getMaxHealth()§f - returns max health");
        mod.log("  §bAltoClef.getHealthPercent()§f - returns health percentage");
        mod.log("  §bAltoClef.isLowHealth()§f - returns true if health < 50%");
        mod.log("");
        mod.log("§eExample Usage:");
        mod.log("  §7if AltoClef.getHealthPercent() < 30 then");
        mod.log("  §7  AltoClef.log('Low health!')");
        mod.log("  §7  -- Find healing items");
        mod.log("  §7end");
    }
    
    private void showFoodDebug(AltoClef mod, PlayerEntity player) {
        boolean hasFood = mod.getFoodChain().hasFood();
        
        mod.log("§6=== FOOD DEBUG ===");
        mod.log("§eCurrent Values:");
        mod.log("  §fHas Food: §a" + hasFood);
        mod.log("  §fFood Chain Active: §a" + (mod.getFoodChain() != null));
        mod.log("");
        mod.log("§eLua API Calls:");
        mod.log("  §bAltoClef.hasFood()§f - check if player has any food");
        mod.log("  §bAltoClef.eatFood()§f - trigger eating food");
        mod.log("  §bAltoClef.needsFood()§f - check if should eat");
        mod.log("  §bAltoClef.runCommand('food 20')§f - get food to 20 hunger");
        mod.log("");
        mod.log("§eExample Usage:");
        mod.log("  §7if AltoClef.needsFood() and AltoClef.hasFood() then");
        mod.log("  §7  AltoClef.eatFood()");
        mod.log("  §7elseif AltoClef.needsFood() then");
        mod.log("  §7  AltoClef.runCommand('food 20')");
        mod.log("  §7end");
    }
    
    private void showBlocksDebug(AltoClef mod, PlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        
        mod.log("§6=== BLOCKS DEBUG ===");
        mod.log("§eCurrent Values:");
        mod.log("  §fBlock below: §a" + player.getWorld().getBlockState(pos.down()).getBlock().getName().getString());
        mod.log("  §fBlock at feet: §a" + player.getWorld().getBlockState(pos).getBlock().getName().getString());
        mod.log("");
        mod.log("§eLua API Calls:");
        mod.log("  §bAltoClef.getBlockAt(x, y, z)§f - get block at position");
        mod.log("  §bAltoClef.isBlockAt('block_name', x, y, z)§f - check specific block");
        mod.log("  §bAltoClef.findNearestBlock('block_name')§f - find nearest block");
        mod.log("  §bAltoClef.mineBlock('block_name')§f - mine specific block");
        mod.log("");
        mod.log("§eExample Usage:");
        mod.log("  §7local pos = AltoClef.findNearestBlock('iron_ore')");
        mod.log("  §7if pos then");
        mod.log("  §7  AltoClef.mineBlock('iron_ore')");
        mod.log("  §7end");
    }
    
    private void showItemsDebug(AltoClef mod, PlayerEntity player) {
        mod.log("§6=== ITEMS DEBUG ===");
        mod.log("§eCommon Item Names:");
        mod.log("  §fFood: §7bread, cooked_beef, apple, golden_apple");
        mod.log("  §fTools: §7wooden_pickaxe, stone_pickaxe, iron_pickaxe");
        mod.log("  §fBlocks: §7dirt, stone, cobblestone, wood");
        mod.log("  §fOres: §7iron_ore, gold_ore, diamond_ore, coal_ore");
        mod.log("");
        mod.log("§eLua API Calls:");
        mod.log("  §bAltoClef.craftItem('item_name', count)§f - craft items");
        mod.log("  §bAltoClef.collectItem('item_name', count)§f - collect items");
        mod.log("  §bAltoClef.dropItem('item_name', count)§f - drop items");
        mod.log("");
        mod.log("§eExample Usage:");
        mod.log("  §7AltoClef.collectItem('iron_ingot', 10)");
        mod.log("  §7AltoClef.craftItem('iron_pickaxe', 1)");
    }
    
    private void showTimeDebug(AltoClef mod) {
        long currentTime = System.currentTimeMillis();
        
        mod.log("§6=== TIME DEBUG ===");
        mod.log("§eCurrent Values:");
        mod.log("  §fSystem Time: §a" + currentTime);
        mod.log("  §fGame Time: §a" + (MinecraftClient.getInstance().world != null ? 
            MinecraftClient.getInstance().world.getTime() : "N/A"));
        mod.log("");
        mod.log("§eLua API Calls:");
        mod.log("  §bos.clock()§f - get current time in seconds");
        mod.log("  §bAltoClef.getGameTime()§f - get minecraft world time");
        mod.log("  §bAltoClef.wait(seconds)§f - wait for specified time");
        mod.log("");
        mod.log("§eExample Usage:");
        mod.log("  §7local startTime = os.clock()");
        mod.log("  §7-- do something");
        mod.log("  §7local elapsed = os.clock() - startTime");
        mod.log("  §7AltoClef.log('Took ' .. elapsed .. ' seconds')");
    }
    
    private void showWorldDebug(AltoClef mod, PlayerEntity player) {
        mod.log("§6=== WORLD DEBUG ===");
        mod.log("§eCurrent Values:");
        mod.log("  §fIn Game: §a" + (MinecraftClient.getInstance().world != null));
        mod.log("  §fSingleplayer: §a" + MinecraftClient.getInstance().isInSingleplayer());
        mod.log("  §fDifficulty: §a" + (MinecraftClient.getInstance().world != null ? 
            MinecraftClient.getInstance().world.getDifficulty() : "N/A"));
        mod.log("");
        mod.log("§eLua API Calls:");
        mod.log("  §bAltoClef.isInGame()§f - check if in game world");
        mod.log("  §bAltoClef.isSingleplayer()§f - check if singleplayer");
        mod.log("  §bAltoClef.getDifficulty()§f - get world difficulty");
        mod.log("");
        mod.log("§eExample Usage:");
        mod.log("  §7if not AltoClef.isInGame() then");
        mod.log("  §7  return -- exit script if not in game");
        mod.log("  §7end");
    }
    
    private void showErrorsDebug(AltoClef mod) {
        mod.log("§6=== SCRIPT ERRORS DEBUG ===");
        
        if (mod.getScriptEngine() == null) {
            mod.log("§cScript engine not available");
            return;
        }
        
        var errorHandler = mod.getScriptEngine().getErrorHandler();
        var recentErrors = errorHandler.getAllRecentErrors();
        
        mod.log("§eCurrent Error Status:");
        mod.log("  §fTotal Recent Errors: §a" + recentErrors.size());
        
        if (recentErrors.isEmpty()) {
            mod.log("  §a✓ No recent script errors!");
            mod.log("");
            mod.log("§eError Monitoring:");
            mod.log("  • Script errors are automatically logged with detailed info");
            mod.log("  • Error rate limiting prevents spam");
            mod.log("  • Scripts auto-disable after 10 errors");
            return;
        }
        
        mod.log("");
        mod.log("§eRecent Errors (last 5):");
        int displayed = 0;
        for (int i = recentErrors.size() - 1; i >= 0 && displayed < 5; i--) {
            var error = recentErrors.get(i);
            mod.log(String.format("  §c%d. [%s] %s in %s: §f%s", 
                displayed + 1, error.getFormattedTime(), error.getErrorType(), 
                error.getScriptName(), error.getMessage()));
            displayed++;
        }
        
        mod.log("");
        mod.log("§eError Management:");
        mod.log("  • Full error details are in the console logs");
        mod.log("  • Use @luadebug scripts to see per-script error counts");
        mod.log("  • Restart scripts to reset error counters");
    }
    
    private void showScriptsDebug(AltoClef mod) {
        mod.log("§6=== SCRIPTS DEBUG ===");
        
        if (mod.getScriptEngine() == null) {
            mod.log("§cScript engine not available");
            return;
        }
        
        var loadedScripts = mod.getScriptEngine().getLoadedScripts();
        var errorHandler = mod.getScriptEngine().getErrorHandler();
        
        mod.log("§eCurrent Script Status:");
        mod.log("  §fLoaded Scripts: §a" + loadedScripts.size());
        mod.log("  §fEngine Enabled: §a" + mod.getScriptEngine().isEnabled());
        mod.log("");
        
        if (loadedScripts.isEmpty()) {
            mod.log("  §eNo scripts currently loaded");
            mod.log("  Use @script load <filename> to load scripts");
            return;
        }
        
        mod.log("§eLoaded Scripts:");
        for (var entry : loadedScripts.entrySet()) {
            var script = entry.getValue();
            int errorCount = errorHandler.getErrorCount(script.getName());
            String status = script.isEnabled() ? "§aEnabled" : "§cDisabled";
            
            mod.log(String.format("  §f%s: %s §f(Errors: %d, Ticks: %d, Avg: %.1fms)",
                script.getName(), status, errorCount, script.getTickCount(), 
                script.getAverageExecutionTime()));
        }
        
        mod.log("");
        mod.log("§eScript Management:");
        mod.log("  • @script list - List all available scripts");
        mod.log("  • @script reload <name> - Reload a script");
        mod.log("  • @script enable/disable <name> - Enable/disable scripts");
    }
    
    private void showRecentLogsDebug(AltoClef mod) {
        mod.log("§6=== RECENT LOGS DEBUG ===");
        mod.log("§eLog Configuration:");
        mod.log("  • Enhanced error logging is §aENABLED");
        mod.log("  • Errors include line numbers, stack traces, and system state");
        mod.log("  • Performance warnings for scripts taking >50ms per tick");
        mod.log("  • Rate limiting prevents error spam");
        mod.log("");
        mod.log("§eLog Locations:");
        mod.log("  • In-game chat: Basic error summaries");
        mod.log("  • Console/Latest.log: Full detailed error information");
        mod.log("  • Look for lines starting with '🔥 LUA SCRIPT ERROR'");
        mod.log("");
        mod.log("§eDebugging Tips:");
        mod.log("  • Check console for full stack traces");
        mod.log("  • Script errors show function names and line numbers");
        mod.log("  • System state is logged with each error");
        mod.log("  • Use @luadebug errors for recent error summary");
    }
    
    private void showPerformanceDebug(AltoClef mod) {
        mod.log("§6=== PERFORMANCE DEBUG ===");
        
        if (mod.getScriptEngine() == null) {
            mod.log("§cScript engine not available");
            return;
        }
        
        var loadedScripts = mod.getScriptEngine().getLoadedScripts();
        
        // Calculate performance metrics
        long totalTicks = 0;
        long totalExecutionTime = 0;
        int enabledScripts = 0;
        
        for (var script : loadedScripts.values()) {
            if (script.isEnabled()) {
                enabledScripts++;
                totalTicks += script.getTickCount();
                totalExecutionTime += script.getTotalExecutionTime();
            }
        }
        
        double avgExecutionTime = totalTicks > 0 ? (double) totalExecutionTime / totalTicks : 0;
        
        mod.log("§ePerformance Metrics:");
        mod.log("  §fEnabled Scripts: §a" + enabledScripts);
        mod.log("  §fTotal Ticks: §a" + totalTicks);
        mod.log("  §fTotal Execution Time: §a" + totalExecutionTime + "ms");
        mod.log("  §fAverage Per Tick: §a" + String.format("%.2f", avgExecutionTime) + "ms");
        mod.log("");
        
        if (enabledScripts > 0) {
            mod.log("§eScript Performance (slowest first):");
            loadedScripts.values().stream()
                .filter(script -> script.isEnabled() && script.getTickCount() > 0)
                .sorted((a, b) -> Double.compare(b.getAverageExecutionTime(), a.getAverageExecutionTime()))
                .limit(5)
                .forEach(script -> {
                    String warning = script.getAverageExecutionTime() > 50 ? " §c⚠" : "";
                    mod.log(String.format("  §f%s: §e%.2fms§f per tick%s", 
                        script.getName(), script.getAverageExecutionTime(), warning));
                });
        }
        
        mod.log("");
        mod.log("§ePerformance Tips:");
        mod.log("  • Keep script ticks under 50ms");
        mod.log("  • Use timers to throttle expensive operations");
        mod.log("  • Avoid running heavy calculations every tick");
    }
    
    private void showPlayerUtilsDebug(AltoClef mod, PlayerEntity player) {
        mod.log("§6=== PLAYER UTILS DEBUG ===");
        mod.log("§ePlayer Control APIs:");
        mod.log("");
        mod.log("§e🦘 Jump Controls:");
        mod.log("  §bAltoClef.isJumping()§f - Check if player is jumping");
        mod.log("  §bAltoClef.jump()§f - Make the player jump");
        mod.log("");
        mod.log("§e🚀 Velocity Controls:");
        mod.log("  §bAltoClef.getVelocity()§f - Get current velocity {x, y, z}");
        mod.log("  §bAltoClef.setVelocity(x, y, z)§f - Set player velocity");
        mod.log("");
        mod.log("§e📦 Also available via Utils API:");
        mod.log("  §bUtils.Player.isJumping()§f, §bUtils.Player.jump()§f, etc.");
        mod.log("");
        
        try {
            boolean isJumping = !player.isOnGround() && player.getVelocity().y > 0;
            var velocity = player.getVelocity();
            
            mod.log("§eCurrent Player State:");
            mod.log("  §fIs Jumping: §a" + isJumping);
            mod.log("  §fOn Ground: §a" + player.isOnGround());
            mod.log("  §fVelocity X: §a" + String.format("%.3f", velocity.x));
            mod.log("  §fVelocity Y: §a" + String.format("%.3f", velocity.y));
            mod.log("  §fVelocity Z: §a" + String.format("%.3f", velocity.z));
            mod.log("  §fSpeed (horizontal): §a" + String.format("%.3f", Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z)));
        } catch (Exception e) {
            mod.log("  §cError getting player state: " + e.getMessage());
        }
        
        mod.log("");
        mod.log("§eExample Usage:");
        mod.log("  §7-- Auto-jump when moving");
        mod.log("  §7if not AltoClef.isJumping() then");
        mod.log("  §7  AltoClef.jump()");
        mod.log("  §7end");
        mod.log("");
        mod.log("  §7-- Speed boost");
        mod.log("  §7local vel = AltoClef.getVelocity()");
        mod.log("  §7AltoClef.setVelocity(vel.x * 1.5, vel.y, vel.z * 1.5)");
        mod.log("");
        mod.log("  §7-- Also available via Utils API:");
        mod.log("  §7Utils.Player.isJumping(), Utils.Player.jump(), etc.");
    }
    
    private void showHelpDebug(AltoClef mod) {
        mod.log("§6=== LUA DEBUG HELP ===");
        mod.log("§eAPI Debug Commands:");
        mod.log("  §b@luadebug hunger§f - Show hunger API info");
        mod.log("  §b@luadebug position§f - Show position API info");
        mod.log("  §b@luadebug inventory§f - Show inventory API info");
        mod.log("  §b@luadebug health§f - Show health API info");
        mod.log("  §b@luadebug food§f - Show food API info");
        mod.log("  §b@luadebug blocks§f - Show blocks API info");
        mod.log("  §b@luadebug items§f - Show items API info");
        mod.log("  §b@luadebug time§f - Show time API info");
        mod.log("  §b@luadebug world§f - Show world API info");
        mod.log("  §b@luadebug player§f - Show new player control APIs");
        mod.log("");
        mod.log("§eScript Debug Commands:");
        mod.log("  §b@luadebug errors§f - Show recent script errors");
        mod.log("  §b@luadebug scripts§f - Show loaded scripts status");
        mod.log("  §b@luadebug logs§f - Show logging configuration");
        mod.log("  §b@luadebug performance§f - Show script performance metrics");
        mod.log("");
        mod.log("§eQuick Commands:");
        mod.log("  §b@hunger§f - Shortcut for hunger debug");
        mod.log("  §b@pos§f - Shortcut for position debug");
        mod.log("  §b@inv§f - Shortcut for inventory debug");
    }
} 