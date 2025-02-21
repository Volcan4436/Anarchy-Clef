package adris.altoclef.altomenu.managers;

import adris.altoclef.altomenu.modules.Baritone.ChatBot;
import net.minecraft.network.message.SignedMessage;

//TODO
// make it possible to use this for making full commands instead of just checking 1 word
// make it check for all messages not just Signed Messages
// @ChiefWarCry please attempt a fix?
public class ChatHandler {
    private static final String PREFIX = "!"; //todo Make this configurable
    public void handleChatMessage(SignedMessage message) {
        if (ChatBot.instance.isEnabled()) {
            String messageString = message.getContent().getString();
            if (messageString.startsWith(PREFIX)) {
                // Remove the prefix from the message
                String command = messageString.substring(PREFIX.length());
                // Call your chatbot logic here
                ChatBot chatbot = new ChatBot();
                chatbot.processCommand(command);
            }
        }
    }
}
