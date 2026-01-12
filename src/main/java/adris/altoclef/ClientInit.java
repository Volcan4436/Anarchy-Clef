package adris.altoclef;


import adris.altoclef.altomenu.ExperimetalGUI.ClickGuiWindow;
import net.fabricmc.api.ClientModInitializer;

import javax.swing.*;
import java.awt.*;

public class ClientInit implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Force headless=false immediately
        System.setProperty("java.awt.headless", "false");

        // Spawn a thread BEFORE Minecraft’s LWJGL init locks display
        new Thread(() -> {
            try {
/*                // Give Minecraft a small delay if needed
                Thread.sleep(500); // optional*/

                // Schedule Swing on EDT
                SwingUtilities.invokeLater(() -> {
                    ClickGuiWindow window = new ClickGuiWindow();
                    window.open();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "ClickGUI-Thread").start();
        System.out.println("[MyMod] ClientModInitializer loaded");
    }
}
