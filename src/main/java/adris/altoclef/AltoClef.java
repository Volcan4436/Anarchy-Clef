package adris.altoclef;

import adris.altoclef.altomenu.command.ChatUtils;
import adris.altoclef.altomenu.config.Config;
import adris.altoclef.altomenu.config.configloader;
import adris.altoclef.butler.Butler;
import adris.altoclef.chains.*;
import adris.altoclef.altomenu.*;
import adris.altoclef.altomenu.UI.screens.clickgui.ClickGUI;
import adris.altoclef.altomenu.managers.ModuleManager;
import adris.altoclef.commandsystem.CommandExecutor;
import adris.altoclef.scripting.LuaScriptEngine;
import adris.altoclef.control.InputControls;
import adris.altoclef.control.PlayerExtraController;
import adris.altoclef.control.SlotHandler;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ClientRenderEvent;
import adris.altoclef.eventbus.events.ClientTickEvent;
import adris.altoclef.eventbus.events.SendChatEvent;
import adris.altoclef.eventbus.events.TitleScreenEntryEvent;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.trackers.*;
import adris.altoclef.trackers.storage.ContainerSubTracker;
import adris.altoclef.trackers.storage.ItemStorageTracker;
import adris.altoclef.ui.CommandStatusOverlay;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.ui.MessageSender;
import adris.altoclef.util.helpers.InputHelper;
import baritone.Baritone;
import baritone.altoclef.AltoClefSettings;
import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Central access point for AltoClef
 */
public class AltoClef implements ModInitializer {

    public static final AltoClef INSTANCE = new AltoClef();
    
    public AltoClef() {
        System.err.println("🏗️ AltoClef instance created: " + this.hashCode());
        System.err.println("🏗️ Stack trace:");
        Thread.dumpStack();
    }
    // Static access to altoclef
    private static final Queue<Consumer<AltoClef>> _postInitQueue = new ArrayDeque<>();

    // Central Managers
    private static CommandExecutor _commandExecutor;
    private TaskRunner _taskRunner;
    private TrackerManager _trackerManager;
    private BotBehaviour _botBehaviour;
    private PlayerExtraController _extraController;
    // Task chains
    private UserTaskChain _userTaskChain;
    private FoodChain _foodChain;
    private MobDefenseChain _mobDefenseChain;
    private MLGBucketFallChain _mlgBucketChain;
    // Trackers
    private ItemStorageTracker _storageTracker;
    private ContainerSubTracker _containerSubTracker;
    private EntityTracker _entityTracker;
    private BlockTracker _blockTracker;
    private SimpleChunkTracker _chunkTracker;
    private static final String name = "AltoClefCONFIG";
    public static final String commandPrefix = "$";
    private MiscBlockTracker _miscBlockTracker;
    // Renderers
    private CommandStatusOverlay _commandStatusOverlay;
    // Settings
    private adris.altoclef.Settings _settings;
    // Misc managers/input
    private MessageSender _messageSender;
    private InputControls _inputControls;
    private SlotHandler _slotHandler;
    public static Config selectedConfig;
    // Butler
    private Butler _butler;
    
    // Lua Scripting System  
    private volatile LuaScriptEngine _scriptEngine;
    private volatile boolean _wasInitialized = false;
    
    /**
     * Track when script engine is set to help debug null issues
     */
    public void setScriptEngine(LuaScriptEngine engine) {
        System.err.println("🔧 SCRIPT ENGINE FIELD CHANGE:");
        System.err.println("  From: " + (_scriptEngine != null ? "INITIALIZED" : "NULL"));
        System.err.println("  To: " + (engine != null ? "INITIALIZED" : "NULL"));
        System.err.println("  Instance: " + this.hashCode());
        System.err.println("  Stack trace:");
        Thread.dumpStack();
        this._scriptEngine = engine;
        if (engine != null) {
            _wasInitialized = true;
            System.err.println("🔧 Marking instance as initialized");
        }
    }
    
    protected static MinecraftClient mc = MinecraftClient.getInstance();


    // Are we in game (playing in a server/world)
    public static boolean inGame() {
        return MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().getNetworkHandler() != null;
    }

    /**
     * Executes commands (ex. `@get`/`@gamer`)
     */
    public static CommandExecutor getCommandExecutor() {
        return _commandExecutor;
    }

