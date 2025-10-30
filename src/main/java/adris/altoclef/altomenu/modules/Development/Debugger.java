package adris.altoclef.altomenu.modules.Development;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.cheatUtils.CChatUtil;
import adris.altoclef.eventbus.EventHandler;

public class Debugger extends Mod {

    public Debugger() {
        super("Debugger (BETA)", "Funne", Mod.Category.DEVELOPMENT);
    }



    @EventHandler
    public boolean onShitTick() {
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
    public void onMove() {
        //CChatUtil.addChatMessage(" Player Moved");
    }
}
