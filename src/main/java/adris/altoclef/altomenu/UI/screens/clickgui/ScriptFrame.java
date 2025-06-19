package adris.altoclef.altomenu.UI.screens.clickgui;

import adris.altoclef.AltoClef;
import adris.altoclef.altomenu.Mod;
import adris.altoclef.scripting.ScriptResourceExtractor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Specialized frame for Lua script management
 * Provides file browser, script control, and management features
 * 
 * Created: 2025-01-10
 * @author Hearty
 */
public class ScriptFrame {
    public int x, y, width, height, dragX, dragY;
    public boolean dragging, extended;
    
    private final List<ScriptButton> scriptButtons;
    private final List<ScriptFolder> folders;
    private final Path scriptsDirectory;
    private ScriptButton hoveredButton;
    private ScriptButton selectedButton;
    private ScriptFolder hoveredFolder;
    private ScriptFolder selectedFolder;
    private boolean showNewScriptDialog;
    private String newScriptName = "";
    private boolean showDeleteConfirmDialog;
    private ScriptButton scriptToDelete;
    private boolean showNewFolderDialog;
    private String newFolderName = "";
    private boolean showContextMenu;
    private int contextMenuX, contextMenuY;
    private boolean contextMenuOnFolder;
    private long lastActionTime = 0;
    private static final long ACTION_COOLDOWN = 200; // 200ms cooldown between actions
    private long lastCharInputTime = 0;
    private static final long CHAR_INPUT_COOLDOWN = 100; // 100ms cooldown between character inputs
    
    // UI Constants
    private static final int BUTTON_HEIGHT = 15;
    private static final int NEW_SCRIPT_BUTTON_HEIGHT = 20;
    private static final Color FRAME_COLOR = new Color(128, 128, 128, 200);
    private static final Color HEADER_COLOR = Color.RED;
    private static final Color BORDER_COLOR = Color.BLACK;
    private static final Color HOVER_COLOR = new Color(255, 255, 255, 50);
    
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    
    public ScriptFrame(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.dragging = false;
        this.extended = true;
        
        this.scriptButtons = new ArrayList<>();
        this.folders = new ArrayList<>();
        this.scriptsDirectory = Paths.get("AltoClefLUA", "scripts");
        
        // Initialize the scripts directory
        initializeScriptsDirectory();
        
        // Load scripts and folders
        refreshScripts();
    }
    
    /**
     * Initialize the scripts directory structure and extract bundled scripts
     */
    private void initializeScriptsDirectory() {
        try {
            Files.createDirectories(scriptsDirectory);
            
            // Create default folders if they don't exist
            Path examplesDir = scriptsDirectory.resolve("examples");
            Path librariesDir = scriptsDirectory.resolve("libraries");
            Path userScriptsDir = scriptsDirectory.resolve("user_scripts");
            
            Files.createDirectories(examplesDir);
            Files.createDirectories(librariesDir);
            Files.createDirectories(userScriptsDir);
            
            // Extract bundled scripts if needed
            if (ScriptResourceExtractor.needsExtraction()) {
                System.out.println("Extracting bundled scripts...");
                ScriptResourceExtractor.extractBundledScripts();
                ScriptResourceExtractor.createDefaultUserScript();
            }
            
        } catch (IOException e) {
            System.err.println("Failed to initialize scripts directory: " + e.getMessage());
        }
    }
    
    /**
     * Refresh the script list from the file system
     */
    public void refreshScripts() {
        scriptButtons.clear();
        folders.clear();
        
        if (!Files.exists(scriptsDirectory)) {
            return;
        }
        
        try {
            loadScriptsFromDirectory(scriptsDirectory, 0);
        } catch (IOException e) {
            System.err.println("Failed to load scripts: " + e.getMessage());
        }
        
        updateButtonPositions();
    }
    
    /**
     * Recursively load scripts from directory
     */
    private void loadScriptsFromDirectory(Path directory, int indentLevel) throws IOException {
        try (Stream<Path> paths = Files.list(directory)) {
            List<Path> sortedPaths = paths.sorted(Comparator.comparing(path -> {
                // Folders first, then files
                if (Files.isDirectory(path)) return "0_" + path.getFileName().toString();
                return "1_" + path.getFileName().toString();
            })).toList();
            
            for (Path path : sortedPaths) {
                if (Files.isDirectory(path)) {
                    // Add folder
                    ScriptFolder folder = new ScriptFolder(path.getFileName().toString(), path, indentLevel, this);
                    folders.add(folder);
                    
                    // Recursively load scripts from subdirectory
                    if (folder.isExpanded()) {
                        loadScriptsFromDirectory(path, indentLevel + 1);
                    }
                    
                } else if (path.toString().endsWith(".lua")) {
                    // Add script file
                    ScriptButton button = new ScriptButton(path, this, 0, indentLevel);
                    scriptButtons.add(button);
                }
            }
        }
    }
    
