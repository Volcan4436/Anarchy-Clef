package adris.altoclef.altomenu.UI.screens.clickgui;

import adris.altoclef.altomenu.Mod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static adris.altoclef.altomenu.UI.screens.clickgui.ModuleButton.mc;

//todo:
// Move Frames up and Down with ScrollWheel to fix the issue with modules going off screen if too many options
// improve the look of the ClickGUI
// Add a search bar
// add a Bind Button
// add Config System
// Add Position Reset Button (for if they go off screen)
// Add Scripting API (Similar to Astolfo Client) + Support Hooking into Baritone with the Scripting API
// add Panic Button (toggles all modules and removes all HUD elements of the Bot and Disables Bots Prefix)
// **Difficult** allow you to connect to the ClickGUI using a Websocket (Create a App for 3DS, Switch and Android so we can remotely toggle modules) over Direct IP Connection
// Add TabGUI on HUD (Similar to 1.8 Clients)
// Add Support for Gamepads to use the GUI
public class ClickGUI extends Screen {

    private final List<Frame> frames;

    public ClickGUI() {
        super(Text.literal("ClickGUI"));

        frames = new ArrayList<>();
        int x = 20;
        for (adris.altoclef.altomenu.Mod.Category category : Mod.Category.values()) {
            frames.add(new Frame(category, x, 30, 100, 15));
            x+=120;
        }
    
        // Add the frames to the list of frames
/*        frames.add(settingsFrame);
        frames.add(playerFrame);
        frames.add(renderFrame);
        frames.add(movementFrame);
        frames.add(worldFrame);
        frames.add(chatFrame);
        frames.add(hudFrame);*/

/*        frames = new ArrayList<>();

        for (Mod.Category category : Mod.Category.values()) {
            frames.add(new Frame(category, x, 30, 100, 15,category.name()));
            x += 120;
        }*/
    }

    public static ClickGUI INSTANCE = new ClickGUI();


    @Override
    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        matrices.fill(0, 0, 100000000, 100000000, 0x80000000);
        //Draw a text that says "AltoMenu on Top" in the bottom right corner
        matrices.drawCenteredTextWithShadow(this.textRenderer, "AltoMenu [BETA]", this.width - 50, this.height - 20, Color.RED.getRGB());
        for (Frame frame : frames) {
            frame.render(matrices, mouseX, mouseY, delta);
            frame.updatePosition(mouseX, mouseY);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Frame frame : frames) {
            frame.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Frame frame : frames) {
            frame.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public static void open() {
        mc.setScreen(INSTANCE);
    }
}
