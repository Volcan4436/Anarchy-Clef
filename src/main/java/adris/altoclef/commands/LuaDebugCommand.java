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
            case "help" -> showHelpDebug(mod);
            default -> {
                mod.log("§eUnknown debug type: " + debugType);
                mod.log("§eAvailable types: hunger, position, inventory, health, food, blocks, items, time, world, help");
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
    
    private void showHelpDebug(AltoClef mod) {
        mod.log("§6=== LUA DEBUG HELP ===");
        mod.log("§eAvailable Debug Commands:");
        mod.log("  §b@luadebug hunger§f - Show hunger API info");
        mod.log("  §b@luadebug position§f - Show position API info");
        mod.log("  §b@luadebug inventory§f - Show inventory API info");
        mod.log("  §b@luadebug health§f - Show health API info");
        mod.log("  §b@luadebug food§f - Show food API info");
        mod.log("  §b@luadebug blocks§f - Show blocks API info");
        mod.log("  §b@luadebug items§f - Show items API info");
        mod.log("  §b@luadebug time§f - Show time API info");
        mod.log("  §b@luadebug world§f - Show world API info");
        mod.log("");
        mod.log("§eQuick Commands:");
        mod.log("  §b@hunger§f - Shortcut for hunger debug");
        mod.log("  §b@pos§f - Shortcut for position debug");
        mod.log("  §b@inv§f - Shortcut for inventory debug");
    }
} 