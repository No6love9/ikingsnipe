package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.casino.models.TradeConfig;
import com.ikingsnipe.casino.models.PlayerSession;
import com.ikingsnipe.casino.utils.ProvablyFair;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
<<<<<<< Updated upstream
import org.dreambot.api.input.Keyboard;
=======
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.wrappers.widgets.Widget;
import org.dreambot.api.wrappers.widgets.WidgetChild;
>>>>>>> Stashed changes
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
            
            // Anti-scam verification
            if (verifyTradeValue(currentValue)) {
                currentBetAmount = currentValue;
                if (verifyAndAcceptScreen1(currentValue, false)) {
                    return TradeAction.ACCEPTED_SCREEN1;
                }
            }
        }
        
        return TradeAction.WAIT;
    }
    
    /**
     * Verify trade value hasn't changed (anti-scam)
     * Fast but thorough verification
     */
    private boolean verifyTradeValue(long currentValue) {
        long now = System.currentTimeMillis();
        
        // If value changed, reset verification
        if (currentValue != lastVerifiedValue) {
            lastVerifiedValue = currentValue;
            lastValueChangeTime = now;
            verificationCount = 0;
            valueStable = false;
            return false;
        }
        
        // Check if value has been stable long enough
        long stableTime = now - lastValueChangeTime;
        if (stableTime >= tradeConfig.valueStabilityTime) {
            verificationCount++;
            
            // Require multiple verification checks for large bets
            int requiredChecks = getRequiredVerificationChecks(currentValue);
            if (verificationCount >= requiredChecks) {
                valueStable = true;
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get required verification checks based on bet size
     */
    private int getRequiredVerificationChecks(long value) {
        if (!tradeConfig.enableAntiScam) return 1;
        
        if (value >= tradeConfig.highValueThreshold) {
            return tradeConfig.highValueVerifyCount;
        } else if (value >= tradeConfig.mediumValueThreshold) {
            return tradeConfig.mediumValueVerifyCount;
        }
        return tradeConfig.lowValueVerifyCount;
    }
    
    /**
     * Verify and accept trade screen 1
     */
    private boolean verifyAndAcceptScreen1(long value, boolean usingBalance) {
        // Final verification check
        if (!usingBalance && value > 0) {
            Sleep.sleep(tradeConfig.minVerifyDelay, tradeConfig.maxVerifyDelay);
            long finalCheck = getTheirTradeValue();
            if (finalCheck != value) {
                Logger.log("[TradeManager] Value changed during final verification! Expected: " + value + ", Got: " + finalCheck);
                sendTradeMessage("Please don't change the trade amount!");
                return false;
            }
        }
        
        // Send confirmation message
        if (tradeConfig.sendConfirmationMessages) {
            String confirmMsg = String.format("Confirmed %s bet. Accepting trade...", formatGP(value > 0 ? value : currentBetAmount));
            sendTradeMessage(confirmMsg);
        }
        
        // Accept trade
        if (Trade.acceptTrade()) {
            boolean screen2Opened = Sleep.sleepUntil(() -> Trade.isOpen(2), tradeConfig.screen2WaitTime);
            if (screen2Opened) {
                screen2StartTime = System.currentTimeMillis();
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Handle Trade Screen 2 - Final confirmation screen
     * Includes double-check anti-scam verification
     */
    public TradeAction handleTradeScreen2(PlayerSession session) {
        if (!Trade.isOpen(2)) {
            if (!Trade.isOpen()) {
                return TradeAction.TRADE_CLOSED;
            }
            // Might have gone back to screen 1
            if (Trade.isOpen(1)) {
                screen2Verified = false;
                screen2VerifyAttempts = 0;
                return TradeAction.BACK_TO_SCREEN1;
            }
            return TradeAction.WAIT;
        }
        
        // Check for screen 2 timeout
        if (System.currentTimeMillis() - screen2StartTime > tradeConfig.screen2Timeout) {
            sendTradeMessage("Trade confirmation timed out!");
            declineTrade("Screen2 Timeout");
            return TradeAction.DECLINED;
        }
        
        // Perform screen 2 verification
        if (!screen2Verified && tradeConfig.enableScreen2Verification) {
            if (!verifyScreen2()) {
                screen2VerifyAttempts++;
                if (screen2VerifyAttempts >= tradeConfig.maxScreen2VerifyAttempts) {
                    sendTradeMessage("Trade verification failed. Please try again.");
                    declineTrade("Screen2 Verification Failed");
                    return TradeAction.DECLINED;
                }
                return TradeAction.WAIT;
            }
            screen2Verified = true;
        }
        
        // Accept the trade
        if (Trade.acceptTrade()) {
            // Wait for trade to complete
            boolean tradeComplete = Sleep.sleepUntil(() -> !Trade.isOpen(), tradeConfig.tradeCompleteWaitTime);
            if (tradeComplete) {
                Logger.log("[TradeManager] Trade completed successfully with: " + currentTrader);
                return TradeAction.TRADE_COMPLETE;
            }
        }
        
        return TradeAction.WAIT;
    }
    
    /**
     * Verify trade screen 2 values match screen 1
     */
    private boolean verifyScreen2() {
        // Small delay for screen to fully load
        Sleep.sleep(tradeConfig.screen2VerifyDelay, tradeConfig.screen2VerifyDelay + 100);
        
        // Get the value shown on screen 2
        long screen2Value = getScreen2TheirValue();
        
        // Verify the value matches what we accepted in screen 1
        if (screen2Value != currentBetAmount) {
            Logger.log("[TradeManager] Screen 2 value mismatch! Expected: " + currentBetAmount + ", Got: " + screen2Value);
            return false;
        }
        
        return true;
    }
    
    /**
     * Get their trade value from screen 2
     */
    private long getScreen2TheirValue() {
        // The Trade.getTheirItems() API works for both screens in DreamBot
        return getTheirTradeValue();
    }
    
    /**
     * Validate bet is within configured limits
     */
    private boolean validateBetLimits(long value) {
        if (value < config.minBet) {
            sendTradeMessage("Minimum bet is " + formatGP(config.minBet) + "!");
            declineTrade("Below minimum");
            return false;
        }
        
        if (value > config.maxBet) {
            sendTradeMessage("Maximum bet is " + formatGP(config.maxBet) + "!");
            declineTrade("Above maximum");
            return false;
        }
        
        // Check if we have enough to pay out potential winnings
        if (tradeConfig.checkPayoutCapacity) {
            double maxMultiplier = getMaxGameMultiplier();
            long potentialPayout = (long)(value * maxMultiplier);
            long ourValue = getOurTotalValue();
            
            if (potentialPayout > ourValue) {
                sendTradeMessage("Bet too large for current bank. Max bet: " + formatGP((long)(ourValue / maxMultiplier)));
                declineTrade("Insufficient bank");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get maximum game multiplier for payout calculation
     */
    private double getMaxGameMultiplier() {
        double max = 2.0;
        for (CasinoConfig.GameSettings game : config.games.values()) {
            if (game.enabled && game.multiplier > max) {
                max = game.multiplier;
            }
        }
        return max;
    }
    
    /**
     * Get our total value (inventory)
     */
    private long getOurTotalValue() {
        long coins = Inventory.count(CasinoConfig.COINS_ID);
        long tokens = Inventory.count(CasinoConfig.PLATINUM_TOKEN_ID) * CasinoConfig.TOKEN_VALUE;
        return coins + tokens;
    }
    
    /**
     * Get their trade value (coins + tokens)
     */
    public long getTheirTradeValue() {
        long total = 0;
        try {
            for (Item item : Trade.getTheirItems()) {
                if (item != null) {
                    if (item.getID() == CasinoConfig.PLATINUM_TOKEN_ID) {
                        total += item.getAmount() * CasinoConfig.TOKEN_VALUE;
                    } else if (item.getID() == CasinoConfig.COINS_ID) {
                        total += item.getAmount();
                    }
                }
            }
        } catch (Exception e) {
            Logger.log("[TradeManager] Error getting trade value: " + e.getMessage());
        }
        return total;
    }
    
    /**
     * Send welcome message to trader
     */
    private void sendWelcomeMessage() {
        String greeting = tradeConfig.customWelcomeMessage
            .replace("{player}", currentTrader != null ? currentTrader : "Player")
            .replace("{hash}", provablyFair.getHash().substring(0, 12))
            .replace("{min}", formatGP(config.minBet))
            .replace("{max}", formatGP(config.maxBet));
        
        sendTradeMessage(greeting);
        
        // Send game commands info if enabled
        if (tradeConfig.sendGameCommands) {
            Sleep.sleep(300, 500);
            sendTradeMessage("Commands: !c (Craps) !dw (Dice War) !fp (Flower Poker)");
        }
    }
    
    /**
     * Send a message in trade chat
     */
    public void sendTradeMessage(String message) {
        if (message == null || message.isEmpty()) return;
        Keyboard.type(message, true);
        Sleep.sleep(200, 400);
    }
    
    /**
     * Decline trade with reason logging
     */
    public void declineTrade(String reason) {
        Logger.log("[TradeManager] Declining trade with " + currentTrader + ". Reason: " + reason);
        
        if (currentTrader != null) {
            recentlyDeclined.add(currentTrader.toLowerCase());
        }
        
        if (Trade.isOpen()) {
            Trade.declineTrade();
        }
        
        resetTradeState();
    }
    
    /**
     * Check if trade has timed out
     */
    private boolean isTradeTimedOut() {
        return System.currentTimeMillis() - tradeStartTime > tradeConfig.tradeTimeout;
    }
    
    /**
     * Clean up recently declined players list
     */
    private void cleanupDeclinedPlayers() {
        long now = System.currentTimeMillis();
        if (now - lastDeclineCleanup > tradeConfig.declineCooldown) {
            recentlyDeclined.clear();
            lastDeclineCleanup = now;
        }
    }
    
    /**
     * Reset trade state
     */
    public void resetTradeState() {
        currentTrader = null;
        currentBetAmount = 0;
        tradeStartTime = 0;
        welcomeSent = false;
        lastVerifiedValue = 0;
        verificationCount = 0;
        valueStable = false;
        screen2Verified = false;
        screen2VerifyAttempts = 0;
    }
    
    /**
     * Add items to trade for payout
     */
    public boolean addPayoutItems(long amount) {
        if (!Trade.isOpen(1)) return false;
        
        long remaining = amount;
        
        // Add platinum tokens first (more efficient)
        if (remaining >= CasinoConfig.TOKEN_VALUE) {
            int tokensNeeded = (int)(remaining / CasinoConfig.TOKEN_VALUE);
            int tokensAvailable = Inventory.count(CasinoConfig.PLATINUM_TOKEN_ID);
            int tokensToAdd = Math.min(tokensNeeded, tokensAvailable);
            
            if (tokensToAdd > 0) {
                Trade.addItem(CasinoConfig.PLATINUM_TOKEN_ID, tokensToAdd);
                remaining -= (long)tokensToAdd * CasinoConfig.TOKEN_VALUE;
                Sleep.sleep(200, 400);
            }
        }
        
        // Add remaining coins
        if (remaining > 0) {
            int coinsAvailable = Inventory.count(CasinoConfig.COINS_ID);
            int coinsToAdd = (int)Math.min(remaining, coinsAvailable);
            
            if (coinsToAdd > 0) {
                Trade.addItem(CasinoConfig.COINS_ID, coinsToAdd);
            }
        }
        
        return true;
    }
    
    /**
     * Queue a trade request for later processing
     */
    public void queueTradeRequest(String playerName) {
        if (playerName != null && !playerName.isEmpty()) {
            pendingTradeRequests.offer(new TradeRequest(playerName, System.currentTimeMillis()));
        }
    }
    
    /**
     * Format GP value for display
     */
    private String formatGP(long amount) {
        if (amount >= 1_000_000_000) return String.format("%.1fB", amount / 1_000_000_000.0);
        if (amount >= 1_000_000) return String.format("%.1fM", amount / 1_000_000.0);
        if (amount >= 1_000) return String.format("%.1fK", amount / 1_000.0);
        return String.valueOf(amount);
    }
    
    // Getters
    public String getCurrentTrader() { return currentTrader; }
    public long getCurrentBetAmount() { return currentBetAmount; }
    public void setCurrentBetAmount(long amount) { this.currentBetAmount = amount; }
    public String getSelectedGame() { return selectedGame; }
    public void setSelectedGame(String game) { this.selectedGame = game; }
    public boolean isWelcomeSent() { return welcomeSent; }
    public long getLastVerifiedValue() { return lastVerifiedValue; }
    
    /**
     * Trade detection result
     */
    public static class TradeDetectionResult {
        public final boolean detected;
        public final String playerName;
        public final TradeDetectionType type;
        
        public TradeDetectionResult(boolean detected, String playerName, TradeDetectionType type) {
            this.detected = detected;
            this.playerName = playerName;
            this.type = type;
        }
    }
    
    public enum TradeDetectionType {
        NO_REQUEST,
        TRADE_WINDOW_OPEN,
        ACCEPTED_REQUEST,
        QUEUED_REQUEST,
        RECENTLY_DECLINED
    }
    
    public enum TradeAction {
        WAIT,
        ACCEPTED_SCREEN1,
        BACK_TO_SCREEN1,
        TRADE_COMPLETE,
        TRADE_CLOSED,
        DECLINED
    }
    
    /**
     * Trade request holder
     */
    private static class TradeRequest {
        final String playerName;
        final long timestamp;
        
        TradeRequest(String playerName, long timestamp) {
            this.playerName = playerName;
            this.timestamp = timestamp;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 30000; // 30 second expiry
        }
    }
}
