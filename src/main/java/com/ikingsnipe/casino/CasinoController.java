package com.ikingsnipe.casino;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.script.AbstractScript;
import com.ikingsnipe.casino.managers.*;
import com.ikingsnipe.casino.models.*;
import com.ikingsnipe.casino.games.*;
import com.ikingsnipe.casino.utils.*;

import java.awt.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Main orchestrator for the casino system
 * Coordinates all managers and handles system state
 */
public class CasinoController {
    
    private final AbstractScript script;
    
    // Core Managers
    private final SessionManager sessionManager;
    private final TradeManager tradeManager;
    private final ChatManager chatManager;
    private final GameManager gameManager;
    private final ErrorHandler errorHandler;
    private final DreamBotAdapter apiAdapter;
    
    // Configuration
    private CasinoConfig config;
    
    // System State
    private volatile boolean isRunning = false;
    private volatile boolean emergencyStop = false;
    private SystemState currentState = SystemState.STOPPED;
    
    // Statistics
    private final Map<String, Object> stats = new ConcurrentHashMap<>();
    private long lastUpdateTime = 0;
    
    // Paint Data
    private final List<String> paintLines = Collections.synchronizedList(new ArrayList<>());
    
    public enum SystemState {
        STOPPED, INITIALIZING, RUNNING, ADVERTISING, TRADING, 
        GAMING, PAYOUT, ERROR_RECOVERY, SHUTTING_DOWN
    }
    
    public CasinoController(AbstractScript script) {
        this.script = script;
        
        // Load configuration first
        this.config = ConfigLoader.loadConfig();
        
        // Initialize adapters and utilities
        this.apiAdapter = new DreamBotAdapter(script);
        this.errorHandler = new ErrorHandler(this);
        
        // Initialize managers
        this.sessionManager = new SessionManager();
        this.gameManager = new GameManager();
        this.tradeManager = new TradeManager(this, sessionManager, gameManager, apiAdapter);
        this.chatManager = new ChatManager(this, sessionManager, tradeManager, gameManager, apiAdapter);
        
        // Initialize statistics
        initializeStats();
        
        // Register default games
        registerGames();
        
        log("CasinoController initialized successfully");
    }
    
    private void initializeStats() {
        stats.put("total_profit", 0);
        stats.put("total_wagered", 0);
        stats.put("total_payouts", 0);
        stats.put("games_played", 0);
        stats.put("wins", 0);
        stats.put("losses", 0);
        stats.put("active_sessions", 0);
        stats.put("peak_sessions", 0);
    }
    
    private void registerGames() {
        // Register all available games
        gameManager.registerGame("craps", new CrapsGame(config));
        gameManager.registerGame("dice", new DiceDuelGame(config));
        gameManager.registerGame("flower", new FlowerPokerGame(config));
        
        // Set enabled status from config
        gameManager.setGameEnabled("craps", config.isCrapsEnabled());
        gameManager.setGameEnabled("dice", config.isDiceEnabled());
        gameManager.setGameEnabled("flower", config.isFlowerEnabled());
    }
    
    /**
     * Main processing loop called from script onLoop()
     */
    public void process() {
        if (!isRunning || emergencyStop) {
            return;
        }
        
        try {
            // Check if we need to change state
            updateSystemState();
            
            // Process based on current state
            switch (currentState) {
                case RUNNING:
                    processRunningState();
                    break;
                case ADVERTISING:
                    processAdvertising();
                    break;
                case TRADING:
                    processTrading();
                    break;
                case GAMING:
                    processGaming();
                    break;
                case PAYOUT:
                    processPayout();
                    break;
                case ERROR_RECOVERY:
                    processErrorRecovery();
                    break;
            }
            
            // Always process chat and cleanup
            chatManager.processMessages();
            sessionManager.cleanupExpiredSessions();
            
            // Update statistics
            updateStatistics();
            
        } catch (Exception e) {
            logError("Error in process loop: " + e.getMessage());
            currentState = SystemState.ERROR_RECOVERY;
        }
    }
    
    private void processRunningState() {
        // Check for new trades
        if (apiAdapter.isTradeOpen()) {
            currentState = SystemState.TRADING;
            return;
        }
        
        // Check if we should advertise
        if (config.isAdvertisingEnabled() && 
            System.currentTimeMillis() - lastUpdateTime > config.getAdCooldown()) {
            currentState = SystemState.ADVERTISING;
        }
    }
    
