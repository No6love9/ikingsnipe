package com.ikingsnipe.casino.gui;

import com.ikingsnipe.casino.models.AdminConfig;
import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.models.TradeConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.function.Consumer;

public class CasinoGUI extends JFrame {
    private final CasinoConfig config;
    private final Consumer<Boolean> onFinish;

    public CasinoGUI(CasinoConfig config, Consumer<Boolean> onFinish) {
        this.config = config;
        this.onFinish = onFinish;

        setTitle("snipes♧scripts Enterprise v4.0");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(new Color(30, 30, 35));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(40, 40, 45));
        tabs.setForeground(Color.WHITE);
        
        // --- General Tab ---
        JPanel generalTab = createTabPanel();
        generalTab.add(createBettingPanel(config));
        generalTab.add(createLocationPanel(config));
        tabs.addTab("General", generalTab);

        // --- Games Tab ---
        tabs.addTab("Games", createGamesTab(config));
        
        // --- Trade Tab (NEW) ---
        tabs.addTab("Trade", createTradeTab(config));
        
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

        // --- Admin Tab (NEW) ---
        tabs.addTab("Admin", createAdminTab(config));

        // --- Chat Tab (NEW) ---
        tabs.addTab("Chat", createChatTab(config));

        // --- Advanced Tab ---
        tabs.addTab("Advanced", createAdvancedTab(config));

        main.add(tabs);
        main.add(Box.createRigidArea(new Dimension(0, 15)));

        // Version info
        JLabel versionLabel = new JLabel("v4.0 - Advanced Trade Handling System");
        versionLabel.setForeground(new Color(150, 150, 150));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        main.add(versionLabel);
        main.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton startBtn = new JButton("LAUNCH ENTERPRISE SYSTEM");
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.setBackground(new Color(0, 255, 127));
        startBtn.setForeground(Color.BLACK);
        startBtn.setFont(new Font("Arial", Font.BOLD, 14));
        startBtn.setFocusPainted(false);
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
        
        JTextField minField = new JTextField(String.valueOf(config.minBet), 12);
        JTextField maxField = new JTextField(String.valueOf(config.maxBet), 12);
        
        styleTextField(minField);
        styleTextField(maxField);
        
        p.add(createLabel("Min Bet:"));
        p.add(minField);
        p.add(Box.createHorizontalStrut(10));
        p.add(createLabel("Max Bet:"));
        p.add(maxField);
        
        minField.addActionListener(e -> {
            try { config.minBet = Long.parseLong(minField.getText().replace(",", "")); }
            catch (Exception ex) { minField.setText(String.valueOf(config.minBet)); }
        });
        maxField.addActionListener(e -> {
            try { config.maxBet = Long.parseLong(maxField.getText().replace(",", "")); }
            catch (Exception ex) { maxField.setText(String.valueOf(config.maxBet)); }
        });
        
