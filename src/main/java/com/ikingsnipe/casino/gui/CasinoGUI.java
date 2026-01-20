package com.ikingsnipe.casino.gui;

import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.managers.ProfitTracker;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.function.Consumer;
import java.util.Map;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Enhanced Enterprise GUI for iKingSnipe GoatGang Casino
 * Features a modern dashboard, real-time stats, and extensive configuration options.
 */
public class CasinoGUI extends JFrame {
    private final CasinoConfig config;
    private final Consumer<Boolean> onFinish;
    private ProfitTracker profitTracker;
    
    // Colors
    private final Color BG_DARK = new Color(18, 18, 24);
    private final Color BG_CARD = new Color(30, 30, 40);
    private final Color ACCENT = new Color(255, 215, 0); // Gold
    private final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private final Color TEXT_SECONDARY = new Color(180, 180, 180);
    private final Color SUCCESS = new Color(76, 175, 80);
    private final Color DANGER = new Color(244, 67, 54);

    public CasinoGUI(CasinoConfig config, Consumer<Boolean> onFinish) {
        this.config = config;
        this.onFinish = onFinish;
        
        setTitle("iKingSnipe GoatGang Casino - Master Version");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(850, 650));
        setResizable(true);

        // Main Container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BG_DARK);
        
        // Sidebar
        JPanel sidebar = createSidebar();
        mainContainer.add(sidebar, BorderLayout.WEST);
        
        // Content Area
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(BG_DARK);
        tabs.setForeground(TEXT_PRIMARY);
        tabs.setBorder(null);
        
        tabs.addTab("Dashboard", createDashboardTab());
        tabs.addTab("General", createGeneralTab());
        tabs.addTab("Games", createGamesTab());
        tabs.addTab("Banking & Mule", createBankingTab());
        tabs.addTab("Humanization", createHumanizationTab());
        tabs.addTab("Discord & CC", createSocialTab());
        
        mainContainer.add(tabs, BorderLayout.CENTER);
        
        // Footer
        JPanel footer = createFooter();
        mainContainer.add(footer, BorderLayout.SOUTH);