    private void processAdvertising() {
        if (!config.isAdvertisingEnabled()) {
            currentState = SystemState.RUNNING;
            return;
        }
        
        String adMessage = config.getRandomAdMessage();
        if (apiAdapter.sendPublicMessage(adMessage)) {
            log("Advertisement sent: " + adMessage);
            lastUpdateTime = System.currentTimeMillis();
        }
        
        currentState = SystemState.RUNNING;
    }
    
    private void processTrading() {
        tradeManager.process();
        
        // If trade is no longer open and we're not in gaming/payout, go back to running
        if (!apiAdapter.isTradeOpen() && 
            currentState != SystemState.GAMING && 
            currentState != SystemState.PAYOUT) {
            currentState = SystemState.RUNNING;
        }
    }
    
    private void processGaming() {
        // Game logic is handled by TradeManager during trade flow
        // This state is mainly for tracking
        
        // Check if we should move to payout
        PlayerSession activeSession = sessionManager.getActiveSession();
        if (activeSession != null && activeSession.isGameComplete()) {
            if (activeSession.isWin()) {
                currentState = SystemState.PAYOUT;
            } else {
                completeSession(activeSession);
                currentState = SystemState.RUNNING;
            }
        }
    }
    
    private void processPayout() {
        tradeManager.processPayout();
        
        // Check if payout is complete
        if (!apiAdapter.isTradeOpen()) {
            PlayerSession activeSession = sessionManager.getActiveSession();
            if (activeSession != null) {
                completeSession(activeSession);
            }
            currentState = SystemState.RUNNING;
        }
    }
    
    private void processErrorRecovery() {
        log("Attempting error recovery...");
        
        if (errorHandler.attemptRecovery()) {
            log("Error recovery successful");
            currentState = SystemState.RUNNING;
        } else {
            log("Error recovery failed, stopping casino");
            emergencyStop();
        }
    }
    
    private void updateSystemState() {
        // Auto-transition logic based on conditions
        if (currentState == SystemState.TRADING && !apiAdapter.isTradeOpen()) {
            currentState = SystemState.RUNNING;
        }
        
        // Timeout protection
        long stateDuration = System.currentTimeMillis() - lastUpdateTime;
        if (stateDuration > 60000 && currentState != SystemState.RUNNING) {
            log("State timeout detected, resetting to RUNNING");
            currentState = SystemState.RUNNING;
        }
    }
    
    private void completeSession(PlayerSession session) {
        // Update statistics
        int profit = session.isWin() ? -session.getPayout() : session.getBetAmount();
        stats.put("total_profit", (int)stats.get("total_profit") + profit);
        stats.put("total_wagered", (int)stats.get("total_wagered") + session.getBetAmount());
        
        if (session.isWin()) {
            stats.put("wins", (int)stats.get("wins") + 1);
            stats.put("total_payouts", (int)stats.get("total_payouts") + session.getPayout());
        } else {
            stats.put("losses", (int)stats.get("losses") + 1);
        }
        
        stats.put("games_played", (int)stats.get("games_played") + 1);
        
        // Remove session
        sessionManager.removeSession(session.getPlayerName());
        
        log("Session completed: " + session.getPlayerName() + 
            " - " + (session.isWin() ? "WIN" : "LOSS") + 
            " $" + (session.isWin() ? session.getPayout() : session.getBetAmount()));
    }
    
    private void updateStatistics() {
        int activeSessions = sessionManager.getActiveSessionCount();
        stats.put("active_sessions", activeSessions);
        
        if (activeSessions > (int)stats.get("peak_sessions")) {
            stats.put("peak_sessions", activeSessions);
        }
    }
    
    // ===== PUBLIC API METHODS =====
    
    public void startCasino() {
        if (isRunning) {
            log("Casino is already running");
            return;
        }
        
        log("Starting Elite Titan Casino...");
        
        // Validate configuration
        if (!config.validate()) {
            log("Configuration validation failed. Please check settings.");
            return;
        }
        
        // Initialize systems
        try {
            if (!apiAdapter.initialize()) {
                log("Failed to initialize DreamBot API adapter");
                return;
            }
            
            isRunning = true;
            emergencyStop = false;
            currentState = SystemState.RUNNING;
            lastUpdateTime = System.currentTimeMillis();
            
            log("Casino started successfully!");
            log("Min Bet: " + config.getMinBet() + " | Max Bet: " + config.getMaxBet());
            log("Enabled Games: " + String.join(", ", gameManager.getEnabledGames()));
            
        } catch (Exception e) {
            logError("Failed to start casino: " + e.getMessage());
            isRunning = false;
        }
    }
    
