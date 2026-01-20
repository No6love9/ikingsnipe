package com.ikingsnipe.casino.gui;

import com.ikingsnipe.casino.managers.ProfitTracker;
import com.ikingsnipe.casino.models.CasinoConfig;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.function.Consumer;
import java.util.Map;

/**
 * GoatGang Edition GUI by iKingSnipe
 * Professional high-end design with custom branding and extensive features.
 */
public class CasinoGUI extends JFrame {
    private final CasinoConfig config;
    private final Consumer<Boolean> onStart;
    private ProfitTracker profitTracker;

    // Modern Color Palette
    private static final Color BG_DARK = new Color(15, 15, 20);
    private static final Color BG_CARD = new Color(25, 25, 35);
    private static final Color ACCENT_GOLD = new Color(212, 175, 55);
    private static final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private static final Color TEXT_SECONDARY = new Color(180, 180, 190);
    private static final Color SUCCESS_GREEN = new Color(76, 175, 80);

    public CasinoGUI(CasinoConfig config, Consumer<Boolean> onStart) {
        this.config = config;
        this.onStart = onStart;
        
        setTitle("GoatGang Edition by iKingSnipe");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setUndecorated(true); // Modern frameless look
        
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(BorderFactory.createLineBorder(ACCENT_GOLD, 2));

        // --- Header Section ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setPreferredSize(new Dimension(0, 100));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Logo and Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        titlePanel.setOpaque(false);
        
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/assets/goat_crown.webp"));
            Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            JLabel logo = new JLabel(new ImageIcon(img));
            titlePanel.add(logo);
        } catch (Exception e) {
            // Fallback if icon fails
            JLabel logo = new JLabel("🐐");
            logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
            titlePanel.add(logo);
        }

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        JLabel title = new JLabel("GoatGang Edition");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(ACCENT_GOLD);
        JLabel subtitle = new JLabel("by iKingSnipe | Enterprise Casino Solutions");
        subtitle.setFont(new Font("Arial", Font.ITALIC, 14));
        subtitle.setForeground(TEXT_SECONDARY);
        textPanel.add(title);
        textPanel.add(subtitle);
        titlePanel.add(textPanel);
        header.add(titlePanel, BorderLayout.WEST);

