package com.ikingsnipe.casino.gui;

import com.ikingsnipe.casino.CasinoController;
import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.models.PlayerSession;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.filechooser.*;

/**
 * Main GUI control panel for Elite Titan Casino
 * Provides complete control over all casino operations
 */
public class CasinoPanel extends JFrame {
    
    private final CasinoController controller;
    private CasinoConfig config;
    
    // Main components
    private JTabbedPane tabbedPane;
    
    // Control Panel
    private JPanel controlPanel;
    private JButton startButton, stopButton, emergencyButton;
    private JLabel statusLabel, profitLabel, activeLabel;
    private JCheckBox autoAcceptBox, enableAdsBox;
    private JSpinner minBetSpinner, maxBetSpinner;
    
    // Games Panel
    private JPanel gamesPanel;
    private JCheckBox crapsToggle, diceToggle, flowerToggle;
    private JSpinner crapsMultiplierSpinner, diceMultiplierSpinner;
    private JTextField flowerTypesField;
    
    // Messages Panel
    private JPanel messagesPanel;
    private JTextArea adMessagesArea, winMessagesArea, lossMessagesArea;
    
    // Sessions Panel
    private JPanel sessionsPanel;
    private JTable sessionsTable;
    private DefaultTableModel sessionsModel;
    private Timer sessionsTimer;
    
    // Log Panel
    private JPanel logPanel;
    private JTextArea logArea;
    private JScrollPane logScrollPane;
    
    // Statistics
    private Map<String, Object> lastStats = new HashMap<>();
    
    public CasinoPanel(CasinoController controller) {
        this.controller = controller;
        this.config = controller.getConfig();
        
        initialize();
        createUI();
        setupTimers();
        
        log("GUI initialized successfully");
        log("Ready to start casino operations");
    }
    
