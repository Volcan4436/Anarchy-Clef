package adris.altoclef.altomenu.cheatUtils;


import adris.altoclef.AltoClef;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import static adris.altoclef.altomenu.cheatUtils.CMoveUtil.mc;

//Credits:
// Cascade ChatUtil (Lagoon)
public class CChatUtil {


    public static void addChatMessage(final String msg) {
        MutableText message = Text.empty();
        message.append(AltoClef.INSTANCE.getModSettings().getChatLogPrefix() + " ");
        message.append(msg);
        mc.inGameHud.getChatHud().addMessage(message);
    }
}
