package com.ikingsnipe.casino.gui;

import com.ikingsnipe.casino.models.CasinoConfig;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

public class CasinoGUI extends JFrame {
    public CasinoGUI(CasinoConfig config, Consumer<Boolean> onComplete) {
        setTitle("Elite Titan Casino Enterprise v13.0");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 550);
        setLocationRelativeTo(null);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(20, 20, 20, 20));
        main.setBackground(new Color(30, 30, 35));

        JLabel title = new JLabel("ELITE TITAN CASINO");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(255, 215, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        main.add(title);
        main.add(Box.createRigidArea(new Dimension(0, 20)));

        // Location Panel
        JPanel locPanel = createSection("Location Settings");
        JComboBox<CasinoConfig.LocationPreset> locCombo = new JComboBox<>(CasinoConfig.LocationPreset.values());
        locCombo.setSelectedItem(config.locationPreset);
        locPanel.add(locCombo);
        main.add(locPanel);

        // Betting Panel
        JPanel betPanel = createSection("Betting Limits (GP)");
        JTextField minField = new JTextField(String.valueOf(config.minBet));
        JTextField maxField = new JTextField(String.valueOf(config.maxBet));
        betPanel.add(new JLabel("Min:")); betPanel.add(minField);
        betPanel.add(new JLabel("Max:")); betPanel.add(maxField);
        main.add(betPanel);

        // Games Panel
        JPanel gamesPanel = createSection("Enabled Games");
        gamesPanel.setLayout(new GridLayout(3, 2));
        for (String game : config.games.keySet()) {
            JCheckBox cb = new JCheckBox(config.games.get(game).name, config.games.get(game).enabled);
            cb.setForeground(Color.WHITE); cb.setBackground(new Color(45, 45, 50));
            cb.addActionListener(e -> config.games.get(game).enabled = cb.isSelected());
            gamesPanel.add(cb);
        }
        main.add(gamesPanel);

        main.add(Box.createVerticalGlue());

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setOpaque(false);
        JButton startBtn = new JButton("START SCRIPT");
        startBtn.setBackground(new Color(0, 150, 0));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFont(new Font("Arial", Font.BOLD, 14));
        startBtn.addActionListener(e -> {
            config.locationPreset = (CasinoConfig.LocationPreset) locCombo.getSelectedItem();
            config.minBet = Long.parseLong(minField.getText());
            config.maxBet = Long.parseLong(maxField.getText());
            onComplete.accept(true);
            dispose();
        });

        JButton cancelBtn = new JButton("CANCEL");
        cancelBtn.addActionListener(e -> { onComplete.accept(false); dispose(); });

        btnPanel.add(startBtn); btnPanel.add(cancelBtn);
        main.add(btnPanel);

        add(main);
    }

    private JPanel createSection(String title) {
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), title));
        ((javax.swing.border.TitledBorder)p.getBorder()).setTitleColor(Color.LIGHT_GRAY);
        p.setBackground(new Color(45, 45, 50));
        p.setMaximumSize(new Dimension(400, 100));
        return p;
    }
}
