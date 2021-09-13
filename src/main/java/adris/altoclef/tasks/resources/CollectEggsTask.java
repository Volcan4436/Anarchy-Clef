package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.DefaultGoToDimensionTask;
import adris.altoclef.tasks.DoToClosestEntityTask;
import adris.altoclef.tasks.GetToEntityTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.Items;

public class CollectEggsTask extends ResourceTask {

    private final int _count;

    private final DoToClosestEntityTask _waitNearChickens;

    private AltoClef _mod;

    public CollectEggsTask(int targetCount) {
        super(Items.EGG, targetCount);
        _count = targetCount;
        _waitNearChickens = new DoToClosestEntityTask(() -> _mod.getPlayer().getPos(), chicken -> new GetToEntityTask(chicken, 5), ChickenEntity.class);
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        _mod = mod;
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        // Wrong dimension check.
        if (_waitNearChickens.wasWandering() && mod.getCurrentDimension() != Dimension.OVERWORLD) {
            setDebugState("Going to right dimension.");
            return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
        }
        // Just wait around chickens.
        setDebugState("Waiting around chickens. Yes.");
        return _waitNearChickens;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectEggsTask;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting " + _count + " eggs.";
    }
}