        // Window Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controls.setOpaque(false);
        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 18));
        closeBtn.setForeground(TEXT_SECONDARY);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dispose());
        controls.add(closeBtn);
        header.add(controls, BorderLayout.NORTH);

        mainPanel.add(header, BorderLayout.NORTH);

        // --- Sidebar Navigation ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_DARK);
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setBackground(BG_DARK);
        tabs.setForeground(TEXT_PRIMARY);
        tabs.setFont(new Font("Arial", Font.BOLD, 14));

        tabs.addTab("Dashboard", createDashboardPanel());
        tabs.addTab("Games", createGamesPanel());
        tabs.addTab("Clan Settings", createClanPanel());
        tabs.addTab("Trade Config", createTradePanel());
        tabs.addTab("Chat/AI", createChatPanel());
        tabs.addTab("Security", createSecurityPanel());

        mainPanel.add(tabs, BorderLayout.CENTER);

        // --- Footer ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG_DARK);
        footer.setPreferredSize(new Dimension(0, 40));
        footer.setBorder(new EmptyBorder(5, 20, 5, 20));
        
        JLabel copyright = new JLabel("© 2026 iKingSnipe. All Rights Reserved. Ownership: GoatGang.");
        copyright.setFont(new Font("Arial", Font.PLAIN, 10));
        copyright.setForeground(new Color(100, 100, 110));
        footer.add(copyright, BorderLayout.WEST);

        JButton startBtn = new JButton("LAUNCH GOATGANG");
        startBtn.setBackground(ACCENT_GOLD);
        startBtn.setForeground(Color.BLACK);
        startBtn.setFont(new Font("Arial", Font.BOLD, 14));
        startBtn.setFocusPainted(false);
        startBtn.addActionListener(e -> {
            onStart.accept(true);
            dispose();
        });
        footer.add(startBtn, BorderLayout.EAST);

        mainPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        
        // Make window draggable
        MouseAdapter ma = new MouseAdapter() {
            int lastX, lastY;
            public void mousePressed(MouseEvent e) { lastX = e.getXOnScreen(); lastY = e.getYOnScreen(); }
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen(); int y = e.getYOnScreen();
                setLocation(getLocationOnScreen().x + x - lastX, getLocationOnScreen().y + y - lastY);
                lastX = x; lastY = y;
            }
        };
        header.addMouseListener(ma);
        header.addMouseMotionListener(ma);
    }

    private JPanel createDashboardPanel() {
        JPanel panel = createBasePanel();
        panel.add(createSection("Live Statistics", new Component[]{
            createStatLabel("Status:", "ONLINE", SUCCESS_GREEN),
            createStatLabel("Runtime:", "00:00:00", TEXT_PRIMARY),
            createStatLabel("Total Profit:", "0 GP", TEXT_PRIMARY),
            createStatLabel("Active Players:", "0", TEXT_PRIMARY)
        }));
        
        panel.add(createSection("Recent Activity", new Component[]{
            new JScrollPane(new JTextArea("Waiting for activity...") {{
                setEditable(false);
                setBackground(BG_CARD);
                setForeground(TEXT_SECONDARY);
                setBorder(null);
            }})
        }));
        return panel;
    }

    private JPanel createGamesPanel() {
        JPanel panel = createBasePanel();
        panel.add(createSection("Game Selection", new Component[]{
            createCheckbox("Dice Duel", true, b -> {}),
            createCheckbox("Flower Poker", true, b -> {}),
            createCheckbox("Craps", true, b -> {}),
            createCheckbox("Blackjack", true, b -> {}),
            createCheckbox("Hot/Cold", true, b -> {})
        }));
        panel.add(createSection("Betting Limits", new Component[]{
            createLabel("Min Bet:"), createTextField("100K", s -> {}),
            createLabel("Max Bet:"), createTextField("100M", s -> {})
        }));
        return panel;
    }

    private JPanel createClanPanel() {
        JPanel panel = createBasePanel();
        panel.add(createSection("Clan Chat Configuration", new Component[]{
            createCheckbox("Enable Clan Chat", config.clanChatEnabled, b -> config.clanChatEnabled = b),
            createLabel("CC Name:"), createTextField(config.clanChatName, s -> config.clanChatName = s),
            createCheckbox("Announce Wins in CC", config.clanChatAnnounceWins, b -> config.clanChatAnnounceWins = b),
            createLabel("Rank Required:"), createTextField("Smiley", s -> {})
        }));
        return panel;
    }

    private JPanel createTradePanel() {
        JPanel panel = createBasePanel();
        panel.add(createSection("Trade Management", new Component[]{
            createCheckbox("Auto Accept Trades", config.autoAcceptTrades, b -> config.autoAcceptTrades = b),
            createCheckbox("Verify Trade Window 1", true, b -> {}),
            createCheckbox("Anti-Scam Protection", true, b -> {}),
            createLabel("Trade Message:"), createTextField("Trading with {player}...", s -> {})
        }));
        return panel;
    }

    private JPanel createChatPanel() {
        JPanel panel = createBasePanel();
        panel.add(createSection("Chat & AI Responses", new Component[]{
            createCheckbox("Enable Chat AI", config.chatAIEnabled, b -> config.chatAIEnabled = b),
            createLabel("Ad Interval (s):"), createTextField(String.valueOf(config.adIntervalSeconds), s -> {}),
            createLabel("Custom Ad Message:"), createTextField("Join GoatGang Casino! Best Odds!", s -> {})
        }));
        return panel;
    }

    private JPanel createSecurityPanel() {
        JPanel panel = createBasePanel();
        panel.add(createSection("License & Security", new Component[]{
            createLabel("License Key:"), createTextField("GG-XXXX-XXXX-XXXX", s -> {}),
            createLabel("Master Password:"), new JPasswordField("********") {{
                setEditable(false);
                setBackground(new Color(45, 45, 55));
                setForeground(TEXT_PRIMARY);
            }},
            new JLabel("Source code is encrypted and obfuscated.") {{
                setForeground(ACCENT_GOLD);
                setFont(new Font("Arial", Font.ITALIC, 10));
            }}
        }));
        return panel;
    }

    // --- Helper Methods ---

    private JPanel createBasePanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        return p;
    }

    private JPanel createSection(String title, Component[] components) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ACCENT_GOLD),
            title, TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12), ACCENT_GOLD
        ));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        for (Component c : components) {
            p.add(c);
            p.add(Box.createVerticalStrut(5));
        }
        return p;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    private JLabel createStatLabel(String label, String value, Color valueColor) {
        JLabel l = new JLabel("<html>" + label + " <font color='" + toHex(valueColor) + "'>" + value + "</font></html>");
        l.setForeground(TEXT_SECONDARY);
        l.setFont(new Font("Arial", Font.PLAIN, 14));
        return l;
    }

    private String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
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
        
        // Clipboard Support
        JPopupMenu menu = new JPopupMenu();
        JMenuItem copy = new JMenuItem("Copy");
        JMenuItem paste = new JMenuItem("Paste");
        copy.addActionListener(e -> f.copy());
        paste.addActionListener(e -> { f.paste(); onChange.accept(f.getText()); });
        menu.add(copy); menu.add(paste);
        f.setComponentPopupMenu(menu);
        
        return f;
    }

    private JCheckBox createCheckbox(String text, boolean selected, Consumer<Boolean> onChange) {
        JCheckBox c = new JCheckBox(text, selected);
        c.setBackground(BG_CARD);
        c.setForeground(TEXT_PRIMARY);
        c.setFocusPainted(false);
        c.addActionListener(e -> onChange.accept(c.isSelected()));
        return c;
    }

    public void setProfitTracker(ProfitTracker tracker) {
        this.profitTracker = tracker;
    }
}
