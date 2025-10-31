package adris.altoclef.altomenu.command.impl;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.command.ChatUtils;
import adris.altoclef.altomenu.command.Command;
import adris.altoclef.altomenu.config.ConfigManager;
import adris.altoclef.altomenu.managers.ModuleManager;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class BindCommand extends Command {

    private static final Map<String, Integer> KEY_MAP = new HashMap<>();

    static {
        // Letters manually
        KEY_MAP.put("A", GLFW.GLFW_KEY_A);
        KEY_MAP.put("B", GLFW.GLFW_KEY_B);
        KEY_MAP.put("C", GLFW.GLFW_KEY_C);
        KEY_MAP.put("D", GLFW.GLFW_KEY_D);
        KEY_MAP.put("E", GLFW.GLFW_KEY_E);
        KEY_MAP.put("F", GLFW.GLFW_KEY_F);
        KEY_MAP.put("G", GLFW.GLFW_KEY_G);
        KEY_MAP.put("H", GLFW.GLFW_KEY_H);
        KEY_MAP.put("I", GLFW.GLFW_KEY_I);
        KEY_MAP.put("J", GLFW.GLFW_KEY_J);
        KEY_MAP.put("K", GLFW.GLFW_KEY_K);
        KEY_MAP.put("L", GLFW.GLFW_KEY_L);
        KEY_MAP.put("M", GLFW.GLFW_KEY_M);
        KEY_MAP.put("N", GLFW.GLFW_KEY_N);
        KEY_MAP.put("O", GLFW.GLFW_KEY_O);
        KEY_MAP.put("P", GLFW.GLFW_KEY_P);
        KEY_MAP.put("Q", GLFW.GLFW_KEY_Q);
        KEY_MAP.put("R", GLFW.GLFW_KEY_R);
        KEY_MAP.put("S", GLFW.GLFW_KEY_S);
        KEY_MAP.put("T", GLFW.GLFW_KEY_T);
        KEY_MAP.put("U", GLFW.GLFW_KEY_U);
        KEY_MAP.put("V", GLFW.GLFW_KEY_V);
        KEY_MAP.put("W", GLFW.GLFW_KEY_W);
        KEY_MAP.put("X", GLFW.GLFW_KEY_X);
        KEY_MAP.put("Y", GLFW.GLFW_KEY_Y);
        KEY_MAP.put("Z", GLFW.GLFW_KEY_Z);

        // Numbers
        for (int i = 0; i <= 9; i++) KEY_MAP.put(String.valueOf(i), GLFW.GLFW_KEY_0 + i);

        // Function keys
        for (int i = 1; i <= 12; i++) KEY_MAP.put("F" + i, GLFW.GLFW_KEY_F1 + (i - 1));

        // Common keys
        KEY_MAP.put("SPACE", GLFW.GLFW_KEY_SPACE);
        KEY_MAP.put("TAB", GLFW.GLFW_KEY_TAB);
        KEY_MAP.put("SHIFT", GLFW.GLFW_KEY_LEFT_SHIFT);
        KEY_MAP.put("CTRL", GLFW.GLFW_KEY_LEFT_CONTROL);
        KEY_MAP.put("ALT", GLFW.GLFW_KEY_LEFT_ALT);
        KEY_MAP.put("ENTER", GLFW.GLFW_KEY_ENTER);
        KEY_MAP.put("BACKSPACE", GLFW.GLFW_KEY_BACKSPACE);
        KEY_MAP.put("ESC", GLFW.GLFW_KEY_ESCAPE);
        KEY_MAP.put("CAPS", GLFW.GLFW_KEY_CAPS_LOCK);
        KEY_MAP.put("UP", GLFW.GLFW_KEY_UP);
        KEY_MAP.put("DOWN", GLFW.GLFW_KEY_DOWN);
        KEY_MAP.put("LEFT", GLFW.GLFW_KEY_LEFT);
        KEY_MAP.put("RIGHT", GLFW.GLFW_KEY_RIGHT);
        KEY_MAP.put("DELETE", GLFW.GLFW_KEY_DELETE);
        KEY_MAP.put("INSERT", GLFW.GLFW_KEY_INSERT);
        KEY_MAP.put("HOME", GLFW.GLFW_KEY_HOME);
        KEY_MAP.put("END", GLFW.GLFW_KEY_END);
        KEY_MAP.put("PAGEUP", GLFW.GLFW_KEY_PAGE_UP);
        KEY_MAP.put("PAGEDOWN", GLFW.GLFW_KEY_PAGE_DOWN);
    }

    public BindCommand() {
        super("bind", "Binds a key to a module", "setbind");
    }

    @Override
    public void onCmd(String message, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("bind")) {
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);
            args = newArgs;
        }

        if (args.length < 2) {
            ChatUtils.addChatMessage("Usage: $bind <module> <key>");
            return;
        }

        String modName = args[0];
        String keyName = args[1].toUpperCase();

        Mod mod = ModuleManager.INSTANCE.getModuleByName(modName);
        if (mod == null) {
            ChatUtils.addChatMessage("Unknown module: " + modName);
            return;
        }

        // Handle ~ as "unbind"
        if (keyName.equals("~")) {
            mod.setKey(0);
            ChatUtils.addChatMessage("Unbound " + modName);
        } else {
            Integer keyCode = KEY_MAP.get(keyName);
            if (keyCode == null) {
                ChatUtils.addChatMessage("Invalid key: " + keyName);
                return;
            }
            mod.setKey(keyCode);
            ChatUtils.addChatMessage("Bound " + modName + " to key: " + keyName);
        }

        // Save updated config immediately
        try {
            ConfigManager.saveConfig();
        } catch (Exception e) {
            ChatUtils.addChatMessage("Failed to save config: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
