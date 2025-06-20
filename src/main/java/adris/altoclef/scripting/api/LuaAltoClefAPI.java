package adris.altoclef.scripting.api;

import adris.altoclef.AltoClef;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.ui.MessagePriority;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.Map;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main API interface between Lua scripts and AltoClef functionality
 * Provides safe access to bot functions like logging, player info, world access, etc.
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class LuaAltoClefAPI extends LuaTable {
    private final AltoClef mod;
    private final String scriptName;
    
    // Event handlers for chat and commands
    private LuaValue onChatHandler = LuaValue.NIL;
    private LuaValue onCommandHandler = LuaValue.NIL;
    private LuaValue onWhisperHandler = LuaValue.NIL;
    private LuaValue onUserEventHandler = LuaValue.NIL;
    
    // Registered commands from this script
    private final Map<String, LuaScriptCommand> registeredCommands = new ConcurrentHashMap<>();
    private final Map<String, String> commandDescriptions = new ConcurrentHashMap<>();
    
    public LuaAltoClefAPI(AltoClef mod) {
        this.mod = mod;
        this.scriptName = "unknown";
        initializeAPI();
    }
    
    public LuaAltoClefAPI(AltoClef mod, String scriptName) {
        this.mod = mod;
        this.scriptName = scriptName;
        initializeAPI();
    }
    
    /**
     * Initializes all API functions available to scripts
     */
    private void initializeAPI() {
        // Logging functions
        set("log", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue message) {
                mod.log("[Lua] " + message.tojstring());
                return LuaValue.NIL;
            }
        });
        
        set("logWarning", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue message) {
                mod.logWarning("[Lua] " + message.tojstring());
                return LuaValue.NIL;
            }
        });
        
        // Player information
        set("getPlayer", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return createPlayerTable();
                }
                return LuaValue.NIL;
            }
        });
        
        // World information
        set("getWorld", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getWorld() != null) {
                    return createWorldTable();
                }
                return LuaValue.NIL;
            }
        });
        
        // Dimension information
        set("getCurrentDimension", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(WorldHelper.getCurrentDimension().toString());
                } catch (Exception e) {
                    return LuaValue.valueOf("UNKNOWN");
                }
            }
        });
        
        // Basic bot status
        set("isInGame", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                return LuaValue.valueOf(AltoClef.inGame());
            }
        });
        
        // Direct player info methods for easier access
        set("getHunger", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return LuaValue.valueOf(mod.getPlayer().getHungerManager().getFoodLevel());
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("getSaturation", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return LuaValue.valueOf(mod.getPlayer().getHungerManager().getSaturationLevel());
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("isHungry", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return LuaValue.valueOf(mod.getPlayer().getHungerManager().getFoodLevel() < 20);
                }
                return LuaValue.FALSE;
            }
        });
        
        set("getHealth", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return LuaValue.valueOf(mod.getPlayer().getHealth());
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("getMaxHealth", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return LuaValue.valueOf(mod.getPlayer().getMaxHealth());
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("getHealthPercent", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    float health = mod.getPlayer().getHealth();
                    float maxHealth = mod.getPlayer().getMaxHealth();
                    return LuaValue.valueOf((health / maxHealth) * 100);
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("isLowHealth", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    float health = mod.getPlayer().getHealth();
                    float maxHealth = mod.getPlayer().getMaxHealth();
                    return LuaValue.valueOf((health / maxHealth) < 0.5);
                }
                return LuaValue.FALSE;
            }
        });
        
        set("getPlayerPos", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    LuaTable pos = new LuaTable();
                    pos.set("x", LuaValue.valueOf(mod.getPlayer().getX()));
                    pos.set("y", LuaValue.valueOf(mod.getPlayer().getY()));
                    pos.set("z", LuaValue.valueOf(mod.getPlayer().getZ()));
                    return pos;
                }
                return LuaValue.NIL;
            }
        });
        
        set("getPlayerX", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return LuaValue.valueOf(mod.getPlayer().getX());
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("getPlayerY", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return LuaValue.valueOf(mod.getPlayer().getY());
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("getPlayerZ", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() != null) {
                    return LuaValue.valueOf(mod.getPlayer().getZ());
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("getDimension", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(WorldHelper.getCurrentDimension().toString());
                } catch (Exception e) {
                    return LuaValue.valueOf("UNKNOWN");
                }
            }
        });
        
        set("hasFood", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                return LuaValue.valueOf(mod.getFoodChain().hasFood());
            }
        });
        
        set("needsFood", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                return LuaValue.valueOf(mod.getFoodChain().needsToEat());
            }
        });
        
        // Jump functionality - direct API access
        set("isJumping", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                // Check if player is currently jumping (not on ground and has positive Y velocity)
                return LuaValue.valueOf(!mod.getPlayer().isOnGround() && mod.getPlayer().getVelocity().y > 0);
            }
        });
        
        set("jump", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Use AltoClef's input controls to trigger a jump
                    mod.getInputControls().tryPress(baritone.api.utils.input.Input.JUMP);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.jump: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Velocity functionality - direct API access
        set("getVelocity", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.NIL;
                try {
                    Vec3d velocity = mod.getPlayer().getVelocity();
                    LuaTable result = new LuaTable();
                    result.set("x", LuaValue.valueOf(velocity.x));
                    result.set("y", LuaValue.valueOf(velocity.y));
                    result.set("z", LuaValue.valueOf(velocity.z));
                    return result;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getVelocity: " + e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });
        
        set("setVelocity", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Handle both AltoClef.setVelocity() and AltoClef:setVelocity() syntax
                    int offset = args.narg() == 4 ? 1 : 0;
                    if (args.narg() < 3 + offset) return LuaValue.FALSE;
                    
                    double x = args.arg(1 + offset).todouble();
                    double y = args.arg(2 + offset).todouble();
                    double z = args.arg(3 + offset).todouble();
                    
                    mod.getPlayer().setVelocity(x, y, z);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.setVelocity: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== INPUT CONTROLS =====
        
        // Attack/punch (left click)
        set("attack", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    mod.getInputControls().tryPress(baritone.api.utils.input.Input.CLICK_LEFT);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.attack: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Use/interact (right click)
        set("use", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    mod.getInputControls().tryPress(baritone.api.utils.input.Input.CLICK_RIGHT);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.use: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Sneak control
        set("sneak", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Handle both AltoClef.sneak() and AltoClef.sneak(true/false)
                    int offset = args.narg() >= 2 ? 1 : 0;
                    
                    if (args.narg() == 1 + offset) {
                        // Toggle sneak or set specific state
                        boolean state = args.arg(1 + offset).toboolean();
                        if (state) {
                            mod.getInputControls().hold(baritone.api.utils.input.Input.SNEAK);
                        } else {
                            mod.getInputControls().release(baritone.api.utils.input.Input.SNEAK);
                        }
                    } else {
                        // Just trigger a sneak press
                        mod.getInputControls().tryPress(baritone.api.utils.input.Input.SNEAK);
                    }
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.sneak: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Sprint control
        set("sprint", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Handle both AltoClef.sprint() and AltoClef.sprint(true/false)
                    int offset = args.narg() >= 2 ? 1 : 0;
                    
                    if (args.narg() == 1 + offset) {
                        // Toggle sprint or set specific state
                        boolean state = args.arg(1 + offset).toboolean();
                        if (state) {
                            mod.getInputControls().hold(baritone.api.utils.input.Input.SPRINT);
                        } else {
                            mod.getInputControls().release(baritone.api.utils.input.Input.SPRINT);
                        }
                    } else {
                        // Just trigger a sprint press
                        mod.getInputControls().tryPress(baritone.api.utils.input.Input.SPRINT);
                    }
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.sprint: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Check if key is held down
        set("isHoldingKey", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue key) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    String keyName = key.tojstring().toLowerCase();
                    baritone.api.utils.input.Input input = switch (keyName) {
                        case "sneak" -> baritone.api.utils.input.Input.SNEAK;
                        case "sprint" -> baritone.api.utils.input.Input.SPRINT;
                        case "jump" -> baritone.api.utils.input.Input.JUMP;
                        case "attack", "left_click" -> baritone.api.utils.input.Input.CLICK_LEFT;
                        case "use", "right_click" -> baritone.api.utils.input.Input.CLICK_RIGHT;
                        case "forward", "w" -> baritone.api.utils.input.Input.MOVE_FORWARD;
                        case "back", "s" -> baritone.api.utils.input.Input.MOVE_BACK;
                        case "left", "a" -> baritone.api.utils.input.Input.MOVE_LEFT;
                        case "right", "d" -> baritone.api.utils.input.Input.MOVE_RIGHT;
                        default -> null;
                    };
                    
                    if (input != null) {
                        return LuaValue.valueOf(mod.getInputControls().isHeldDown(input));
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isHoldingKey: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Hotbar slot selection
        set("selectHotbarSlot", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue slot) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    int slotNum = slot.toint();
                    if (slotNum >= 1 && slotNum <= 9) {
                        mod.getPlayer().getInventory().selectedSlot = slotNum - 1; // Convert to 0-based
                        return LuaValue.TRUE;
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.selectHotbarSlot: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Get current hotbar slot
        set("getSelectedHotbarSlot", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getPlayer().getInventory().selectedSlot + 1); // Convert to 1-based
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getSelectedHotbarSlot: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        // ===== PLAYER STATUS & EFFECTS =====
        
        // Experience system
        set("getXP", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getPlayer().totalExperience);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getXP: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("getLevel", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getPlayer().experienceLevel);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getLevel: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("getXPProgress", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getPlayer().experienceProgress);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getXPProgress: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        // Status effects
        set("getActiveEffects", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return new LuaTable();
                try {
                    LuaTable effects = new LuaTable();
                    Collection<StatusEffectInstance> activeEffects = mod.getPlayer().getStatusEffects();
                    
                    int index = 1;
                    for (StatusEffectInstance effect : activeEffects) {
                        LuaTable effectInfo = new LuaTable();
                        effectInfo.set("name", LuaValue.valueOf(effect.getEffectType().getTranslationKey()));
                        effectInfo.set("amplifier", LuaValue.valueOf(effect.getAmplifier()));
                        effectInfo.set("duration", LuaValue.valueOf(effect.getDuration()));
                        effectInfo.set("infinite", LuaValue.valueOf(effect.isInfinite()));
                        effectInfo.set("visible", LuaValue.valueOf(!effect.isAmbient()));
                        
                        effects.set(index++, effectInfo);
                    }
                    
                    return effects;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getActiveEffects: " + e.getMessage());
                    return new LuaTable();
                }
            }
        });
        
        set("hasEffect", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue effectName) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    String effectStr = effectName.tojstring().toLowerCase();
                    
                    // Map common effect names to their registry IDs
                    String registryName = switch (effectStr) {
                        case "speed" -> "minecraft:speed";
                        case "slowness" -> "minecraft:slowness"; 
                        case "haste" -> "minecraft:haste";
                        case "mining_fatigue" -> "minecraft:mining_fatigue";
                        case "strength" -> "minecraft:strength";
                        case "instant_health" -> "minecraft:instant_health";
                        case "instant_damage" -> "minecraft:instant_damage";
                        case "jump_boost" -> "minecraft:jump_boost";
                        case "nausea" -> "minecraft:nausea";
                        case "regeneration" -> "minecraft:regeneration";
                        case "resistance" -> "minecraft:resistance";
                        case "fire_resistance" -> "minecraft:fire_resistance";
                        case "water_breathing" -> "minecraft:water_breathing";
                        case "invisibility" -> "minecraft:invisibility";
                        case "blindness" -> "minecraft:blindness";
                        case "night_vision" -> "minecraft:night_vision";
                        case "hunger" -> "minecraft:hunger";
                        case "weakness" -> "minecraft:weakness";
                        case "poison" -> "minecraft:poison";
                        case "wither" -> "minecraft:wither";
                        case "absorption" -> "minecraft:absorption";
                        case "saturation" -> "minecraft:saturation";
                        case "levitation" -> "minecraft:levitation";
                        case "luck" -> "minecraft:luck";
                        case "unluck" -> "minecraft:unluck";
                        default -> effectStr.contains(":") ? effectStr : "minecraft:" + effectStr;
                    };
                    
                    StatusEffect effect = Registries.STATUS_EFFECT.get(new Identifier(registryName));
                    if (effect != null) {
                        return LuaValue.valueOf(mod.getPlayer().hasStatusEffect(effect));
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.hasEffect: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("getEffect", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue effectName) {
                if (mod.getPlayer() == null) return LuaValue.NIL;
                try {
                    String effectStr = effectName.tojstring().toLowerCase();
                    
                    // Use the same mapping as hasEffect
                    String registryName = switch (effectStr) {
                        case "speed" -> "minecraft:speed";
                        case "slowness" -> "minecraft:slowness"; 
                        case "haste" -> "minecraft:haste";
                        case "mining_fatigue" -> "minecraft:mining_fatigue";
                        case "strength" -> "minecraft:strength";
                        case "instant_health" -> "minecraft:instant_health";
                        case "instant_damage" -> "minecraft:instant_damage";
                        case "jump_boost" -> "minecraft:jump_boost";
                        case "nausea" -> "minecraft:nausea";
                        case "regeneration" -> "minecraft:regeneration";
                        case "resistance" -> "minecraft:resistance";
                        case "fire_resistance" -> "minecraft:fire_resistance";
                        case "water_breathing" -> "minecraft:water_breathing";
                        case "invisibility" -> "minecraft:invisibility";
                        case "blindness" -> "minecraft:blindness";
                        case "night_vision" -> "minecraft:night_vision";
                        case "hunger" -> "minecraft:hunger";
                        case "weakness" -> "minecraft:weakness";
                        case "poison" -> "minecraft:poison";
                        case "wither" -> "minecraft:wither";
                        case "absorption" -> "minecraft:absorption";
                        case "saturation" -> "minecraft:saturation";
                        case "levitation" -> "minecraft:levitation";
                        case "luck" -> "minecraft:luck";
                        case "unluck" -> "minecraft:unluck";
                        default -> effectStr.contains(":") ? effectStr : "minecraft:" + effectStr;
                    };
                    
                    StatusEffect effect = Registries.STATUS_EFFECT.get(new Identifier(registryName));
                    if (effect != null) {
                        StatusEffectInstance instance = mod.getPlayer().getStatusEffect(effect);
                        if (instance != null) {
                            LuaTable effectInfo = new LuaTable();
                            effectInfo.set("name", LuaValue.valueOf(instance.getEffectType().getTranslationKey()));
                            effectInfo.set("amplifier", LuaValue.valueOf(instance.getAmplifier()));
                            effectInfo.set("duration", LuaValue.valueOf(instance.getDuration()));
                            effectInfo.set("infinite", LuaValue.valueOf(instance.isInfinite()));
                            effectInfo.set("visible", LuaValue.valueOf(!instance.isAmbient()));
                            return effectInfo;
                        }
                    }
                    return LuaValue.NIL;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getEffect: " + e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });
        
        // Gamemode detection
        set("getGameMode", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf("UNKNOWN");
                try {
                    if (mod.getPlayer().isCreative()) {
                        return LuaValue.valueOf("CREATIVE");
                    } else if (mod.getPlayer().isSpectator()) {
                        return LuaValue.valueOf("SPECTATOR");
                    } else {
                        // For survival vs adventure, we need to check the actual gamemode
                        // This is a simplified check - in practice might need network packet info
                        return LuaValue.valueOf("SURVIVAL");
                    }
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getGameMode: " + e.getMessage());
                    return LuaValue.valueOf("UNKNOWN");
                }
            }
        });
        
        set("isCreative", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getPlayer().isCreative());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isCreative: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isSpectator", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getPlayer().isSpectator());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isSpectator: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Player abilities
        set("canFly", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getPlayer().getAbilities().allowFlying);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.canFly: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isFlying", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getPlayer().getAbilities().flying);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isFlying: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("getWalkSpeed", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getPlayer().getAbilities().getWalkSpeed());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getWalkSpeed: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("getFlySpeed", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getPlayer().getAbilities().getFlySpeed());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getFlySpeed: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        // Enhanced player state
        set("isOnGround", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getPlayer().isOnGround());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isOnGround: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isInWater", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getPlayer().isTouchingWater());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isInWater: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isInLava", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getPlayer().isInLava());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isInLava: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isSneaking", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getPlayer().isSneaking());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isSneaking: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isSprinting", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getPlayer().isSprinting());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isSprinting: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("getFallDistance", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getPlayer().fallDistance);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getFallDistance: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("getArmor", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getPlayer().getArmor());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getArmor: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("getAbsorption", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getPlayer().getAbsorptionAmount());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getAbsorption: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        // Enhanced movement direction info
        set("getYaw", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getPlayer().getYaw());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getYaw: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("getPitch", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getPlayer().getPitch());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getPitch: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("setLook", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Handle both AltoClef.setLook() and AltoClef:setLook() syntax
                    int offset = args.narg() == 3 ? 1 : 0;
                    if (args.narg() < 2 + offset) return LuaValue.FALSE;
                    
                    float yaw = (float) args.arg(1 + offset).todouble();
                    float pitch = (float) args.arg(2 + offset).todouble();
                    
                    mod.getInputControls().forceLook(yaw, pitch);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.setLook: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== WORLD INFORMATION =====
        
        // Time and weather
        set("getWorldTime", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getWorld() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getWorld().getTime());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getWorldTime: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("getTimeOfDay", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getWorld() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getWorld().getTimeOfDay());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getTimeOfDay: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("isDay", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getWorld().isDay());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isDay: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isNight", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getWorld().isNight());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isNight: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isRaining", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getWorld().isRaining());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isRaining: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isThundering", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    return LuaValue.valueOf(mod.getWorld().isThundering());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isThundering: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("canSleep", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(WorldHelper.canSleep());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.canSleep: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Dimension information
        set("getCurrentDimension", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(WorldHelper.getCurrentDimension().toString());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getCurrentDimension: " + e.getMessage());
                    return LuaValue.valueOf("UNKNOWN");
                }
            }
        });
        
        set("isInOverworld", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(WorldHelper.getCurrentDimension() == adris.altoclef.util.Dimension.OVERWORLD);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isInOverworld: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isInNether", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(WorldHelper.getCurrentDimension() == adris.altoclef.util.Dimension.NETHER);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isInNether: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isInEnd", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(WorldHelper.getCurrentDimension() == adris.altoclef.util.Dimension.END);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isInEnd: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Biome information
        set("getCurrentBiome", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null || mod.getWorld() == null) return LuaValue.valueOf("UNKNOWN");
                try {
                    BlockPos playerPos = mod.getPlayer().getBlockPos();
                    var biome = mod.getWorld().getBiome(playerPos);
                    // Get the biome's registry key if available
                    var biomeKey = biome.getKey();
                    if (biomeKey.isPresent()) {
                        return LuaValue.valueOf(biomeKey.get().getValue().toString());
                    }
                    return LuaValue.valueOf("UNKNOWN");
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getCurrentBiome: " + e.getMessage());
                    return LuaValue.valueOf("UNKNOWN");
                }
            }
        });
        
        set("getBiomeAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.valueOf("UNKNOWN");
                try {
                    // Handle both AltoClef.getBiomeAt() and AltoClef:getBiomeAt() syntax
                    int offset = args.narg() == 4 ? 1 : 0;
                    if (args.narg() < 3 + offset) return LuaValue.valueOf("UNKNOWN");
                    
                    int x = args.arg(1 + offset).toint();
                    int y = args.arg(2 + offset).toint();
                    int z = args.arg(3 + offset).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    var biome = mod.getWorld().getBiome(pos);
                    var biomeKey = biome.getKey();
                    if (biomeKey.isPresent()) {
                        return LuaValue.valueOf(biomeKey.get().getValue().toString());
                    }
                    return LuaValue.valueOf("UNKNOWN");
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getBiomeAt: " + e.getMessage());
                    return LuaValue.valueOf("UNKNOWN");
                }
            }
        });
        
        set("isInOcean", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null || mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    BlockPos playerPos = mod.getPlayer().getBlockPos();
                    var biome = mod.getWorld().getBiome(playerPos);
                    return LuaValue.valueOf(WorldHelper.isOcean(biome));
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isInOcean: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== BLOCK INFORMATION =====
        
        // Block state queries
        set("getBlockAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.valueOf("air");
                try {
                    // Handle both AltoClef.getBlockAt() and AltoClef:getBlockAt() syntax
                    int offset = args.narg() == 4 ? 1 : 0;
                    if (args.narg() < 3 + offset) return LuaValue.valueOf("air");
                    
                    int x = args.arg(1 + offset).toint();
                    int y = args.arg(2 + offset).toint();
                    int z = args.arg(3 + offset).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    Block block = mod.getWorld().getBlockState(pos).getBlock();
                    return LuaValue.valueOf(Registries.BLOCK.getId(block).toString());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getBlockAt: " + e.getMessage());
                    return LuaValue.valueOf("air");
                }
            }
        });
        
        set("isBlockAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    // Handle both AltoClef.isBlockAt() and AltoClef:isBlockAt() syntax
                    int offset = args.narg() == 5 ? 1 : 0;
                    if (args.narg() < 4 + offset) return LuaValue.FALSE;
                    
                    int x = args.arg(1 + offset).toint();
                    int y = args.arg(2 + offset).toint();
                    int z = args.arg(3 + offset).toint();
                    String blockName = args.arg(4 + offset).tojstring();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    Block currentBlock = mod.getWorld().getBlockState(pos).getBlock();
                    
                    // Parse block name
                    String trimmedName = blockName.toLowerCase();
                    if (!trimmedName.contains(":")) {
                        trimmedName = "minecraft:" + trimmedName;
                    }
                    
                    Identifier blockId = new Identifier(trimmedName);
                    if (Registries.BLOCK.containsId(blockId)) {
                        Block targetBlock = Registries.BLOCK.get(blockId);
                        return LuaValue.valueOf(currentBlock == targetBlock);
                    }
                    
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isBlockAt: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isAirAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.TRUE;
                try {
                    // Handle both AltoClef.isAirAt() and AltoClef:isAirAt() syntax
                    int offset = args.narg() == 4 ? 1 : 0;
                    if (args.narg() < 3 + offset) return LuaValue.TRUE;
                    
                    int x = args.arg(1 + offset).toint();
                    int y = args.arg(2 + offset).toint();
                    int z = args.arg(3 + offset).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    return LuaValue.valueOf(WorldHelper.isAir(mod, pos));
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isAirAt: " + e.getMessage());
                    return LuaValue.TRUE;
                }
            }
        });
        
        set("isSolidAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    // Handle both AltoClef.isSolidAt() and AltoClef:isSolidAt() syntax
                    int offset = args.narg() == 4 ? 1 : 0;
                    if (args.narg() < 3 + offset) return LuaValue.FALSE;
                    
                    int x = args.arg(1 + offset).toint();
                    int y = args.arg(2 + offset).toint();
                    int z = args.arg(3 + offset).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    return LuaValue.valueOf(WorldHelper.isSolid(mod, pos));
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isSolidAt: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Light levels
        set("getLightLevelAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.valueOf(0);
                try {
                    // Handle both AltoClef.getLightLevelAt() and AltoClef:getLightLevelAt() syntax
                    int offset = args.narg() == 4 ? 1 : 0;
                    if (args.narg() < 3 + offset) return LuaValue.valueOf(0);
                    
                    int x = args.arg(1 + offset).toint();
                    int y = args.arg(2 + offset).toint();
                    int z = args.arg(3 + offset).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    int lightLevel = mod.getWorld().getLightLevel(pos);
                    return LuaValue.valueOf(lightLevel);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getLightLevelAt: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("getBlockLightAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.valueOf(0);
                try {
                    // Handle both AltoClef.getBlockLightAt() and AltoClef:getBlockLightAt() syntax
                    int offset = args.narg() == 4 ? 1 : 0;
                    if (args.narg() < 3 + offset) return LuaValue.valueOf(0);
                    
                    int x = args.arg(1 + offset).toint();
                    int y = args.arg(2 + offset).toint();
                    int z = args.arg(3 + offset).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    int blockLight = mod.getWorld().getLightLevel(net.minecraft.world.LightType.BLOCK, pos);
                    return LuaValue.valueOf(blockLight);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getBlockLightAt: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("getSkyLightAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.valueOf(0);
                try {
                    // Handle both AltoClef.getSkyLightAt() and AltoClef:getSkyLightAt() syntax
                    int offset = args.narg() == 4 ? 1 : 0;
                    if (args.narg() < 3 + offset) return LuaValue.valueOf(0);
                    
                    int x = args.arg(1 + offset).toint();
                    int y = args.arg(2 + offset).toint();
                    int z = args.arg(3 + offset).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    int skyLight = mod.getWorld().getLightLevel(net.minecraft.world.LightType.SKY, pos);
                    return LuaValue.valueOf(skyLight);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getSkyLightAt: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        // Block properties
        set("getBlockHardnessAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.valueOf(-1);
                try {
                    // Handle both AltoClef.getBlockHardnessAt() and AltoClef:getBlockHardnessAt() syntax
                    int offset = args.narg() == 4 ? 1 : 0;
                    if (args.narg() < 3 + offset) return LuaValue.valueOf(-1);
                    
                    int x = args.arg(1 + offset).toint();
                    int y = args.arg(2 + offset).toint();
                    int z = args.arg(3 + offset).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = mod.getWorld().getBlockState(pos);
                    float hardness = state.getHardness(mod.getWorld(), pos);
                    return LuaValue.valueOf(hardness);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getBlockHardnessAt: " + e.getMessage());
                    return LuaValue.valueOf(-1);
                }
            }
        });
        
        // Special block detection
        set("isChestAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    // Handle both AltoClef.isChestAt() and AltoClef:isChestAt() syntax
                    int offset = args.narg() == 4 ? 1 : 0;
                    if (args.narg() < 3 + offset) return LuaValue.FALSE;
                    
                    int x = args.arg(1 + offset).toint();
                    int y = args.arg(2 + offset).toint();
                    int z = args.arg(3 + offset).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    return LuaValue.valueOf(WorldHelper.isChest(mod, pos));
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isChestAt: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isInteractableAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getWorld() == null) return LuaValue.FALSE;
                try {
                    // Handle both AltoClef.isInteractableAt() and AltoClef:isInteractableAt() syntax
                    int offset = args.narg() == 4 ? 1 : 0;
                    if (args.narg() < 3 + offset) return LuaValue.FALSE;
                    
                    int x = args.arg(1 + offset).toint();
                    int y = args.arg(2 + offset).toint();
                    int z = args.arg(3 + offset).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    return LuaValue.valueOf(WorldHelper.isInteractableBlock(mod, pos));
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isInteractableAt: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== WORLD INTERACTION =====
        
        // Position validation
        set("canReach", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both AltoClef.canReach() and AltoClef:canReach() syntax
                    int offset = args.narg() == 4 ? 1 : 0;
                    if (args.narg() < 3 + offset) return LuaValue.FALSE;
                    
                    int x = args.arg(1 + offset).toint();
                    int y = args.arg(2 + offset).toint();
                    int z = args.arg(3 + offset).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    return LuaValue.valueOf(WorldHelper.canReach(mod, pos));
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.canReach: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("canBreak", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both AltoClef.canBreak() and AltoClef:canBreak() syntax
                    int offset = args.narg() == 4 ? 1 : 0;
                    if (args.narg() < 3 + offset) return LuaValue.FALSE;
                    
                    int x = args.arg(1 + offset).toint();
                    int y = args.arg(2 + offset).toint();
                    int z = args.arg(3 + offset).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    return LuaValue.valueOf(WorldHelper.canBreak(mod, pos));
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.canBreak: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("canPlace", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both AltoClef.canPlace() and AltoClef:canPlace() syntax
                    int offset = args.narg() == 4 ? 1 : 0;
                    if (args.narg() < 3 + offset) return LuaValue.FALSE;
                    
                    int x = args.arg(1 + offset).toint();
                    int y = args.arg(2 + offset).toint();
                    int z = args.arg(3 + offset).toint();
                    
                    BlockPos pos = new BlockPos(x, y, z);
                    return LuaValue.valueOf(WorldHelper.canPlace(mod, pos));
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.canPlace: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Ground height calculation
        set("getGroundHeight", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both AltoClef.getGroundHeight() and AltoClef:getGroundHeight() syntax
                    int offset = args.narg() == 3 ? 1 : 0;
                    if (args.narg() < 2 + offset) return LuaValue.valueOf(-1);
                    
                    int x = args.arg(1 + offset).toint();
                    int z = args.arg(2 + offset).toint();
                    
                    return LuaValue.valueOf(WorldHelper.getGroundHeight(mod, x, z));
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getGroundHeight: " + e.getMessage());
                    return LuaValue.valueOf(-1);
                }
            }
        });
        
        // ===== INVENTORY MANAGEMENT =====
        
        // Item counting and detection
        set("getItemCount", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(mod.getItemStorage().getItemCount(item));
                    }
                    return LuaValue.valueOf(0);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getItemCount: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("getItemCountInventory", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(mod.getItemStorage().getItemCountInventoryOnly(item));
                    }
                    return LuaValue.valueOf(0);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getItemCountInventory: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("hasItem", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(mod.getItemStorage().hasItem(item));
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.hasItem: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("hasItemInventory", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(mod.getItemStorage().hasItemInventoryOnly(item));
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.hasItemInventory: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Equipment management
        set("isEquipped", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.isEquipped(mod, item));
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isEquipped: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("equipItem", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(mod.getSlotHandler().forceEquipItem(item));
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.equipItem: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("equipItemToOffhand", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        mod.getSlotHandler().forceEquipItemToOffhand(item);
                        return LuaValue.TRUE;
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.equipItemToOffhand: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isArmorEquipped", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.isArmorEquipped(mod, item));
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isArmorEquipped: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Inventory state
        set("hasEmptySlot", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(mod.getItemStorage().hasEmptyInventorySlot());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.hasEmptySlot: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("getInventoryItemCount", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.valueOf(0);
                try {
                    return LuaValue.valueOf(mod.getPlayer().getInventory().main.size());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getInventoryItemCount: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("getFoodCount", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.calculateInventoryFoodScore(mod));
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getFoodCount: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("getBuildingMaterialCount", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.getBuildingMaterialCount(mod));
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getBuildingMaterialCount: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        // Container and screen detection
        set("isInventoryOpen", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.isPlayerInventoryOpen());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isInventoryOpen: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isCraftingTableOpen", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.isBigCraftingOpen());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isCraftingTableOpen: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isFurnaceOpen", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.isFurnaceOpen());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isFurnaceOpen: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isSmokerOpen", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.isSmokerOpen());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isSmokerOpen: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("isBlastFurnaceOpen", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.isBlastFurnaceOpen());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isBlastFurnaceOpen: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("closeScreen", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    // Execute on main thread to avoid render thread issues
                    net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
                        try {
                            adris.altoclef.util.helpers.StorageHelper.closeScreen();
                        } catch (Exception e) {
                            mod.logWarning("Error closing screen on main thread: " + e.getMessage());
                        }
                    });
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.closeScreen: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Furnace operations
        set("getFurnaceFuel", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.getFurnaceFuel());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getFurnaceFuel: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("getFurnaceCookProgress", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.getFurnaceCookPercent());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getFurnaceCookProgress: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("getSmokerFuel", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.getSmokerFuel());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getSmokerFuel: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("getSmokerCookProgress", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.getSmokerCookPercent());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getSmokerCookProgress: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("getBlastFurnaceFuel", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.getBlastFurnaceFuel());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getBlastFurnaceFuel: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("getBlastFurnaceCookProgress", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(adris.altoclef.util.helpers.StorageHelper.getBlastFurnaceCookPercent());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getBlastFurnaceCookProgress: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        // Container information
        set("getContainerItemCount", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(mod.getItemStorage().getItemCountContainer(item));
                    }
                    return LuaValue.valueOf(0);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getContainerItemCount: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        set("hasItemInContainer", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue itemName) {
                try {
                    Item item = parseItemName(itemName.tojstring());
                    if (item != null) {
                        return LuaValue.valueOf(mod.getItemStorage().hasItemContainer(item));
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.hasItemInContainer: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Utility functions  
        set("refreshInventory", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    mod.getSlotHandler().refreshInventory();
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.refreshInventory: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== ADVANCED CONTROL APIS =====
        
        // Drop item (Q key functionality)
        set("dropItem", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Execute on main thread to avoid issues
                    net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
                        try {
                            net.minecraft.client.MinecraftClient.getInstance().options.dropKey.setPressed(true);
                            net.minecraft.client.option.KeyBinding.onKeyPressed(net.minecraft.client.MinecraftClient.getInstance().options.dropKey.getDefaultKey());
                            net.minecraft.client.MinecraftClient.getInstance().options.dropKey.setPressed(false);
                        } catch (Exception e) {
                            mod.logWarning("Error executing drop on main thread: " + e.getMessage());
                        }
                    });
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.dropItem: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Open inventory (E key functionality)
        set("openInventory", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Execute on main thread to avoid issues
                    net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
                        try {
                            net.minecraft.client.MinecraftClient.getInstance().options.inventoryKey.setPressed(true);
                            net.minecraft.client.option.KeyBinding.onKeyPressed(net.minecraft.client.MinecraftClient.getInstance().options.inventoryKey.getDefaultKey());
                            net.minecraft.client.MinecraftClient.getInstance().options.inventoryKey.setPressed(false);
                        } catch (Exception e) {
                            mod.logWarning("Error executing inventory open on main thread: " + e.getMessage());
                        }
                    });
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.openInventory: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Open chat (T key functionality)
        set("openChat", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Execute on main thread to avoid issues
                    net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
                        try {
                            net.minecraft.client.MinecraftClient.getInstance().options.chatKey.setPressed(true);
                            net.minecraft.client.option.KeyBinding.onKeyPressed(net.minecraft.client.MinecraftClient.getInstance().options.chatKey.getDefaultKey());
                            net.minecraft.client.MinecraftClient.getInstance().options.chatKey.setPressed(false);
                        } catch (Exception e) {
                            mod.logWarning("Error executing chat open on main thread: " + e.getMessage());
                        }
                    });
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.openChat: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Advanced look control
        set("lookAt", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Handle both AltoClef.lookAt(yaw, pitch) and AltoClef:lookAt(yaw, pitch) syntax
                    int offset = args.narg() == 3 ? 1 : 0;
                    if (args.narg() < 2 + offset) return LuaValue.FALSE;
                    
                    float yaw = (float) args.arg(1 + offset).todouble();
                    float pitch = (float) args.arg(2 + offset).todouble();
                    
                    mod.getInputControls().forceLook(yaw, pitch);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.lookAt: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Force camera/look direction instantly
        set("setLook", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Handle both AltoClef.setLook(yaw, pitch) and AltoClef:setLook(yaw, pitch) syntax
                    int offset = args.narg() == 3 ? 1 : 0;
                    if (args.narg() < 2 + offset) return LuaValue.FALSE;
                    
                    float yaw = (float) args.arg(1 + offset).todouble();
                    float pitch = (float) args.arg(2 + offset).todouble();
                    
                    mod.getInputControls().forceLook(yaw, pitch);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.setLook: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== PLAYER EXTRA CONTROLLER APIS =====
        
        // Check if breaking a block
        set("isBreakingBlock", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(mod.getControllerExtras().isBreakingBlock());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isBreakingBlock: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Get block breaking position
        set("getBreakingBlockPos", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    net.minecraft.util.math.BlockPos pos = mod.getControllerExtras().getBreakingBlockPos();
                    if (pos != null) {
                        org.luaj.vm2.LuaTable result = new org.luaj.vm2.LuaTable();
                        result.set("x", LuaValue.valueOf(pos.getX()));
                        result.set("y", LuaValue.valueOf(pos.getY()));
                        result.set("z", LuaValue.valueOf(pos.getZ()));
                        return result;
                    }
                    return LuaValue.NIL;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getBreakingBlockPos: " + e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });
        
        // Get block breaking progress (0.0 to 1.0)
        set("getBreakingProgress", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(mod.getControllerExtras().getBreakingBlockProgress());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getBreakingProgress: " + e.getMessage());
                    return LuaValue.valueOf(0.0);
                }
            }
        });
        
        // ===== ADVANCED KEY CONTROLS =====
        
        // Hold a key down
        set("holdKey", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue key) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    String keyName = key.tojstring().toLowerCase();
                    baritone.api.utils.input.Input input = switch (keyName) {
                        case "sneak" -> baritone.api.utils.input.Input.SNEAK;
                        case "sprint" -> baritone.api.utils.input.Input.SPRINT;
                        case "jump" -> baritone.api.utils.input.Input.JUMP;
                        case "attack", "left_click" -> baritone.api.utils.input.Input.CLICK_LEFT;
                        case "use", "right_click" -> baritone.api.utils.input.Input.CLICK_RIGHT;
                        case "forward", "w" -> baritone.api.utils.input.Input.MOVE_FORWARD;
                        case "back", "s" -> baritone.api.utils.input.Input.MOVE_BACK;
                        case "left", "a" -> baritone.api.utils.input.Input.MOVE_LEFT;
                        case "right", "d" -> baritone.api.utils.input.Input.MOVE_RIGHT;
                        default -> null;
                    };
                    
                    if (input != null) {
                        mod.getInputControls().hold(input);
                        return LuaValue.TRUE;
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.holdKey: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Release a held key
        set("releaseKey", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue key) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    String keyName = key.tojstring().toLowerCase();
                    baritone.api.utils.input.Input input = switch (keyName) {
                        case "sneak" -> baritone.api.utils.input.Input.SNEAK;
                        case "sprint" -> baritone.api.utils.input.Input.SPRINT;
                        case "jump" -> baritone.api.utils.input.Input.JUMP;
                        case "attack", "left_click" -> baritone.api.utils.input.Input.CLICK_LEFT;
                        case "use", "right_click" -> baritone.api.utils.input.Input.CLICK_RIGHT;
                        case "forward", "w" -> baritone.api.utils.input.Input.MOVE_FORWARD;
                        case "back", "s" -> baritone.api.utils.input.Input.MOVE_BACK;
                        case "left", "a" -> baritone.api.utils.input.Input.MOVE_LEFT;
                        case "right", "d" -> baritone.api.utils.input.Input.MOVE_RIGHT;
                        default -> null;
                    };
                    
                    if (input != null) {
                        mod.getInputControls().release(input);
                        return LuaValue.TRUE;
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.releaseKey: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Press a key once (single press)
        set("pressKey", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue key) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    String keyName = key.tojstring().toLowerCase();
                    baritone.api.utils.input.Input input = switch (keyName) {
                        case "sneak" -> baritone.api.utils.input.Input.SNEAK;
                        case "sprint" -> baritone.api.utils.input.Input.SPRINT;
                        case "jump" -> baritone.api.utils.input.Input.JUMP;
                        case "attack", "left_click" -> baritone.api.utils.input.Input.CLICK_LEFT;
                        case "use", "right_click" -> baritone.api.utils.input.Input.CLICK_RIGHT;
                        case "forward", "w" -> baritone.api.utils.input.Input.MOVE_FORWARD;
                        case "back", "s" -> baritone.api.utils.input.Input.MOVE_BACK;
                        case "left", "a" -> baritone.api.utils.input.Input.MOVE_LEFT;
                        case "right", "d" -> baritone.api.utils.input.Input.MOVE_RIGHT;
                        default -> null;
                    };
                    
                    if (input != null) {
                        mod.getInputControls().tryPress(input);
                        return LuaValue.TRUE;
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.pressKey: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== ADVANCED CHAT & COMMAND APIS =====
        
        // Send whisper message to specific player
        set("whisper", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue username, LuaValue message) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    String user = username.tojstring();
                    String msg = message.tojstring();
                    mod.getMessageSender().enqueueWhisper(user, msg, adris.altoclef.ui.MessagePriority.OPTIONAL);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.whisper: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Send priority message (for important notifications)
        set("chatPriority", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Handle both AltoClef.chatPriority(message, priority) and AltoClef:chatPriority(message, priority) syntax
                    int offset = args.narg() == 3 ? 1 : 0;
                    if (args.narg() < 2 + offset) return LuaValue.FALSE;
                    
                    String message = args.arg(1 + offset).tojstring();
                    String priorityStr = args.arg(2 + offset).tojstring().toUpperCase();
                    
                    adris.altoclef.ui.MessagePriority priority = switch (priorityStr) {
                        case "LOW" -> adris.altoclef.ui.MessagePriority.OPTIONAL;
                        case "NORMAL", "INFO" -> adris.altoclef.ui.MessagePriority.OPTIONAL;
                        case "IMPORTANT" -> adris.altoclef.ui.MessagePriority.TIMELY;
                        case "TIMELY" -> adris.altoclef.ui.MessagePriority.TIMELY;
                        case "WARNING" -> adris.altoclef.ui.MessagePriority.ASAP;
                        case "UNAUTHORIZED" -> adris.altoclef.ui.MessagePriority.UNAUTHORIZED;
                        default -> adris.altoclef.ui.MessagePriority.OPTIONAL;
                    };
                    
                    mod.getMessageSender().enqueueChat(message, priority);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.chatPriority: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Send whisper with priority
        set("whisperPriority", new org.luaj.vm2.lib.VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (mod.getPlayer() == null) return LuaValue.FALSE;
                try {
                    // Handle both AltoClef.whisperPriority(user, message, priority) and AltoClef:whisperPriority(user, message, priority) syntax
                    int offset = args.narg() == 4 ? 1 : 0;
                    if (args.narg() < 3 + offset) return LuaValue.FALSE;
                    
                    String username = args.arg(1 + offset).tojstring();
                    String message = args.arg(2 + offset).tojstring();
                    String priorityStr = args.arg(3 + offset).tojstring().toUpperCase();
                    
                    adris.altoclef.ui.MessagePriority priority = switch (priorityStr) {
                        case "LOW" -> adris.altoclef.ui.MessagePriority.OPTIONAL;
                        case "NORMAL", "INFO" -> adris.altoclef.ui.MessagePriority.OPTIONAL;
                        case "IMPORTANT" -> adris.altoclef.ui.MessagePriority.TIMELY;
                        case "TIMELY" -> adris.altoclef.ui.MessagePriority.TIMELY;
                        case "WARNING" -> adris.altoclef.ui.MessagePriority.ASAP;
                        case "UNAUTHORIZED" -> adris.altoclef.ui.MessagePriority.UNAUTHORIZED;
                        default -> adris.altoclef.ui.MessagePriority.OPTIONAL;
                    };
                    
                    mod.getMessageSender().enqueueWhisper(username, message, priority);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.whisperPriority: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== BUTLER SYSTEM INTEGRATION =====
        
        // Check if user is authorized for butler commands
        set("isUserAuthorized", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue username) {
                try {
                    String user = username.tojstring();
                    return LuaValue.valueOf(mod.getButler().isUserAuthorized(user));
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isUserAuthorized: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Get current butler user (if any)
        set("getCurrentUser", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    String currentUser = mod.getButler().getCurrentUser();
                    return currentUser != null ? LuaValue.valueOf(currentUser) : LuaValue.NIL;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getCurrentUser: " + e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });
        
        // Check if butler has a current user
        set("hasCurrentUser", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    return LuaValue.valueOf(mod.getButler().hasCurrentUser());
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.hasCurrentUser: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== COMMAND MANAGEMENT =====
        
        // Get list of registered commands
        set("getRegisteredCommands", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    org.luaj.vm2.LuaTable commandList = new org.luaj.vm2.LuaTable();
                    int index = 1;
                    
                    for (String commandName : registeredCommands.keySet()) {
                        org.luaj.vm2.LuaTable commandInfo = new org.luaj.vm2.LuaTable();
                        commandInfo.set("name", LuaValue.valueOf(commandName));
                        commandInfo.set("description", LuaValue.valueOf(commandDescriptions.get(commandName)));
                        commandList.set(index++, commandInfo);
                    }
                    
                    return commandList;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getRegisteredCommands: " + e.getMessage());
                    return new org.luaj.vm2.LuaTable();
                }
            }
        });
        
        // Remove a registered command
        set("removeCommand", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue commandName) {
                try {
                    String name = commandName.tojstring();
                    boolean existed = registeredCommands.containsKey(name);
                    registeredCommands.remove(name);
                    commandDescriptions.remove(name);
                    return LuaValue.valueOf(existed);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.removeCommand: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Check if command exists
        set("hasCommand", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue commandName) {
                try {
                    String name = commandName.tojstring();
                    return LuaValue.valueOf(registeredCommands.containsKey(name));
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.hasCommand: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // ===== ENTITY DETECTION & TRACKING =====
        
        // Get all nearby entities within a range
        set("getNearbyEntities", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both AltoClef.getNearbyEntities() and AltoClef:getNearbyEntities() syntax
                    int offset = args.narg() >= 2 && args.arg(1).istable() ? 1 : 0;
                    double range = args.narg() > offset ? args.arg(1 + offset).todouble() : 20.0;
                    
                    if (mod.getPlayer() == null) return new LuaTable();
                    
                    LuaTable entities = new LuaTable();
                    int index = 1;
                    
                    for (Entity entity : mod.getEntityTracker().getCloseEntities()) {
                        double distance = entity.distanceTo(mod.getPlayer());
                        if (distance <= range) {
                            LuaTable entityInfo = createEntityTable(entity, distance);
                            entities.set(index++, entityInfo);
                        }
                    }
                    
                    return entities;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getNearbyEntities: " + e.getMessage());
                    return new LuaTable();
                }
            }
        });
        
        // Get closest entity of any type
        set("getClosestEntity", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both AltoClef.getClosestEntity() and AltoClef:getClosestEntity() syntax
                    int offset = args.narg() >= 2 && args.arg(1).istable() ? 1 : 0;
                    
                    if (mod.getPlayer() == null) return LuaValue.NIL;
                    
                    // Get all close entities and find the closest one
                    java.util.List<Entity> closeEntities = mod.getEntityTracker().getCloseEntities();
                    if (closeEntities.isEmpty()) return LuaValue.NIL;
                    
                    Entity closest = null;
                    double minDistance = Double.MAX_VALUE;
                    
                    for (Entity entity : closeEntities) {
                        double distance = entity.distanceTo(mod.getPlayer());
                        if (distance < minDistance) {
                            minDistance = distance;
                            closest = entity;
                        }
                    }
                    
                    if (closest != null) {
                        return createEntityTable(closest, minDistance);
                    }
                    
                    return LuaValue.NIL;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getClosestEntity: " + e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });
        
        // Get entities of specific type
        set("getEntitiesOfType", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both AltoClef.getEntitiesOfType() and AltoClef:getEntitiesOfType() syntax
                    int offset = args.narg() >= 2 && args.arg(1).istable() ? 1 : 0;
                    if (args.narg() < 1 + offset) return new LuaTable();
                    
                    String entityType = args.arg(1 + offset).tojstring().toLowerCase();
                    
                    if (mod.getPlayer() == null) return new LuaTable();
                    
                    LuaTable entities = new LuaTable();
                    int index = 1;
                    
                    // Map common entity names to classes
                    Class<?> entityClass = mapEntityTypeToClass(entityType);
                    if (entityClass != null) {
                        @SuppressWarnings("unchecked")
                        java.util.List<Entity> trackedEntities = (java.util.List<Entity>) mod.getEntityTracker().getTrackedEntities((Class<Entity>) entityClass);
                        for (Entity entity : trackedEntities) {
                            double distance = entity.distanceTo(mod.getPlayer());
                            LuaTable entityInfo = createEntityTable(entity, distance);
                            entities.set(index++, entityInfo);
                        }
                    }
                    
                    return entities;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getEntitiesOfType: " + e.getMessage());
                    return new LuaTable();
                }
            }
        });
        
        // Get nearby players
        set("getPlayersNearby", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both AltoClef.getPlayersNearby() and AltoClef:getPlayersNearby() syntax
                    int offset = args.narg() >= 2 && args.arg(1).istable() ? 1 : 0;
                    double range = args.narg() > offset ? args.arg(1 + offset).todouble() : 50.0;
                    
                    if (mod.getPlayer() == null) return new LuaTable();
                    
                    LuaTable players = new LuaTable();
                    int index = 1;
                    
                    java.util.List<net.minecraft.entity.player.PlayerEntity> playerEntities = mod.getEntityTracker().getTrackedEntities(net.minecraft.entity.player.PlayerEntity.class);
                    for (net.minecraft.entity.player.PlayerEntity player : playerEntities) {
                        double distance = player.distanceTo(mod.getPlayer());
                        if (distance <= range) {
                            LuaTable playerInfo = createEntityTable(player, distance);
                            // Add player-specific info
                            playerInfo.set("username", LuaValue.valueOf(player.getName().getString()));
                            playerInfo.set("uuid", LuaValue.valueOf(player.getUuid().toString()));
                            players.set(index++, playerInfo);
                        }
                    }
                    
                    return players;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getPlayersNearby: " + e.getMessage());
                    return new LuaTable();
                }
            }
        });
        
        // Get hostile entities
        set("getHostileEntities", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                try {
                    if (mod.getPlayer() == null) return new LuaTable();
                    
                    LuaTable hostiles = new LuaTable();
                    int index = 1;
                    
                    java.util.List<Entity> hostileEntities = mod.getEntityTracker().getHostiles();
                    for (Entity entity : hostileEntities) {
                        double distance = entity.distanceTo(mod.getPlayer());
                        LuaTable entityInfo = createEntityTable(entity, distance);
                        entityInfo.set("isHostile", LuaValue.TRUE);
                        hostiles.set(index++, entityInfo);
                    }
                    
                    return hostiles;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getHostileEntities: " + e.getMessage());
                    return new LuaTable();
                }
            }
        });
        
        // Get entity health (if it's a living entity)
        set("getEntityHealth", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue entityId) {
                try {
                    // For simplicity, we'll search for entities by their string representation
                    // In a real implementation, we'd need a better entity ID system
                    String idString = entityId.tojstring();
                    
                    // Search through close entities to find matching one
                    for (Entity entity : mod.getEntityTracker().getCloseEntities()) {
                        if (entity.toString().contains(idString) || entity.getName().getString().equals(idString)) {
                            if (entity instanceof LivingEntity) {
                                LivingEntity livingEntity = (LivingEntity) entity;
                                return LuaValue.valueOf(livingEntity.getHealth());
                            }
                        }
                    }
                    
                    return LuaValue.valueOf(0);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getEntityHealth: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        // Get entity position
        set("getEntityPosition", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue entityId) {
                try {
                    String idString = entityId.tojstring();
                    
                    // Search through close entities to find matching one
                    for (Entity entity : mod.getEntityTracker().getCloseEntities()) {
                        if (entity.toString().contains(idString) || entity.getName().getString().equals(idString)) {
                            Vec3d pos = entity.getPos();
                            LuaTable position = new LuaTable();
                            position.set("x", LuaValue.valueOf(pos.x));
                            position.set("y", LuaValue.valueOf(pos.y));
                            position.set("z", LuaValue.valueOf(pos.z));
                            return position;
                        }
                    }
                    
                    return LuaValue.NIL;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getEntityPosition: " + e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });
        
        // Check if entity is in sight/line of sight
        set("isEntityInSight", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue entityId) {
                try {
                    if (mod.getPlayer() == null) return LuaValue.FALSE;
                    
                    String idString = entityId.tojstring();
                    
                    // Search through close entities to find matching one
                    for (Entity entity : mod.getEntityTracker().getCloseEntities()) {
                        if (entity.toString().contains(idString) || entity.getName().getString().equals(idString)) {
                            if (entity instanceof LivingEntity) {
                                LivingEntity livingEntity = (LivingEntity) entity;
                                return LuaValue.valueOf(livingEntity.canSee(mod.getPlayer()));
                            }
                            // For non-living entities, do a simple distance check
                            double distance = entity.distanceTo(mod.getPlayer());
                            return LuaValue.valueOf(distance <= 64); // Minecraft render distance
                        }
                    }
                    
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isEntityInSight: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Check if a specific player is loaded
        set("isPlayerLoaded", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue username) {
                try {
                    String name = username.tojstring();
                    return LuaValue.valueOf(mod.getEntityTracker().isPlayerLoaded(name));
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.isPlayerLoaded: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Get player entity by username
        set("getPlayerByName", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue username) {
                try {
                    String name = username.tojstring();
                    java.util.Optional<net.minecraft.entity.player.PlayerEntity> playerOpt = mod.getEntityTracker().getPlayerEntity(name);
                    
                    if (playerOpt.isPresent()) {
                        net.minecraft.entity.player.PlayerEntity player = playerOpt.get();
                        double distance = player.distanceTo(mod.getPlayer());
                        LuaTable playerInfo = createEntityTable(player, distance);
                        playerInfo.set("username", LuaValue.valueOf(player.getName().getString()));
                        playerInfo.set("uuid", LuaValue.valueOf(player.getUuid().toString()));
                        return playerInfo;
                    }
                    
                    return LuaValue.NIL;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getPlayerByName: " + e.getMessage());
                    return LuaValue.NIL;
                }
            }
        });
        
        // Get dropped items on ground
        set("getDroppedItems", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                try {
                    // Handle both AltoClef.getDroppedItems() and AltoClef:getDroppedItems() syntax
                    int offset = args.narg() >= 2 && args.arg(1).istable() ? 1 : 0;
                    double range = args.narg() > offset ? args.arg(1 + offset).todouble() : 20.0;
                    
                    if (mod.getPlayer() == null) return new LuaTable();
                    
                    LuaTable items = new LuaTable();
                    int index = 1;
                    
                    java.util.List<net.minecraft.entity.ItemEntity> droppedItems = mod.getEntityTracker().getDroppedItems();
                    for (net.minecraft.entity.ItemEntity itemEntity : droppedItems) {
                        double distance = itemEntity.distanceTo(mod.getPlayer());
                        if (distance <= range) {
                            LuaTable itemInfo = createEntityTable(itemEntity, distance);
                            itemInfo.set("itemName", LuaValue.valueOf(itemEntity.getStack().getItem().toString()));
                            itemInfo.set("count", LuaValue.valueOf(itemEntity.getStack().getCount()));
                            items.set(index++, itemInfo);
                        }
                    }
                    
                    return items;
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.getDroppedItems: " + e.getMessage());
                    return new LuaTable();
                }
            }
        });
        
        // Count entities of specific type
        set("countEntitiesOfType", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue entityType) {
                try {
                    String type = entityType.tojstring().toLowerCase();
                    Class<?> entityClass = mapEntityTypeToClass(type);
                    
                    if (entityClass != null) {
                        @SuppressWarnings("unchecked")
                        java.util.List<Entity> entities = (java.util.List<Entity>) mod.getEntityTracker().getTrackedEntities((Class<Entity>) entityClass);
                        return LuaValue.valueOf(entities.size());
                    }
                    
                    return LuaValue.valueOf(0);
                } catch (Exception e) {
                    mod.logWarning("Error in AltoClef.countEntitiesOfType: " + e.getMessage());
                    return LuaValue.valueOf(0);
                }
            }
        });
        
        // ===== CHAT EVENT ENHANCEMENTS =====
        
        // Set whisper event handler (separate from general chat)
        set("onwhisper", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue handler) {
                if (handler.isfunction()) {
                    onWhisperHandler = handler;
                    mod.log("Whisper handler registered for script: " + scriptName);
                    return LuaValue.TRUE;
                } else {
                    onWhisperHandler = LuaValue.NIL;
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Set user join/leave event handler
        set("onuserevent", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue handler) {
                if (handler.isfunction()) {
                    onUserEventHandler = handler;
                    mod.log("User event handler registered for script: " + scriptName);
                    return LuaValue.TRUE;
                } else {
                    onUserEventHandler = LuaValue.NIL;
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("runCommand", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue command) {
                try {
                    String cmd = command.tojstring();
                    if (!cmd.startsWith(mod.getModSettings().getCommandPrefix())) {
                        cmd = mod.getModSettings().getCommandPrefix() + cmd;
                    }
                    AltoClef.getCommandExecutor().execute(cmd);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Script command execution failed: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        set("getGameTime", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getWorld() != null) {
                    return LuaValue.valueOf(mod.getWorld().getTime());
                }
                return LuaValue.valueOf(0);
            }
        });
        
        set("isSingleplayer", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                return LuaValue.valueOf(MinecraftClient.getInstance().isInSingleplayer());
            }
        });
        
        set("getDifficulty", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                if (mod.getWorld() != null) {
                    return LuaValue.valueOf(mod.getWorld().getDifficulty().toString());
                }
                return LuaValue.valueOf("UNKNOWN");
            }
        });
        
        // Item storage access (basic)
        set("getItemStorage", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue modName, LuaValue self) {
                return createItemStorageTable();
            }
        });
        
        // Command execution (limited)
        set("executeCommand", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue command) {
                try {
                    String cmd = command.tojstring();
                    if (!cmd.startsWith(mod.getModSettings().getCommandPrefix())) {
                        cmd = mod.getModSettings().getCommandPrefix() + cmd;
                    }
                    AltoClef.getCommandExecutor().execute(cmd);
                    return LuaValue.TRUE;
                } catch (Exception e) {
                    mod.logWarning("Script command execution failed: " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Task System API - Phase 2
        set("TaskSystem", new LuaTaskSystemAPI(mod));
        
        // Chat and Command System APIs
        initializeChatAndCommandAPIs();
        
        // Note: Utils API is now created per-script in LuaScriptEngine 
        // to support persistence with script-specific context
        
        // TODO: Add more APIs in future phases
        // These will be implemented as we develop Phase 2
        /*
        set("getEntityTracker", new ZeroArgFunction() { ... });
        set("getBlockTracker", new ZeroArgFunction() { ... });
        set("getBehaviour", new ZeroArgFunction() { ... });
        */
    }
    
    /**
     * Creates a Lua table with player information
     */
    private LuaTable createPlayerTable() {
        LuaTable playerTable = new LuaTable();
        
        try {
            if (mod.getPlayer() != null) {
                playerTable.set("name", LuaValue.valueOf(mod.getPlayer().getName().getString()));
                playerTable.set("health", LuaValue.valueOf(mod.getPlayer().getHealth()));
                playerTable.set("maxHealth", LuaValue.valueOf(mod.getPlayer().getMaxHealth()));
                playerTable.set("hunger", LuaValue.valueOf(mod.getPlayer().getHungerManager().getFoodLevel()));
                playerTable.set("saturation", LuaValue.valueOf(mod.getPlayer().getHungerManager().getSaturationLevel()));
                
                // Position
                LuaTable pos = new LuaTable();
                pos.set("x", LuaValue.valueOf(mod.getPlayer().getX()));
                pos.set("y", LuaValue.valueOf(mod.getPlayer().getY()));
                pos.set("z", LuaValue.valueOf(mod.getPlayer().getZ()));
                playerTable.set("position", pos);
                
                // Basic status
                playerTable.set("isOnGround", LuaValue.valueOf(mod.getPlayer().isOnGround()));
                playerTable.set("isInWater", LuaValue.valueOf(mod.getPlayer().isTouchingWater()));
                playerTable.set("isInLava", LuaValue.valueOf(mod.getPlayer().isInLava()));
                playerTable.set("isSneaking", LuaValue.valueOf(mod.getPlayer().isSneaking()));
                playerTable.set("isSprinting", LuaValue.valueOf(mod.getPlayer().isSprinting()));
            }
        } catch (Exception e) {
            mod.logWarning("Error creating player table for script: " + e.getMessage());
        }
        
        return playerTable;
    }
    
    /**
     * Creates a Lua table with world information
     */
    private LuaTable createWorldTable() {
        LuaTable worldTable = new LuaTable();
        
        try {
            if (mod.getWorld() != null) {
                worldTable.set("time", LuaValue.valueOf(mod.getWorld().getTimeOfDay()));
                worldTable.set("isDay", LuaValue.valueOf(mod.getWorld().isDay()));
                worldTable.set("isNight", LuaValue.valueOf(mod.getWorld().isNight()));
                worldTable.set("isRaining", LuaValue.valueOf(mod.getWorld().isRaining()));
                worldTable.set("isThundering", LuaValue.valueOf(mod.getWorld().isThundering()));
                
                // Difficulty
                worldTable.set("difficulty", LuaValue.valueOf(mod.getWorld().getDifficulty().toString()));
                
                // Player count (if available)
                if (mod.getWorld().getPlayers() != null) {
                    worldTable.set("playerCount", LuaValue.valueOf(mod.getWorld().getPlayers().size()));
                }
            }
        } catch (Exception e) {
            mod.logWarning("Error creating world table for script: " + e.getMessage());
        }
        
        return worldTable;
    }
    
    /**
     * Creates a Lua table with basic item storage information
     */
    private LuaTable createItemStorageTable() {
        LuaTable storageTable = new LuaTable();
        
        try {
            // Add basic item checking function
            storageTable.set("hasItem", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue itemName) {
                    try {
                        Item item = parseItemName(itemName.tojstring());
                        if (item != null) {
                            return LuaValue.valueOf(mod.getItemStorage().hasItem(item));
                        }
                        return LuaValue.FALSE;
                    } catch (Exception e) {
                        return LuaValue.FALSE;
                    }
                }
            });
            
            storageTable.set("getItemCount", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue itemName) {
                    try {
                        Item item = parseItemName(itemName.tojstring());
                        if (item != null) {
                            return LuaValue.valueOf(mod.getItemStorage().getItemCount(item));
                        }
                        return LuaValue.valueOf(0);
                    } catch (Exception e) {
                        return LuaValue.valueOf(0);
                    }
                }
            });
            
            // TODO: Add more storage functions in Phase 2
            // getInventorySlots(), hasItemInHotbar(), etc.
            
        } catch (Exception e) {
            mod.logWarning("Error creating item storage table for script: " + e.getMessage());
        }
        
        return storageTable;
    }
    
    /**
     * Helper function to convert item name strings to Item objects
     */
    private Item parseItemName(String itemName) {
        try {
            // Trim and format the item name
            String trimmedName = ItemHelper.trimItemName(itemName);
            
            // Handle common cases where users might not include namespace
            if (!trimmedName.contains(":")) {
                trimmedName = "minecraft:" + trimmedName;
            }
            
            Identifier identifier = new Identifier(trimmedName);
            if (Registries.ITEM.containsId(identifier)) {
                return Registries.ITEM.get(identifier);
            }
            
            mod.logWarning("Script tried to access unknown item: " + itemName);
            return null;
        } catch (Exception e) {
            mod.logWarning("Script item parsing error for '" + itemName + "': " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Creates a Lua table with entity information
     */
    private LuaTable createEntityTable(Entity entity, double distance) {
        LuaTable entityTable = new LuaTable();
        
        try {
            // Basic entity information
            entityTable.set("name", LuaValue.valueOf(entity.getName().getString()));
            entityTable.set("type", LuaValue.valueOf(entity.getType().toString()));
            entityTable.set("distance", LuaValue.valueOf(distance));
            entityTable.set("id", LuaValue.valueOf(entity.getId()));
            
            // Position
            Vec3d pos = entity.getPos();
            LuaTable position = new LuaTable();
            position.set("x", LuaValue.valueOf(pos.x));
            position.set("y", LuaValue.valueOf(pos.y));
            position.set("z", LuaValue.valueOf(pos.z));
            entityTable.set("position", position);
            
            // Velocity
            Vec3d velocity = entity.getVelocity();
            LuaTable velocityTable = new LuaTable();
            velocityTable.set("x", LuaValue.valueOf(velocity.x));
            velocityTable.set("y", LuaValue.valueOf(velocity.y));
            velocityTable.set("z", LuaValue.valueOf(velocity.z));
            entityTable.set("velocity", velocityTable);
            
            // Entity state
            entityTable.set("isAlive", LuaValue.valueOf(entity.isAlive()));
            entityTable.set("isOnGround", LuaValue.valueOf(entity.isOnGround()));
            entityTable.set("isInWater", LuaValue.valueOf(entity.isTouchingWater()));
            entityTable.set("isInLava", LuaValue.valueOf(entity.isInLava()));
            
            // Living entity specific information
            if (entity instanceof LivingEntity livingEntity) {
                entityTable.set("health", LuaValue.valueOf(livingEntity.getHealth()));
                entityTable.set("maxHealth", LuaValue.valueOf(livingEntity.getMaxHealth()));
                entityTable.set("isLiving", LuaValue.TRUE);
                
                // Check if entity can see player
                if (mod.getPlayer() != null) {
                    entityTable.set("canSeePlayer", LuaValue.valueOf(livingEntity.canSee(mod.getPlayer())));
                }
            } else {
                entityTable.set("isLiving", LuaValue.FALSE);
            }
            
            // Player specific information
            if (entity instanceof PlayerEntity playerEntity) {
                entityTable.set("username", LuaValue.valueOf(playerEntity.getName().getString()));
                entityTable.set("uuid", LuaValue.valueOf(playerEntity.getUuid().toString()));
                entityTable.set("isPlayer", LuaValue.TRUE);
            } else {
                entityTable.set("isPlayer", LuaValue.FALSE);
            }
            
            // Item entity specific information
            if (entity instanceof ItemEntity itemEntity) {
                entityTable.set("itemName", LuaValue.valueOf(itemEntity.getStack().getItem().toString()));
                entityTable.set("count", LuaValue.valueOf(itemEntity.getStack().getCount()));
                entityTable.set("isItem", LuaValue.TRUE);
            } else {
                entityTable.set("isItem", LuaValue.FALSE);
            }
            
        } catch (Exception e) {
            mod.logWarning("Error creating entity table: " + e.getMessage());
        }
        
        return entityTable;
    }
    
    /**
     * Maps entity type strings to their corresponding classes
     */
    private Class<?> mapEntityTypeToClass(String entityType) {
        return switch (entityType.toLowerCase()) {
            // Players
            case "player", "players" -> PlayerEntity.class;
            
            // Hostile mobs
            case "zombie", "zombies" -> ZombieEntity.class;
            case "skeleton", "skeletons" -> SkeletonEntity.class;
            case "creeper", "creepers" -> CreeperEntity.class;
            case "spider", "spiders" -> SpiderEntity.class;
            case "enderman", "endermen" -> EndermanEntity.class;
            case "witch", "witches" -> WitchEntity.class;
            case "slime", "slimes" -> SlimeEntity.class;
            
            // Passive mobs
            case "cow", "cows" -> CowEntity.class;
            case "pig", "pigs" -> PigEntity.class;
            case "sheep" -> SheepEntity.class;
            case "chicken", "chickens" -> ChickenEntity.class;
            case "horse", "horses" -> HorseEntity.class;
            case "wolf", "wolves" -> WolfEntity.class;
            case "cat", "cats" -> CatEntity.class;
            case "villager", "villagers" -> VillagerEntity.class;
            
            // Items
            case "item", "items", "drop", "drops" -> ItemEntity.class;
            
            // Generic categories
            case "hostile", "hostiles", "monster", "monsters" -> HostileEntity.class;
            case "passive", "animal", "animals" -> AnimalEntity.class;
            case "mob", "mobs" -> MobEntity.class;
            case "living" -> LivingEntity.class;
            case "entity", "entities", "all" -> Entity.class;
            
            default -> null;
        };
    }
    
    /**
     * Initializes chat and command functionality
     */
    private void initializeChatAndCommandAPIs() {
        // Chat sending functionality
        set("chat", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue message) {
                try {
                    if (MinecraftClient.getInstance().player != null) {
                        String msg = message.tojstring();
                        MinecraftClient.getInstance().player.networkHandler.sendChatMessage(msg);
                        return LuaValue.TRUE;
                    }
                    return LuaValue.FALSE;
                } catch (Exception e) {
                    mod.logWarning("Error sending chat message from script '" + scriptName + "': " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Set chat event handler
        set("onchat", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue handler) {
                if (handler.isfunction()) {
                    onChatHandler = handler;
                    mod.log("Chat handler registered for script: " + scriptName);
                    return LuaValue.TRUE;
                } else {
                    onChatHandler = LuaValue.NIL;
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Set command event handler
        set("oncommand", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue handler) {
                if (handler.isfunction()) {
                    onCommandHandler = handler;
                    mod.log("Command handler registered for script: " + scriptName);
                    return LuaValue.TRUE;
                } else {
                    onCommandHandler = LuaValue.NIL;
                    return LuaValue.FALSE;
                }
            }
        });
        
        // Create custom command
        set("createcommand", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (args.narg() < 3) {
                    mod.logWarning("createcommand requires 3 arguments: name, description, handler");
                    return LuaValue.FALSE;
                }
                
                try {
                    String cmdName = args.arg(1).tojstring();
                    String cmdDescription = args.arg(2).tojstring();
                    LuaValue cmdHandler = args.arg(3);
                    
                    if (!cmdHandler.isfunction()) {
                        mod.logWarning("Command handler must be a function");
                        return LuaValue.FALSE;
                    }
                    
                    // Create and register the command
                    LuaScriptCommand luaCommand = new LuaScriptCommand(cmdName, cmdDescription, cmdHandler, scriptName);
                    AltoClef.getCommandExecutor().registerNewCommand(luaCommand);
                    registeredCommands.put(cmdName, luaCommand);
                    
                    mod.log("Registered command '@" + cmdName + "' from script: " + scriptName);
                    return LuaValue.TRUE;
                    
                } catch (Exception e) {
                    mod.logWarning("Error creating command in script '" + scriptName + "': " + e.getMessage());
                    return LuaValue.FALSE;
                }
            }
        });
    }
    
    /**
     * Custom command created by Lua scripts
     */
    private class LuaScriptCommand extends Command {
        private final LuaValue luaHandler;
        private final String scriptName;
        
        public LuaScriptCommand(String name, String description, LuaValue handler, String scriptName) {
            super(name, description); // No args for now, we'll parse them manually
            this.luaHandler = handler;
            this.scriptName = scriptName;
        }
        
        @Override
        protected void call(AltoClef mod, ArgParser parser) throws CommandException {
            try {
                if (!luaHandler.isnil()) {
                    // Create arguments table for Lua
                    LuaTable args = new LuaTable();
                    String[] argUnits = parser.getArgUnits();
                    if (argUnits != null && argUnits.length > 0) {
                        for (int i = 0; i < argUnits.length; i++) {
                            args.set(i + 1, LuaValue.valueOf(argUnits[i]));
                        }
                    }
                    
                    // Call the Lua handler with arguments
                    luaHandler.call(args);
                } else {
                    mod.log("Command handler is nil for script: " + scriptName);
                }
            } catch (Exception e) {
                throw new CommandException("Error executing Lua command '" + getName() + "': " + e.getMessage());
            }
            finish();
        }
    }
    
    /**
     * Handle chat events from the event bus
     */
    public void handleChatEvent(String message, String sender, String senderUUID, boolean isSelf) {
        if (!onChatHandler.isnil()) {
            try {
                // Create chat info table
                LuaTable chatInfo = new LuaTable();
                chatInfo.set("message", LuaValue.valueOf(message));
                chatInfo.set("sender", LuaValue.valueOf(sender));
                chatInfo.set("senderUUID", LuaValue.valueOf(senderUUID));
                chatInfo.set("isSelf", LuaValue.valueOf(isSelf));
                chatInfo.set("timestamp", LuaValue.valueOf(System.currentTimeMillis()));
                
                // Call the Lua handler
                onChatHandler.call(chatInfo);
            } catch (Exception e) {
                mod.logWarning("Error in Lua chat handler for script '" + scriptName + "': " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle command events
     */
    public void handleCommandEvent(String command, String args) {
        if (!onCommandHandler.isnil()) {
            try {
                // Create command info table
                LuaTable cmdInfo = new LuaTable();
                cmdInfo.set("command", LuaValue.valueOf(command));
                cmdInfo.set("args", LuaValue.valueOf(args != null ? args : ""));
                cmdInfo.set("timestamp", LuaValue.valueOf(System.currentTimeMillis()));
                
                // Call the Lua handler
                onCommandHandler.call(cmdInfo);
            } catch (Exception e) {
                mod.logWarning("Error in Lua command handler for script '" + scriptName + "': " + e.getMessage());
            }
        }
    }
    
    /**
     * Cleanup when script is unloaded
     */
    public void cleanup() {
        // Unregister any commands created by this script
        for (String commandName : registeredCommands.keySet()) {
            // Note: We'd need access to CommandExecutor to properly unregister
            // For now, we'll just clear our local tracking
        }
        registeredCommands.clear();
        onChatHandler = LuaValue.NIL;
        onCommandHandler = LuaValue.NIL;
    }
} 