    /**
     * Update button positions after changes
     */
    private void updateButtonPositions() {
        int currentOffset = height + NEW_SCRIPT_BUTTON_HEIGHT;
        
        // Position folders and scripts
        for (ScriptFolder folder : folders) {
            folder.offset = currentOffset;
            currentOffset += BUTTON_HEIGHT;
        }
        
        for (ScriptButton button : scriptButtons) {
            button.offset = currentOffset;
            currentOffset += BUTTON_HEIGHT;
        }
    }
    
    /**
     * Render the script frame
     */
    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        // Draw frame background
        matrices.fill(x, y, x + width, y + height, FRAME_COLOR.getRGB());
        
        // Draw border
        matrices.fill(x, y, x + width, y + 1, BORDER_COLOR.getRGB());
        matrices.fill(x, y, x + 1, y + height, BORDER_COLOR.getRGB());
        matrices.fill(x + width - 1, y, x + width, y + height, BORDER_COLOR.getRGB());
        matrices.fill(x, y + height - 1, x + width, y + height, BORDER_COLOR.getRGB());
        
        // Draw header (only the header bar, not the whole frame)
        matrices.fill(x, y, x + width, y + 15, HEADER_COLOR.getRGB());
        matrices.drawTextWithShadow(mc.textRenderer, "Scripts", x + 2, y + 2, -1);
        matrices.drawTextWithShadow(mc.textRenderer, extended ? "-" : "+", 
                                   x + width - 2 - mc.textRenderer.getWidth("+"), y + 2, -1);
        
        if (!extended) return;
        
        // Draw "New Script" button
        int newButtonY = y + height;
        boolean hoveringNewButton = isHoveringArea(mouseX, mouseY, x, newButtonY, width, NEW_SCRIPT_BUTTON_HEIGHT);
        
        Color newButtonColor = hoveringNewButton ? new Color(100, 200, 100, 200) : new Color(50, 150, 50, 200);
        matrices.fill(x, newButtonY, x + width, newButtonY + NEW_SCRIPT_BUTTON_HEIGHT, newButtonColor.getRGB());
        matrices.fill(x, newButtonY, x + width, newButtonY + 1, BORDER_COLOR.getRGB());
        matrices.fill(x, newButtonY + NEW_SCRIPT_BUTTON_HEIGHT - 1, x + width, newButtonY + NEW_SCRIPT_BUTTON_HEIGHT, BORDER_COLOR.getRGB());
        
        String newButtonText = "+ New Script";
        int textX = x + (width - mc.textRenderer.getWidth(newButtonText)) / 2;
        matrices.drawTextWithShadow(mc.textRenderer, newButtonText, textX, newButtonY + 6, -1);
        
        // Draw folders
        for (ScriptFolder folder : folders) {
            folder.render(matrices, mouseX, mouseY, delta);
        }
        
        // Draw script buttons
        hoveredButton = null;
        for (ScriptButton button : scriptButtons) {
            button.render(matrices, mouseX, mouseY, delta);
            if (button.isHovered(mouseX, mouseY)) {
                hoveredButton = button;
            }
        }
        
        // Draw hover actions for scripts (only if no dialogs are open)
        if (hoveredButton != null && !showNewScriptDialog && !showDeleteConfirmDialog) {
            renderHoverActions(matrices, mouseX, mouseY, hoveredButton);
        }
        
        // Draw context menu (render before dialogs but after everything else)
        if (showContextMenu) {
            renderContextMenu(matrices, mouseX, mouseY);
        }
        
        // Draw dialogs with full screen overlay (render last so they're on top)
        if (showNewScriptDialog) {
            renderDialogOverlay(matrices);
            renderNewScriptDialog(matrices, mouseX, mouseY);
        }
        
        if (showNewFolderDialog) {
            renderDialogOverlay(matrices);
            renderNewFolderDialog(matrices, mouseX, mouseY);
        }
        
