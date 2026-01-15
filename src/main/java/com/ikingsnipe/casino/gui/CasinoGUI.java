package com.ikingsnipe.casino.gui;

import com.ikingsnipe.casino.models.CasinoConfig;


import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.function.Consumer;

public class CasinoGUI extends JFrame {
    private final CasinoConfig config;
    private final Consumer<Boolean> onFinish;

    public CasinoGUI(CasinoConfig config, Consumer<Boolean> onFinish) {
        this.config = config;
        this.onFinish = onFinish;

        setTitle("snipes♧scripts Enterprise");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(new Color(30, 30, 35));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTabbedPane tabs = new JTabbedPane();
        
        // --- General Tab ---
        JPanel generalTab = createTabPanel();
        generalTab.add(createBettingPanel(config));
        tabs.addTab("General", generalTab);

        // --- Games Tab ---
        tabs.addTab("Games", createGamesTab(config));
        
        // --- Advertising Tab ---
        tabs.addTab("Advertising", createAdTab(config));

        // --- Enterprise Tab ---
        JPanel enterpriseTab = createTabPanel();
        enterpriseTab.add(createJackpotPanel(config));
        enterpriseTab.add(createMulingPanel(config));
        enterpriseTab.add(createHumanizationPanel(config));
        tabs.addTab("Enterprise", enterpriseTab);

        // --- Discord Tab ---
        tabs.addTab("Discord", createDiscordTab(config));

        // --- Advanced Tab ---
        tabs.addTab("Advanced", createAdvancedTab(config));

        main.add(tabs);
        main.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton startBtn = new JButton("LAUNCH ENTERPRISE SYSTEM");
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.setBackground(new Color(0, 255, 127));
        startBtn.setForeground(Color.BLACK);
        startBtn.setFont(new Font("Arial", Font.BOLD, 14));
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
        p.setBackground(new Color(30, 30, 35));
        return p;
    }

    private JPanel createBettingPanel(CasinoConfig config) {
        JPanel p = createSection("Betting Limits");
        JTextField minField = new JTextField(String.valueOf(config.minBet), 10);
        JTextField maxField = new JTextField(String.valueOf(config.maxBet), 10);
        p.add(new JLabel("Min:")); p.add(minField);
        p.add(new JLabel("Max:")); p.add(maxField);
        minField.addActionListener(e -> config.minBet = Long.parseLong(minField.getText()));
        maxField.addActionListener(e -> config.maxBet = Long.parseLong(maxField.getText()));
        return p;
    }

    private JPanel createAdTab(CasinoConfig config) {
        JPanel p = createTabPanel();
        JPanel section = createSection("Anti-Mute Advertising");
        JCheckBox antiMute = new JCheckBox("Enable Anti-Mute Variations", config.enableAntiMute);
        antiMute.addActionListener(e -> config.enableAntiMute = antiMute.isSelected());
        section.add(antiMute);
        
        DefaultListModel<String> model = new DefaultListModel<>();
        config.adMessages.forEach(model::addElement);
        JList<String> list = new JList<>(model);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(450, 100));
        section.add(scroll);
        
        JTextField newAd = new JTextField(20);
        JButton addBtn = new JButton("Add");
        addBtn.addActionListener(e -> {
            if (!newAd.getText().isEmpty()) {
                config.adMessages.add(newAd.getText());
                model.addElement(newAd.getText());
                newAd.setText("");
            }
        });
        section.add(newAd); section.add(addBtn);
        p.add(section);
        return p;
    }

    private JPanel createGamesTab(CasinoConfig config) {
        JPanel p = new JPanel(new GridLayout(0, 2, 10, 10));
        p.setBackground(new Color(30, 30, 35));
        config.games.forEach((id, settings) -> {
            JPanel gamePanel = createSection(settings.name);
            JCheckBox enabled = new JCheckBox("Enabled", settings.enabled);
            JTextField mult = new JTextField(String.valueOf(settings.multiplier), 5);
            gamePanel.add(enabled);
            gamePanel.add(new JLabel("x")); gamePanel.add(mult);
            enabled.addActionListener(e -> settings.enabled = enabled.isSelected());
            mult.addActionListener(e -> settings.multiplier = Double.parseDouble(mult.getText()));
            p.add(gamePanel);
        });
        return p;
    }

    private JPanel createJackpotPanel(CasinoConfig config) {
        JPanel p = createSection("Global Jackpot");
        JTextField contrib = new JTextField(String.valueOf(config.jackpotContributionPercent), 5);
        p.add(new JLabel("Contrib %:")); p.add(contrib);
        contrib.addActionListener(e -> config.jackpotContributionPercent = Double.parseDouble(contrib.getText()));
        return p;
    }

    private JPanel createMulingPanel(CasinoConfig config) {
        JPanel p = createSection("Auto-Muling");
        JCheckBox enabled = new JCheckBox("Enable", config.autoMule);
        JTextField threshold = new JTextField(String.valueOf(config.muleThreshold), 10);
        p.add(enabled); p.add(new JLabel("Threshold:")); p.add(threshold);
        enabled.addActionListener(e -> config.autoMule = enabled.isSelected());
        threshold.addActionListener(e -> config.muleThreshold = Long.parseLong(threshold.getText()));
        return p;
    }

    private JPanel createHumanizationPanel(CasinoConfig config) {
        JPanel p = createSection("Humanization");
        JCheckBox chatAi = new JCheckBox("Enable Chat AI", config.chatAIEnabled);
        p.add(chatAi);
        chatAi.addActionListener(e -> config.chatAIEnabled = chatAi.isSelected());
        return p;
    }

    private JPanel createDiscordTab(CasinoConfig config) {
        JPanel p = createTabPanel();
        JPanel section = createSection("Discord Webhook");
        JCheckBox enabled = new JCheckBox("Enable", config.discordEnabled);
        JTextField url = new JTextField(config.discordWebhookUrl, 25);
        section.add(enabled); section.add(new JLabel("URL:")); section.add(url);
        enabled.addActionListener(e -> config.discordEnabled = enabled.isSelected());
        url.addActionListener(e -> config.discordWebhookUrl = url.getText());
        p.add(section);
        return p;
    }

    private JPanel createAdvancedTab(CasinoConfig config) {
        JPanel p = createTabPanel();
        JPanel humanSection = createSection("Advanced Anti-Ban");
        JCheckBox breakEnabled = new JCheckBox("Micro-Breaks", config.enableMicroBreaks);
        JCheckBox jitterEnabled = new JCheckBox("Camera Jitter", config.enableCameraJitter);
        JCheckBox fatigueEnabled = new JCheckBox("Mouse Fatigue", config.enableMouseFatigue);
        humanSection.add(breakEnabled);
        humanSection.add(jitterEnabled);
        humanSection.add(fatigueEnabled);
        breakEnabled.addActionListener(e -> config.enableMicroBreaks = breakEnabled.isSelected());
        jitterEnabled.addActionListener(e -> config.enableCameraJitter = jitterEnabled.isSelected());
        fatigueEnabled.addActionListener(e -> config.enableMouseFatigue = fatigueEnabled.isSelected());
        p.add(humanSection);
        return p;
    }

    private JPanel createSection(String title) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBackground(new Color(45, 45, 50));
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), title);
        border.setTitleColor(Color.WHITE);
        p.setBorder(border);
        return p;
    }
}
