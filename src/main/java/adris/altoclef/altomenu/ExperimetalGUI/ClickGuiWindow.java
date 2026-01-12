package adris.altoclef.altomenu.ExperimetalGUI;

import adris.altoclef.altomenu.Mod;
import adris.altoclef.altomenu.managers.ModuleManager;
import adris.altoclef.altomenu.settings.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class ClickGuiWindow {

    private JFrame frame;
    private JPanel modulesContainer;
    private boolean open = false; // <-- flag to track open state

    public void open() {
        if (frame != null && frame.isVisible()) {
            return; // already open
        }

        frame = new JFrame("ClickGUI");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.BLACK);

        // Update flag when window closes
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                open = true;
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                open = false;
            }

            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                open = false;
            }
        });

        // Top panel for categories
        JPanel categoriesPanel = new JPanel();
        categoriesPanel.setLayout(new GridLayout(1, Mod.Category.values().length, 10, 0));
        categoriesPanel.setBackground(Color.BLACK);
        categoriesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Container for all modules
        modulesContainer = new JPanel();
        modulesContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        modulesContainer.setBackground(Color.BLACK);

        // Populate category buttons
        for (Mod.Category category : Mod.Category.values()) {
            JButton categoryButton = new JButton(category.name());
            categoryButton.setBackground(Color.DARK_GRAY);
            categoryButton.setForeground(Color.RED);
            categoryButton.setFocusPainted(false);

            categoryButton.addActionListener(e -> filterModulesByCategory(category));

            categoriesPanel.add(categoryButton);
        }

        // Populate all modules
        for (Mod mod : ModuleManager.INSTANCE.getModules()) {
            JPanel wrapper = new JPanel();
            wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
            wrapper.setBackground(Color.DARK_GRAY);
            wrapper.setBorder(BorderFactory.createLineBorder(Color.RED));

            JToggleButton toggle = new JToggleButton(mod.getName());
            toggle.setBackground(Color.BLACK);
            toggle.setForeground(Color.RED);
            toggle.setFocusPainted(false);
            toggle.setAlignmentX(Component.CENTER_ALIGNMENT);

            toggle.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        mod.toggle();
                        toggle.setSelected(mod.isEnabled());
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        openSettingsPopup(mod);
                    }
                }
            });

            wrapper.add(toggle);
            modulesContainer.add(wrapper);
        }

        frame.add(categoriesPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(modulesContainer), BorderLayout.CENTER);
        frame.setVisible(true);
        open = true;
    }

    public boolean isOpen() {
        return open;
    }

    private void filterModulesByCategory(Mod.Category category) {
        Component[] components = modulesContainer.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel wrapper) {
                JToggleButton toggle = (JToggleButton) wrapper.getComponent(0);
                Mod mod = ModuleManager.INSTANCE.getModuleByName(toggle.getText());
                wrapper.setVisible(mod.getCategory() == category);
            }
        }
        modulesContainer.revalidate();
        modulesContainer.repaint();
    }

    private void openSettingsPopup(Mod mod) {
        JFrame settingsFrame = new JFrame(mod.getName() + " Settings");
        settingsFrame.setSize(300, 400);
        settingsFrame.setLocationRelativeTo(frame);
        settingsFrame.setLayout(new BoxLayout(settingsFrame.getContentPane(), BoxLayout.Y_AXIS));
        settingsFrame.getContentPane().setBackground(Color.BLACK);

        for (Setting setting : mod.getSettings()) {
            if (!setting.isVisible()) continue;

            if (setting instanceof BooleanSetting bs) {
                JCheckBox cb = new JCheckBox(setting.getName(), bs.isEnabled());
                cb.setBackground(Color.BLACK);
                cb.setForeground(Color.RED);
                cb.addActionListener(e -> bs.setEnabled(cb.isSelected()));
                settingsFrame.add(cb);
            } else if (setting instanceof NumberSetting ns) {
                JLabel label = new JLabel(setting.getName() + ": " + ns.getValue());
                label.setForeground(Color.RED);
                JSlider slider = new JSlider((int) ns.getMin(), (int) ns.getMax(), (int) ns.getValue());
                slider.setBackground(Color.BLACK);
                slider.setForeground(Color.RED);
                slider.addChangeListener(e -> {
                    ns.setValue(slider.getValue());
                    label.setText(setting.getName() + ": " + ns.getValue());
                });
                settingsFrame.add(label);
                settingsFrame.add(slider);
            } else if (setting instanceof ModeSetting ms) {
                JComboBox<String> combo = new JComboBox<>(ms.getModes().toArray(new String[0]));
                combo.setSelectedItem(ms.getMode());
                combo.setBackground(Color.BLACK);
                combo.setForeground(Color.RED);
                combo.addActionListener(e -> ms.setMode((String) combo.getSelectedItem()));
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBackground(Color.BLACK);
                JLabel lbl = new JLabel(setting.getName());
                lbl.setForeground(Color.RED);
                panel.add(lbl, BorderLayout.WEST);
                panel.add(combo, BorderLayout.CENTER);
                settingsFrame.add(panel);
            }
        }

        settingsFrame.setVisible(true);
    }
}

