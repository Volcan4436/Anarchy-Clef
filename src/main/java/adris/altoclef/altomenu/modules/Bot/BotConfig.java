package adris.altoclef.altomenu.modules.Bot;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.NumberSetting;

public class BotConfig extends Mod {

    public static BotConfig INSTANCE = new BotConfig();

    public BotConfig() {
        super("BotConfig", "Configure Bot Stuff", Mod.Category.BOT);
        INSTANCE = this;
    }


    public final BooleanSetting botReachOverride = new BooleanSetting("Override Reach", false);
    public final NumberSetting botReach = new NumberSetting("Bot Reach", 0.1, 8, 4, 0.1);
}
