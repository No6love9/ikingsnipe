package com.ikingsnipe.casino.gui;

import com.ikingsnipe.casino.models.CasinoConfig;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.function.Consumer;

public class CasinoGUI extends JFrame {
    public CasinoGUI(CasinoConfig config, Consumer<Boolean> onComplete) {
        setTitle("snipes♧scripts - Enterprise Edition");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(550, 750);
        setLocationRelativeTo(null);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(15, 15, 15, 15));
        main.setBackground(new Color(20, 20, 25));

        JLabel title = new JLabel("SNIPES ♧ SCRIPTS");
        title.setFont(new Font("Verdana", Font.BOLD, 32));
        title.setForeground(new Color(0, 255, 127));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        main.add(title);
        
        JLabel subtitle = new JLabel("Enterprise Casino Management");
        subtitle.setFont(new Font("Verdana", Font.ITALIC, 14));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        main.add(subtitle);
        
        main.add(Box.createRigidArea(new Dimension(0, 20)));

        JTabbedPane tabs = new JTabbedPane();
        
        // --- General Tab ---
        JPanel generalTab = createTabPanel();
        generalTab.add(createLocationPanel(config));
        generalTab.add(createBettingPanel(config));
        generalTab.add(createAdPanel(config));
        tabs.addTab("General", generalTab);

        // --- Games Tab ---
        tabs.addTab("Games", createGamesTab(config));

        // --- Enterprise Tab (Jackpot & Muling) ---
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

        // Start Button
        JButton startBtn = new JButton("LAUNCH ENTERPRISE SYSTEM");
        startBtn.setBackground(new Color(0, 180, 80));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFont(new Font("Arial", Font.BOLD, 16));
        startBtn.setPreferredSize(new Dimension(300, 50));
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.addActionListener(e -> {
            onComplete.accept(true);
            dispose();
        });
        main.add(startBtn);

        add(main);
    }

    private JPanel createTabPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(30, 30, 35));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        return p;
    }

    private JPanel createLocationPanel(CasinoConfig config) {
        JPanel p = createSection("Location & Movement");
        JComboBox<CasinoConfig.LocationPreset> locCombo = new JComboBox<>(CasinoConfig.LocationPreset.values());
        locCombo.setSelectedItem(config.locationPreset);
        p.add(new JLabel("Preset:")); p.add(locCombo);
        JCheckBox walkCb = new JCheckBox("Walk on Start", config.walkOnStart);
        p.add(walkCb);
        locCombo.addActionListener(e -> config.locationPreset = (CasinoConfig.LocationPreset) locCombo.getSelectedItem());
        walkCb.addActionListener(e -> config.walkOnStart = walkCb.isSelected());
        return p;
    }

    private JPanel createBettingPanel(CasinoConfig config) {
        JPanel p = createSection("Betting Limits (GP)");
        JTextField minField = new JTextField(String.valueOf(config.minBet), 10);
        JTextField maxField = new JTextField(String.valueOf(config.maxBet), 10);
        p.add(new JLabel("Min:")); p.add(minField);
        p.add(new JLabel("Max:")); p.add(maxField);
        minField.addActionListener(e -> config.minBet = Long.parseLong(minField.getText()));
        maxField.addActionListener(e -> config.maxBet = Long.parseLong(maxField.getText()));
        return p;
    }

    private JPanel createAdPanel(CasinoConfig config) {
        JPanel p = createSection("Advertising");
        JTextField adField = new JTextField(config.adMessage, 20);
        p.add(new JLabel("Msg:")); p.add(adField);
        adField.addActionListener(e -> config.adMessage = adField.getText());
        return p;
    }

    private JPanel createGamesTab(CasinoConfig config) {
        JPanel p = new JPanel(new GridLayout(0, 2, 10, 10));
        p.setBackground(new Color(30, 30, 35));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        for (String key : config.games.keySet()) {
            CasinoConfig.GameSettings gs = config.games.get(key);
            JPanel gp = new JPanel(new BorderLayout());
            gp.setBackground(new Color(45, 45, 50));
            JCheckBox cb = new JCheckBox(gs.name, gs.enabled);
            cb.addActionListener(e -> gs.enabled = cb.isSelected());
            gp.add(cb, BorderLayout.WEST);
            p.add(gp);
        }
        return p;
    }

    private JPanel createJackpotPanel(CasinoConfig config) {
        JPanel p = createSection("Global Jackpot");
        JCheckBox enabled = new JCheckBox("Enable Jackpot", config.jackpotEnabled);
        JTextField percent = new JTextField(String.valueOf(config.jackpotContributionPercent), 4);
        p.add(enabled);
        p.add(new JLabel("Contribution %:")); p.add(percent);
        enabled.addActionListener(e -> config.jackpotEnabled = enabled.isSelected());
        percent.addActionListener(e -> config.jackpotContributionPercent = Double.parseDouble(percent.getText()));
        return p;
    }

    private JPanel createMulingPanel(CasinoConfig config) {
        JPanel p = createSection("Auto-Muling");
        JCheckBox enabled = new JCheckBox("Enable Muling", config.autoMule);
        JTextField name = new JTextField(config.muleName, 10);
        JTextField threshold = new JTextField(String.valueOf(config.muleThreshold), 10);
        p.add(enabled);
        p.add(new JLabel("Mule Name:")); p.add(name);
        p.add(new JLabel("Threshold:")); p.add(threshold);
        enabled.addActionListener(e -> config.autoMule = enabled.isSelected());
        name.addActionListener(e -> config.muleName = name.getText());
        threshold.addActionListener(e -> config.muleThreshold = Long.parseLong(threshold.getText()));
        return p;
    }

    private JPanel createHumanizationPanel(CasinoConfig config) {
        JPanel p = createSection("Humanization & AI");
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
        
        // Muling Section
        JPanel muleSection = createSection("Auto-Muling");
        JCheckBox muleEnabled = new JCheckBox("Enable Muling", config.autoMule);
        JTextField muleName = new JTextField(config.muleName, 10);
        JTextField muleThreshold = new JTextField(String.valueOf(config.muleThreshold), 10);
        muleSection.add(muleEnabled);
        muleSection.add(new JLabel("Mule Name:")); muleSection.add(muleName);
        muleSection.add(new JLabel("Threshold:")); muleSection.add(muleThreshold);
        
        muleEnabled.addActionListener(e -> config.autoMule = muleEnabled.isSelected());
        muleName.addActionListener(e -> config.muleName = muleName.getText());
        muleThreshold.addActionListener(e -> config.muleThreshold = Long.parseLong(muleThreshold.getText()));
        p.add(muleSection);

        // Humanization Section
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
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), title);
        border.setTitleColor(new Color(0, 255, 127));
        p.setBorder(border);
        p.setBackground(new Color(40, 40, 45));
        p.setMaximumSize(new Dimension(520, 100));
        return p;
    }
}
