package adris.altoclef.altomenu.UI.screens.clickgui;

import adris.altoclef.AltoClef;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a Lua script button in the script management interface
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class ScriptButton {
    private final Path scriptPath;
    private final ScriptFrame parent;
    private final String scriptName;
    private final int indentLevel;
    
    public int offset;
    private boolean enabled;
    private boolean dragging;
    
    // UI Constants
    private static final int BUTTON_HEIGHT = 15;
    private static final Color ENABLED_COLOR = new Color(100, 200, 100, 200);
    private static final Color DISABLED_COLOR = new Color(200, 100, 100, 200);
    private static final Color HOVER_COLOR = new Color(255, 255, 255, 50);
    private static final Color BORDER_COLOR = Color.BLACK;
    
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    
    public ScriptButton(Path scriptPath, ScriptFrame parent, int offset, int indentLevel) {
        this.scriptPath = scriptPath;
        this.parent = parent;
        this.offset = offset;
        this.indentLevel = indentLevel;
        this.scriptName = scriptPath.getFileName().toString().replace(".lua", "");
        this.enabled = false;
        this.dragging = false;
        
        // Check if script is currently loaded
        checkScriptStatus();
    }
    
    /**
     * Check if this script is currently loaded in the script engine
     */
    private void checkScriptStatus() {
        try {
            AltoClef mod = AltoClef.INSTANCE;
            if (mod != null && mod.getScriptEngine() != null && mod.getScriptEngine().isEnabled()) {
                this.enabled = mod.getScriptEngine().isScriptLoaded(scriptName);
            } else {
                this.enabled = false;
            }
        } catch (Exception e) {
            // Script engine might not be available
            this.enabled = false;
        }
    }
    
    /**
     * Render the script button
     */
    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        int x = parent.x + (indentLevel * 10);
        int y = parent.y + offset;
        int width = parent.width - (indentLevel * 10);
        
        // Check hover state
        boolean hovered = isHovered(mouseX, mouseY);
        
        // Choose button color based on state
        Color buttonColor = enabled ? ENABLED_COLOR : DISABLED_COLOR;
        if (hovered) {
            buttonColor = new Color(
                Math.min(255, buttonColor.getRed() + 30),
                Math.min(255, buttonColor.getGreen() + 30),
                Math.min(255, buttonColor.getBlue() + 30),
                buttonColor.getAlpha()
            );
        }
        
        // Draw button background
        matrices.fill(x, y, x + width, y + BUTTON_HEIGHT, buttonColor.getRGB());
        
        // Draw border
        matrices.fill(x, y, x + width, y + 1, BORDER_COLOR.getRGB());
        matrices.fill(x, y + BUTTON_HEIGHT - 1, x + width, y + BUTTON_HEIGHT, BORDER_COLOR.getRGB());
        
        // Draw status indicator
        String statusIndicator = enabled ? "●" : "○";
        Color statusColor = enabled ? Color.GREEN : Color.RED;
        matrices.drawTextWithShadow(mc.textRenderer, statusIndicator, x + 2, y + 3, statusColor.getRGB());
        
        // Draw script name
        int textX = x + 15;
        int textY = y + 3;
        String displayName = scriptName;
        
        // Truncate name if too long
        int maxWidth = width - 70; // Leave space for status and hover actions
        if (mc.textRenderer.getWidth(displayName) > maxWidth) {
            while (mc.textRenderer.getWidth(displayName + "...") > maxWidth && displayName.length() > 1) {
                displayName = displayName.substring(0, displayName.length() - 1);
            }
            displayName += "...";
        }
        
        matrices.drawTextWithShadow(mc.textRenderer, displayName, textX, textY, -1);
        
        // Draw file extension hint
        if (!hovered) {
            matrices.drawTextWithShadow(mc.textRenderer, ".lua", x + width - 30, textY, Color.GRAY.getRGB());
        }
    }
    
    /**
     * Handle mouse clicks
     */
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!isHovered(mouseX, mouseY)) return;
        
        if (button == 0) { // Left click
            // This will be handled by the parent frame to check for hover actions first
        }
    }
    
    /**
     * Handle mouse release
     */
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }
    
    /**
     * Toggle the script on/off
     */
    public void toggleScript() {
        System.out.println("=== SCRIPT TOGGLE DEBUG START ===");
        System.out.println("Attempting to toggle script: " + scriptName);
        System.out.println("Current enabled state: " + enabled);
        System.out.println("Script path: " + scriptPath);
        
        try {
            AltoClef mod = AltoClef.INSTANCE;
            System.out.println("AltoClef.INSTANCE: " + (mod != null ? "Available" : "NULL"));
            
            if (mod == null) {
                System.err.println("❌ CRITICAL: AltoClef not available");
                return;
            }
            
            System.out.println("Script engine: " + (mod.getScriptEngine() != null ? "Available" : "NULL"));
            if (mod.getScriptEngine() == null) {
                System.err.println("❌ CRITICAL: Script engine not initialized");
                return;
            }
            
            System.out.println("Script engine enabled: " + mod.getScriptEngine().isEnabled());
            if (!mod.getScriptEngine().isEnabled()) {
                System.err.println("❌ CRITICAL: Script engine is disabled");
                return;
            }
            
            if (enabled) {
                // Unload script
                System.out.println("🔄 Unloading script...");
                mod.getScriptEngine().unloadScript(scriptName);
                System.out.println("Unload completed");
                enabled = false;
                System.out.println("✅ Script '" + scriptName + "' disabled");
            } else {
                // Load script
                System.out.println("🔄 Loading script...");
                System.out.println("Reading script content from: " + scriptPath);
                String scriptContent = Files.readString(scriptPath);
                System.out.println("Script content length: " + scriptContent.length() + " characters");
                
                boolean success = mod.getScriptEngine().loadScript(scriptName, scriptContent);
                System.out.println("Load result: " + success);
                enabled = success;
                if (success) {
                    System.out.println("✅ Script '" + scriptName + "' enabled successfully!");
                } else {
                    System.err.println("❌ Failed to enable script '" + scriptName + "'");
                }
            }
            
        } catch (IOException e) {
            System.err.println("❌ IO ERROR: Failed to read script file '" + scriptName + "': " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ GENERAL ERROR: Failed to toggle script '" + scriptName + "': " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Final enabled state: " + enabled);
        System.out.println("=== SCRIPT TOGGLE DEBUG END ===");
        System.out.println();
    }
    
    /**
     * Check if mouse is hovering over this button
     */
    public boolean isHovered(double mouseX, double mouseY) {
        int x = parent.x + (indentLevel * 10);
        int y = parent.y + offset;
        int width = parent.width - (indentLevel * 10);
        
        return mouseX >= x && mouseX <= x + width && 
               mouseY >= y && mouseY <= y + BUTTON_HEIGHT;
    }
    
    /**
     * Get the X position of this button
     */
    public int getX() {
        return parent.x + (indentLevel * 10);
    }
    
    /**
     * Get the Y position of this button
     */
    public int getY() {
        return parent.y + offset;
    }
    
    /**
     * Get the width of this button
     */
    public int getWidth() {
        return parent.width - (indentLevel * 10);
    }
    
    /**
     * Get the script name (without .lua extension)
     */
    public String getScriptName() {
        return scriptName;
    }
    
    /**
     * Get the full script path
     */
    public Path getScriptPath() {
        return scriptPath;
    }
    
    /**
     * Check if the script is currently enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Set the enabled state (for external control)
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Get the indent level for folder hierarchy
     */
    public int getIndentLevel() {
        return indentLevel;
    }
    
    /**
     * Check if this button is currently being dragged
     */
    public boolean isDragging() {
        return dragging;
    }
    
    /**
     * Set the dragging state
     */
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }
    
    /**
     * Refresh the script status from the script engine
     */
    public void refreshStatus() {
        checkScriptStatus();
    }
} 