    @Override
    public void onInitialize() {
        loadDefaultConfig();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            loadConfig(); // This is your method
        });
        
        // Initialize Lua scripting system EARLY on the correct Fabric instance
        System.out.println("=== SCRIPT ENGINE INITIALIZATION DEBUG START ===");
        System.out.println("Attempting to initialize LuaScriptEngine on Fabric instance: " + this.hashCode());
        
        // Check if LuaJ is available on classpath
        try {
            Class.forName("org.luaj.vm2.Globals");
            System.out.println("✅ LuaJ dependency is available on classpath");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ CRITICAL: LuaJ dependency not found on classpath!");
            System.err.println("This indicates a build/dependency issue");
            setScriptEngine(null);
            System.out.println("=== SCRIPT ENGINE INITIALIZATION DEBUG END ===");
            return;
        }
        
        try {
            System.out.println("Creating new LuaScriptEngine instance...");
            LuaScriptEngine scriptEngine = new LuaScriptEngine(this);
            setScriptEngine(scriptEngine);
            
            // CRITICAL: Also set on static INSTANCE if this is a different instance
            if (this != INSTANCE) {
                System.out.println("🔄 Setting script engine on static INSTANCE as well");
                INSTANCE.setScriptEngine(scriptEngine);
            }
            
            System.out.println("✅ LuaScriptEngine created successfully!");
            System.out.println("✅ Script engine initialization COMPLETE");
        } catch (Exception e) {
            System.err.println("❌ SCRIPT ENGINE INITIALIZATION FAILED!");
            System.err.println("Exception: " + e.getClass().getSimpleName());
            System.err.println("Message: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            setScriptEngine(null);
            
            // Also clear on static INSTANCE
            if (this != INSTANCE) {
                INSTANCE.setScriptEngine(null);
            }
            
            System.err.println("Script engine set to NULL due to initialization failure");
        }
        
        System.out.println("Script engine final state: " + (_scriptEngine != null ? "INITIALIZED" : "NULL"));
        System.out.println("=== SCRIPT ENGINE INITIALIZATION DEBUG END ===");
        System.out.println();
        
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // As such, nothing will be loaded here but basic initialization.
        EventBus.subscribe(TitleScreenEntryEvent.class, evt -> onInitializeLoad());
    }
    public static String getName() {
        return name;
    }
    private void loadDefaultConfig() {
        configloader configLoader = new configloader();
        try {
            configloader.loadConfigs(); // Load available configs from file
            // Check if the default config already exists
            Config defaultConfig = configLoader.getConfigByName("default");
            if (defaultConfig == null) {
                // If not, create a new default config and save it
                Config newDefaultConfig = new Config("default", "Default Configuration");
                configloader.saveNewConfig("default");
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }
    private void loadConfig() {
        configloader configLoader = new configloader();
        try {
            configLoader.loadConfigs(); // Load available configs from file
            Config config = configLoader.getConfigByName("default");
            if (config != null) {
                configLoader.loadConfig(config);
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }
    public void onInitializeLoad() {
        // This code should be run after Minecraft loads everything else in.
        // This is the actual start point, controlled by a mixin.

        initializeBaritoneSettings();

        // Central Managers
        _commandExecutor = new CommandExecutor(this);
        _taskRunner = new TaskRunner(this);
        _trackerManager = new TrackerManager(this);
        _botBehaviour = new BotBehaviour(this);
        _extraController = new PlayerExtraController(this);

        // Task chains
        _userTaskChain = new UserTaskChain(_taskRunner);
        _mobDefenseChain = new MobDefenseChain(_taskRunner);
        new DeathMenuChain(_taskRunner);
        new PlayerInteractionFixChain(_taskRunner);
        _mlgBucketChain = new MLGBucketFallChain(_taskRunner);
        new WorldSurvivalChain(_taskRunner);
        _foodChain = new FoodChain(_taskRunner);

        // Trackers
        _storageTracker = new ItemStorageTracker(this, _trackerManager, container -> _containerSubTracker = container);
        _entityTracker = new EntityTracker(_trackerManager);
        _blockTracker = new BlockTracker(this, _trackerManager);
        _chunkTracker = new SimpleChunkTracker(this);
        _miscBlockTracker = new MiscBlockTracker(this);

        // Renderers
        _commandStatusOverlay = new CommandStatusOverlay();

        // Misc managers
        _messageSender = new MessageSender();
        _inputControls = new InputControls();
        _slotHandler = new SlotHandler(this);

        _butler = new Butler(this);

        // Script engine should already be initialized from onInitialize()
        if (_scriptEngine != null) {
            System.out.println("✅ Script engine available in onInitializeLoad: " + this.hashCode());
            log("Lua scripting system ready");
        } else {
            System.err.println("❌ Script engine not available in onInitializeLoad: " + this.hashCode());
            logWarning("Script engine not initialized - scripts will not work");
        }

        initializeCommands();

        // Load settings
        adris.altoclef.Settings.load(newSettings -> {
            _settings = newSettings;
            // Baritone's `acceptableThrowawayItems` should match our own.
            List<Item> baritoneCanPlace = Arrays.stream(_settings.getThrowawayItems(this, true))
                    .filter(item -> item != Items.SOUL_SAND && item != Items.MAGMA_BLOCK && item != Items.SAND && item
                            != Items.GRAVEL).toList();
            getClientBaritoneSettings().acceptableThrowawayItems.value.addAll(baritoneCanPlace);
            // If we should run an idle command...
            if ((!getUserTaskChain().isActive() || getUserTaskChain().isRunningIdleTask()) && getModSettings().shouldRunIdleCommandWhenNotActive()) {
                getUserTaskChain().signalNextTaskToBeIdleTask();
                getCommandExecutor().executeWithPrefix(getModSettings().getIdleCommand());
            }
            // Don't break blocks or place blocks where we are explicitly protected.
            getExtraBaritoneSettings().avoidBlockBreak(blockPos -> _settings.isPositionExplicitlyProtected(blockPos));
            getExtraBaritoneSettings().avoidBlockPlace(blockPos -> _settings.isPositionExplicitlyProtected(blockPos));
        });

        // Receive + cancel chat
        EventBus.subscribe(SendChatEvent.class, evt -> {
            String line = evt.message;
            if (getCommandExecutor().isClientCommand(line)) {
                evt.cancel();
                getCommandExecutor().execute(line);
            }
        });

        // Debug jank/hookup
        Debug.jankModInstance = this;

        // Tick with the client
        EventBus.subscribe(ClientTickEvent.class, evt -> onClientTick());
        // Render
        EventBus.subscribe(ClientRenderEvent.class, evt -> onClientRenderOverlay(evt.stack));

        // Playground
        Playground.IDLE_TEST_INIT_FUNCTION(this);

        // External mod initialization
        runEnqueuedPostInits();
    }

    // Client tick
    private void onClientTick() {
        runEnqueuedPostInits();

        _inputControls.onTickPre();

        // Cancel shortcut
        if (InputHelper.isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL) && InputHelper.isKeyPressed(GLFW.GLFW_KEY_K)) {
            _userTaskChain.cancel(this);
            if (_taskRunner.getCurrentTaskChain() != null) {
                _taskRunner.getCurrentTaskChain().stop(this);
            }
        }

        // TODO: should this go here?
        _storageTracker.setDirty();
        _containerSubTracker.onServerTick();
        _miscBlockTracker.tick();

        _trackerManager.tick();
        _blockTracker.preTickTask();
        _taskRunner.tick();
        _blockTracker.postTickTask();

        _butler.tick();
        _messageSender.tick();

        // Tick Lua scripts
        if (_scriptEngine != null && _scriptEngine.isEnabled()) {
            try {
                _scriptEngine.tickAllScripts();
            } catch (Exception e) {
                logWarning("Error ticking Lua scripts: " + e.getMessage());
                e.printStackTrace();
            }
        }

        _inputControls.onTickPost();
    }

    /// GETTERS AND SETTERS

    private void onClientRenderOverlay(MatrixStack matrixStack) {
        _commandStatusOverlay.render(this, matrixStack);
    }


    private void initializeBaritoneSettings() {
        getExtraBaritoneSettings().canWalkOnEndPortal(false);
        getClientBaritoneSettings().freeLook.value = false;
        getClientBaritoneSettings().overshootTraverse.value = false;
        getClientBaritoneSettings().allowOvershootDiagonalDescend.value = true;
        getClientBaritoneSettings().allowInventory.value = true;
        getClientBaritoneSettings().allowParkour.value = false;
        getClientBaritoneSettings().allowParkourAscend.value = false;
        getClientBaritoneSettings().allowParkourPlace.value = false;
        getClientBaritoneSettings().allowDiagonalDescend.value = false;
        getClientBaritoneSettings().allowDiagonalAscend.value = false;
        getClientBaritoneSettings().blocksToAvoid.value = List.of(Blocks.FLOWERING_AZALEA, Blocks.AZALEA,
                Blocks.POWDER_SNOW, Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM, Blocks.CAVE_VINES,
                Blocks.CAVE_VINES_PLANT, Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.SWEET_BERRY_BUSH,
                Blocks.WARPED_ROOTS, Blocks.VINE, Blocks.GRASS_BLOCK, Blocks.FERN, Blocks.TALL_GRASS, Blocks.LARGE_FERN,
                Blocks.SMALL_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD, Blocks.LARGE_AMETHYST_BUD,
                Blocks.AMETHYST_CLUSTER, Blocks.SCULK, Blocks.SCULK_VEIN, Blocks.SUNFLOWER, Blocks.LILAC,
                Blocks.ROSE_BUSH, Blocks.PEONY);
        // Let baritone move items to hotbar to use them
        // Reduces a bit of far rendering to save FPS
        getClientBaritoneSettings().fadePath.value = true;
        // Don't let baritone scan dropped items, we handle that ourselves.
        getClientBaritoneSettings().mineScanDroppedItems.value = false;
        // Don't let baritone wait for drops, we handle that ourselves.
        getClientBaritoneSettings().mineDropLoiterDurationMSThanksLouca.value = 0L;

        // Water bucket placement will be handled by us exclusively
        getExtraBaritoneSettings().configurePlaceBucketButDontFall(true);

        // For render smoothing
        getClientBaritoneSettings().randomLooking.value = 0.0;
        getClientBaritoneSettings().randomLooking113.value = 0.0;

        // Give baritone more time to calculate paths. Sometimes they can be really far away.
        // Was: 2000L
        getClientBaritoneSettings().failureTimeoutMS.reset();
        // Was: 5000L
        getClientBaritoneSettings().planAheadFailureTimeoutMS.reset();
        // Was 100
        getClientBaritoneSettings().movementTimeoutTicks.reset();
    }

    // List all command sources here.
    private void initializeCommands() {
        try {
            // This creates the commands. If you want any more commands feel free to initialize new command lists.
            new AltoClefCommands();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the highest priority task chain
     * (task chains run the task tree)
     */
    public TaskRunner getTaskRunner() {
        return _taskRunner;
    }

    /**
     * The user task chain (runs your command. Ex. Get Diamonds, Beat the Game)
     */
    public UserTaskChain getUserTaskChain() {
        return _userTaskChain;
    }

    /**
     * Controls bot behaviours, like whether to temporarily "protect" certain blocks or items
     */
    public BotBehaviour getBehaviour() {
        return _botBehaviour;
    }

    /**
     * Tracks items in your inventory and in storage containers.
     */
    public ItemStorageTracker getItemStorage() {
        return _storageTracker;
    }

    /**
     * Tracks loaded entities
     */
    public EntityTracker getEntityTracker() {
        return _entityTracker;
    }

    /**
     * Tracks blocks and their positions
     */
    public BlockTracker getBlockTracker() {
        return _blockTracker;
    }

    /**
     * Tracks of whether a chunk is loaded/visible or not
     */
    public SimpleChunkTracker getChunkTracker() {
        return _chunkTracker;
    }

    /**
     * Tracks random block things, like the last nether portal we used
     */
    public MiscBlockTracker getMiscBlockTracker() {
        return _miscBlockTracker;
    }

    /**
     * Baritone access (could just be static honestly)
     */
    public Baritone getClientBaritone() {
        if (getPlayer() == null) {
            return (Baritone) BaritoneAPI.getProvider().getPrimaryBaritone();
        }
        return (Baritone) BaritoneAPI.getProvider().getBaritoneForPlayer(getPlayer());
    }

    /**
     * Baritone settings access (could just be static honestly)
     */
    public Settings getClientBaritoneSettings() {
        return Baritone.settings();
    }

    /**
     * Baritone settings special to AltoClef (could just be static honestly)
     */
    public AltoClefSettings getExtraBaritoneSettings() {
        return AltoClefSettings.getInstance();
    }

    /**
     * AltoClef Settings
     */
    public adris.altoclef.Settings getModSettings() {
        return _settings;
    }

    /**
     * Butler controller. Keeps track of users and lets you receive user messages
     */
    public Butler getButler() {
        return _butler;
    }

    /**
     * Sends chat messages (avoids auto-kicking)
     */
    public MessageSender getMessageSender() {
        return _messageSender;
    }

    /**
     * Does Inventory/container slot actions
     */
    public SlotHandler getSlotHandler() {
        return _slotHandler;
    }

    /**
     * Minecraft player client access (could just be static honestly)
     */
    public ClientPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }

    /**
     * Minecraft world access (could just be static honestly)
     */
    public ClientWorld getWorld() {
        return MinecraftClient.getInstance().world;
    }

    /**
     * Minecraft client interaction controller access (could just be static honestly)
     */
    public ClientPlayerInteractionManager getController() {
        return MinecraftClient.getInstance().interactionManager;
    }

    /**
     * Extra controls not present in ClientPlayerInteractionManager. This REALLY should be made static or combined with something else.
     */
    public PlayerExtraController getControllerExtras() {
        return _extraController;
    }

    /**
     * Manual control over input actions (ex. jumping, attacking)
     */
    public InputControls getInputControls() {
        return _inputControls;
    }

    /**
     * Lua scripting engine for custom scripts
     */
    public LuaScriptEngine getScriptEngine() {
        if (_scriptEngine == null) {
            System.err.println("🚨 WARNING: getScriptEngine() called but _scriptEngine is NULL!");
            System.err.println("🚨 AltoClef instance: " + this.hashCode());
            System.err.println("🚨 INSTANCE reference: " + INSTANCE.hashCode());
            System.err.println("🚨 Are they the same? " + (this == INSTANCE));
            System.err.println("🚨 Was previously initialized? " + _wasInitialized);
            if (_wasInitialized) {
                System.err.println("🚨 CRITICAL: Field was initialized but is now NULL - this indicates DIRECT field access!");
                System.err.println("🚨 This means something set _scriptEngine = null directly, bypassing setScriptEngine()");
            }
            System.err.println("Stack trace of null access:");
            Thread.dumpStack();
        }
        return _scriptEngine;
    }

    /**
     * Run a user task
     */
    public void runUserTask(Task task) {
        runUserTask(task, () -> {
        });
    }

    /**
     * Run a user task
     */
    public void runUserTask(Task task, Runnable onFinish) {
        _userTaskChain.runTask(this, task, onFinish);
    }

    /**
     * Cancel currently running user task
     */
    public void cancelUserTask() {
        _userTaskChain.cancel(this);
    }

    /**
     * Takes control away to eat food
     */
    public FoodChain getFoodChain() {
        return _foodChain;
    }

    /**
     * Takes control away to defend against mobs
     */
    public MobDefenseChain getMobDefenseChain() {
        return _mobDefenseChain;
    }

    /**
     * Takes control away to perform bucket saves
     */
    public MLGBucketFallChain getMLGBucketChain() {
        return _mlgBucketChain;
    }

    public void log(String message) {
        log(message, MessagePriority.TIMELY);
    }

    /**
     * Logs to the console and also messages any player using the bot as a butler.
     */
    public void log(String message, MessagePriority priority) {
        Debug.logMessage(message);
        if (_butler != null) {
            _butler.onLog(message, priority);
        }
    }

    public void logWarning(String message) {
        logWarning(message, MessagePriority.TIMELY);
    }

    /**
     * Logs a warning to the console and also alerts any player using the bot as a butler.
     */
    public void logWarning(String message, MessagePriority priority) {
        Debug.logWarning(message);
        if (_butler != null) {
            _butler.onLogWarning(message, priority);
        }
    }

    private void runEnqueuedPostInits() {
        synchronized (_postInitQueue) {
            while (!_postInitQueue.isEmpty()) {
                _postInitQueue.poll().accept(this);
            }
        }
    }

    // Cheat Menu Stuff

    public void onKeypress(int key, int action) {
        if (action == GLFW.GLFW_PRESS) {
            for (Mod module : ModuleManager.INSTANCE.getModules()) {
                if (key == module.getKey()) module.toggle();
            }
            if (key == GLFW.GLFW_KEY_INSERT) mc.setScreenAndRender(ClickGUI.INSTANCE);
        }
    }

    public void openClickGUI() {
        assert mc.currentScreen != null;
        mc.currentScreen.close();
        mc.setScreen(ClickGUI.INSTANCE);
    }

    public void onTick() {
        if (mc.player != null) {
            for (Mod module : ModuleManager.INSTANCE.getEnabledModules()) {
                module.onTick();
                module.onShitTick();
                module.onCockAndBallTorture();
            }
        }
    }
}
