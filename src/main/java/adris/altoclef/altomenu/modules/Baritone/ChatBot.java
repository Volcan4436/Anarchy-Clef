package adris.altoclef.altomenu.modules.Baritone;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.settings.BooleanSetting;
import adris.altoclef.altomenu.settings.NumberSetting;
import adris.altoclef.eventbus.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

//Todo
// - Add Player Whitelist
// - Support Private Messaging
// - Integrate Discord Bot Support
// - add AutoSnitch (Automatically Snitches on players that Change to Creative Mode or Players that hold out Illegal Items)
// - Support GreenText messages for Anarchy Servers
// - Support Running Baritone / AltoClef Commands (With a User Whitelist)
// - Support Toggling Modules with Chat (With a User Whitelist)
// - Support BlackListing Players
// - Add AntiSpam to stop bot from getting kicked from AntiSpam Plugins
// - Send Errors to Chat (with private messaging support)
// - Send Chat Screenshots to Discord Webhook
// - Screenshot Command (Screenshots then uploads to img.bb or imgur and sends image link to chat)
// - Add GithubSearch Command to search for Github Repo's using GithubAPI
// - Support Chatting with a Twitch Chat using LLM's like DeepSeek or ChatGPT
// - Support Chatting with a Discord Server using LLM's like DeepSeek or ChatGPT
// - Support Chatting in Minecraft Chat using LLM's like DeepSeek or ChatGPT
// - Add Magic8Ball Command
// - Add ServerFinder Command (Scans for Server's on a certain IP Address)
// - Add ServerStatus Commmand (Check's if a Server is current online and gets their current PlayerCount)
// - Add ServerList Command (Lists all Servers found through ServerFinder by sending a Chat Message with link to a list sent on PasteBin)

public class ChatBot extends Mod {
    public static ChatBot instance;
    public ChatBot() {
        super("ChatBot", "ChatBot", Mod.Category.BARITONE);
        instance = this;
    }

    NumberSetting messageDelay = new NumberSetting("Message Delay", 1, 10, 1, 0.1);
    BooleanSetting greentext = new BooleanSetting("GreenText", false);

    @Override
    public void onEnable() {
        System.out.println("ChatBot Enabled");
        super.onEnable();
    }

    @Override
    public void onDisable() {
        System.out.println("ChatBot Disabled");
        super.onDisable();
    }

    private boolean hasSentMessageAlready = false;

    @EventHandler
    public boolean onShitTick() {
        boolean justDied = !mc.player.isAlive() && !hasSentMessageAlready;
        boolean justRespawned = mc.player.isAlive() && hasSentMessageAlready;
        if (justDied) {
            mc.player.networkHandler.sendChatMessage("Why is my only Job to die?");
            hasSentMessageAlready = true;
        } else if (justRespawned) {
            hasSentMessageAlready = false;
        }
        return false;
    }

    public void processMessage(Text message) {

    }

    public void processCommand(String command) {
        //Check if it contains the command
        switch (command) {
            case "coords":
                sendMessage("Current coordinates: " + "X: " + Math.round(mc.player.getX()) + " Y: " + Math.round(mc.player.getY()) + " Z: " + Math.round(mc.player.getZ()));
                break;
            case "github":
                sendMessage("github.com/Volcan4436/Anarchy-Clef");
                break;
            case "help":
                // Handle the help command
                break;
            case "info":
                // Handle the info command
                break;
            default:
                // Handle unknown commands
                break;
        }
    }

    String prefix = "";

    private void sendMessage(String s) {
        //Send a Chat Message
        assert MinecraftClient.getInstance().player != null;
        if (greentext.isEnabled()) prefix = "> ";
        else prefix = "";
        MinecraftClient.getInstance().player.networkHandler.sendChatMessage(prefix + s);
    }
}
