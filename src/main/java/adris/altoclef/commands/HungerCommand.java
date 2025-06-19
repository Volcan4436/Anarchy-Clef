package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;

/**
 * Shortcut command for @luadebug hunger
 * 
 * @author Hearty
 */
public class HungerCommand extends Command {
    
    public HungerCommand() throws CommandException {
        super("hunger", "Show hunger API debug information (shortcut for @luadebug hunger)");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        // Show hunger debug information directly
        if (mod.getPlayer() == null) {
            mod.log("§cNot in game - cannot get debug values");
            finish();
            return;
        }
        
        showHungerDebug(mod);
        finish();
    }
    
    private void showHungerDebug(AltoClef mod) {
        int hunger = mod.getPlayer().getHungerManager().getFoodLevel();
        float saturation = mod.getPlayer().getHungerManager().getSaturationLevel();
        
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
} 