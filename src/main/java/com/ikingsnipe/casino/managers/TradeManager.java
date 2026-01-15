package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.models.TradeConfig;
import com.ikingsnipe.casino.models.PlayerSession;
import com.ikingsnipe.casino.utils.ProvablyFair;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Comprehensive Trade Manager for snipes♧scripts Enterprise
 * Handles all trade operations including:
 * - Trade request detection and acceptance
 * - Anti-scam verification with double-checks
 * - Player-friendly fast verification
 * - Trade timeout protection
 * - Configurable trade settings
 */
public class TradeManager {
    
    private final CasinoConfig config;
    private final TradeConfig tradeConfig;
    private final SessionManager sessionManager;
    private final ProvablyFair provablyFair;
    
    // Trade state tracking
    private String currentTrader = null;
    private long currentBetAmount = 0;
    private long tradeStartTime = 0;
    private boolean welcomeSent = false;
    private String selectedGame = "dice";
    
    // Anti-scam verification state
    private long lastVerifiedValue = 0;
    private int verificationCount = 0;
    private boolean valueStable = false;
    private long lastValueChangeTime = 0;
    
    // Trade request queue for handling multiple requests
    private final Queue<TradeRequest> pendingTradeRequests = new ConcurrentLinkedQueue<>();
    private final Set<String> recentlyDeclined = new HashSet<>();
    private long lastDeclineCleanup = 0;
    
    // Trade screen 2 verification
    private long screen2StartTime = 0;
    private boolean screen2Verified = false;
    private int screen2VerifyAttempts = 0;
    
    public TradeManager(CasinoConfig config, TradeConfig tradeConfig, SessionManager sessionManager, ProvablyFair provablyFair) {
        this.config = config;
        this.tradeConfig = tradeConfig;
        this.sessionManager = sessionManager;
        this.provablyFair = provablyFair;
    }
    
    /**
     * Main trade detection method - call this during IDLE state
     * Detects incoming trade requests from nearby players
     */
    public TradeDetectionResult detectTradeRequest() {
        // Clean up old declined players periodically
        cleanupDeclinedPlayers();
        
        // First check if trade window is already open
        if (Trade.isOpen()) {
            String tradingWith = Trade.getTradingWith();
            if (tradingWith != null && !tradingWith.isEmpty()) {
                return new TradeDetectionResult(true, tradingWith, TradeDetectionType.TRADE_WINDOW_OPEN);
            }
        }
        
        // Check for players requesting to trade with us
        Player tradeRequester = findTradeRequester();
        if (tradeRequester != null) {
            String requesterName = tradeRequester.getName();
            
            // Skip if recently declined
            if (recentlyDeclined.contains(requesterName.toLowerCase())) {
                return new TradeDetectionResult(false, null, TradeDetectionType.RECENTLY_DECLINED);
            }
            
            // Accept the trade request
            if (acceptTradeRequest(tradeRequester)) {
                return new TradeDetectionResult(true, requesterName, TradeDetectionType.ACCEPTED_REQUEST);
            }
        }
        
        // Check for pending trade requests in queue
        TradeRequest pending = pendingTradeRequests.poll();
        if (pending != null && !pending.isExpired()) {
            Player requester = Players.closest(pending.playerName);
            if (requester != null && acceptTradeRequest(requester)) {
                return new TradeDetectionResult(true, pending.playerName, TradeDetectionType.QUEUED_REQUEST);
            }
        }
        
        return new TradeDetectionResult(false, null, TradeDetectionType.NO_REQUEST);
    }
    
    /**
     * Find a player who is trying to trade with us
     */
    private Player findTradeRequester() {
        Player local = Players.getLocal();
        if (local == null) return null;
        
        // Method 1: Check for players interacting with us
        Player interacting = Players.closest(p -> 
            p != null && 
            p.isInteracting(local) && 
            p.distance() <= tradeConfig.maxTradeDistance
        );
        
        if (interacting != null) {
            return interacting;
        }
        
        // Method 2: Check trade request widget/notification
        // Widget 335 is the trade request interface
        if (Widgets.getWidget(335) != null && Widgets.getWidget(335).isVisible()) {
            // Trade request popup is showing
            String requesterName = getTradeRequestName();
            if (requesterName != null) {
                return Players.closest(requesterName);
            }
        }
        
        return null;
    }
    
