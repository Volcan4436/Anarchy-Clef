package adris.altoclef.mixins;

import adris.altoclef.AltoClef;
import adris.altoclef.altomenu.command.Command;
import adris.altoclef.altomenu.command.CommandManager;
import adris.altoclef.altomenu.command.CommandSuggestEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    /*@Inject(method = "onCommandSuggestions", at = @At("TAIL"))
    public void onCmdSuggest(CommandSuggestionsS2CPacket packet, CallbackInfo ci) {
        new CommandSuggestEvent(packet).call();
    }*/

    // TODO: make a randomized prefix and make the rnadomized string prefix thing in gui
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void sendChatMessage(String msg, CallbackInfo ci) {

        StringBuilder CMD = new StringBuilder();
        for (int i = 1; i < msg.toCharArray().length; ++i) {
            CMD.append(msg.toCharArray()[i]);
        }
        String[] args = CMD.toString().split(" ");

        if (msg.startsWith(AltoClef.commandPrefix)) {
            for (Command command : CommandManager.INSTANCE.getCmds()) {
                if (args[0].equalsIgnoreCase(command.getName())) {
                    command.onCmd(msg, args);
                    ci.cancel();
                    break;
                }
            }
        }
    }
}
