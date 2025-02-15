package adris.altoclef.altomenu.managers;

import adris.altoclef.altomenu.modules.Baritone.ChatBot;
import net.minecraft.network.message.SignedMessage;

public class ChatHandler {
    private static final String PREFIX = "!";
    public void handleChatMessage(SignedMessage message) {
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