        if (showDeleteConfirmDialog) {
            renderDialogOverlay(matrices);
            renderDeleteConfirmDialog(matrices, mouseX, mouseY);
        }
    }
    
    /**
     * Render hover actions (edit/delete buttons)
     */
    private void renderHoverActions(DrawContext matrices, int mouseX, int mouseY, ScriptButton button) {
        int buttonX = button.getX() + width - 60;
        int buttonY = button.getY();
        
        // Edit button
        matrices.fill(buttonX, buttonY, buttonX + 25, buttonY + BUTTON_HEIGHT, new Color(100, 100, 200, 200).getRGB());
        matrices.drawTextWithShadow(mc.textRenderer, "Edit", buttonX + 2, buttonY + 3, -1);
        
        // Delete button
        matrices.fill(buttonX + 30, buttonY, buttonX + 55, buttonY + BUTTON_HEIGHT, new Color(200, 100, 100, 200).getRGB());
        matrices.drawTextWithShadow(mc.textRenderer, "Del", buttonX + 32, buttonY + 3, -1);
    }
    
    /**
     * Render full screen overlay for dialogs
     */
    private void renderDialogOverlay(DrawContext matrices) {
        // Draw semi-transparent overlay over entire screen
        matrices.fill(0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(), 
                     new Color(0, 0, 0, 128).getRGB());
    }
    
    /**
     * Render new script creation dialog
     */
    private void renderNewScriptDialog(DrawContext matrices, int mouseX, int mouseY) {
        int dialogWidth = 200;
        int dialogHeight = 80;
        int dialogX = (mc.getWindow().getScaledWidth() - dialogWidth) / 2;
        int dialogY = (mc.getWindow().getScaledHeight() - dialogHeight) / 2;
        
        // Dialog background
        matrices.fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, new Color(60, 60, 60, 240).getRGB());
        matrices.fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + 1, BORDER_COLOR.getRGB());
        matrices.fill(dialogX, dialogY, dialogX + 1, dialogY + dialogHeight, BORDER_COLOR.getRGB());
        matrices.fill(dialogX + dialogWidth - 1, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, BORDER_COLOR.getRGB());
        matrices.fill(dialogX, dialogY + dialogHeight - 1, dialogX + dialogWidth, dialogY + dialogHeight, BORDER_COLOR.getRGB());
        
        // Dialog title
        matrices.drawTextWithShadow(mc.textRenderer, "Create New Script", dialogX + 5, dialogY + 5, -1);
        
        // Input field
        matrices.fill(dialogX + 5, dialogY + 20, dialogX + dialogWidth - 5, dialogY + 35, Color.WHITE.getRGB());
        matrices.drawTextWithShadow(mc.textRenderer, newScriptName, dialogX + 7, dialogY + 23, Color.BLACK.getRGB());
        
        // Create button
        matrices.fill(dialogX + 10, dialogY + 45, dialogX + 70, dialogY + 65, new Color(100, 200, 100, 200).getRGB());
        matrices.drawTextWithShadow(mc.textRenderer, "Create", dialogX + 25, dialogY + 52, -1);
        
        // Cancel button
        matrices.fill(dialogX + 80, dialogY + 45, dialogX + 140, dialogY + 65, new Color(200, 100, 100, 200).getRGB());
        matrices.drawTextWithShadow(mc.textRenderer, "Cancel", dialogX + 95, dialogY + 52, -1);
    }
    
    /**
     * Render delete confirmation dialog
     */
    private void renderDeleteConfirmDialog(DrawContext matrices, int mouseX, int mouseY) {
        if (scriptToDelete == null) return;
        
        int dialogWidth = 220;
        int dialogHeight = 90;
        int dialogX = (mc.getWindow().getScaledWidth() - dialogWidth) / 2;
        int dialogY = (mc.getWindow().getScaledHeight() - dialogHeight) / 2;
        
        // Dialog background
        matrices.fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, new Color(60, 60, 60, 240).getRGB());
        matrices.fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + 1, BORDER_COLOR.getRGB());
        matrices.fill(dialogX, dialogY, dialogX + 1, dialogY + dialogHeight, BORDER_COLOR.getRGB());
        matrices.fill(dialogX + dialogWidth - 1, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, BORDER_COLOR.getRGB());
        matrices.fill(dialogX, dialogY + dialogHeight - 1, dialogX + dialogWidth, dialogY + dialogHeight, BORDER_COLOR.getRGB());
        
        // Dialog title
        matrices.drawTextWithShadow(mc.textRenderer, "Delete Script?", dialogX + 5, dialogY + 5, Color.RED.getRGB());
        
        // Warning message
        String scriptName = scriptToDelete.getScriptName();
        matrices.drawTextWithShadow(mc.textRenderer, "Delete '" + scriptName + "'?", dialogX + 5, dialogY + 25, -1);
        matrices.drawTextWithShadow(mc.textRenderer, "This cannot be undone!", dialogX + 5, dialogY + 40, Color.YELLOW.getRGB());
        
        // Delete button (red)
        matrices.fill(dialogX + 10, dialogY + 55, dialogX + 80, dialogY + 75, new Color(200, 50, 50, 200).getRGB());
        matrices.drawTextWithShadow(mc.textRenderer, "DELETE", dialogX + 25, dialogY + 62, -1);
        
        // Cancel button (green)
        matrices.fill(dialogX + 90, dialogY + 55, dialogX + 160, dialogY + 75, new Color(50, 200, 50, 200).getRGB());
        matrices.drawTextWithShadow(mc.textRenderer, "Cancel", dialogX + 110, dialogY + 62, -1);
    }
    
    /**
     * Render context menu for right-click actions
     */
    private void renderContextMenu(DrawContext matrices, int mouseX, int mouseY) {
        int menuWidth = 120;
        int menuHeight = contextMenuOnFolder ? 100 : 80; // More options for folders
        
        // Adjust position if menu would go off screen
        int menuX = contextMenuX;
        int menuY = contextMenuY;
        
        if (menuX + menuWidth > mc.getWindow().getScaledWidth()) {
            menuX = mc.getWindow().getScaledWidth() - menuWidth - 5;
        }
        if (menuY + menuHeight > mc.getWindow().getScaledHeight()) {
            menuY = mc.getWindow().getScaledHeight() - menuHeight - 5;
        }
        
        // Menu background
        matrices.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, new Color(50, 50, 50, 240).getRGB());
        matrices.fill(menuX, menuY, menuX + menuWidth, menuY + 1, BORDER_COLOR.getRGB());
        matrices.fill(menuX, menuY, menuX + 1, menuY + menuHeight, BORDER_COLOR.getRGB());
        matrices.fill(menuX + menuWidth - 1, menuY, menuX + menuWidth, menuY + menuHeight, BORDER_COLOR.getRGB());
        matrices.fill(menuX, menuY + menuHeight - 1, menuX + menuWidth, menuY + menuHeight, BORDER_COLOR.getRGB());
        
        int itemHeight = 18;
        int currentY = menuY + 5;
        
        if (contextMenuOnFolder) {
            // Folder context menu options
            renderContextMenuItem(matrices, menuX, currentY, menuWidth, itemHeight, "New Script", mouseX, mouseY);
            currentY += itemHeight;
            renderContextMenuItem(matrices, menuX, currentY, menuWidth, itemHeight, "New Folder", mouseX, mouseY);
            currentY += itemHeight;
            renderContextMenuItem(matrices, menuX, currentY, menuWidth, itemHeight, "Rename", mouseX, mouseY);
            currentY += itemHeight;
            renderContextMenuItem(matrices, menuX, currentY, menuWidth, itemHeight, "Delete", mouseX, mouseY);
            currentY += itemHeight;
            renderContextMenuItem(matrices, menuX, currentY, menuWidth, itemHeight, "Refresh", mouseX, mouseY);
        } else {
            // General area context menu options
            renderContextMenuItem(matrices, menuX, currentY, menuWidth, itemHeight, "New Script", mouseX, mouseY);
            currentY += itemHeight;
            renderContextMenuItem(matrices, menuX, currentY, menuWidth, itemHeight, "New Folder", mouseX, mouseY);
            currentY += itemHeight;
            renderContextMenuItem(matrices, menuX, currentY, menuWidth, itemHeight, "Refresh", mouseX, mouseY);
            currentY += itemHeight;
            renderContextMenuItem(matrices, menuX, currentY, menuWidth, itemHeight, "Reset Layout", mouseX, mouseY);
        }
    }
    
    /**
     * Render a single context menu item
     */
    private void renderContextMenuItem(DrawContext matrices, int x, int y, int width, int height, String text, int mouseX, int mouseY) {
        boolean hovered = isHoveringArea(mouseX, mouseY, x, y, width, height);
        
        if (hovered) {
            matrices.fill(x + 2, y, x + width - 2, y + height, new Color(100, 100, 100, 200).getRGB());
        }
        
        matrices.drawTextWithShadow(mc.textRenderer, text, x + 5, y + 5, -1);
    }
    
    /**
     * Render new folder creation dialog
     */
    private void renderNewFolderDialog(DrawContext matrices, int mouseX, int mouseY) {
        int dialogWidth = 200;
        int dialogHeight = 80;
        int dialogX = (mc.getWindow().getScaledWidth() - dialogWidth) / 2;
        int dialogY = (mc.getWindow().getScaledHeight() - dialogHeight) / 2;
        
        // Dialog background
        matrices.fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, new Color(60, 60, 60, 240).getRGB());
        matrices.fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + 1, BORDER_COLOR.getRGB());
        matrices.fill(dialogX, dialogY, dialogX + 1, dialogY + dialogHeight, BORDER_COLOR.getRGB());
        matrices.fill(dialogX + dialogWidth - 1, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, BORDER_COLOR.getRGB());
        matrices.fill(dialogX, dialogY + dialogHeight - 1, dialogX + dialogWidth, dialogY + dialogHeight, BORDER_COLOR.getRGB());
        
        // Dialog title
        matrices.drawTextWithShadow(mc.textRenderer, "Create New Folder", dialogX + 5, dialogY + 5, -1);
        
        // Input field
        matrices.fill(dialogX + 5, dialogY + 20, dialogX + dialogWidth - 5, dialogY + 35, Color.WHITE.getRGB());
        matrices.drawTextWithShadow(mc.textRenderer, newFolderName, dialogX + 7, dialogY + 23, Color.BLACK.getRGB());
        
        // Create button
        matrices.fill(dialogX + 10, dialogY + 45, dialogX + 70, dialogY + 65, new Color(100, 200, 100, 200).getRGB());
        matrices.drawTextWithShadow(mc.textRenderer, "Create", dialogX + 25, dialogY + 52, -1);
        
        // Cancel button
        matrices.fill(dialogX + 80, dialogY + 45, dialogX + 140, dialogY + 65, new Color(200, 100, 100, 200).getRGB());
        matrices.drawTextWithShadow(mc.textRenderer, "Cancel", dialogX + 95, dialogY + 52, -1);
    }
    
    /**
     * Handle mouse clicks
     */
    public void mouseClicked(double mouseX, double mouseY, int button) {
        // Handle delete confirmation dialog first (highest priority)
        if (showDeleteConfirmDialog) {
            handleDeleteConfirmDialogClick(mouseX, mouseY, button);
            return;
        }
        
        // Handle new script dialog second priority
        if (showNewScriptDialog) {
            handleNewScriptDialogClick(mouseX, mouseY, button);
            return;
        }
        
        // Handle new folder dialog third priority
        if (showNewFolderDialog) {
            handleNewFolderDialogClick(mouseX, mouseY, button);
            return;
        }
        
        // Handle context menu fourth priority
        if (showContextMenu) {
            handleContextMenuClick(mouseX, mouseY, button);
            return;
        }
        
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                dragging = true;
                dragX = (int) (mouseX - x);
                dragY = (int) (mouseY - y);
            } else if (button == 1) {
                extended = !extended;
                updateButtonPositions();
            }
            return;
        }
        
        if (!extended) return;
        
        // Check "New Script" button
        int newButtonY = y + height;
        if (isHoveringArea(mouseX, mouseY, x, newButtonY, width, NEW_SCRIPT_BUTTON_HEIGHT)) {
            if (button == 0) {
                showNewScriptDialog = true;
                newScriptName = "";
            }
            return;
        }
        
        // Check folder clicks
        for (ScriptFolder folder : folders) {
            if (folder.isHovered(mouseX, mouseY)) {
                if (button == 0) {
                    folder.toggleExpanded();
                    refreshScripts();
                } else if (button == 1) {
                    // Right click - show context menu
                    showContextMenu = true;
                    contextMenuX = (int) mouseX;
                    contextMenuY = (int) mouseY;
                    contextMenuOnFolder = true;
                    selectedFolder = folder;
                    System.out.println("🖱️ Right-clicked folder: " + folder.getFolderName());
                }
                return;
            }
        }
        
        // Check script button clicks
        for (ScriptButton scriptButton : scriptButtons) {
            if (scriptButton.isHovered(mouseX, mouseY)) {
                System.out.println("🖱️ Script button clicked: " + scriptButton.getScriptName());
                System.out.println("Mouse button: " + button + " (0=left, 1=right)");
                
                if (button == 0) {
                    // Check for hover action clicks first
                    if (handleHoverActionClick(mouseX, mouseY, scriptButton)) {
                        System.out.println("🎯 Hover action handled");
                        return;
                    }
                    // Toggle script
                    System.out.println("🔄 Calling toggleScript() for: " + scriptButton.getScriptName());
                    scriptButton.toggleScript();
                } else if (button == 1) {
                    // Right click - context menu
                    selectedButton = scriptButton;
                    System.out.println("🖱️ Right-clicked script: " + scriptButton.getScriptName());
                }
                return;
            }
        }
    }
    
    /**
     * Handle new script dialog clicks
     */
    private void handleNewScriptDialogClick(double mouseX, double mouseY, int button) {
        int dialogWidth = 200;
        int dialogHeight = 80;
        int dialogX = (mc.getWindow().getScaledWidth() - dialogWidth) / 2;
        int dialogY = (mc.getWindow().getScaledHeight() - dialogHeight) / 2;
        
        // Create button
        if (isHoveringArea(mouseX, mouseY, dialogX + 10, dialogY + 45, 60, 20)) {
            createNewScript();
            showNewScriptDialog = false;
        }
        // Cancel button
        else if (isHoveringArea(mouseX, mouseY, dialogX + 80, dialogY + 45, 60, 20)) {
            showNewScriptDialog = false;
        }
        // Input field - for now just close dialog on click outside
        else if (!isHoveringArea(mouseX, mouseY, dialogX, dialogY, dialogWidth, dialogHeight)) {
            showNewScriptDialog = false;
        }
    }
    
    /**
     * Handle delete confirmation dialog clicks
     */
    private void handleDeleteConfirmDialogClick(double mouseX, double mouseY, int button) {
        if (scriptToDelete == null) return;
        
        int dialogWidth = 220;
        int dialogHeight = 90;
        int dialogX = (mc.getWindow().getScaledWidth() - dialogWidth) / 2;
        int dialogY = (mc.getWindow().getScaledHeight() - dialogHeight) / 2;
        
        // Delete button
        if (isHoveringArea(mouseX, mouseY, dialogX + 10, dialogY + 55, 70, 20)) {
            // Actually delete the script
            try {
                if (scriptToDelete.isEnabled()) {
                    scriptToDelete.toggleScript(); // Disable first
                }
                
                // Delete the file
                Files.deleteIfExists(scriptToDelete.getScriptPath());
                System.out.println("Deleted script: " + scriptToDelete.getScriptName());
                
                // Refresh the script list
                refreshScripts();
                
            } catch (Exception e) {
                System.err.println("Failed to delete script: " + e.getMessage());
            }
            
            showDeleteConfirmDialog = false;
            scriptToDelete = null;
        }
        // Cancel button
        else if (isHoveringArea(mouseX, mouseY, dialogX + 90, dialogY + 55, 70, 20)) {
            showDeleteConfirmDialog = false;
            scriptToDelete = null;
        }
        // Click outside dialog - cancel
        else if (!isHoveringArea(mouseX, mouseY, dialogX, dialogY, dialogWidth, dialogHeight)) {
            showDeleteConfirmDialog = false;
            scriptToDelete = null;
        }
    }
    
    /**
     * Handle hover action clicks (edit/delete)
     */
    private boolean handleHoverActionClick(double mouseX, double mouseY, ScriptButton scriptButton) {
        // Debounce rapid clicks
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastActionTime < ACTION_COOLDOWN) {
            return true; // Consume click but don't process
        }
        
        int buttonX = scriptButton.getX() + width - 60;
        int buttonY = scriptButton.getY();
        
        // Edit button
        if (isHoveringArea(mouseX, mouseY, buttonX, buttonY, 25, BUTTON_HEIGHT)) {
            lastActionTime = currentTime;
            openScriptEditor(scriptButton);
            return true;
        }
        
        // Delete button
        if (isHoveringArea(mouseX, mouseY, buttonX + 30, buttonY, 25, BUTTON_HEIGHT)) {
            lastActionTime = currentTime;
            showDeleteConfirmation(scriptButton);
            return true;
        }
        
        return false;
    }
    
    /**
     * Handle new folder dialog clicks
     */
    private void handleNewFolderDialogClick(double mouseX, double mouseY, int button) {
        int dialogWidth = 200;
        int dialogHeight = 80;
        int dialogX = (mc.getWindow().getScaledWidth() - dialogWidth) / 2;
        int dialogY = (mc.getWindow().getScaledHeight() - dialogHeight) / 2;
        
        // Create button
        if (isHoveringArea(mouseX, mouseY, dialogX + 10, dialogY + 45, 60, 20)) {
            createNewFolder();
            showNewFolderDialog = false;
        }
        // Cancel button
        else if (isHoveringArea(mouseX, mouseY, dialogX + 80, dialogY + 45, 60, 20)) {
            showNewFolderDialog = false;
        }
        // Click outside dialog - cancel
        else if (!isHoveringArea(mouseX, mouseY, dialogX, dialogY, dialogWidth, dialogHeight)) {
            showNewFolderDialog = false;
        }
    }
    
    /**
     * Handle context menu clicks
     */
    private void handleContextMenuClick(double mouseX, double mouseY, int button) {
        int menuWidth = 120;
        int menuHeight = contextMenuOnFolder ? 100 : 80;
        
        // Adjust position if menu would go off screen
        int menuX = contextMenuX;
        int menuY = contextMenuY;
        
        if (menuX + menuWidth > mc.getWindow().getScaledWidth()) {
            menuX = mc.getWindow().getScaledWidth() - menuWidth - 5;
        }
        if (menuY + menuHeight > mc.getWindow().getScaledHeight()) {
            menuY = mc.getWindow().getScaledHeight() - menuHeight - 5;
        }
        
        int itemHeight = 18;
        int currentY = menuY + 5;
        
        if (contextMenuOnFolder) {
            // Folder context menu options
            if (isHoveringArea(mouseX, mouseY, menuX, currentY, menuWidth, itemHeight)) {
                // New Script in folder
                showNewScriptDialog = true;
                newScriptName = "";
                showContextMenu = false;
                return;
            }
            currentY += itemHeight;
            
            if (isHoveringArea(mouseX, mouseY, menuX, currentY, menuWidth, itemHeight)) {
                // New Folder in folder
                showNewFolderDialog = true;
                newFolderName = "";
                showContextMenu = false;
                return;
            }
            currentY += itemHeight;
            
            if (isHoveringArea(mouseX, mouseY, menuX, currentY, menuWidth, itemHeight)) {
                // Rename folder
                System.out.println("Rename folder: " + (selectedFolder != null ? selectedFolder.getFolderName() : "null"));
                showContextMenu = false;
                return;
            }
            currentY += itemHeight;
            
            if (isHoveringArea(mouseX, mouseY, menuX, currentY, menuWidth, itemHeight)) {
                // Delete folder
                System.out.println("Delete folder: " + (selectedFolder != null ? selectedFolder.getFolderName() : "null"));
                showContextMenu = false;
                return;
            }
            currentY += itemHeight;
            
            if (isHoveringArea(mouseX, mouseY, menuX, currentY, menuWidth, itemHeight)) {
                // Refresh
                refreshScripts();
                showContextMenu = false;
                return;
            }
        } else {
            // General area context menu options
            if (isHoveringArea(mouseX, mouseY, menuX, currentY, menuWidth, itemHeight)) {
                // New Script
                showNewScriptDialog = true;
                newScriptName = "";
                showContextMenu = false;
                return;
            }
            currentY += itemHeight;
            
            if (isHoveringArea(mouseX, mouseY, menuX, currentY, menuWidth, itemHeight)) {
                // New Folder
                showNewFolderDialog = true;
                newFolderName = "";
                showContextMenu = false;
                return;
            }
            currentY += itemHeight;
            
            if (isHoveringArea(mouseX, mouseY, menuX, currentY, menuWidth, itemHeight)) {
                // Refresh
                refreshScripts();
                showContextMenu = false;
                return;
            }
            currentY += itemHeight;
            
            if (isHoveringArea(mouseX, mouseY, menuX, currentY, menuWidth, itemHeight)) {
                // Reset Layout
                System.out.println("Reset layout requested");
                showContextMenu = false;
                return;
            }
        }
        
        // Click outside menu - close
        showContextMenu = false;
    }
    
    /**
     * Create a new script file
     */
    private void createNewScript() {
        if (newScriptName.isEmpty()) return;
        
        try {
            // Ensure .lua extension
            String fileName = newScriptName.endsWith(".lua") ? newScriptName : newScriptName + ".lua";
            Path scriptPath = scriptsDirectory.resolve("user_scripts").resolve(fileName);
            
            // Create script with template
            String template = generateScriptTemplate(newScriptName);
            Files.writeString(scriptPath, template);
            
            // Refresh the script list
            refreshScripts();
            
        } catch (IOException e) {
            System.err.println("Failed to create script: " + e.getMessage());
        }
    }
    
    /**
     * Create a new folder
     */
    private void createNewFolder() {
        if (newFolderName.isEmpty()) return;
        
        try {
            // Determine parent directory
            Path parentDir = scriptsDirectory;
            if (selectedFolder != null) {
                parentDir = selectedFolder.getFolderPath();
            }
            
            // Create the new folder
            Path newFolderPath = parentDir.resolve(newFolderName);
            
            if (Files.exists(newFolderPath)) {
                System.err.println("Folder already exists: " + newFolderName);
                return;
            }
            
            Files.createDirectories(newFolderPath);
            System.out.println("✅ Created new folder: " + newFolderPath);
            
            // Refresh the script list to show the new folder
            refreshScripts();
            
        } catch (IOException e) {
            System.err.println("❌ Failed to create folder '" + newFolderName + "': " + e.getMessage());
        }
    }
    
    /**
     * Generate a template for new scripts
     */
    private String generateScriptTemplate(String scriptName) {
        return String.format("""
            --[[
            @name %s
            @description A new Lua script for AltoClef
            @version 1.0.0
            @author %s
            @category Utility
            ]]--
            
            local script = {}
            
            function onLoad()
                AltoClef.log("Script '%s' loaded!")
            end
            
            function onTick()
                -- Your script logic here
            end
            
            function onEnable()
                AltoClef.log("Script '%s' enabled")
            end
            
            function onDisable()
                AltoClef.log("Script '%s' disabled")
            end
            
            function onCleanup()
                AltoClef.log("Script '%s' cleaning up")
            end
            
            -- Your custom functions here
            
            return script
            """, scriptName, "Player", scriptName, scriptName, scriptName, scriptName);
    }
    
    /**
     * Open script editor
     */
    private void openScriptEditor(ScriptButton scriptButton) {
        System.out.println("=== SCRIPT EDITOR DEBUG START ===");
        System.out.println("Attempting to open editor for: " + scriptButton.getScriptName());
        System.out.println("Script path: " + scriptButton.getScriptPath());
        System.out.println("File exists: " + Files.exists(scriptButton.getScriptPath()));
        
        try {
            if (Desktop.isDesktopSupported()) {
                System.out.println("✅ Desktop is supported");
                Desktop desktop = Desktop.getDesktop();
                
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    System.out.println("✅ OPEN action is supported");
                    desktop.open(scriptButton.getScriptPath().toFile());
                    System.out.println("✅ Successfully opened script editor for: " + scriptButton.getScriptName());
                } else {
                    System.err.println("❌ OPEN action not supported on this system");
                    // Fallback: try to open with system command
                    openWithSystemCommand(scriptButton.getScriptPath());
                }
            } else {
                System.err.println("❌ Desktop not supported on this system");
                // Fallback: try to open with system command
                openWithSystemCommand(scriptButton.getScriptPath());
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to open script editor: " + e.getMessage());
            e.printStackTrace();
            // Try fallback method
            openWithSystemCommand(scriptButton.getScriptPath());
        }
        
        System.out.println("=== SCRIPT EDITOR DEBUG END ===");
        System.out.println();
    }
    
    /**
     * Fallback method to open script with system command
     */
    private void openWithSystemCommand(Path scriptPath) {
        System.out.println("🔄 Trying fallback system command...");
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {
                // Windows
                pb = new ProcessBuilder("cmd", "/c", "start", "", scriptPath.toString());
                System.out.println("Using Windows command: cmd /c start \"\" \"" + scriptPath + "\"");
            } else if (os.contains("mac")) {
                // macOS
                pb = new ProcessBuilder("open", scriptPath.toString());
                System.out.println("Using macOS command: open \"" + scriptPath + "\"");
            } else {
                // Linux/Unix
                pb = new ProcessBuilder("xdg-open", scriptPath.toString());
                System.out.println("Using Linux command: xdg-open \"" + scriptPath + "\"");
            }
            
            Process process = pb.start();
            System.out.println("✅ Fallback command executed successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Fallback method also failed: " + e.getMessage());
            System.err.println("Manual edit required - Script location: " + scriptPath);
        }
    }
    
    /**
     * Show delete confirmation dialog
     */
    private void showDeleteConfirmation(ScriptButton scriptButton) {
        scriptToDelete = scriptButton;
        showDeleteConfirmDialog = true;
        System.out.println("Showing delete confirmation for: " + scriptButton.getScriptName());
    }
    
    /**
     * Handle mouse release
     */
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
        }
        
        for (ScriptButton scriptButton : scriptButtons) {
            scriptButton.mouseReleased(mouseX, mouseY, button);
        }
    }
    
    /**
     * Update position during drag
     */
    public void updatePosition(double mouseX, double mouseY) {
        if (dragging) {
            x = (int) mouseX - dragX;
            y = (int) mouseY - dragY;
        }
    }
    
    /**
     * Update position without mouse
     */
    public void updatePositionNoMouse(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Check if mouse is hovering over frame header
     */
    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }
    
    /**
     * Check if mouse is hovering over specific area
     */
    private boolean isHoveringArea(double mouseX, double mouseY, int areaX, int areaY, int areaWidth, int areaHeight) {
        return mouseX >= areaX && mouseX <= areaX + areaWidth && 
               mouseY >= areaY && mouseY <= areaY + areaHeight;
    }
    
    /**
     * Handle keyboard input for dialogs (key presses only)
     */
    public void handleKeyPressed(int keyCode, char character) {
        // Handle new script dialog input
        if (showNewScriptDialog && !showDeleteConfirmDialog && !showNewFolderDialog) {
            if (keyCode == 259) { // Backspace
                if (!newScriptName.isEmpty()) {
                    newScriptName = newScriptName.substring(0, newScriptName.length() - 1);
                }
            } else if (keyCode == 257 || keyCode == 335) { // Enter
                createNewScript();
                showNewScriptDialog = false;
            } else if (keyCode == 256) { // Escape
                showNewScriptDialog = false;
            }
        }
        // Handle new folder dialog input
        else if (showNewFolderDialog && !showDeleteConfirmDialog && !showNewScriptDialog) {
            if (keyCode == 259) { // Backspace
                if (!newFolderName.isEmpty()) {
                    newFolderName = newFolderName.substring(0, newFolderName.length() - 1);
                }
            } else if (keyCode == 257 || keyCode == 335) { // Enter
                createNewFolder();
                showNewFolderDialog = false;
            } else if (keyCode == 256) { // Escape
                showNewFolderDialog = false;
            }
        }
        // Handle delete confirmation dialog input
        else if (showDeleteConfirmDialog) {
            if (keyCode == 256) { // Escape - cancel delete
                showDeleteConfirmDialog = false;
                scriptToDelete = null;
            }
        }
        // Handle context menu input
        else if (showContextMenu) {
            if (keyCode == 256) { // Escape - close context menu
                showContextMenu = false;
            }
        }
    }
    
    /**
     * Handle character input for dialogs (with debouncing)
     */
    public void handleCharTyped(char character) {
        // Debounce character input to prevent multiple registrations
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCharInputTime < CHAR_INPUT_COOLDOWN) {
            return;
        }
        
        // Handle new script dialog character input
        if (showNewScriptDialog && !showDeleteConfirmDialog && !showNewFolderDialog) {
            if (Character.isLetterOrDigit(character) || character == '_' || character == '-') {
                if (newScriptName.length() < 50) {
                    newScriptName += character;
                    lastCharInputTime = currentTime;
                }
            }
        }
        // Handle new folder dialog character input
        else if (showNewFolderDialog && !showDeleteConfirmDialog && !showNewScriptDialog) {
            if (Character.isLetterOrDigit(character) || character == '_' || character == '-' || character == ' ') {
                if (newFolderName.length() < 50) {
                    newFolderName += character;
                    lastCharInputTime = currentTime;
                }
            }
        }
    }
} 