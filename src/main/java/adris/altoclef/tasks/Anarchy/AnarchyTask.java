package adris.altoclef.tasks.Anarchy;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.Anarchy.*;
import adris.altoclef.tasksystem.Task;

public class AnarchyTask extends Task {

    // TODO
    //  Goals:
    //  - Escape Spawn Consistently
    //  - Kill a Player with an End Crystal Fully Automatically
    //  - Search for Less Geared Players to Spawn Camp at Server Spawn
    //  - Automatically Lava Cast
    //  - Obtain All Gear Needed to Survive including Enchants
    //  - Travel with Elytra if possible
    //  - Avoid End Crystals
    //  - Detect Safe Holes and go to them if crystals are being placed nearby
    //  - Find an EnderChest Stash Gear and Suicide if no escape methods work
    //  - Create a Swarm of Bots that can work together
    //  - Implement Awareness System
    //  - Add Logging
    //  - Detect and Bypass: NCP, UNCP, Grim, Volcanware (StrikeAC) Matrix, Matrix Labs, Matrix Cloud, Vulcan, Intave, Verus, Hypixel (WatchDog && Watchdog Prediction), HyCraft, MMC (AGC), HopLite (Grim Fork)

    @Override
    protected void onStart(AltoClef mod) {
    }

    @Override
    protected Task onTick(AltoClef mod) {
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
    }

    @Override
    protected boolean isEqual(Task other) {
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Anarchy Task [EXPERIMENTAL]";
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return false;
    }
}