    /**
     * Get the name from a trade request widget if present
     */
    private String getTradeRequestName() {
        try {
            // Check various widget locations for trade request text
            // Widget 335 is the trade interface
            Widget tradeWidget = Widgets.getWidget(335);
            if (tradeWidget != null && tradeWidget.isVisible()) {
                // Try to get child widget with trade request text
                WidgetChild child = tradeWidget.getChild(4);
                if (child != null) {
                    String text = child.getText();
                    if (text != null && text.contains("wishes to trade")) {
                        // Extract player name from "PlayerName wishes to trade with you"
                        return text.split(" ")[0];
                    }
                }
            }
            
            // Alternative: Check chat messages for trade requests if widget fails
            // This is handled by TradeRequestListener, but we can also check here
        } catch (Exception e) {
            Logger.log("Error getting trade request name: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Accept a trade request from a player
     */
    private boolean acceptTradeRequest(Player player) {
        if (player == null) return false;
        
        String playerName = player.getName();
        Logger.log("[TradeManager] Accepting trade from: " + playerName);
        
        // Interact with player to accept trade
        if (player.interact("Trade with")) {
            // Wait for trade window with configurable timeout
            boolean tradeOpened = Sleep.sleepUntil(() -> Trade.isOpen(), tradeConfig.tradeAcceptTimeout);
            
            if (tradeOpened) {
                // Small delay for stability
                Sleep.sleep(tradeConfig.minVerifyDelay, tradeConfig.minVerifyDelay + 200);
                return true;
            } else {
                Logger.log("[TradeManager] Trade window didn't open for: " + playerName);
            }
        }
        
        return false;
    }
    
    /**
     * Initialize a new trade session
     */
    public void initializeTradeSession(String playerName) {
        this.currentTrader = playerName;
        this.tradeStartTime = System.currentTimeMillis();
        this.welcomeSent = false;
        this.currentBetAmount = 0;
        this.lastVerifiedValue = 0;
        this.verificationCount = 0;
        this.valueStable = false;
        this.lastValueChangeTime = System.currentTimeMillis();
        this.screen2Verified = false;
        this.screen2VerifyAttempts = 0;
        this.selectedGame = config.defaultGame;
        
        // Generate new provably fair seed for this trade
        provablyFair.generateNewSeed();
        
        Logger.log("[TradeManager] Trade session initialized with: " + playerName);
    }
    
    /**
     * Handle Trade Screen 1 - Main betting/verification screen
     * Returns: TradeAction indicating what to do next
     */
    public TradeAction handleTradeScreen1(PlayerSession session) {
        if (!Trade.isOpen(1)) {
            if (!Trade.isOpen()) {
                return TradeAction.TRADE_CLOSED;
            }
            return TradeAction.WAIT;
        }
        
        // Check for trade timeout
        if (isTradeTimedOut()) {
            sendTradeMessage("Trade timed out. Please trade again!");
            declineTrade("Timeout");
            return TradeAction.DECLINED;
        }
        
        // Send welcome message if not sent
        if (!welcomeSent && tradeConfig.sendWelcomeMessage) {
            sendWelcomeMessage();
            welcomeSent = true;
            return TradeAction.WAIT;
        }
        
        // Get current trade value
        long currentValue = getTheirTradeValue();
        
        // Handle case where player has balance and is betting from balance
        if (currentBetAmount > 0 && currentValue == 0 && session.getBalance() >= currentBetAmount) {
            // Player is using their existing balance
            if (verifyAndAcceptScreen1(0, true)) {
                return TradeAction.ACCEPTED_SCREEN1;
            }
            return TradeAction.WAIT;
        }
        
        // Handle new bet from trade
        if (currentValue > 0) {
            // Validate bet limits
            if (!validateBetLimits(currentValue)) {
                return TradeAction.DECLINED;
            }
            
            // Verify stability and accept
            if (verifyAndAcceptScreen1(currentValue, false)) {
                this.currentBetAmount = currentValue;
                return TradeAction.ACCEPTED_SCREEN1;
            }
        }
        
        return TradeAction.WAIT;
    }
    
    /**
     * Handle Trade Screen 2 - Final confirmation screen
     */
    public TradeAction handleTradeScreen2() {
        if (!Trade.isOpen(2)) {
            if (!Trade.isOpen()) {
                return TradeAction.TRADE_CLOSED;
            }
            return TradeAction.WAIT;
        }
        
        // Anti-scam: Wait for a moment on screen 2 to ensure no last-second changes
        if (screen2StartTime == 0) {
            screen2StartTime = System.currentTimeMillis();
            return TradeAction.WAIT;
        }
        
        long elapsed = System.currentTimeMillis() - screen2StartTime;
        if (elapsed < tradeConfig.screen2VerifyDelay) {
            return TradeAction.WAIT;
        }
        
        // Final verification of value on screen 2
        long screen2Value = getTheirTradeValue();
        if (screen2Value != currentBetAmount && currentBetAmount > 0) {
            Logger.log("[TradeManager] SCAM DETECTED: Value changed on screen 2!");
            declineTrade("Scam attempt");
            return TradeAction.DECLINED;
        }
        
        // Accept screen 2
        // In DreamBot, Trade.accept() is used for both screens
        if (Trade.acceptTrade()) {
            Logger.log("[TradeManager] Trade screen 2 accepted.");
            return TradeAction.COMPLETED;
        }
        
        return TradeAction.WAIT;
    }
    
    /**
     * Verify stability of trade value and accept screen 1
     */
    private boolean verifyAndAcceptScreen1(long currentValue, boolean balanceBet) {
        // If value changed, reset stability check
        if (currentValue != lastVerifiedValue) {
            lastVerifiedValue = currentValue;
            lastValueChangeTime = System.currentTimeMillis();
            verificationCount = 0;
            valueStable = false;
            return false;
        }
        
        // Check if value has been stable for required time
        long stableTime = System.currentTimeMillis() - lastValueChangeTime;
        if (stableTime >= tradeConfig.valueStabilityTime) {
            valueStable = true;
        }
        
        if (valueStable) {
            // Increment verification count (multiple checks for security)
            verificationCount++;
            
            int requiredChecks = tradeConfig.lowValueVerifyCount;
            if (currentValue >= tradeConfig.highValueThreshold) {
                requiredChecks = tradeConfig.highValueVerifyCount;
            } else if (currentValue >= tradeConfig.mediumValueThreshold) {
                requiredChecks = tradeConfig.mediumValueVerifyCount;
            }
            
            if (verificationCount >= requiredChecks) {
                // Final check: did they accept?
                // Note: DreamBot API might have different method names, 
                // but based on common usage:
                if (Trade.hasAcceptedTrade(org.dreambot.api.methods.trade.TradeUser.THEM)) {
                    if (Trade.acceptTrade()) {
                        Logger.log("[TradeManager] Trade screen 1 accepted. Value: " + currentValue);
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Get the total value of items they have offered
     */
    private long getTheirTradeValue() {
        Item[] items = Trade.getTheirItems();
        if (items == null) return 0;
        
        long total = 0;
        for (Item item : items) {
            if (item != null) {
                if (item.getID() == 995) {
                    total += item.getAmount();
                }
            }
        }
        return total;
    }
    
    /**
     * Validate if the bet is within allowed limits
     */
    private boolean validateBetLimits(long amount) {
        if (amount < config.minBet) {
            sendTradeMessage("Bet too low! Min: " + formatAmount(config.minBet));
            declineTrade("Bet too low");
            return false;
        }
        if (amount > config.maxBet) {
            sendTradeMessage("Bet too high! Max: " + formatAmount(config.maxBet));
            declineTrade("Bet too high");
            return false;
        }
        return true;
    }
    
    /**
     * Decline the current trade
     */
    public void declineTrade(String reason) {
        Logger.log("[TradeManager] Declining trade. Reason: " + reason);
        if (Trade.isOpen()) {
            Trade.declineTrade();
        }
        if (currentTrader != null) {
            recentlyDeclined.add(currentTrader.toLowerCase());
        }
        resetTradeState();
    }
    
    /**
     * Reset trade manager state
     */
    public void resetTradeState() {
        currentTrader = null;
        currentBetAmount = 0;
        tradeStartTime = 0;
        welcomeSent = false;
        lastVerifiedValue = 0;
        verificationCount = 0;
        valueStable = false;
        screen2StartTime = 0;
        screen2Verified = false;
    }
    
    /**
     * Check if the trade has timed out
     */
    private boolean isTradeTimedOut() {
        if (tradeStartTime == 0) return false;
        return (System.currentTimeMillis() - tradeStartTime) > tradeConfig.tradeTimeout;
    }
    
    /**
     * Send a message to the player via trade chat or public chat
     */
    private void sendTradeMessage(String message) {
        if (Trade.isOpen()) {
            Keyboard.type(message, true);
        }
    }
    
    /**
     * Send the welcome message to the player
     */
    private void sendWelcomeMessage() {
        String msg = "Welcome to Elite Titan Casino! Current game: " + selectedGame;
        sendTradeMessage(msg);
        Sleep.sleep(600, 1000);
        sendTradeMessage("Min bet: " + formatAmount(config.minBet) + " | Max: " + formatAmount(config.maxBet));
    }
    
    /**
     * Format currency amounts (e.g., 1000000 -> 1M)
     */
    private String formatAmount(long amount) {
        if (amount >= 1000000) return (amount / 1000000) + "M";
        if (amount >= 1000) return (amount / 1000) + "K";
        return String.valueOf(amount);
    }
    
    /**
     * Clean up the recently declined players list
     */
    private void cleanupDeclinedPlayers() {
        if (System.currentTimeMillis() - lastDeclineCleanup > 300000) { // 5 minutes
            recentlyDeclined.clear();
            lastDeclineCleanup = System.currentTimeMillis();
        }
    }

    // Missing methods called by other classes
    public void setSelectedGame(String game) {
        this.selectedGame = game;
    }

    public void setCurrentBetAmount(long amount) {
        this.currentBetAmount = amount;
    }

    public long getCurrentBetAmount() {
        return this.currentBetAmount;
    }

    public void queueTradeRequest(String playerName) {
        if (pendingTradeRequests.size() < tradeConfig.maxQueuedRequests) {
            pendingTradeRequests.add(new TradeRequest(playerName));
            Logger.log("[TradeManager] Queued trade request from: " + playerName);
        }
    }
    
    // Helper classes and enums
    
    public enum TradeAction {
        WAIT,
        ACCEPTED_SCREEN1,
        ACCEPTED_SCREEN2,
        COMPLETED,
        DECLINED,
        TRADE_CLOSED
    }
    
    public enum TradeDetectionType {
        NO_REQUEST,
        ACCEPTED_REQUEST,
        QUEUED_REQUEST,
        TRADE_WINDOW_OPEN,
        RECENTLY_DECLINED
    }
    
    public static class TradeDetectionResult {
        public final boolean success;
        public final String playerName;
        public final TradeDetectionType type;
        
        public TradeDetectionResult(boolean success, String playerName, TradeDetectionType type) {
            this.success = success;
            this.playerName = playerName;
            this.type = type;
        }
    }
    
    private static class TradeRequest {
        public final String playerName;
        public final long timestamp;
        
        public TradeRequest(String playerName) {
            this.playerName = playerName;
            this.timestamp = System.currentTimeMillis();
        }
        
        public boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > 30000; // 30 seconds
        }
    }
}