        return p;
    }
    
    private JPanel createLocationPanel(CasinoConfig config) {
        JPanel p = createSection("Location Settings");
        
        JComboBox<CasinoConfig.LocationPreset> locationCombo = new JComboBox<>(CasinoConfig.LocationPreset.values());
        locationCombo.setSelectedItem(config.locationPreset);
        locationCombo.addActionListener(e -> config.locationPreset = (CasinoConfig.LocationPreset) locationCombo.getSelectedItem());
        
        JCheckBox walkOnStart = new JCheckBox("Walk on Start", config.walkOnStart);
        styleCheckbox(walkOnStart);
        walkOnStart.addActionListener(e -> config.walkOnStart = walkOnStart.isSelected());
        
        p.add(createLabel("Location:"));
        p.add(locationCombo);
        p.add(Box.createHorizontalStrut(10));
        p.add(walkOnStart);
        
        return p;
    }

    /**
     * Create the Chat configuration tab
     */
    private JPanel createChatTab(CasinoConfig config) {
        JPanel mainPanel = createTabPanel();
        
        // Chat AI Settings
        JPanel aiPanel = createSection("Chat AI & Automation");
        JCheckBox chatAIEnabled = new JCheckBox("Enable Chat AI", config.chatAIEnabled);
        styleCheckbox(chatAIEnabled);
        chatAIEnabled.addActionListener(e -> config.chatAIEnabled = chatAIEnabled.isSelected());
        
        JCheckBox autoReplyScam = new JCheckBox("Auto-Reply to Scam Accusations", config.autoReplyToScamAccusations);
        styleCheckbox(autoReplyScam);
        autoReplyScam.addActionListener(e -> config.autoReplyToScamAccusations = autoReplyScam.isSelected());
        
        aiPanel.add(chatAIEnabled);
        aiPanel.add(Box.createHorizontalStrut(10));
        aiPanel.add(autoReplyScam);
        mainPanel.add(aiPanel);
        
        // Announcements
        JPanel announcePanel = createSection("Announcements");
        JCheckBox autoAnnounceWins = new JCheckBox("Auto-Announce Big Wins", config.autoAnnounceBigWins);
        styleCheckbox(autoAnnounceWins);
        autoAnnounceWins.addActionListener(e -> config.autoAnnounceBigWins = autoAnnounceWins.isSelected());
        
        JTextField thresholdField = new JTextField(String.valueOf(config.bigWinThreshold), 10);
        styleTextField(thresholdField);
        thresholdField.addActionListener(e -> {
            try { config.bigWinThreshold = Long.parseLong(thresholdField.getText().replace(",", "")); }
            catch (Exception ex) { thresholdField.setText(String.valueOf(config.bigWinThreshold)); }
        });
        
        announcePanel.add(autoAnnounceWins);
        announcePanel.add(Box.createHorizontalStrut(10));
        announcePanel.add(createLabel("Threshold:"));
        announcePanel.add(thresholdField);
        mainPanel.add(announcePanel);
        
        // Clan Chat Settings
        JPanel clanPanel = createSection("Clan Chat Integration");
        JCheckBox clanEnabled = new JCheckBox("Enable Clan Chat", config.clanChatEnabled);
        styleCheckbox(clanEnabled);
        clanEnabled.addActionListener(e -> config.clanChatEnabled = clanEnabled.isSelected());
        
        JCheckBox clanAnnounce = new JCheckBox("Announce Wins in Clan", config.clanChatAnnounceWins);
        styleCheckbox(clanAnnounce);
        clanAnnounce.addActionListener(e -> config.clanChatAnnounceWins = clanAnnounce.isSelected());
        
        JCheckBox clanRespond = new JCheckBox("Respond to Clan Commands", config.clanChatRespondToCommands);
        styleCheckbox(clanRespond);
        clanRespond.addActionListener(e -> config.clanChatRespondToCommands = clanRespond.isSelected());
        
        clanPanel.add(clanEnabled);
        clanPanel.add(clanAnnounce);
        clanPanel.add(clanRespond);
        mainPanel.add(clanPanel);
        
        return mainPanel;
    }

    /**
     * Create the Admin/Owner configuration tab
     */
    private JPanel createAdminTab(CasinoConfig config) {
        JPanel mainPanel = createTabPanel();
        AdminConfig ac = config.adminConfig;

        // Login Panel
        JPanel loginPanel = createSection("Admin Authentication");
        JPasswordField passField = new JPasswordField(10);
        JButton loginBtn = new JButton("Unlock Admin Rights");
        styleButton(loginBtn);
        
        JPanel contentWrapper = new JPanel(new CardLayout());
        contentWrapper.setOpaque(false);
        
        JPanel lockedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        lockedPanel.setOpaque(false);
        lockedPanel.add(createLabel("Password:"));
        lockedPanel.add(passField);
        lockedPanel.add(loginBtn);
        
        JPanel unlockedPanel = new JPanel();
        unlockedPanel.setLayout(new BoxLayout(unlockedPanel, BoxLayout.Y_AXIS));
        unlockedPanel.setOpaque(false);
        
        // --- Admin Controls (Visible only when unlocked) ---
        
        // System Overrides
        JPanel systemPanel = createSection("System Overrides");
        JCheckBox emergencyStop = new JCheckBox("EMERGENCY STOP", ac.emergencyStop);
        emergencyStop.setForeground(Color.RED);
        emergencyStop.setFont(new Font("Arial", Font.BOLD, 12));
        emergencyStop.addActionListener(e -> ac.emergencyStop = emergencyStop.isSelected());
        
        JCheckBox disableGames = new JCheckBox("Disable All Games", ac.disableAllGames);
        styleCheckbox(disableGames);
        disableGames.addActionListener(e -> ac.disableAllGames = disableGames.isSelected());
        
        systemPanel.add(emergencyStop);
        systemPanel.add(Box.createHorizontalStrut(20));
        systemPanel.add(disableGames);
        unlockedPanel.add(systemPanel);
        
        // Player Management
        JPanel playerPanel = createSection("Player Management");
        JTextField blacklistField = new JTextField(10);
        JButton addBlacklist = new JButton("Blacklist");
        styleButton(addBlacklist);
        addBlacklist.addActionListener(e -> {
            String name = blacklistField.getText().trim();
            if (!name.isEmpty()) {
                ac.blacklistPlayer(name);
                blacklistField.setText("");
                JOptionPane.showMessageDialog(this, name + " added to blacklist.");
            }
        });
        
        playerPanel.add(createLabel("Player Name:"));
        playerPanel.add(blacklistField);
        playerPanel.add(addBlacklist);
        unlockedPanel.add(playerPanel);
        
        // Advanced Settings
        JPanel advancedAdmin = createSection("Advanced Admin Settings");
        JCheckBox verboseLogs = new JCheckBox("Verbose Admin Logs", ac.enableVerboseAdminLogs);
        styleCheckbox(verboseLogs);
        verboseLogs.addActionListener(e -> ac.enableVerboseAdminLogs = verboseLogs.isSelected());
        
        advancedAdmin.add(verboseLogs);
        unlockedPanel.add(advancedAdmin);

        contentWrapper.add(lockedPanel, "LOCKED");
        contentWrapper.add(unlockedPanel, "UNLOCKED");
        
        loginBtn.addActionListener(e -> {
            String input = new String(passField.getPassword());
            if (input.equals(ac.adminPassword)) {
                ac.isAdminModeEnabled = true;
                ((CardLayout)contentWrapper.getLayout()).show(contentWrapper, "UNLOCKED");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Admin Password", "Access Denied", JOptionPane.ERROR_MESSAGE);
            }
        });

        mainPanel.add(loginPanel);
        mainPanel.add(contentWrapper);
        
        // If already logged in (e.g. GUI reopened)
        if (ac.isAdminModeEnabled) {
            ((CardLayout)contentWrapper.getLayout()).show(contentWrapper, "UNLOCKED");
        }

        return mainPanel;
    }

    private void styleButton(JButton btn) {
        btn.setBackground(new Color(60, 60, 70));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 110)),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
    }

    /**
     * Create comprehensive Trade configuration tab
     */
    private JPanel createTradeTab(CasinoConfig config) {
        JPanel mainPanel = createTabPanel();
        TradeConfig tc = config.tradeConfig;
        
        // Trade Preset Selection
        JPanel presetPanel = createSection("Trade Preset");
        JComboBox<TradeConfig.TradePreset> presetCombo = new JComboBox<>(TradeConfig.TradePreset.values());
        presetCombo.setSelectedItem(config.tradePreset);
        presetCombo.addActionListener(e -> {
            TradeConfig.TradePreset selected = (TradeConfig.TradePreset) presetCombo.getSelectedItem();
            config.applyTradePreset(selected);
            // Refresh the tab to show new values
            JOptionPane.showMessageDialog(this, 
                "Preset applied: " + selected.getDescription() + "\n\nRestart GUI to see updated values.",
                "Preset Applied", JOptionPane.INFORMATION_MESSAGE);
        });
        
        JLabel presetDesc = new JLabel("<html><i>" + config.tradePreset.getDescription() + "</i></html>");
        presetDesc.setForeground(Color.LIGHT_GRAY);
        
        presetPanel.add(createLabel("Preset:"));
        presetPanel.add(presetCombo);
        presetPanel.add(Box.createHorizontalStrut(10));
        presetPanel.add(presetDesc);
        mainPanel.add(presetPanel);
        
        // Anti-Scam Settings
        JPanel antiScamPanel = createSection("Anti-Scam Protection");
        
        JCheckBox enableAntiScam = new JCheckBox("Enable Anti-Scam", tc.enableAntiScam);
        styleCheckbox(enableAntiScam);
        enableAntiScam.addActionListener(e -> tc.enableAntiScam = enableAntiScam.isSelected());
        
        JCheckBox screen2Verify = new JCheckBox("Screen 2 Verification", tc.enableScreen2Verification);
        styleCheckbox(screen2Verify);
        screen2Verify.addActionListener(e -> tc.enableScreen2Verification = screen2Verify.isSelected());
        
        JSpinner stabilityTime = new JSpinner(new SpinnerNumberModel(tc.valueStabilityTime, 100, 5000, 100));
        stabilityTime.addChangeListener(e -> tc.valueStabilityTime = (int) stabilityTime.getValue());
        
        antiScamPanel.add(enableAntiScam);
        antiScamPanel.add(Box.createHorizontalStrut(10));
        antiScamPanel.add(screen2Verify);
        antiScamPanel.add(Box.createHorizontalStrut(10));
        antiScamPanel.add(createLabel("Stability (ms):"));
        antiScamPanel.add(stabilityTime);
        mainPanel.add(antiScamPanel);
        
        // Verification Thresholds
        JPanel thresholdPanel = createSection("Value Thresholds");
        
        JTextField medThreshold = new JTextField(String.valueOf(tc.mediumValueThreshold), 10);
        JTextField highThreshold = new JTextField(String.valueOf(tc.highValueThreshold), 10);
        styleTextField(medThreshold);
        styleTextField(highThreshold);
        
        medThreshold.addActionListener(e -> {
            try { tc.mediumValueThreshold = Long.parseLong(medThreshold.getText().replace(",", "")); }
            catch (Exception ex) { medThreshold.setText(String.valueOf(tc.mediumValueThreshold)); }
        });
        highThreshold.addActionListener(e -> {
            try { tc.highValueThreshold = Long.parseLong(highThreshold.getText().replace(",", "")); }
            catch (Exception ex) { highThreshold.setText(String.valueOf(tc.highValueThreshold)); }
        });
        
        thresholdPanel.add(createLabel("Medium Value:"));
        thresholdPanel.add(medThreshold);
        thresholdPanel.add(Box.createHorizontalStrut(10));
        thresholdPanel.add(createLabel("High Value:"));
        thresholdPanel.add(highThreshold);
        mainPanel.add(thresholdPanel);
        
        // Verification Counts
        JPanel verifyCountPanel = createSection("Verification Counts");
        
        JSpinner lowCount = new JSpinner(new SpinnerNumberModel(tc.lowValueVerifyCount, 1, 10, 1));
        JSpinner medCount = new JSpinner(new SpinnerNumberModel(tc.mediumValueVerifyCount, 1, 10, 1));
        JSpinner highCount = new JSpinner(new SpinnerNumberModel(tc.highValueVerifyCount, 1, 10, 1));
        
        lowCount.addChangeListener(e -> tc.lowValueVerifyCount = (int) lowCount.getValue());
        medCount.addChangeListener(e -> tc.mediumValueVerifyCount = (int) medCount.getValue());
        highCount.addChangeListener(e -> tc.highValueVerifyCount = (int) highCount.getValue());
        
        verifyCountPanel.add(createLabel("Low:"));
        verifyCountPanel.add(lowCount);
        verifyCountPanel.add(Box.createHorizontalStrut(5));
        verifyCountPanel.add(createLabel("Med:"));
        verifyCountPanel.add(medCount);
        verifyCountPanel.add(Box.createHorizontalStrut(5));
        verifyCountPanel.add(createLabel("High:"));
        verifyCountPanel.add(highCount);
        mainPanel.add(verifyCountPanel);
        
        // Timing Settings
        JPanel timingPanel = createSection("Timing (milliseconds)");
        
        JSpinner tradeTimeout = new JSpinner(new SpinnerNumberModel(tc.tradeTimeout / 1000, 10, 300, 5));
        JSpinner screen2Timeout = new JSpinner(new SpinnerNumberModel(tc.screen2Timeout / 1000, 10, 120, 5));
        JSpinner acceptTimeout = new JSpinner(new SpinnerNumberModel(tc.tradeAcceptTimeout / 1000, 1, 30, 1));
        
        tradeTimeout.addChangeListener(e -> tc.tradeTimeout = (int) tradeTimeout.getValue() * 1000);
        screen2Timeout.addChangeListener(e -> tc.screen2Timeout = (int) screen2Timeout.getValue() * 1000);
        acceptTimeout.addChangeListener(e -> tc.tradeAcceptTimeout = (int) acceptTimeout.getValue() * 1000);
        
        timingPanel.add(createLabel("Trade (s):"));
        timingPanel.add(tradeTimeout);
        timingPanel.add(Box.createHorizontalStrut(5));
        timingPanel.add(createLabel("Screen2 (s):"));
        timingPanel.add(screen2Timeout);
        timingPanel.add(Box.createHorizontalStrut(5));
        timingPanel.add(createLabel("Accept (s):"));
        timingPanel.add(acceptTimeout);
        mainPanel.add(timingPanel);
        
        // Messaging Settings
        JPanel msgPanel = createSection("Trade Messages");
        
        JCheckBox sendWelcome = new JCheckBox("Welcome Msg", tc.sendWelcomeMessage);
        JCheckBox sendCommands = new JCheckBox("Game Commands", tc.sendGameCommands);
        JCheckBox sendConfirm = new JCheckBox("Confirmations", tc.sendConfirmationMessages);
        
        styleCheckbox(sendWelcome);
        styleCheckbox(sendCommands);
        styleCheckbox(sendConfirm);
        
        sendWelcome.addActionListener(e -> tc.sendWelcomeMessage = sendWelcome.isSelected());
        sendCommands.addActionListener(e -> tc.sendGameCommands = sendCommands.isSelected());
        sendConfirm.addActionListener(e -> tc.sendConfirmationMessages = sendConfirm.isSelected());
        
        msgPanel.add(sendWelcome);
        msgPanel.add(sendCommands);
        msgPanel.add(sendConfirm);
        mainPanel.add(msgPanel);
        
        // Custom Welcome Message
        JPanel welcomePanel = createSection("Custom Welcome Message");
        JTextField welcomeField = new JTextField(tc.customWelcomeMessage, 40);
        styleTextField(welcomeField);
        welcomeField.addActionListener(e -> tc.customWelcomeMessage = welcomeField.getText());
        
        JLabel placeholders = new JLabel("<html><small>Placeholders: {player}, {hash}, {min}, {max}</small></html>");
        placeholders.setForeground(Color.GRAY);
        
        welcomePanel.add(welcomeField);
        welcomePanel.add(placeholders);
        mainPanel.add(welcomePanel);
        
        // Player Experience
        JPanel expPanel = createSection("Player Experience");
        
        JCheckBox fastAccept = new JCheckBox("Fast Accept Returning", tc.enableFastAcceptReturning);
        JCheckBox reducedVerify = new JCheckBox("Reduced Verify Trusted", tc.reducedVerifyForTrusted);
        JCheckBox autoSmall = new JCheckBox("Auto-Accept Small Bets", tc.autoAcceptSmallBets);
        JCheckBox checkPayout = new JCheckBox("Check Payout Capacity", tc.checkPayoutCapacity);
        
        styleCheckbox(fastAccept);
        styleCheckbox(reducedVerify);
        styleCheckbox(autoSmall);
        styleCheckbox(checkPayout);
        
        fastAccept.addActionListener(e -> tc.enableFastAcceptReturning = fastAccept.isSelected());
        reducedVerify.addActionListener(e -> tc.reducedVerifyForTrusted = reducedVerify.isSelected());
        autoSmall.addActionListener(e -> tc.autoAcceptSmallBets = autoSmall.isSelected());
        checkPayout.addActionListener(e -> tc.checkPayoutCapacity = checkPayout.isSelected());
        
        expPanel.add(fastAccept);
        expPanel.add(reducedVerify);
        expPanel.add(autoSmall);
        expPanel.add(checkPayout);
        mainPanel.add(expPanel);
        
        // Trusted Player Settings
        JPanel trustPanel = createSection("Trusted Player Settings");
        
        JSpinner trustedCount = new JSpinner(new SpinnerNumberModel(tc.trustedPlayerTradeCount, 1, 20, 1));
        trustedCount.addChangeListener(e -> tc.trustedPlayerTradeCount = (int) trustedCount.getValue());
        
        JTextField smallThreshold = new JTextField(String.valueOf(tc.smallBetThreshold), 10);
        styleTextField(smallThreshold);
        smallThreshold.addActionListener(e -> {
            try { tc.smallBetThreshold = Long.parseLong(smallThreshold.getText().replace(",", "")); }
            catch (Exception ex) { smallThreshold.setText(String.valueOf(tc.smallBetThreshold)); }
        });
        
        trustPanel.add(createLabel("Trades to Trust:"));
        trustPanel.add(trustedCount);
        trustPanel.add(Box.createHorizontalStrut(10));
        trustPanel.add(createLabel("Small Bet Limit:"));
        trustPanel.add(smallThreshold);
        mainPanel.add(trustPanel);
        
        // Advanced Trade Settings
        JPanel advPanel = createSection("Advanced");
        
        JCheckBox enableQueue = new JCheckBox("Trade Queue", tc.enableTradeQueue);
        JCheckBox verboseLog = new JCheckBox("Verbose Logging", tc.verboseLogging);
        JCheckBox trackStats = new JCheckBox("Track Statistics", tc.trackTradeStats);
        
        styleCheckbox(enableQueue);
        styleCheckbox(verboseLog);
        styleCheckbox(trackStats);
        
        enableQueue.addActionListener(e -> tc.enableTradeQueue = enableQueue.isSelected());
        verboseLog.addActionListener(e -> tc.verboseLogging = verboseLog.isSelected());
        trackStats.addActionListener(e -> tc.trackTradeStats = trackStats.isSelected());
        
        JSpinner maxDistance = new JSpinner(new SpinnerNumberModel(tc.maxTradeDistance, 1, 50, 1));
        maxDistance.addChangeListener(e -> tc.maxTradeDistance = (int) maxDistance.getValue());
        
        advPanel.add(enableQueue);
        advPanel.add(verboseLog);
        advPanel.add(trackStats);
        advPanel.add(Box.createHorizontalStrut(10));
        advPanel.add(createLabel("Max Distance:"));
        advPanel.add(maxDistance);
        mainPanel.add(advPanel);
        
        return mainPanel;
    }

    private JPanel createAdTab(CasinoConfig config) {
        JPanel p = createTabPanel();
        JPanel section = createSection("Anti-Mute Advertising");
        
        JCheckBox antiMute = new JCheckBox("Enable Anti-Mute Variations", config.enableAntiMute);
        styleCheckbox(antiMute);
        antiMute.addActionListener(e -> config.enableAntiMute = antiMute.isSelected());
        section.add(antiMute);
        
        JSpinner interval = new JSpinner(new SpinnerNumberModel(config.adIntervalSeconds, 10, 300, 5));
        interval.addChangeListener(e -> config.adIntervalSeconds = (int) interval.getValue());
        section.add(Box.createHorizontalStrut(10));
        section.add(createLabel("Interval (s):"));
        section.add(interval);
        
        p.add(section);
        
        // Ad Messages List
        JPanel listSection = createSection("Advertisement Messages");
        DefaultListModel<String> model = new DefaultListModel<>();
        config.adMessages.forEach(model::addElement);
        JList<String> list = new JList<>(model);
        list.setBackground(new Color(50, 50, 55));
        list.setForeground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(450, 100));
        listSection.add(scroll);
        
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addPanel.setBackground(new Color(45, 45, 50));
        JTextField newAd = new JTextField(30);
        styleTextField(newAd);
        JButton addBtn = createStyledButton("Add", new Color(0, 200, 100));
        JButton removeBtn = createStyledButton("Remove", new Color(200, 80, 80));
        
        addBtn.addActionListener(e -> {
            if (!newAd.getText().isEmpty()) {
                config.adMessages.add(newAd.getText());
                model.addElement(newAd.getText());
                newAd.setText("");
            }
        });
        
        removeBtn.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx >= 0) {
                config.adMessages.remove(idx);
                model.remove(idx);
            }
        });
        
        addPanel.add(newAd);
        addPanel.add(addBtn);
        addPanel.add(removeBtn);
        listSection.add(addPanel);
        p.add(listSection);
        
        return p;
    }

    private JPanel createGamesTab(CasinoConfig config) {
        JPanel p = new JPanel(new GridLayout(0, 2, 10, 10));
        p.setBackground(new Color(30, 30, 35));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        config.games.forEach((id, settings) -> {
            JPanel gamePanel = createSection(settings.name);
            
            JCheckBox enabled = new JCheckBox("Enabled", settings.enabled);
            styleCheckbox(enabled);
            
            JTextField mult = new JTextField(String.valueOf(settings.multiplier), 5);
            styleTextField(mult);
            
            gamePanel.add(enabled);
            gamePanel.add(createLabel("Multiplier:"));
            gamePanel.add(mult);
            
            enabled.addActionListener(e -> settings.enabled = enabled.isSelected());
            mult.addActionListener(e -> {
                try { settings.multiplier = Double.parseDouble(mult.getText()); }
                catch (Exception ex) { mult.setText(String.valueOf(settings.multiplier)); }
            });
            
            p.add(gamePanel);
        });
        
        return p;
    }

    private JPanel createJackpotPanel(CasinoConfig config) {
        JPanel p = createSection("Global Jackpot");
        
        JCheckBox enabled = new JCheckBox("Enable Jackpot", config.jackpotEnabled);
        styleCheckbox(enabled);
        enabled.addActionListener(e -> config.jackpotEnabled = enabled.isSelected());
        
        JTextField contrib = new JTextField(String.valueOf(config.jackpotContributionPercent), 5);
        styleTextField(contrib);
        contrib.addActionListener(e -> {
            try { config.jackpotContributionPercent = Double.parseDouble(contrib.getText()); }
            catch (Exception ex) { contrib.setText(String.valueOf(config.jackpotContributionPercent)); }
        });
        
        p.add(enabled);
        p.add(Box.createHorizontalStrut(10));
        p.add(createLabel("Contribution %:"));
        p.add(contrib);
        
        return p;
    }

    private JPanel createMulingPanel(CasinoConfig config) {
        JPanel p = createSection("Auto-Muling");
        
        JCheckBox enabled = new JCheckBox("Enable", config.autoMule);
        styleCheckbox(enabled);
        enabled.addActionListener(e -> config.autoMule = enabled.isSelected());
        
        JTextField muleName = new JTextField(config.muleName, 12);
        styleTextField(muleName);
        muleName.addActionListener(e -> config.muleName = muleName.getText());
        
        JTextField threshold = new JTextField(String.valueOf(config.muleThreshold), 12);
        styleTextField(threshold);
        threshold.addActionListener(e -> {
            try { config.muleThreshold = Long.parseLong(threshold.getText().replace(",", "")); }
            catch (Exception ex) { threshold.setText(String.valueOf(config.muleThreshold)); }
        });
        
        p.add(enabled);
        p.add(createLabel("Mule Name:"));
        p.add(muleName);
        p.add(createLabel("Threshold:"));
        p.add(threshold);
        
        return p;
    }

    private JPanel createHumanizationPanel(CasinoConfig config) {
        JPanel p = createSection("Humanization & AI");
        
        JCheckBox chatAi = new JCheckBox("Chat AI", config.chatAIEnabled);
        JCheckBox microBreaks = new JCheckBox("Micro-Breaks", config.enableMicroBreaks);
        JCheckBox cameraJitter = new JCheckBox("Camera Jitter", config.enableCameraJitter);
        JCheckBox mouseFatigue = new JCheckBox("Mouse Fatigue", config.enableMouseFatigue);
        
        styleCheckbox(chatAi);
        styleCheckbox(microBreaks);
        styleCheckbox(cameraJitter);
        styleCheckbox(mouseFatigue);
        
        chatAi.addActionListener(e -> config.chatAIEnabled = chatAi.isSelected());
        microBreaks.addActionListener(e -> config.enableMicroBreaks = microBreaks.isSelected());
        cameraJitter.addActionListener(e -> config.enableCameraJitter = cameraJitter.isSelected());
        mouseFatigue.addActionListener(e -> config.enableMouseFatigue = mouseFatigue.isSelected());
        
        p.add(chatAi);
        p.add(microBreaks);
        p.add(cameraJitter);
        p.add(mouseFatigue);
        
        return p;
    }

    private JPanel createDiscordTab(CasinoConfig config) {
        JPanel p = createTabPanel();
        JPanel section = createSection("Discord Webhook");
        
        JCheckBox enabled = new JCheckBox("Enable Discord", config.discordEnabled);
        styleCheckbox(enabled);
        enabled.addActionListener(e -> config.discordEnabled = enabled.isSelected());
        
        JTextField url = new JTextField(config.discordWebhookUrl, 35);
        styleTextField(url);
        url.addActionListener(e -> config.discordWebhookUrl = url.getText());
        
        section.add(enabled);
        section.add(Box.createHorizontalStrut(10));
        section.add(createLabel("Webhook URL:"));
        section.add(url);
        p.add(section);
        
        JPanel optionsSection = createSection("Notification Options");
        
        JCheckBox notifyWins = new JCheckBox("Notify Wins", config.discordNotifyWins);
        JCheckBox notifyLosses = new JCheckBox("Notify Losses", config.discordNotifyLosses);
        JCheckBox showSeeds = new JCheckBox("Show Seeds", config.discordShowSeeds);
        
        styleCheckbox(notifyWins);
        styleCheckbox(notifyLosses);
        styleCheckbox(showSeeds);
        
        notifyWins.addActionListener(e -> config.discordNotifyWins = notifyWins.isSelected());
        notifyLosses.addActionListener(e -> config.discordNotifyLosses = notifyLosses.isSelected());
        showSeeds.addActionListener(e -> config.discordShowSeeds = showSeeds.isSelected());
        
        optionsSection.add(notifyWins);
        optionsSection.add(notifyLosses);
        optionsSection.add(showSeeds);
        p.add(optionsSection);
        
        return p;
    }

    private JPanel createAdvancedTab(CasinoConfig config) {
        JPanel p = createTabPanel();
        
        JPanel humanSection = createSection("Anti-Ban Settings");
        
        JSpinner breakFreq = new JSpinner(new SpinnerNumberModel(config.breakFrequencyMinutes, 15, 180, 5));
        JSpinner breakDur = new JSpinner(new SpinnerNumberModel(config.breakDurationMinutes, 1, 30, 1));
        
        breakFreq.addChangeListener(e -> config.breakFrequencyMinutes = (int) breakFreq.getValue());
        breakDur.addChangeListener(e -> config.breakDurationMinutes = (int) breakDur.getValue());
        
        humanSection.add(createLabel("Break Every (min):"));
        humanSection.add(breakFreq);
        humanSection.add(Box.createHorizontalStrut(10));
        humanSection.add(createLabel("Break Duration (min):"));
        humanSection.add(breakDur);
        p.add(humanSection);
        
        JPanel bankSection = createSection("Banking Settings");
        
        JCheckBox autoBank = new JCheckBox("Auto-Bank", config.autoBank);
        styleCheckbox(autoBank);
        autoBank.addActionListener(e -> config.autoBank = autoBank.isSelected());

        JCheckBox skipBanking = new JCheckBox("Skip Banking (Bypass)", config.skipBanking);
        styleCheckbox(skipBanking);
        skipBanking.addActionListener(e -> config.skipBanking = skipBanking.isSelected());
        
        JTextField restockThreshold = new JTextField(String.valueOf(config.restockThreshold), 12);
        JTextField restockAmount = new JTextField(String.valueOf(config.restockAmount), 12);
        styleTextField(restockThreshold);
        styleTextField(restockAmount);
        
        restockThreshold.addActionListener(e -> {
            try { config.restockThreshold = Long.parseLong(restockThreshold.getText().replace(",", "")); }
            catch (Exception ex) { restockThreshold.setText(String.valueOf(config.restockThreshold)); }
        });
        restockAmount.addActionListener(e -> {
            try { config.restockAmount = Long.parseLong(restockAmount.getText().replace(",", "")); }
            catch (Exception ex) { restockAmount.setText(String.valueOf(config.restockAmount)); }
        });
        
        bankSection.add(autoBank);
        bankSection.add(skipBanking);
        bankSection.add(createLabel("Threshold:"));
        bankSection.add(restockThreshold);
        bankSection.add(createLabel("Amount:"));
        bankSection.add(restockAmount);
        p.add(bankSection);
        
        JPanel pfSection = createSection("Provably Fair");
        JCheckBox usePF = new JCheckBox("Enable Provably Fair", config.useProvablyFair);
        styleCheckbox(usePF);
        usePF.addActionListener(e -> config.useProvablyFair = usePF.isSelected());
        pfSection.add(usePF);
        p.add(pfSection);
        
        return p;
    }

    private JPanel createSection(String title) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        p.setBackground(new Color(45, 45, 50));
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 85)), title);
        border.setTitleColor(new Color(0, 255, 127));
        border.setTitleFont(new Font("Arial", Font.BOLD, 11));
        p.setBorder(border);
        return p;
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.PLAIN, 11));
        return label;
    }
    
    private void styleTextField(JTextField field) {
        field.setBackground(new Color(60, 60, 65));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 85)),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
    }
    
    private void styleCheckbox(JCheckBox cb) {
        cb.setForeground(Color.WHITE);
        cb.setBackground(new Color(45, 45, 50));
        cb.setFocusPainted(false);
        cb.setFont(new Font("Arial", Font.PLAIN, 11));
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        return btn;
    }
}
