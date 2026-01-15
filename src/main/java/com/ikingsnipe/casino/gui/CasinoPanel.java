package com.ikingsnipe.casino.gui;

import com.ikingsnipe.casino.models.CasinoConfig;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CasinoPanel extends JFrame {
    private final CasinoConfig config;
    
    public CasinoPanel(CasinoConfig config) {
        this.config = config;
        setTitle("Elite Titan Casino v12.0 - Control Panel");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);
        
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(45, 45, 45));

        // Header
        JLabel header = new JLabel("Casino Configuration");
        header.setForeground(Color.CYAN);
        header.setFont(new Font("Verdana", Font.BOLD, 18));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(header);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Betting Limits Section
        mainPanel.add(createSectionLabel("Betting Limits"));
        JTextField minBetField = createTextField(String.valueOf(config.minBet));
        JTextField maxBetField = createTextField(String.valueOf(config.maxBet));
        mainPanel.add(createFieldPanel("Min Bet (GP):", minBetField));
        mainPanel.add(createFieldPanel("Max Bet (GP):", maxBetField));

        // Intervals Section
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(createSectionLabel("Intervals (ms)"));
        JTextField adIntervalField = createTextField(String.valueOf(config.adIntervalMs));
        JTextField tradeTimeoutField = createTextField(String.valueOf(config.tradeTimeoutMs));
        mainPanel.add(createFieldPanel("Ad Interval:", adIntervalField));
        mainPanel.add(createFieldPanel("Trade Timeout:", tradeTimeoutField));

        // Toggles Section
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(createSectionLabel("Features"));
        JCheckBox pfBox = createCheckBox("Use Provably Fair", config.useProvablyFair);
        JCheckBox restockBox = createCheckBox("Auto Restock", config.autoRestock);
        mainPanel.add(pfBox);
        mainPanel.add(restockBox);

        // Save Button
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        JButton saveBtn = new JButton("Apply Settings");
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.setBackground(new Color(0, 150, 0));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        
        saveBtn.addActionListener(e -> {
            try {
                config.minBet = Long.parseLong(minBetField.getText());
                config.maxBet = Long.parseLong(maxBetField.getText());
                config.adIntervalMs = Integer.parseInt(adIntervalField.getText());
                config.tradeTimeoutMs = Integer.parseInt(tradeTimeoutField.getText());
                config.useProvablyFair = pfBox.isSelected();
                config.autoRestock = restockBox.isSelected();
                JOptionPane.showMessageDialog(this, "Settings applied successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input! Please check your values.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        mainPanel.add(saveBtn);

        add(new JScrollPane(mainPanel));
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.YELLOW);
        label.setFont(new Font("Verdana", Font.BOLD, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel createFieldPanel(String labelText, JTextField field) {
        JPanel p = new JPanel(new BorderLayout());
        p.setMaximumSize(new Dimension(380, 30));
        p.setBackground(new Color(45, 45, 45));
        JLabel l = new JLabel(labelText);
        l.setForeground(Color.WHITE);
        l.setPreferredSize(new Dimension(120, 25));
        p.add(l, BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JTextField createTextField(String value) {
        JTextField f = new JTextField(value);
        f.setBackground(new Color(60, 60, 60));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        return f;
    }

    private JCheckBox createCheckBox(String text, boolean selected) {
        JCheckBox b = new JCheckBox(text, selected);
        b.setBackground(new Color(45, 45, 45));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }
}
