package adris.altoclef.altomenu.managers;

import adris.altoclef.altomenu.modules.Bot.ChatBot;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

//TODO
// make it possible to use this for making full commands instead of just checking 1 word
// make it check for all messages not just Signed Messages
// @ChiefWarCry please attempt a fix?
public class ChatHandler {
    private static final String PREFIX = "!"; //todo Make this configurable
    public void handleChatMessage(ChatMessageS2CPacket packet) {
        if (ChatBot.instance.isEnabled()) {
            String messageString = packet.body().content();
            if (messageString.startsWith(PREFIX)) {
                // Remove the prefix from the message
                String command = messageString.substring(PREFIX.length());
                // Call your chatbot logic here
                ChatBot chatbot = new ChatBot();
                chatbot.processCommand(command);
            }
        }
    }

    //todo:
    // use tokenization instead (recommended by Lagoon)
    public void handleGameMessage(GameMessageS2CPacket packet) {
        if (ChatBot.instance.isEnabled()) {
            String messageString = packet.content().getString();
            for (int i = 0; i < messageString.length(); i++) {
                if (messageString.startsWith(PREFIX, i)) {
                    String command = messageString.substring(i + PREFIX.length());
                    ChatBot chatbot = new ChatBot();
                    chatbot.processCommand(command);
                    break;
                }
            }
        }
    }
}
