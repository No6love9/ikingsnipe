package com.ikingsnipe.core;

import com.ikingsnipe.casino.gui.CasinoGUI;
import com.ikingsnipe.casino.managers.*;
import com.ikingsnipe.casino.messaging.EnterpriseMessageHandler;
import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.database.DatabaseManager;
import com.ikingsnipe.framework.core.TreeScript;
import com.ikingsnipe.framework.branches.*;
import com.ikingsnipe.framework.leaves.*;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Enterprise-Grade Bot Application v2.0
 * 
 * Features:
 * - Modern lifecycle management (replaces deprecated onStart/onLoop/onMessage)
 * - Thread-safe operations
 * - Comprehensive error handling
 * - Performance monitoring
 * - Graceful shutdown
 * - Statistics tracking
 */
// Legacy Manifest Removed
public class BotApplicationV2 extends TreeScript {
    
    // Configuration
    private CasinoConfig config;
    private CasinoGUI gui;
    
    // Managers
    private EnterpriseTradeManager tradeManager;
    private BankingManager bankingManager;
    private MuleManager muleManager;
    private HumanizationManager humanizationManager;
    private LocationManager locationManager;
    private EnterpriseMessageHandler messageHandler;
    
    // State management
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile long lastLoopTime = 0;
    private volatile long loopCount = 0;
    
    // Statistics
    private volatile long startTime = 0;
    private volatile long totalLoops = 0;
    private volatile long totalErrors = 0;
    
    /**
     * Script initialization (replaces deprecated onStart)
     */
    @Override
    public void onStart() {
        try {
            Logger.log("[BotApp] Starting GoatGang Edition v2.0...");
            
            // Authenticate
            if (!SecurityManager.authenticate()) {
                Logger.error("[BotApp] Authentication failed");
                stop();
                return;
            }
            
            // Load configuration
            config = CasinoConfig.load();
            if (config == null) {
                Logger.error("[BotApp] Failed to load configuration");
                stop();
                return;
            }
            
            // Initialize database
            try {
                DatabaseManager.setup(
                    config.dbHost,
                    config.dbPort,
                    config.dbName,
                    config.dbUser,
                    config.dbPass
                );
                Logger.log("[BotApp] Database initialized");
            } catch (Exception e) {
                Logger.warn("[BotApp] Database initialization failed: " + e.getMessage());
            }
            
            // Initialize managers
            initializeManagers();
            
            // Initialize message handler
            messageHandler = new EnterpriseMessageHandler();
            messageHandler.addListener(tradeManager.getTradeListener());
            Logger.log("[BotApp] Message handler initialized");
            
            // Initialize framework tree
            initializeTree();
            
            // Show GUI
            gui = new CasinoGUI(config, (start) -> {
                running.set(start);
                if (start) {
                    Logger.log("[BotApp] Script started by user");
                    startTime = System.currentTimeMillis();
                } else {
                    Logger.log("[BotApp] Script stopped by user");
                }
            });
            gui.setVisible(true);
            
            initialized.set(true);
            Logger.log("[BotApp] Initialization complete");
            
        } catch (Exception e) {
            Logger.error("[BotApp] Fatal error during initialization: " + e.getMessage());
            e.printStackTrace();
            stop();
        }
    }
    
    /**
     * Initialize all managers
     */
    private void initializeManagers() {
        try {
            tradeManager = new EnterpriseTradeManager(config, null);
            bankingManager = new BankingManager(config);
            muleManager = new MuleManager(config);
            humanizationManager = new HumanizationManager(config);
            locationManager = new LocationManager(config);
            
            Logger.log("[BotApp] All managers initialized successfully");
        } catch (Exception e) {
            Logger.error("[BotApp] Error initializing managers: " + e.getMessage());
            throw new RuntimeException("Failed to initialize managers", e);
        }
    }
    
    /**
     * Initialize framework tree
     */
    private void initializeTree() {
        try {
            // 1. Humanization (Highest Priority)
            getRoot().addChild(new HumanizationLeaf(humanizationManager));
            
            // 2. Maintenance (Banking/Muling)
            MaintenanceBranch maintenance = new MaintenanceBranch();
            maintenance.addChild(new MulingLeaf(muleManager));
            maintenance.addChild(new BankingLeaf(bankingManager));
            getRoot().addChild(maintenance);
            
            // 3. Hosting (Core Logic)
            HostingBranch hosting = new HostingBranch(locationManager);
            // Note: TradeLeaf requires TradeManager, not EnterpriseTradeManager
            // hosting.addChild(new TradeLeaf(tradeManager));
            hosting.addChild(new GameExecutionLeaf(new GameManager(config), new SessionManager()));
            hosting.addChild(new AutoChatLeaf(config));
            getRoot().addChild(hosting);
            
            Logger.log("[BotApp] Framework tree initialized");
        } catch (Exception e) {
            Logger.error("[BotApp] Error initializing tree: " + e.getMessage());
            throw new RuntimeException("Failed to initialize tree", e);
        }
    }
    
