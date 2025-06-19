package adris.altoclef.altomenu.UI.screens.clickgui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.nio.file.Path;

/**
 * Represents a collapsible folder in the script management interface
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class ScriptFolder {
    private final String folderName;
    private final Path folderPath;
    private final int indentLevel;
    private final ScriptFrame parent;
    
    public int offset;
    private boolean expanded;
    
    // UI Constants
    private static final int FOLDER_HEIGHT = 15;
    private static final Color FOLDER_COLOR = new Color(100, 100, 150, 200);
    private static final Color FOLDER_HOVER_COLOR = new Color(130, 130, 180, 200);
    private static final Color BORDER_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR = Color.WHITE;
    
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    
    public ScriptFolder(String folderName, Path folderPath, int indentLevel, ScriptFrame parent) {
        this.folderName = folderName;
        this.folderPath = folderPath;
        this.indentLevel = indentLevel;
        this.parent = parent;
        this.offset = 0;
        this.expanded = true; // Folders start expanded by default
    }
    
    /**
     * Render the folder in the UI
     */
    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        // This will be set by the parent ScriptFrame during layout
        int x = getX();
        int y = getY();
        int width = getWidth();
        
        // Check hover state
        boolean hovered = isHovered(mouseX, mouseY);
        
        // Choose folder color based on hover state
        Color folderColor = hovered ? FOLDER_HOVER_COLOR : FOLDER_COLOR;
        
        // Draw folder background
        matrices.fill(x, y, x + width, y + FOLDER_HEIGHT, folderColor.getRGB());
        
        // Draw border
        matrices.fill(x, y, x + width, y + 1, BORDER_COLOR.getRGB());
        matrices.fill(x, y + FOLDER_HEIGHT - 1, x + width, y + FOLDER_HEIGHT, BORDER_COLOR.getRGB());
        
        // Draw expand/collapse icon
        String expandIcon = expanded ? "▼" : "▶";
        matrices.drawTextWithShadow(mc.textRenderer, expandIcon, x + 2, y + 3, TEXT_COLOR.getRGB());
        
        // Draw folder icon
        String folderIcon = "📁";
        matrices.drawTextWithShadow(mc.textRenderer, folderIcon, x + 15, y + 3, TEXT_COLOR.getRGB());
        
        // Draw folder name
        int textX = x + 30;
        int textY = y + 3;
        String displayName = folderName;
        
        // Truncate name if too long
        int maxWidth = width - 50;
        if (mc.textRenderer.getWidth(displayName) > maxWidth) {
            while (mc.textRenderer.getWidth(displayName + "...") > maxWidth && displayName.length() > 1) {
                displayName = displayName.substring(0, displayName.length() - 1);
            }
            displayName += "...";
        }
        
        matrices.drawTextWithShadow(mc.textRenderer, displayName, textX, textY, TEXT_COLOR.getRGB());
        
        // Draw item count hint if not expanded
        if (!expanded) {
            // TODO: Could add script count in folder here
            matrices.drawTextWithShadow(mc.textRenderer, "...", x + width - 20, textY, Color.GRAY.getRGB());
        }
    }
    
    /**
     * Check if mouse is hovering over this folder
     */
    public boolean isHovered(double mouseX, double mouseY) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        
        return mouseX >= x && mouseX <= x + width && 
               mouseY >= y && mouseY <= y + FOLDER_HEIGHT;
    }
    
    /**
     * Toggle the expanded state
     */
    public void toggleExpanded() {
        expanded = !expanded;
    }
    
    /**
     * Check if the folder is expanded
     */
    public boolean isExpanded() {
        return expanded;
    }
    
    /**
     * Set the expanded state
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
    
    /**
     * Get the folder name
     */
    public String getFolderName() {
        return folderName;
    }
    
    /**
     * Get the folder path
     */
    public Path getFolderPath() {
        return folderPath;
    }
    
    /**
     * Get the indent level for hierarchy display
     */
    public int getIndentLevel() {
        return indentLevel;
    }
    
    /**
     * Get the X position (calculated with indent)
     */
    private int getX() {
        return parent.x + (indentLevel * 10);
    }
    
    /**
     * Get the Y position
     */
    private int getY() {
        return parent.y + offset;
    }
    
    /**
     * Get the width (calculated with indent)
     */
    private int getWidth() {
        return parent.width - (indentLevel * 10);
    }
    
    /**
     * Check if this folder contains a specific path
     */
    public boolean containsPath(Path path) {
        return path.startsWith(folderPath);
    }
    
    /**
     * Get the relative depth of a path within this folder
     */
    public int getRelativeDepth(Path path) {
        if (!containsPath(path)) {
            return -1;
        }
        
        Path relativePath = folderPath.relativize(path);
        return relativePath.getNameCount() - 1; // -1 because the file itself doesn't count as depth
    }
    
    /**
     * Check if this folder should be shown based on parent folder states
     */
    public boolean shouldBeVisible() {
        // This would be calculated by the parent frame based on folder hierarchy
        return true;
    }
    
    /**
     * Compare folders for sorting (folders with same indent level)
     */
    public int compareTo(ScriptFolder other) {
        // First sort by indent level, then by name
        int indentComparison = Integer.compare(this.indentLevel, other.indentLevel);
        if (indentComparison != 0) {
            return indentComparison;
        }
        return this.folderName.compareToIgnoreCase(other.folderName);
    }
    
    @Override
    public String toString() {
        return String.format("ScriptFolder{name='%s', path='%s', indent=%d, expanded=%s}", 
                           folderName, folderPath, indentLevel, expanded);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ScriptFolder other = (ScriptFolder) obj;
        return folderPath.equals(other.folderPath) && 
               indentLevel == other.indentLevel;
    }
    
    @Override
    public int hashCode() {
        return folderPath.hashCode() * 31 + indentLevel;
    }
} 