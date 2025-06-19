package adris.altoclef.altomenu.UI.screens.clickgui;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.util.math.InterpUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

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
    private ScriptFrame scriptFrame;

    boolean pressed = false;


    public ClickGUI() {
        super(Text.literal("ClickGUI"));

        frames = new ArrayList<>();
        int x = 20;
        for (adris.altoclef.altomenu.Mod.Category category : Mod.Category.values()) {
            if (category == Mod.Category.SCRIPTS) {
                // Create special script frame for Scripts category
                scriptFrame = new ScriptFrame(x, 30, 120, 15);
            } else {
                frames.add(new Frame(category, x, 30, 100, 15));
            }
            x += (category == Mod.Category.SCRIPTS) ? 120 : 100;
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


    // Step 1: Declare the texture Identifier somewhere in your class
    private static final Identifier CUSTOM_ICON = new Identifier("altoclef", "textures/gui/hibiki.png");


    @Override
    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        // 1. Draw everything else first
        matrices.drawCenteredTextWithShadow(this.textRenderer, "AltoMenu [Developed by Volcan & ChiefWarCry]", 125, this.height - 20, Color.RED.getRGB());

        for (Frame frame : frames) {
            frame.render(matrices, mouseX, mouseY, delta);
            frame.updatePosition(mouseX, mouseY);
        }
        
        // Render script frame if it exists
        if (scriptFrame != null) {
            scriptFrame.render(matrices, mouseX, mouseY, delta);
            scriptFrame.updatePosition(mouseX, mouseY);
        }

        // 2. Then draw the image on top
        int textureWidth = 750;
        int textureHeight = 727;
        float scale = 0.15f;

        int drawWidth = (int) (textureWidth * scale);
        int drawHeight = (int) (textureHeight * scale);

        int x = this.width - drawWidth - 5;
        int y = this.height - drawHeight - 5;

        matrices.drawTexture(CUSTOM_ICON, x, y, drawWidth, drawHeight, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);


        // 3. Do NOT call super.render if it might draw above your image
        // super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Frame frame : frames) {
            frame.mouseClicked(mouseX, mouseY, button);
        }
        
        // Handle script frame clicks
        if (scriptFrame != null) {
            scriptFrame.mouseClicked(mouseX, mouseY, button);
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Frame frame : frames) {
            frame.mouseReleased(mouseX, mouseY, button);
        }
        
        // Handle script frame mouse release
        if (scriptFrame != null) {
            scriptFrame.mouseReleased(mouseX, mouseY, button);
        }
        
        return super.mouseReleased(mouseX, mouseY, button);
    }


    public static void open() {
        mc.setScreen(INSTANCE);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        onKeypress(keyCode, 1);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char chr, int modifiers) {
        // Handle script frame character input
        if (scriptFrame != null) {
            scriptFrame.handleCharTyped(chr);
        }
        return super.charTyped(chr, modifiers);
    }

    public void onKeypress(int key, int action) {
        // Handle script frame keyboard input first (for new script dialog)
        if (scriptFrame != null) {
            // Convert key to character if possible
            char character = (char) key;
            scriptFrame.handleKeyPressed(key, character);
        }
        
        if (key == GLFW.GLFW_KEY_DOWN) {
            for (Frame frame : frames) {
                frame.updatePositionNoMouse(frame.x, frame.y + 10);
            }
            if (scriptFrame != null) {
                scriptFrame.updatePositionNoMouse(scriptFrame.x, scriptFrame.y + 10);
            }
        }
        if (key == GLFW.GLFW_KEY_UP) {
            for (Frame frame : frames) {
                frame.updatePositionNoMouse(frame.x, frame.y - 10);
            }
            if (scriptFrame != null) {
                scriptFrame.updatePositionNoMouse(scriptFrame.x, scriptFrame.y - 10);
            }
        }
        if (key == GLFW.GLFW_KEY_RIGHT) {
            for (Frame frame : frames) {
                frame.updatePositionNoMouse(frame.x + 10, frame.y);
            }
            if (scriptFrame != null) {
                scriptFrame.updatePositionNoMouse(scriptFrame.x + 10, scriptFrame.y);
            }
        }
        if (key == GLFW.GLFW_KEY_LEFT) {
            for (Frame frame : frames) {
                frame.updatePositionNoMouse(frame.x - 10, frame.y);
            }
            if (scriptFrame != null) {
                scriptFrame.updatePositionNoMouse(scriptFrame.x - 10, scriptFrame.y);
            }
        }
    }
    
    /**
     * Refreshes all module lists in all frames
     * Call this when modules are dynamically added/removed
     */
    public void refreshModules() {
        for (Frame frame : frames) {
            frame.refreshModules();
        }
        
        // Refresh script frame if it exists
        if (scriptFrame != null) {
            scriptFrame.refreshScripts();
        }
    }
}