        add(mainContainer);
        pack();
        setLocationRelativeTo(null);
    }

    public void setProfitTracker(ProfitTracker tracker) {
        this.profitTracker = tracker;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(180, 0));
        sidebar.setBackground(new Color(25, 25, 35));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        JLabel logo = new JLabel("iKingSnipe");
        logo.setFont(new Font("Verdana", Font.BOLD, 24));
        logo.setForeground(ACCENT);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(logo);

        JLabel subLogo = new JLabel("GoatGang Elite");
        subLogo.setFont(new Font("Verdana", Font.PLAIN, 12));
        subLogo.setForeground(TEXT_SECONDARY);
        subLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(subLogo);

        sidebar.add(Box.createVerticalGlue());

        JLabel version = new JLabel("v" + config.version);
        version.setForeground(TEXT_SECONDARY);
        version.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(version);

        return sidebar;
    }

    private JPanel createDashboardTab() {
        JPanel panel = createTabPanel();
        
        JPanel statsGrid = new JPanel(new GridLayout(2, 2, 15, 15));
        statsGrid.setOpaque(false);
        
        statsGrid.add(createStatCard("Total Profit", "0 GP", SUCCESS));
        statsGrid.add(createStatCard("Total Wagered", "0 GP", ACCENT));
        statsGrid.add(createStatCard("Runtime", "00:00:00", TEXT_PRIMARY));
        statsGrid.add(createStatCard("Active Players", "0", TEXT_PRIMARY));
        
        panel.add(statsGrid);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JPanel recentActivity = createSection("Recent Winners", new Component[]{
            new JLabel("Waiting for activity..."),
            new JLabel("Script must be running to track live data.")
        });
        panel.add(recentActivity);
        
        return panel;
    }

    private JPanel createGeneralTab() {
        JPanel panel = createTabPanel();
        
        panel.add(createSection("Core Settings", new Component[]{
            createLabel("Script Name:"), createTextField(config.scriptName, s -> config.scriptName = s),
            createLabel("Min Bet:"), createTextField(String.valueOf(config.minBet), s -> config.minBet = Long.parseLong(s)),
            createLabel("Max Bet:"), createTextField(String.valueOf(config.maxBet), s -> config.maxBet = Long.parseLong(s)),
            createLabel("Default Game:"), createComboBox(new String[]{"craps", "dice", "flower", "blackjack", "hotcold"}, config.defaultGame, s -> config.defaultGame = s)
        }));

        panel.add(createSection("Location Settings", new Component[]{
            createLabel("Preset:"), createComboBox(new String[]{"GE Southwest", "Clan Hall", "Custom"}, config.locationPreset.getName(), s -> {
                for (CasinoConfig.LocationPreset p : CasinoConfig.LocationPreset.values()) {
                    if (p.getName().equals(s)) config.locationPreset = p;
                }
            }),
            createCheckbox("Walk to location on start", config.walkOnStart, b -> config.walkOnStart = b)
        }));

        return panel;
    }

    private JPanel createGamesTab() {
        JPanel panel = createTabPanel();
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        for (Map.Entry<String, CasinoConfig.GameSettings> entry : config.games.entrySet()) {
            CasinoConfig.GameSettings s = entry.getValue();
            panel.add(createSection(s.name, new Component[]{
                createCheckbox("Enabled", s.enabled, b -> s.enabled = b),
                createLabel("Multiplier:"), createTextField(String.valueOf(s.multiplier), val -> s.multiplier = Double.parseDouble(val)),
                createLabel("Trigger:"), createTextField(s.trigger, val -> s.trigger = val)
            }));
        }

        JPanel container = new JPanel(new BorderLayout());
        container.add(scroll);
        return container;
    }

    private JPanel createBankingTab() {
        JPanel panel = createTabPanel();
        
        panel.add(createSection("Banking", new Component[]{
            createCheckbox("Auto Bank", config.autoBank, b -> config.autoBank = b),
            createLabel("Restock Threshold:"), createTextField(String.valueOf(config.restockThreshold), s -> config.restockThreshold = Long.parseLong(s)),
            createLabel("Restock Amount:"), createTextField(String.valueOf(config.restockAmount), s -> config.restockAmount = Long.parseLong(s))
        }));

        panel.add(createSection("Muling", new Component[]{
            createCheckbox("Auto Mule", config.autoMule, b -> config.autoMule = b),
            createLabel("Mule Name:"), createTextField(config.muleName, s -> config.muleName = s),
            createLabel("Mule Threshold:"), createTextField(String.valueOf(config.muleThreshold), s -> config.muleThreshold = Long.parseLong(s)),
            createLabel("Keep Amount:"), createTextField(String.valueOf(config.muleKeepAmount), s -> config.muleKeepAmount = Long.parseLong(s))
        }));

        return panel;
    }

    private JPanel createHumanizationTab() {
        JPanel panel = createTabPanel();
        
        panel.add(createSection("Anti-Ban Logic", new Component[]{
            createCheckbox("Smart Adaptive Logic", config.enableSmartAdaptiveLogic, b -> config.enableSmartAdaptiveLogic = b),
            createCheckbox("Camera Jitter", config.enableCameraJitter, b -> config.enableCameraJitter = b),
            createCheckbox("Mouse Fatigue", config.enableMouseFatigue, b -> config.enableMouseFatigue = b),
            createCheckbox("Random Walking", config.enableRandomWalking, b -> config.enableRandomWalking = b)
        }));

        panel.add(createSection("Breaks", new Component[]{
            createCheckbox("Enable Micro-Breaks", config.enableMicroBreaks, b -> config.enableMicroBreaks = b),
            createLabel("Frequency (min):"), createTextField(String.valueOf(config.breakFrequencyMinutes), s -> config.breakFrequencyMinutes = Integer.parseInt(s)),
            createLabel("Duration (min):"), createTextField(String.valueOf(config.breakDurationMinutes), s -> config.breakDurationMinutes = Integer.parseInt(s))
        }));

        return panel;
    }

    private JPanel createSocialTab() {
        JPanel panel = createTabPanel();
        
        panel.add(createSection("Discord Integration", new Component[]{
            createCheckbox("Enable Discord", config.discordEnabled, b -> config.discordEnabled = b),
            createLabel("Webhook URL:"), createTextField(config.discordWebhookUrl, s -> config.discordWebhookUrl = s),
            createCheckbox("Notify Wins", config.discordNotifyWins, b -> config.discordNotifyWins = b),
            createCheckbox("Notify Losses", config.discordNotifyLosses, b -> config.discordNotifyLosses = b)
        }));

        panel.add(createSection("Clan Chat", new Component[]{
            createCheckbox("Enable CC", config.clanChatEnabled, b -> config.clanChatEnabled = b),
            createLabel("CC Name:"), createTextField(config.clanChatName, s -> config.clanChatName = s),
            createCheckbox("Announce Wins", config.clanChatAnnounceWins, b -> config.clanChatAnnounceWins = b),
            createCheckbox("Auto Accept Trades", config.autoAcceptTrades, b -> config.autoAcceptTrades = b)
        }));

        return panel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(new Color(25, 25, 35));
        footer.setBorder(new EmptyBorder(10, 20, 10, 20));

        JButton startBtn = new JButton("LAUNCH MASTER VERSION");
        startBtn.setBackground(SUCCESS);
        startBtn.setForeground(Color.WHITE);
        startBtn.setFont(new Font("Arial", Font.BOLD, 14));
        startBtn.setFocusPainted(false);
        startBtn.setPreferredSize(new Dimension(220, 40));
        startBtn.addActionListener(e -> {
            onFinish.accept(true);
            dispose();
        });
        
        footer.add(startBtn);
        return footer;
    }

    private JPanel createTabPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        return p;
    }

    private JPanel createSection(String title, Component[] components) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel p = new JPanel(new GridLayout(0, 2, 10, 10));
        p.setBackground(BG_CARD);
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleColor(ACCENT);
        border.setTitleFont(new Font("Verdana", Font.BOLD, 12));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 70), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        p.setBorder(BorderFactory.createCompoundBorder(border, p.getBorder()));
        
        for (Component c : components) p.add(c);
        container.add(p, BorderLayout.CENTER);
        return container;
    }

    private JPanel createStatCard(String title, String value, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 70), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setFont(new Font("Verdana", Font.PLAIN, 12));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(valueColor);
        valueLabel.setFont(new Font("Verdana", Font.BOLD, 18));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    private JTextField createTextField(String text, Consumer<String> onChange) {
        JTextField f = new JTextField(text);
        f.setBackground(new Color(45, 45, 55));
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 90)),
            new EmptyBorder(5, 5, 5, 5)
        ));
        f.addActionListener(e -> onChange.accept(f.getText()));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                onChange.accept(f.getText());
            }
        });

        // Add Clipboard Support (Right-click menu)
        JPopupMenu menu = new JPopupMenu();
        JMenuItem copy = new JMenuItem("Copy");
        JMenuItem paste = new JMenuItem("Paste");
        
        copy.addActionListener(e -> {
            String selection = f.getSelectedText();
            if (selection == null) selection = f.getText();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(selection), null);
        });
        
        paste.addActionListener(e -> {
            try {
                String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                f.replaceSelection(data);
                onChange.accept(f.getText());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        menu.add(copy);
        menu.add(paste);
        f.setComponentPopupMenu(menu);

        return f;
    }

    private JCheckBox createCheckbox(String text, boolean selected, Consumer<Boolean> onChange) {
        JCheckBox c = new JCheckBox(text, selected);
        c.setForeground(TEXT_PRIMARY);
        c.setOpaque(false);
        c.setFocusPainted(false);
        c.addActionListener(e -> onChange.accept(c.isSelected()));
        return c;
    }

    private JComboBox<String> createComboBox(String[] items, String selected, Consumer<String> onChange) {
        JComboBox<String> b = new JComboBox<>(items);
        b.setSelectedItem(selected);
        b.setBackground(new Color(45, 45, 55));
        b.setForeground(TEXT_PRIMARY);
        b.addActionListener(e -> onChange.accept((String) b.getSelectedItem()));
        return b;
    }
}