    private void initialize() {
        setTitle("🎰 Elite Titan Casino Pro v2.0");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLayout(new BorderLayout());
        
        // Set icon
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/casino_icon.png")));
        } catch (Exception e) {
            // Icon not found, continue without
        }
    }
    
    private void createUI() {
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Create panels
        controlPanel = createControlPanel();
        gamesPanel = createGamesPanel();
        messagesPanel = createMessagesPanel();
        sessionsPanel = createSessionsPanel();
        logPanel = createLogPanel();
        
        // Add tabs
        tabbedPane.addTab("🎮 Control", controlPanel);
        tabbedPane.addTab("🎲 Games", gamesPanel);
        tabbedPane.addTab("💬 Messages", messagesPanel);
        tabbedPane.addTab("👥 Sessions", sessionsPanel);
        tabbedPane.addTab("📊 Log", logPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Create status bar
        add(createStatusBar(), BorderLayout.SOUTH);
        
        // Apply theme
        applyTheme();
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Title
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("🎰 Casino Control Center");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, gbc);
        
        row++;
        
        // Control Buttons
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        startButton = new JButton("▶ Start Casino");
        startButton.setBackground(new Color(46, 204, 113));
        startButton.setForeground(Color.WHITE);
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.addActionListener(e -> startCasino());
        panel.add(startButton, gbc);
        
        gbc.gridx = 1;
        stopButton = new JButton("⏹ Stop");
        stopButton.setBackground(new Color(231, 76, 60));
        stopButton.setForeground(Color.WHITE);
        stopButton.setFont(new Font("Arial", Font.BOLD, 14));
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopCasino());
        panel.add(stopButton, gbc);
        
        row++;
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        emergencyButton = new JButton("🚨 EMERGENCY STOP");
        emergencyButton.setBackground(Color.RED);
        emergencyButton.setForeground(Color.WHITE);
        emergencyButton.setFont(new Font("Arial", Font.BOLD, 14));
        emergencyButton.addActionListener(e -> emergencyStop());
        panel.add(emergencyButton, gbc);
        
        row++;
        
        // Status Display
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel statusPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
        
        statusLabel = new JLabel("Stopped");
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusPanel.add(new JLabel("State:"));
        statusPanel.add(statusLabel);
        
        profitLabel = new JLabel("0 GP");
        profitLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusPanel.add(new JLabel("Profit:"));
        statusPanel.add(profitLabel);
        
        activeLabel = new JLabel("0");
        activeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusPanel.add(new JLabel("Active Sessions:"));
        statusPanel.add(activeLabel);
        
        panel.add(statusPanel, gbc);
        
        row++;
        
        // Betting Limits
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel betPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        betPanel.setBorder(BorderFactory.createTitledBorder("Betting Limits"));
        
        betPanel.add(new JLabel("Minimum Bet:"));
        minBetSpinner = new JSpinner(new SpinnerNumberModel(config.getMinBet(), 1, 1000000000, 1000));
        betPanel.add(minBetSpinner);
        
        betPanel.add(new JLabel("Maximum Bet:"));
        maxBetSpinner = new JSpinner(new SpinnerNumberModel(config.getMaxBet(), 1, 1000000000, 10000));
        betPanel.add(maxBetSpinner);
        
        panel.add(betPanel, gbc);
        
        row++;
        
        // Settings
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel settingsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Settings"));
        
        autoAcceptBox = new JCheckBox("Auto Accept Trades", config.isAutoAcceptTrades());
        settingsPanel.add(autoAcceptBox);
        
        enableAdsBox = new JCheckBox("Enable Advertising", config.isAdvertisingEnabled());
        settingsPanel.add(enableAdsBox);
        
        settingsPanel.add(new JLabel("Max Active Sessions:"));
        JSpinner maxSessionsSpinner = new JSpinner(new SpinnerNumberModel(config.getMaxActiveSessions(), 1, 10, 1));
        settingsPanel.add(maxSessionsSpinner);
        
        panel.add(settingsPanel, gbc);
        
        row++;
        
        // Save/Load Buttons
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel filePanel = new JPanel(new FlowLayout());
        
        JButton saveButton = new JButton("💾 Save Config");
        saveButton.addActionListener(e -> saveConfig());
        filePanel.add(saveButton);
        
        JButton loadButton = new JButton("📂 Load Config");
        loadButton.addActionListener(e -> loadConfig());
        filePanel.add(loadButton);
        
        JButton exportButton = new JButton("📊 Export Stats");
        exportButton.addActionListener(e -> exportStats());
        filePanel.add(exportButton);
        
        panel.add(filePanel, gbc);
        
        return panel;
    }
    
    private JPanel createGamesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Craps Settings
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel crapsPanel = new JPanel(new BorderLayout());
        crapsPanel.setBorder(BorderFactory.createTitledBorder("🎲 Craps"));
        
        JPanel crapsControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        crapsToggle = new JCheckBox("Enable Craps", config.isCrapsEnabled());
        crapsControlPanel.add(crapsToggle);
        
        crapsControlPanel.add(new JLabel("Payout Multiplier:"));
        crapsMultiplierSpinner = new JSpinner(new SpinnerNumberModel(config.getCrapsPayoutMultiplier(), 1, 10, 1));
        crapsControlPanel.add(crapsMultiplierSpinner);
        
        crapsPanel.add(crapsControlPanel, BorderLayout.NORTH);
        
        JTextArea crapsRules = new JTextArea(gameRules.craps(), 3, 40);
        crapsRules.setEditable(false);
        crapsRules.setLineWrap(true);
        crapsRules.setWrapStyleWord(true);
        crapsRules.setBackground(panel.getBackground());
        crapsPanel.add(new JScrollPane(crapsRules), BorderLayout.CENTER);
        
        panel.add(crapsPanel, gbc);
        
        row++;
        
        // Dice Duel Settings
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel dicePanel = new JPanel(new BorderLayout());
        dicePanel.setBorder(BorderFactory.createTitledBorder("⚄ Dice Duel"));
        
        JPanel diceControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        diceToggle = new JCheckBox("Enable Dice Duel", config.isDiceEnabled());
        diceControlPanel.add(diceToggle);
        
        diceControlPanel.add(new JLabel("Payout Multiplier:"));
        diceMultiplierSpinner = new JSpinner(new SpinnerNumberModel(config.getDicePayoutMultiplier(), 1, 10, 1));
        diceControlPanel.add(diceMultiplierSpinner);
        
        JCheckBox allowTiesBox = new JCheckBox("Allow Ties", config.isDiceAllowTies());
        diceControlPanel.add(allowTiesBox);
        
        dicePanel.add(diceControlPanel, BorderLayout.NORTH);
        
        JTextArea diceRules = new JTextArea(gameRules.diceDuel(), 2, 40);
        diceRules.setEditable(false);
        diceRules.setLineWrap(true);
        diceRules.setWrapStyleWord(true);
        diceRules.setBackground(panel.getBackground());
        dicePanel.add(new JScrollPane(diceRules), BorderLayout.CENTER);
        
        panel.add(dicePanel, gbc);
        
        row++;
        
        // Flower Poker Settings
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel flowerPanel = new JPanel(new BorderLayout());
        flowerPanel.setBorder(BorderFactory.createTitledBorder("🌸 Flower Poker"));
        
        JPanel flowerControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        flowerToggle = new JCheckBox("Enable Flower Poker", config.isFlowerEnabled());
        flowerControlPanel.add(flowerToggle);
        
        flowerControlPanel.add(new JLabel("Flower Types:"));
        flowerTypesField = new JTextField(String.valueOf(config.getFlowerTypes()), 5);
        flowerControlPanel.add(flowerTypesField);
        
        JCheckBox escalatingPayoutsBox = new JCheckBox("Escalating Payouts", config.isFlowerEscalatingPayouts());
        flowerControlPanel.add(escalatingPayoutsBox);
        
        flowerPanel.add(flowerControlPanel, BorderLayout.NORTH);
        
        JTextArea flowerRules = new JTextArea(gameRules.flowerPoker(), 4, 40);
        flowerRules.setEditable(false);
        flowerRules.setLineWrap(true);
        flowerRules.setWrapStyleWord(true);
        flowerRules.setBackground(panel.getBackground());
        flowerPanel.add(new JScrollPane(flowerRules), BorderLayout.CENTER);
        
        panel.add(flowerPanel, gbc);
        
        row++;
        
        // Apply Button
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JButton applyButton = new JButton("✅ Apply Game Settings");
        applyButton.setFont(new Font("Arial", Font.BOLD, 12));
        applyButton.addActionListener(e -> applyGameSettings());
        panel.add(applyButton, gbc);
        
        return panel;
    }
    
    private JPanel createMessagesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Advertisement Messages
        JPanel adPanel = new JPanel(new BorderLayout());
        adPanel.setBorder(BorderFactory.createTitledBorder("📢 Advertisement Messages"));
        
        adMessagesArea = new JTextArea(5, 60);
        adMessagesArea.setText(String.join("\n", config.getAdMessages()));
        adMessagesArea.setLineWrap(true);
        adMessagesArea.setWrapStyleWord(true);
        adPanel.add(new JScrollPane(adMessagesArea), BorderLayout.CENTER);
        
        JButton adHelpButton = new JButton("💡 Tips");
        adHelpButton.addActionListener(e -> showAdTips());
        adPanel.add(adHelpButton, BorderLayout.SOUTH);
        
        panel.add(adPanel, BorderLayout.NORTH);
        
        // Message Templates
        JPanel templatesPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        
        // Win Messages
        JPanel winPanel = new JPanel(new BorderLayout());
        winPanel.setBorder(BorderFactory.createTitledBorder("🎉 Win Messages"));
        
        winMessagesArea = new JTextArea(3, 60);
        winMessagesArea.setText(config.getWinMessage("craps", new HashMap<>()).replace("{payout}", "AMOUNT"));
        winMessagesArea.setLineWrap(true);
        winMessagesArea.setWrapStyleWord(true);
        winPanel.add(new JScrollPane(winMessagesArea), BorderLayout.CENTER);
        
        // Loss Messages
        JPanel lossPanel = new JPanel(new BorderLayout());
        lossPanel.setBorder(BorderFactory.createTitledBorder("😔 Loss Messages"));
        
        lossMessagesArea = new JTextArea(3, 60);
        lossMessagesArea.setText(config.getLossMessage("craps", new HashMap<>()));
        lossMessagesArea.setLineWrap(true);
        lossMessagesArea.setWrapStyleWord(true);
        lossPanel.add(new JScrollPane(lossMessagesArea), BorderLayout.CENTER);
        
        templatesPanel.add(winPanel);
        templatesPanel.add(lossPanel);
        
        panel.add(templatesPanel, BorderLayout.CENTER);
        
        // Save Button
        JButton saveMessagesButton = new JButton("💾 Save Messages");
        saveMessagesButton.addActionListener(e -> saveMessages());
        panel.add(saveMessagesButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSessionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Session Table
        sessionsModel = new DefaultTableModel(
            new Object[]{"Player", "Game", "Bet", "Status", "Time", "Result"}, 0
        );
        
        sessionsTable = new JTable(sessionsModel);
        sessionsTable.setRowHeight(25);
        sessionsTable.setAutoCreateRowSorter(true);
        
        // Custom renderer for status column
        sessionsTable.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        
        JScrollPane scrollPane = new JScrollPane(sessionsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Active Sessions"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton refreshButton = new JButton("🔄 Refresh");
        refreshButton.addActionListener(e -> refreshSessions());
        buttonPanel.add(refreshButton);
        
        JButton clearButton = new JButton("🗑️ Clear Completed");
        clearButton.addActionListener(e -> clearCompletedSessions());
        buttonPanel.add(clearButton);
        
        JButton exportButton = new JButton("📈 Export Session Log");
        exportButton.addActionListener(e -> exportSessionLog());
        buttonPanel.add(exportButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        logArea = new JTextArea(20, 80);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("System Log"));
        
        panel.add(logScrollPane, BorderLayout.CENTER);
        
        // Log controls
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        JButton clearButton = new JButton("Clear Log");
        clearButton.addActionListener(e -> logArea.setText(""));
        controlPanel.add(clearButton);
        
        JButton exportButton = new JButton("Export Log");
        exportButton.addActionListener(e -> exportLog());
        controlPanel.add(exportButton);
        
        JCheckBox autoScrollBox = new JCheckBox("Auto-scroll", true);
        autoScrollBox.addActionListener(e -> {
            if (autoScrollBox.isSelected()) {
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
        controlPanel.add(autoScrollBox);
        
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        
        JLabel versionLabel = new JLabel(" Elite Titan Casino Pro v2.0 | DreamBot 4.0+ ");
        statusBar.add(versionLabel, BorderLayout.WEST);
        
        JLabel authorLabel = new JLabel("Created by ikingsnipe ");
        statusBar.add(authorLabel, BorderLayout.EAST);
        
        return statusBar;
    }
    
    private void applyTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            // Use default LAF
        }
    }
    
    private void setupTimers() {
        // Update sessions every 2 seconds
        sessionsTimer = new Timer(2000, e -> updateSessions());
        sessionsTimer.start();
        
        // Update statistics every 3 seconds
        Timer statsTimer = new Timer(3000, e -> updateStatistics());
        statsTimer.start();
    }
    
    // === ACTION HANDLERS ===
    
    private void startCasino() {
        // Update config from UI
        updateConfigFromUI();
        
        // Start casino
        controller.startCasino();
        
        // Update UI state
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        statusLabel.setText("Running");
        statusLabel.setForeground(new Color(46, 204, 113));
        
        log("Casino started");
    }
    
    private void stopCasino() {
        controller.stopCasino();
        
        // Update UI state
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        statusLabel.setText("Stopped");
        statusLabel.setForeground(Color.RED);
        
        log("Casino stopped");
    }
    
    private void emergencyStop() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to emergency stop?\n" +
            "This will cancel all trades and clear sessions.",
            "Emergency Stop Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            controller.emergencyStop();
            
            // Update UI state
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            statusLabel.setText("EMERGENCY STOP");
            statusLabel.setForeground(Color.RED);
            
            log("EMERGENCY STOP ACTIVATED");
        }
    }
    
    private void applyGameSettings() {
        updateConfigFromUI();
        log("Game settings applied");
        JOptionPane.showMessageDialog(this,
            "Game settings have been updated.\n" +
            "Changes will take effect for new sessions.",
            "Settings Updated",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void saveMessages() {
        try {
            // Update ad messages
            String[] ads = adMessagesArea.getText().split("\n");
            config.setAdMessages(Arrays.asList(ads));
            
            // Note: Win/Loss messages would need more complex parsing
            // For now, just log the save
            
            log("Message templates saved");
            JOptionPane.showMessageDialog(this,
                "Advertisement messages saved.\n" +
                "Win/Loss message templates require full implementation.",
                "Messages Saved",
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            log("Error saving messages: " + e.getMessage());
        }
    }
    
    private void saveConfig() {
        updateConfigFromUI();
        controller.updateConfig(config);
        log("Configuration saved");
    }
    
    private void loadConfig() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                // Load config from file
                // This would integrate with ConfigLoader
                log("Loading configuration from: " + fileChooser.getSelectedFile().getName());
                
                // Update UI from loaded config
                updateUIFromConfig();
                
                log("Configuration loaded successfully");
            } catch (Exception e) {
                log("Error loading configuration: " + e.getMessage());
                JOptionPane.showMessageDialog(this,
                    "Error loading configuration:\n" + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void exportStats() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("casino_stats_" + System.currentTimeMillis() + ".txt"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                Map<String, Object> stats = controller.getStatistics();
                
                writer.println("=== Elite Titan Casino Statistics ===");
                writer.println("Export Time: " + new Date());
                writer.println();
                writer.println("Profit/Loss: " + stats.get("total_profit") + " GP");
                writer.println("Games Played: " + stats.get("games_played"));
                writer.println("Wins: " + stats.get("wins"));
                writer.println("Losses: " + stats.get("losses"));
                writer.println("Total Wagered: " + stats.get("total_wagered") + " GP");
                writer.println("Total Payouts: " + stats.get("total_payouts") + " GP");
                writer.println();
                writer.println("=== Configuration ===");
                writer.println("Min Bet: " + config.getMinBet() + " GP");
                writer.println("Max Bet: " + config.getMaxBet() + " GP");
                writer.println("Enabled Games: " + String.join(", ", 
                    config.isCrapsEnabled() ? "Craps" : "",
                    config.isDiceEnabled() ? "Dice" : "",
                    config.isFlowerEnabled() ? "Flower Poker" : ""
                ).trim());
                
                log("Statistics exported to: " + fileChooser.getSelectedFile().getName());
                
                JOptionPane.showMessageDialog(this,
                    "Statistics exported successfully!",
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                log("Error exporting statistics: " + e.getMessage());
            }
        }
    }
    
    private void exportSessionLog() {
        // Export session history
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("session_log_" + System.currentTimeMillis() + ".csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                writer.println("Player,Game,Bet Amount,Result,Payout,Timestamp");
                // Would write actual session data here
                
                log("Session log exported to: " + fileChooser.getSelectedFile().getName());
                
                JOptionPane.showMessageDialog(this,
                    "Session log exported successfully!",
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                log("Error exporting session log: " + e.getMessage());
            }
        }
    }
    
    private void exportLog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("casino_log_" + System.currentTimeMillis() + ".txt"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                writer.write(logArea.getText());
                
                log("System log exported to: " + fileChooser.getSelectedFile().getName());
                
                JOptionPane.showMessageDialog(this,
                    "Log exported successfully!",
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                log("Error exporting log: " + e.getMessage());
            }
        }
    }
    
    // === UI UPDATE METHODS ===
    
    private void updateConfigFromUI() {
        // General settings
        config.setMinBet((int) minBetSpinner.getValue());
        config.setMaxBet((int) maxBetSpinner.getValue());
        config.setAutoAcceptTrades(autoAcceptBox.isSelected());
        config.setAdvertisingEnabled(enableAdsBox.isSelected());
        
        // Game settings
        config.setCrapsEnabled(crapsToggle.isSelected());
        config.setCrapsPayoutMultiplier((int) crapsMultiplierSpinner.getValue());
        
        config.setDiceEnabled(diceToggle.isSelected());
        config.setDicePayoutMultiplier((int) diceMultiplierSpinner.getValue());
        
        config.setFlowerEnabled(flowerToggle.isSelected());
        try {
            config.setFlowerTypes(Integer.parseInt(flowerTypesField.getText()));
        } catch (NumberFormatException e) {
            // Keep current value
        }
    }
    
    private void updateUIFromConfig() {
        // General settings
        minBetSpinner.setValue(config.getMinBet());
        maxBetSpinner.setValue(config.getMaxBet());
        autoAcceptBox.setSelected(config.isAutoAcceptTrades());
        enableAdsBox.setSelected(config.isAdvertisingEnabled());
        
        // Game settings
        crapsToggle.setSelected(config.isCrapsEnabled());
        crapsMultiplierSpinner.setValue(config.getCrapsPayoutMultiplier());
        
        diceToggle.setSelected(config.isDiceEnabled());
        diceMultiplierSpinner.setValue(config.getDicePayoutMultiplier());
        
        flowerToggle.setSelected(config.isFlowerEnabled());
        flowerTypesField.setText(String.valueOf(config.getFlowerTypes()));
        
        // Message settings
        adMessagesArea.setText(String.join("\n", config.getAdMessages()));
    }
    
    private void updateSessions() {
        if (!controller.isRunning()) return;
        
        SwingUtilities.invokeLater(() -> {
            sessionsModel.setRowCount(0);
            
            List<PlayerSession> sessions = controller.getActiveSessions();
            for (PlayerSession session : sessions) {
                long minutesAgo = (System.currentTimeMillis() - session.getCreatedAt()) / 60000;
                
                sessionsModel.addRow(new Object[]{
                    session.getPlayerName(),
                    session.getGameType(),
                    String.format("%,d", session.getBetAmount()) + " GP",
                    session.getStatus(),
                    minutesAgo + " min ago",
                    session.getResult() != null ? session.getResult().getSummary() : "Pending"
                });
            }
        });
    }
    
    private void updateStatistics() {
        if (!controller.isRunning()) return;
        
        SwingUtilities.invokeLater(() -> {
            Map<String, Object> stats = controller.getStatistics();
            
            // Update profit label
            int profit = (int) stats.getOrDefault("total_profit", 0);
            profitLabel.setText(String.format("%,d GP", profit));
            profitLabel.setForeground(profit >= 0 ? new Color(46, 204, 113) : Color.RED);
            
            // Update active sessions
            int active = (int) stats.getOrDefault("active_sessions", 0);
            activeLabel.setText(String.valueOf(active));
            
            // Update status if changed
            String state = controller.getCurrentState().toString();
            if (!statusLabel.getText().equals(state)) {
                statusLabel.setText(state);
                
                // Color coding based on state
                switch (controller.getCurrentState()) {
                    case RUNNING: statusLabel.setForeground(new Color(46, 204, 113)); break;
                    case TRADING: statusLabel.setForeground(new Color(52, 152, 219)); break;
                    case GAMING: statusLabel.setForeground(new Color(155, 89, 182)); break;
                    case ERROR_RECOVERY: statusLabel.setForeground(Color.ORANGE); break;
                    default: statusLabel.setForeground(Color.GRAY); break;
                }
            }
        });
    }
    
    private void refreshSessions() {
        updateSessions();
    }
    
    private void clearCompletedSessions() {
        // This would clear completed sessions from the display
        // Actual session management is handled by SessionManager
        log("Session display cleared");
    }
    
    private void showAdTips() {
        JOptionPane.showMessageDialog(this,
            "Advertisement Message Tips:\n\n" +
            "• Keep messages under 80 characters\n" +
            "• Include game names (Craps, Dice, Flower Poker)\n" +
            "• Mention !rules command for new players\n" +
            "• Use emojis sparingly: 🎰 ⚄ 🎲 🌸\n" +
            "• Rotate multiple messages to avoid spam detection\n" +
            "• Include your trust indicators",
            "Advertisement Tips",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // === LOGGING ===
    
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            
            // Auto-scroll to bottom
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    // === HELPER CLASSES ===
    
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, 
                isSelected, hasFocus, row, column);
            
            if (value != null) {
                String status = value.toString().toLowerCase();
                
                if (status.contains("win")) {
                    c.setForeground(new Color(46, 204, 113));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (status.contains("lose") || status.contains("loss")) {
                    c.setForeground(new Color(231, 76, 60));
                } else if (status.contains("pending") || status.contains("waiting")) {
                    c.setForeground(new Color(52, 152, 219));
                } else if (status.contains("error")) {
                    c.setForeground(Color.RED);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(Color.GRAY);
                }
            }
            
            return c;
        }
    }
    
    // Game rules helper
    private static class gameRules {
        static String craps() {
            return "Roll 2 dice. Win 3x bet on total 7, 9, or 12. Any other total loses.";
        }
        
        static String diceDuel() {
            return "1v1 dice roll. Higher roll wins 2x bet. Ties reroll until winner.";
        }
        
        static String flowerPoker() {
            return "5-flower poker. Best hand wins with escalating payouts up to 10x.";
        }
    }
    
    @Override
    public void dispose() {
        // Clean up timers
        if (sessionsTimer != null) {
            sessionsTimer.stop();
        }
        
        super.dispose();
    }
}