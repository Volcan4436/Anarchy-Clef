package adris.altoclef.altomenu.modules.Development;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.CChatUtil;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.eventbus.EventHandler;
import adris.altoclef.eventbus.events.PacketEvent;


//todo:
//  - Implement Delay so that you don't get your Console Spammed too much
//  - Implement KeepAlive Cancel
//  - Implement Transaction Cancel
//  - Allow AnarchyClef Errors to output in this module
//  - Implement Debugging of Other Players
//  - Implement Packet Logging
//  - Implement Debug Command
//  - Implement DisconnectToggle
//  - Implement Entity Render Debugging
//  - Implement Logging of Block Updates
//  - Implement LightUpdate Logging
//  - Implement SetBack Notification
//  - Implement Transaction Debugging (This Will Allow us to Determine the Anticheat the server uses)
//  - Implement Velocity Debugging
//  - Implement End Crystal Debugging
//  - Implement Debug Crosshair (Hover over a block or entity and send its info into the chat box)
//  - Implement Debug Menu (Open with a Command) (features will include: ClientSide Player Data Editing, ClientSide Inventory Editing, ClientSide GameMode Changer, Position Editor, Command Text Box, Entity List, SendArbitraryPacket+Data, Server Information)
public class Debugger extends Mod {

    public Debugger() {
        super("Debugger (BETA)", "Funne", Mod.Category.DEVELOPMENT);
        PacketEvent.addGlobalListener(this::onPacket);
    }


    BooleanSetting ground = new BooleanSetting("Ground", false);
    BooleanSetting moved = new BooleanSetting("Moved", false);
    BooleanSetting tick = new BooleanSetting("Tick", false);
    BooleanSetting packetLog = new BooleanSetting("Packet Logger", true); // New toggle


    // BooleanSetting crosshair = new BooleanSetting("Crosshair Debug", false);

    @EventHandler
    public boolean onShitTick() {
        if (tick.isEnabled()) CChatUtil.addChatMessage(" Ticked");
        // ChatUtil.addChatMessage(" Debug Example");
        return false;
    }

    @Override
    public void onEnable() {
        CChatUtil.addChatMessage(" [Alert] Debugger Module is for Development of the Mod not for Debugging anything important!");
        super.onEnable();
    }

    @Override
    public void onDisable() {
        CChatUtil.addChatMessage(" [Alert] Debugger Disabled");
        super.onDisable();
    }

    @EventHandler
    public void onMove() { //This Method needs to be rewritten as it is essentially just onTick with extra steps
        assert mc.player != null;
        if (moved.isEnabled()) CChatUtil.addChatMessage(" Player Moved");
        if (ground.isEnabled()) CChatUtil.addChatMessage(" Ground: " + mc.player.isOnGround());
    }

    //This can easily cause lag if called too often
    // Will Be Better if we made it just flash a box on the screen with a counter
    @Override
    public void onRender() {
        //CChatUtil.addChatMessage(" Frame Rendered");
    }

    // Packet logging
    private void onPacket(PacketEvent evt) {
        if (!packetLog.isEnabled() || !Debugger.super.isEnabled()) return;

        String direction = (evt.direction == PacketEvent.Direction.SEND) ? "OUTGOING" : "INCOMING";
        String packetName = evt.packet.getClass().getSimpleName();

        // Print packet info to chat
        CChatUtil.addChatMessage("[" + direction + "] " + packetName);
    }
}
