package adris.altoclef.tasks.Anarchy;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.experimental.awareness.AwarenessSystem;
import adris.altoclef.tasks.construction.PlaceBlockTask;
import adris.altoclef.tasks.construction.PlaceStructureBlockTask;
import adris.altoclef.tasks.container.SmeltInFurnaceTask;
import adris.altoclef.tasks.entity.DoToClosestEntityTask;
import adris.altoclef.tasks.entity.KillPlayerTask;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasks.movement.RunAwayFromEntitiesTask;
import adris.altoclef.tasks.movement.SearchChunksExploreTask;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.SmeltTarget;
import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FightBotTask extends Task {


    private static final int FEAR_SEE_DISTANCE = 30;
    private static final int FEAR_DISTANCE = 20;
    private static final int RUN_AWAY_DISTANCE = 80;

    private static final int MIN_BUILDING_BLOCKS = 10;
    private static final int PREFERRED_BUILDING_BLOCKS = 60;
    private final Task _foodTask = new CollectFoodTask(80);
    private final TimerGame _runAwayExtraTime = new TimerGame(10);
    private final Predicate<PlayerEntity> _canTerminate;
    private final FightBotTask.ScanChunksInRadius _scanTask;
    private final TimerGame _funnyMessageTimer = new TimerGame(10);
    private Vec3d _closestPlayerLastPos;
    private Vec3d _closestPlayerLastObservePos;
    private Task _runAwayTask;
    private String _currentVisibleTarget;

    private Task _armorTask;

    public FightBotTask(BlockPos center, double scanRadius, Predicate<PlayerEntity> canTerminate) {
        _canTerminate = canTerminate;
        _scanTask = new FightBotTask.ScanChunksInRadius(center, scanRadius);
    }

    public FightBotTask(BlockPos center, double scanRadius) {
        this(center, scanRadius, accept -> true);
    }

    @Override
    protected void onStart(AltoClef mod) {
        mod.getBehaviour().setForceFieldPlayers(true);
        mod.getBehaviour().push();
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (_runAwayTask == null && isReadyToPunk(mod) && _closestPlayerLastPos != null) {
            //AwarenessSystem
//            if (AwarenessSystem.getThreatLevel() >= 100) {
//                Debug.logMessage("TerminatorTask: Threat level is high, RunningAway.");
//                return _runAwayTask;
//            }
/*            else if (mod.getPlayer().hurtTime == 1 && mod.getPlayer().getHealth() < 6) {
                AwarenessSystem.addThreatLevel(10);
            }*/
/*            else if (_closestPlayerLastPos.distanceTo(mod.getPlayer().getPos()) > 25 && AwarenessSystem.getThreatLevel() < 50) {
                Debug.logMessage("TerminatorTask: Threat Level is low, setting to 50.");
                AwarenessSystem.setThreatLevel(50);
            }
            else if (_closestPlayerLastPos.distanceTo(mod.getPlayer().getPos()) < 25 && AwarenessSystem.getThreatLevel() < 75) {
                Debug.logMessage("TerminatorTask: Threat Level is high, setting to 75.");
                AwarenessSystem.setThreatLevel(75);
            }*/
            /*                AwarenessSystem.setThreatLevel(0);*/
            return new DoToClosestEntityTask(
                    entity -> {
                        if (entity instanceof PlayerEntity) {
                            tryDoFunnyMessageTo(mod, (PlayerEntity) entity);
                            return new KillPlayerTask(entity.getName().getString());
                        }
                        // Should never happen.
                        Debug.logWarning("This should never happen.");
                        return _scanTask;
                    },
                    interact -> shouldPunk(mod, (PlayerEntity) interact),
                    PlayerEntity.class
            );
        }

        Optional<Entity> closest = mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), toPunk -> shouldPunk(mod, (PlayerEntity) toPunk), PlayerEntity.class);

        if (closest.isPresent()) {
            _closestPlayerLastPos = closest.get().getPos();
            _closestPlayerLastObservePos = mod.getPlayer().getPos();
        }

        if (!isReadyToPunk(mod)) {

            if (_runAwayTask != null && _runAwayTask.isActive() && !_runAwayTask.isFinished(mod)) {
                // If our last "scare" was too long ago or there are no more nearby players...
                boolean noneRemote = (closest.isEmpty() || !closest.get().isInRange(mod.getPlayer(), FEAR_DISTANCE));
                if (_runAwayExtraTime.elapsed() && noneRemote) {
                    Debug.logMessage("Stop running away, we're good.");
                    // Stop running away.
                    _runAwayTask = null;
                } else {
                    return _runAwayTask;
                }
            }

            // See if there's anyone nearby.
            if (mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), entityAccept -> {
                if (!shouldPunk(mod, (PlayerEntity) entityAccept)) {
                    return false;
                }
                if (entityAccept.isInRange(mod.getPlayer(), 15)) {
                    // We're close, count us.
                    return true;
                } else {
                    // Too far away.
                    if (!entityAccept.isInRange(mod.getPlayer(), FEAR_DISTANCE)) return false;
                    // We may be far and obstructed, check.
                    return LookHelper.seesPlayer(entityAccept, mod.getPlayer(), FEAR_SEE_DISTANCE);
                }
            }, PlayerEntity.class).isPresent()) {
                // RUN!

                _runAwayExtraTime.reset();
                try {
                    _runAwayTask = new FightBotTask.RunAwayFromPlayersTask(() -> {
                        Stream<PlayerEntity> stream = mod.getEntityTracker().getTrackedEntities(PlayerEntity.class).stream();
                        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                            return stream.filter(toAccept -> shouldPunk(mod, toAccept)).collect(Collectors.toList());
                        }
                    }, RUN_AWAY_DISTANCE);
                } catch (ConcurrentModificationException e) {
                    // oof
                    Debug.logWarning("Duct tape over ConcurrentModificationException (see log)");
                    e.printStackTrace();
                }
                setDebugState("Running away from players.");
                return _runAwayTask;
            }
        } else {
            // We can totally punk
            if (_runAwayTask != null) {
                _runAwayTask = null;
                Debug.logMessage("Stopped running away because we can now punk.");
            }
            // Get building materials if we don't have them.
            if (PlaceStructureBlockTask.getMaterialCount(mod) < MIN_BUILDING_BLOCKS) {
                setDebugState("Collecting building materials");
                return PlaceBlockTask.getMaterialTask(PREFERRED_BUILDING_BLOCKS);
            }

            if (mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), toPunk -> shouldPunk(mod, (PlayerEntity) toPunk), PlayerEntity.class).isPresent()) {
                setDebugState("Punking.");
                return new DoToClosestEntityTask(
                        entity -> {
                            if (entity instanceof PlayerEntity) {
                                tryDoFunnyMessageTo(mod, (PlayerEntity) entity);
                                return new KillPlayerTask(entity.getName().getString());
                            }
                            // Should never happen.
                            Debug.logWarning("This should never happen.");
                            return _scanTask;
                        },
                        interact -> shouldPunk(mod, (PlayerEntity) interact),
                        PlayerEntity.class
                );
            }
        }

        setDebugState("Scanning for players...");
        _currentVisibleTarget = null;
        if (_scanTask.failedSearch()) {
            Debug.logMessage("Re-searching missed places.");
            _scanTask.resetSearch(mod);
        }

        return _scanTask;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
        AwarenessSystem.setThreatLevel(0);
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof FightBotTask;
    }

    @Override
    protected String toDebugString() {
        return "Terminator Task";
    }

    private boolean isReadyToPunk(AltoClef mod) {
        return true;
    }

    private boolean shouldPunk(AltoClef mod, PlayerEntity player) {
        if (player == null || player.isDead()) return false;
        if (player.isCreative() || player.isSpectator()) return false;
        return _canTerminate.test(player);
    }

    private void tryDoFunnyMessageTo(AltoClef mod, PlayerEntity player) {
        if (_funnyMessageTimer.elapsed()) {
            if (LookHelper.seesPlayer(player, mod.getPlayer(), 80)) {
                String name = player.getName().getString();
                if (_currentVisibleTarget == null || !_currentVisibleTarget.equals(name)) {
                    _currentVisibleTarget = name;
                    _funnyMessageTimer.reset();
                    String funnyMessage = getRandomFunnyMessage();
                    mod.getMessageSender().enqueueWhisper(name, funnyMessage, MessagePriority.ASAP);
                }
            }
        }
    }

    private String getRandomFunnyMessage() {
        return "Prepare to get punked, kid";
    }

    private static class RunAwayFromPlayersTask extends RunAwayFromEntitiesTask {

        public RunAwayFromPlayersTask(Supplier<List<Entity>> toRunAwayFrom, double distanceToRun) {
            super(toRunAwayFrom, distanceToRun, true, 0.1);
            // More lenient progress checker
            _checker = new MovementProgressChecker();
        }

        @Override
        protected boolean isEqual(Task other) {
            return other instanceof FightBotTask.RunAwayFromPlayersTask;
        }

        @Override
        protected String toDebugString() {
            return "Running away from players";
        }
    }

    private class ScanChunksInRadius extends SearchChunksExploreTask {

        private final BlockPos _center;
        private final double _radius;

        public ScanChunksInRadius(BlockPos center, double radius) {
            _center = center;
            _radius = radius;
        }

        @Override
        protected boolean isChunkWithinSearchSpace(AltoClef mod, ChunkPos pos) {
            double cx = (pos.getStartX() + pos.getEndX()) / 2.0;
            double cz = (pos.getStartZ() + pos.getEndZ()) / 2.0;
            double dx = _center.getX() - cx,
                    dz = _center.getZ() - cz;
            return dx * dx + dz * dz < _radius * _radius;
        }

        @Override
        protected ChunkPos getBestChunkOverride(AltoClef mod, List<ChunkPos> chunks) {
            // Prioritise the chunk we last saw a player in.
            if (_closestPlayerLastPos != null) {
                double lowestScore = Double.POSITIVE_INFINITY;
                ChunkPos bestChunk = null;
                if (!chunks.isEmpty()) {
                    for (ChunkPos toSearch : chunks) {
                        double cx = (toSearch.getStartX() + toSearch.getEndX() + 1) / 2.0, cz = (toSearch.getStartZ() + toSearch.getEndZ() + 1) / 2.0;
                        double px = mod.getPlayer().getX(), pz = mod.getPlayer().getZ();
                        double distanceSq = (cx - px) * (cx - px) + (cz - pz) * (cz - pz);
                        double pdx = _closestPlayerLastPos.getX() - cx, pdz = _closestPlayerLastPos.getZ() - cz;
                        double distanceToLastPlayerPos = pdx * pdx + pdz * pdz;
                        Vec3d direction = _closestPlayerLastPos.subtract(_closestPlayerLastObservePos).multiply(1, 0, 1).normalize();
                        double dirx = direction.x, dirz = direction.z;
                        double correctDistance = pdx * dirx + pdz * dirz;
                        double tempX = dirx * correctDistance,
                                tempZ = dirz * correctDistance;
                        double perpendicularDistance = ((pdx - tempX) * (pdx - tempX)) + ((pdz - tempZ) * (pdz - tempZ));
                        double score = distanceSq + distanceToLastPlayerPos * 0.6 - correctDistance * 2 + perpendicularDistance * 0.5;
                        if (score < lowestScore) {
                            lowestScore = score;
                            bestChunk = toSearch;
                        }
                    }
                }
                return bestChunk;
            }
            return super.getBestChunkOverride(mod, chunks);
        }

        @Override
        protected boolean isEqual(Task other) {
            if (other instanceof FightBotTask.ScanChunksInRadius scan) {
                return scan._center.equals(_center) && Math.abs(scan._radius - _radius) <= 1;
            }
            return false;
        }

        @Override
        protected String toDebugString() {
            return "Scanning around a radius";
        }
    }
}
