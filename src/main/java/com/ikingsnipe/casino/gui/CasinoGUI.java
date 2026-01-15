package com.ikingsnipe.casino.gui;

import com.ikingsnipe.casino.models.CasinoConfig;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.function.Consumer;

public class CasinoGUI extends JFrame {
    public CasinoGUI(CasinoConfig config, Consumer<Boolean> onComplete) {
        setTitle("snipes♧scripts - Casino Enterprise");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 700);
        setLocationRelativeTo(null);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(15, 15, 15, 15));
        main.setBackground(new Color(25, 25, 30));

        JLabel title = new JLabel("SNIPES ♧ SCRIPTS");
        title.setFont(new Font("Verdana", Font.BOLD, 28));
        title.setForeground(new Color(0, 255, 127));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        main.add(title);
        
        JLabel subtitle = new JLabel("Casino Enterprise Edition");
        subtitle.setFont(new Font("Verdana", Font.PLAIN, 12));
        subtitle.setForeground(Color.LIGHT_GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        main.add(subtitle);
        
        main.add(Box.createRigidArea(new Dimension(0, 20)));

        // Tabbed Pane for organization
        JTabbedPane tabs = new JTabbedPane();
        
        // General Settings Tab
        JPanel generalTab = new JPanel();
        generalTab.setLayout(new BoxLayout(generalTab, BoxLayout.Y_AXIS));
        generalTab.setBackground(new Color(35, 35, 40));
        
        JPanel locPanel = createSection("Location & Movement");
        JComboBox<CasinoConfig.LocationPreset> locCombo = new JComboBox<>(CasinoConfig.LocationPreset.values());
        locCombo.setSelectedItem(config.locationPreset);
        locPanel.add(new JLabel("Preset:"));
        locPanel.add(locCombo);
        JCheckBox walkCb = new JCheckBox("Walk on Start", config.walkOnStart);
        locPanel.add(walkCb);
        generalTab.add(locPanel);

        JPanel betPanel = createSection("Betting Limits (GP)");
        JTextField minField = new JTextField(String.valueOf(config.minBet), 10);
        JTextField maxField = new JTextField(String.valueOf(config.maxBet), 10);
        betPanel.add(new JLabel("Min:")); betPanel.add(minField);
        betPanel.add(new JLabel("Max:")); betPanel.add(maxField);
        generalTab.add(betPanel);
        
        JPanel adPanel = createSection("Advertising");
        JTextField adField = new JTextField(config.adMessage, 20);
        JSpinner adInterval = new JSpinner(new SpinnerNumberModel(config.adIntervalSeconds, 5, 300, 5));
        adPanel.add(new JLabel("Msg:")); adPanel.add(adField);
        adPanel.add(new JLabel("Sec:")); adPanel.add(adInterval);
        generalTab.add(adPanel);

        tabs.addTab("General", generalTab);

        // Games Tab
        JPanel gamesTab = new JPanel();
        gamesTab.setLayout(new GridLayout(0, 2, 10, 10));
        gamesTab.setBackground(new Color(35, 35, 40));
        gamesTab.setBorder(new EmptyBorder(10, 10, 10, 10));
        for (String gameKey : config.games.keySet()) {
            CasinoConfig.GameSettings gs = config.games.get(gameKey);
            JPanel p = new JPanel(new BorderLayout());
            p.setBackground(new Color(45, 45, 50));
            p.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            JCheckBox cb = new JCheckBox(gs.name, gs.enabled);
            cb.setForeground(Color.WHITE); cb.setBackground(new Color(45, 45, 50));
            p.add(cb, BorderLayout.WEST);
            JTextField mult = new JTextField(String.valueOf(gs.multiplier), 3);
            p.add(mult, BorderLayout.EAST);
            gamesTab.add(p);
            
            cb.addActionListener(e -> gs.enabled = cb.isSelected());
            mult.addActionListener(e -> {
                try { gs.multiplier = Double.parseDouble(mult.getText()); } catch(Exception ex) {}
            });
        }
        tabs.addTab("Games", gamesTab);

        // Discord Tab
        JPanel discordTab = new JPanel();
        discordTab.setLayout(new BoxLayout(discordTab, BoxLayout.Y_AXIS));
        discordTab.setBackground(new Color(35, 35, 40));
        JPanel webhookPanel = createSection("Discord Integration");
        JCheckBox discordEnabled = new JCheckBox("Enable Webhook", config.discordEnabled);
        JTextField webhookUrl = new JTextField(config.discordWebhookUrl, 30);
        webhookPanel.add(discordEnabled);
        webhookPanel.add(new JLabel("URL:"));
        webhookPanel.add(webhookUrl);
        discordTab.add(webhookPanel);
        tabs.addTab("Discord", discordTab);

        main.add(tabs);
        main.add(Box.createRigidArea(new Dimension(0, 20)));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);
        JButton startBtn = new JButton("START SNIPES♧SCRIPTS");
        startBtn.setBackground(new Color(0, 180, 80));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFont(new Font("Arial", Font.BOLD, 14));
        startBtn.setPreferredSize(new Dimension(250, 40));
        startBtn.addActionListener(e -> {
            try {
                config.locationPreset = (CasinoConfig.LocationPreset) locCombo.getSelectedItem();
                config.walkOnStart = walkCb.isSelected();
                config.minBet = Long.parseLong(minField.getText().replaceAll("[^0-9]", ""));
                config.maxBet = Long.parseLong(maxField.getText().replaceAll("[^0-9]", ""));
                config.adMessage = adField.getText();
                config.adIntervalSeconds = (Integer) adInterval.getValue();
                config.discordEnabled = discordEnabled.isSelected();
                config.discordWebhookUrl = webhookUrl.getText();
                onComplete.accept(true);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
            }
        });

        btnPanel.add(startBtn);
        main.add(btnPanel);

        add(main);
    }

    private JPanel createSection(String title) {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)), title);
        border.setTitleColor(new Color(0, 255, 127));
        p.setBorder(border);
        p.setBackground(new Color(40, 40, 45));
        p.setMaximumSize(new Dimension(480, 120));
        return p;
    }
}
