package com.ikingsnipe.casino.gui;

import com.ikingsnipe.casino.models.CasinoConfig;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.function.Consumer;

public class CasinoGUI extends JFrame {
    private final CasinoConfig config;
    private final Consumer<Boolean> onFinish;

    public CasinoGUI(CasinoConfig config, Consumer<Boolean> onFinish) {
        this.config = config;
        this.onFinish = onFinish;

        setTitle("SnipesScripts Enterprise v8.0");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(new Color(25, 25, 30));
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JLabel header = new JLabel("SnipesScripts Enterprise");
        header.setFont(new Font("Verdana", Font.BOLD, 22));
        header.setForeground(new Color(255, 215, 0));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        main.add(header);
        main.add(Box.createRigidArea(new Dimension(0, 15)));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(40, 40, 45));
        tabs.setForeground(Color.WHITE);

        // --- General Tab ---
        JPanel generalTab = createTabPanel();
        generalTab.add(createSection("Core Settings", new Component[]{
            createLabel("Min Bet:"), createTextField(String.valueOf(config.minBet), s -> config.minBet = Long.parseLong(s)),
            createLabel("Default Game:"), createComboBox(new String[]{"craps", "dice", "flower"}, config.defaultGame, s -> config.defaultGame = s)
        }));
        tabs.addTab("General", generalTab);

        // --- ChasingCraps Tab ---
        JPanel crapsTab = createTabPanel();
        crapsTab.add(createSection("Craps Rules", new Component[]{
            createLabel("Win Numbers:"), createLabel("7, 9, 12"),
            createLabel("Standard Payout:"), createLabel("x3.0")
        }));
        crapsTab.add(createSection("Chasing Logic", new Component[]{
            createCheckbox("Enable B2B (Chasing)", config.enableB2B, b -> config.enableB2B = b),
            createLabel("B2B Multiplier: x9.0"),
            createLabel("Prediction Multiplier: x12.0")
        }));
        tabs.addTab("ChasingCraps", crapsTab);

        // --- Safety & CC Tab ---
        JPanel safetyTab = createTabPanel();
        safetyTab.add(createSection("Clan Chat Integration", new Component[]{
            createCheckbox("Notify Clan on Trade", config.notifyClanOnTrade, b -> config.notifyClanOnTrade = b),
            createLabel("CC Name:"), createTextField(config.clanChatName, s -> config.clanChatName = s)
        }));
        safetyTab.add(createSection("Trade Safety", new Component[]{
            createCheckbox("Auto Accept Trades", config.autoAcceptTrades, b -> config.autoAcceptTrades = b),
            createLabel("Stability Check: Enabled")
        }));
        tabs.addTab("Safety & CC", safetyTab);

        main.add(tabs);
        main.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton startBtn = new JButton("LAUNCH SNIPESSCRIPTS");
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.setBackground(new Color(0, 180, 0));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFont(new Font("Arial", Font.BOLD, 16));
        startBtn.setFocusPainted(false);
        startBtn.addActionListener(e -> {
            onFinish.accept(true);
            dispose();
        });
        main.add(startBtn);

        add(main);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createTabPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(35, 35, 40));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        return p;
    }

    private JPanel createSection(String title, Component[] components) {
        JPanel p = new JPanel(new GridLayout(0, 2, 10, 10));
        p.setBackground(new Color(45, 45, 50));
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleColor(new Color(200, 200, 200));
        p.setBorder(BorderFactory.createCompoundBorder(border, new EmptyBorder(5, 5, 5, 5)));
        for (Component c : components) p.add(c);
        return p;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        return l;
    }

    private JTextField createTextField(String text, Consumer<String> onChange) {
        JTextField f = new JTextField(text);
        f.addActionListener(e -> onChange.accept(f.getText()));
        return f;
    }

    private JCheckBox createCheckbox(String text, boolean selected, Consumer<Boolean> onChange) {
        JCheckBox c = new JCheckBox(text, selected);
        c.setForeground(Color.WHITE);
        c.setOpaque(false);
        c.addActionListener(e -> onChange.accept(c.isSelected()));
        return c;
    }

    private JComboBox<String> createComboBox(String[] items, String selected, Consumer<String> onChange) {
        JComboBox<String> b = new JComboBox<>(items);
        b.setSelectedItem(selected);
        b.addActionListener(e -> onChange.accept((String) b.getSelectedItem()));
        return b;
    }
}