    public void stopCasino() {
        log("Stopping casino...");
        
        isRunning = false;
        currentState = SystemState.STOPPED;
        
        // Close any open trades
        if (apiAdapter.isTradeOpen()) {
            apiAdapter.declineTrade();
        }
        
        // Clear all sessions
        sessionManager.clearAllSessions();
        
        log("Casino stopped");
    }
    
    public void emergencyStop() {
        log("EMERGENCY STOP ACTIVATED!");
        
        emergencyStop = true;
        isRunning = false;
        currentState = SystemState.STOPPED;
        
        // Force close everything
        apiAdapter.emergencyStop();
        tradeManager.emergencyStop();
        sessionManager.clearAllSessions();
        
        log("All systems halted");
    }
    
    public void shutdown() {
        log("Shutting down casino system...");
        
        stopCasino();
        
        // Save configuration
        ConfigLoader.saveConfig(config);
        
        // Clean up resources
        chatManager.shutdown();
        tradeManager.shutdown();
        
        log("Casino system shutdown complete");
    }
    
    // ===== GETTERS & SETTERS =====
    
    public boolean isRunning() { return isRunning; }
    public boolean isEmergencyStop() { return emergencyStop; }
    public SystemState getCurrentState() { return currentState; }
    
    public CasinoConfig getConfig() { return config; }
    public void updateConfig(CasinoConfig newConfig) { 
        this.config = newConfig; 
        ConfigLoader.saveConfig(config);
    }
    
    public Map<String, Object> getStatistics() { return new HashMap<>(stats); }
    public List<PlayerSession> getActiveSessions() { return sessionManager.getAllSessions(); }
    public Map<String, Integer> getGameStats() { return gameManager.getGameStats(); }
    
    // ===== LOGGING =====
    
    public void log(String message) {
        script.log("[Casino] " + message);
        synchronized (paintLines) {
            paintLines.add(message);
            if (paintLines.size() > 10) {
                paintLines.remove(0);
            }
        }
    }
    
    public void logError(String error) {
        script.log("[ERROR] " + error);
        errorHandler.recordError(error);
    }
    
    // ===== PAINT METHODS =====
    
    public void onPaint(Graphics g) {
        if (!shouldPaint()) return;
        
        // Draw semi-transparent background
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(5, 5, 300, 180);
        
        // Draw header
        g.setColor(new Color(255, 215, 0)); // Gold
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("🎰 Elite Titan Casino Pro", 15, 25);
        
        // Draw status
        g.setColor(currentState == SystemState.ERROR_RECOVERY ? Color.RED : Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Status: " + currentState, 15, 45);
        
        // Draw statistics
        g.setColor(Color.CYAN);
        g.drawString("Profit: " + stats.get("total_profit") + " gp", 15, 65);
        g.drawString("Active: " + stats.get("active_sessions"), 15, 85);
        g.drawString("W/L: " + stats.get("wins") + "/" + stats.get("losses"), 15, 105);
        
        // Draw recent logs
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        int y = 130;
        synchronized (paintLines) {
            List<String> recent = new ArrayList<>(paintLines);
            Collections.reverse(recent);
            for (int i = 0; i < Math.min(3, recent.size()); i++) {
                g.drawString("> " + recent.get(i).substring(0, Math.min(35, recent.get(i).length())), 15, y);
                y += 12;
            }
        }
        
        // Emergency stop indicator
        if (emergencyStop) {
            g.setColor(Color.RED);
            g.fillRect(280, 5, 20, 20);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("!", 287, 20);
        }
    }
    
    public boolean shouldPaint() {
        return isRunning || emergencyStop;
    }
    
    public void updatePaint() {
        // Trigger paint update
        if (script.getCanvas() != null) {
            script.getCanvas().repaint();
        }
    }
    
    // ===== ERROR HANDLING =====
    
    public void handleError(Exception e) {
        errorHandler.handle(e);
        currentState = SystemState.ERROR_RECOVERY;
    }
}