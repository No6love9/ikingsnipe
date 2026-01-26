package com.ikingsnipe.casino.gui;

import com.ikingsnipe.casino.models.CasinoConfig;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * CasinoGUI - Elite Titan Casino
 * 
 * Professional, modular, and configurable GUI.
 */
public class CasinoGUI extends JFrame {
    private CasinoConfig config;
    private final Consumer<Boolean> onStart;

    public CasinoGUI(CasinoConfig config, Consumer<Boolean> onStart) {
        this.config = config;
        this.onStart = onStart;

        setTitle("Elite Titan Casino - GoatGang Edition");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 600);
        setLocationRelativeTo(null);
        setUndecorated(true);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.BG_DARK);
        mainPanel.setBorder(BorderFactory.createLineBorder(Theme.ACCENT_GOLD, 1));

        // Header
        mainPanel.add(createHeader(), BorderLayout.NORTH);

        // Sidebar / Tabs
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setBackground(Theme.BG_DARK);
        tabs.setForeground(Theme.TEXT_PRIMARY);
        tabs.setFont(Theme.FONT_REGULAR);

        tabs.addTab("Dashboard", createDashboard());
        tabs.addTab("Games", createGamesConfig());
        tabs.addTab("Anti-Ban", createAntiBanConfig());
        tabs.addTab("Discord", createDiscordConfig());

        mainPanel.add(tabs, BorderLayout.CENTER);

        // Footer
        mainPanel.add(createFooter(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_DARK);
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("ELITE TITAN CASINO");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.ACCENT_GOLD);
        header.add(title, BorderLayout.WEST);

        JButton closeBtn = new JButton("✕");
        closeBtn.setForeground(Theme.TEXT_SECONDARY);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dispose());
        header.add(closeBtn, BorderLayout.EAST);

        return header;
    }

    private JPanel createDashboard() {
        JPanel p = createBasePanel();
        p.add(new JLabel("Welcome to GoatGang Enterprise Solutions") {{
            setForeground(Theme.TEXT_PRIMARY);
            setFont(Theme.FONT_SUBTITLE);
        }});
        return p;
    }

    private JPanel createGamesConfig() {
        JPanel p = createBasePanel();
        // Add game toggles and multipliers here
        return p;
    }

    private JPanel createAntiBanConfig() {
        JPanel p = createBasePanel();
        // Add anti-ban settings
        return p;
    }

    private JPanel createDiscordConfig() {
        JPanel p = createBasePanel();
        // Add webhook settings
        return p;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Theme.BG_DARK);
        footer.setBorder(new EmptyBorder(10, 20, 10, 20));

        JButton startBtn = new JButton("LAUNCH SCRIPT");
        startBtn.setBackground(Theme.ACCENT_GOLD);
        startBtn.setForeground(Color.BLACK);
        startBtn.setFont(Theme.FONT_SUBTITLE);
        startBtn.addActionListener(e -> {
            onStart.accept(true);
            dispose();
        });
        footer.add(startBtn);

        return footer;
    }

    private JPanel createBasePanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.BG_CARD);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        return p;
    }
}