    /**
     * Main loop (replaces deprecated onLoop)
     */
    @Override
    public int onLoop() {
        try {
            // Check if initialized
            if (!initialized.get()) {
                return 1000;
            }
            
            // Check if running
            if (!running.get()) {
                return 1000;
            }
            
            totalLoops++;
            lastLoopTime = System.currentTimeMillis();
            
            // Process trade events
            if (tradeManager != null) {
                try {
                    tradeManager.processPendingTrades();
                } catch (Exception e) {
                    Logger.error("[BotApp] Error processing trades: " + e.getMessage());
                    totalErrors++;
                }
            }
            
            // Process messages
            if (messageHandler != null) {
                try {
                    processMessages();
                } catch (Exception e) {
                    Logger.error("[BotApp] Error processing messages: " + e.getMessage());
                    totalErrors++;
                }
            }
            
            // Execute framework tree
            try {
                return super.onLoop();
            } catch (Exception e) {
                Logger.error("[BotApp] Error in framework loop: " + e.getMessage());
                totalErrors++;
                return 1000;
            }
            
        } catch (Exception e) {
            Logger.error("[BotApp] Unexpected error in main loop: " + e.getMessage());
            totalErrors++;
            return 1000;
        }
    }
    
    /**
     * Process pending messages
     */
    private void processMessages() {
        try {
            while (messageHandler.hasPendingMessages()) {
                EnterpriseMessageHandler.ProcessedMessage msg = messageHandler.getNextMessage(100);
                if (msg == null) break;
                
                // Message already processed by listeners
                Logger.log("[BotApp] Message: " + msg.text);
            }
        } catch (Exception e) {
            Logger.error("[BotApp] Error processing messages: " + e.getMessage());
        }
    }
    
    /**
     * Paint overlay (replaces deprecated onPaint)
     */
    @Override
    public void onPaint(Graphics g) {
        try {
            if (!initialized.get() || !running.get()) {
                return;
            }
            
            // Draw background
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(5, 45, 300, 150);
            
            // Draw title
            g.setColor(new Color(212, 175, 55));
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("GoatGang Edition v2.0", 10, 65);
            
            // Draw status
            g.setColor(new Color(0, 255, 0));
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("Status: RUNNING", 10, 85);
            
            // Draw statistics
            g.drawString("Loops: " + totalLoops, 10, 105);
            g.drawString("Errors: " + totalErrors, 10, 125);
            
            if (tradeManager != null) {
                EnterpriseTradeManager.TradeManagerStatistics stats = tradeManager.getStatistics();
                g.drawString("Trades: " + stats.accepted, 10, 145);
                g.drawString("Scams Blocked: " + stats.scamsDetected, 10, 165);
            }
            
            // Draw uptime
            if (startTime > 0) {
                long uptime = (System.currentTimeMillis() - startTime) / 1000;
                g.drawString("Uptime: " + formatTime(uptime), 10, 185);
            }
            
        } catch (Exception e) {
            Logger.error("[BotApp] Error in paint: " + e.getMessage());
        }
    }
    
    /**
     * Script shutdown (replaces deprecated onExit)
     */
    @Override
    public void onExit() {
        try {
            Logger.log("[BotApp] Shutting down...");
            
            running.set(false);
            
            // Shutdown managers
            try {
                DatabaseManager.shutdown();
            } catch (Exception e) {
                Logger.warn("[BotApp] Error shutting down database: " + e.getMessage());
            }
            
            // Dispose GUI
            if (gui != null) {
                gui.dispose();
            }
            
            // Log final statistics
            if (startTime > 0) {
                long uptime = (System.currentTimeMillis() - startTime) / 1000;
                Logger.log("[BotApp] Final Statistics:");
                Logger.log("[BotApp] - Uptime: " + formatTime(uptime));
                Logger.log("[BotApp] - Total Loops: " + totalLoops);
                Logger.log("[BotApp] - Total Errors: " + totalErrors);
                
                if (tradeManager != null) {
                    EnterpriseTradeManager.TradeManagerStatistics stats = tradeManager.getStatistics();
                    Logger.log("[BotApp] - Trades Processed: " + stats.processed);
                    Logger.log("[BotApp] - Trades Accepted: " + stats.accepted);
                    Logger.log("[BotApp] - Scams Detected: " + stats.scamsDetected);
                    Logger.log("[BotApp] - GP Processed: " + stats.gpProcessed);
                }
            }
            
            Logger.log("[BotApp] Shutdown complete");
            
        } catch (Exception e) {
            Logger.error("[BotApp] Error during shutdown: " + e.getMessage());
        }
    }
    
    /**
     * Format time in seconds to HH:MM:SS
     */
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
