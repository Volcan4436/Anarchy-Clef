package adris.altoclef.mixins;

import adris.altoclef.AltoClef;
import adris.altoclef.altomenu.command.Command;
import adris.altoclef.altomenu.command.CommandManager;
import adris.altoclef.altomenu.command.CommandSuggestEvent;
import adris.altoclef.altomenu.managers.ChatHandler;
import adris.altoclef.altomenu.modules.Baritone.ChatBot;
import adris.altoclef.altomenu.modules.Player.Velocity;
import adris.altoclef.altomenu.modules.Render.Fullbright;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Shadow public abstract void onChatMessage(ChatMessageS2CPacket packet);

    @Inject(method = "onExplosion", at = @At("HEAD"), cancellable = true)
    private void onExplosionPacket(ExplosionS2CPacket packet, CallbackInfo ci) {
        if (Velocity.Instance.isEnabled() && !Velocity.Instance.isNull()) {
            ci.cancel();
        }
    }

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
    private void onVelocityPacket(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        if (Velocity.Instance.isEnabled() && !Velocity.Instance.isNull()) {
            ci.cancel();
        }
    }

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

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    public void onMessage(ChatMessageS2CPacket packet, CallbackInfo ci) {
        ChatHandler chatHandler = new ChatHandler();
        chatHandler.handleChatMessage(packet);
        System.out.println(packet.body().content());
    }

    //onGameMessage
    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    public void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        ChatHandler chatHandler = new ChatHandler();
        chatHandler.handleGameMessage(packet);
        System.out.println(packet.content().getString());
    }
}
