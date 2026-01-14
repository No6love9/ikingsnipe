package com.ikingsnipe.casino.gui;

import javax.swing.*;
import java.awt.*;

public class CasinoPanel extends JFrame {
    public CasinoPanel() {
        setTitle("Elite Titan Casino v8.0 - Control Panel");
        setSize(400, 300);
        setLayout(new BorderLayout());
        
        JPanel statsPanel = new JPanel(new GridLayout(3, 1));
        statsPanel.add(new JLabel("Profit: 0 GP"));
        statsPanel.add(new JLabel("Wins: 0"));
        statsPanel.add(new JLabel("Losses: 0"));
        
        add(statsPanel, BorderLayout.CENTER);
        
        JButton emergencyStop = new JButton("EMERGENCY STOP");
        emergencyStop.setBackground(Color.RED);
        emergencyStop.setForeground(Color.WHITE);
        add(emergencyStop, BorderLayout.SOUTH);
        
        setLocationRelativeTo(null);
    }
}
