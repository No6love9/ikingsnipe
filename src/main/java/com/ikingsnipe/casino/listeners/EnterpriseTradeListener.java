package com.ikingsnipe.casino.listeners;

import com.ikingsnipe.casino.input.ModernInputHandler;
import com.ikingsnipe.casino.messaging.EnterpriseMessageHandler;
import com.ikingsnipe.casino.models.TradeConfig;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.widgets.message.MessageType;

import java.util.*;
import java.util.concurrent.*;

/**
 * Enterprise-Grade Trade Listener
 * Handles all trade-related events with proper player name detection
 * 
 * Features:
 * - Robust player name extraction
 * - Safe trade validation
 * - Bet amount verification
 * - Scam detection
 * - Trade state management
 * - Comprehensive logging
 * - Thread-safe operations
 */
public class EnterpriseTradeListener implements EnterpriseMessageHandler.MessageListener {
    
    private final TradeConfig config;
    private final BlockingQueue<TradeEvent> tradeQueue = new LinkedBlockingQueue<>(100);
    // Trade state tracking map
    private final Set<String> processedTrades = Collections.synchronizedSet(new HashSet<>());
    
    // Trade state tracking
    private volatile String currentTrader = null;
    private volatile long currentTradeAmount = 0;
    private volatile long tradeStartTime = 0;
    private volatile TradePhase currentPhase = TradePhase.IDLE;
    
    // Statistics
    private volatile long tradesProcessed = 0;
    private volatile long tradesAccepted = 0;
    private volatile long tradesDeclined = 0;
    private volatile long scamsDetected = 0;
    
    // Configuration
    private static final long TRADE_TIMEOUT_MS = 60000; // 60 seconds
    private static final long DUPLICATE_WINDOW_MS = 5000; // 5 seconds
    
    public enum TradePhase {
        IDLE,
        REQUEST_RECEIVED,
        SCREEN_1_OPEN,
        SCREEN_1_ACCEPTED,
        SCREEN_2_OPEN,
        SCREEN_2_ACCEPTED,
        COMPLETED,
        DECLINED,
        TIMEOUT
    }
    
    public EnterpriseTradeListener(TradeConfig config) {
        this.config = config;
        Logger.log("[TradeListener] Initialized with config: " + config);
    }
    
    /**
     * Handle incoming message
     */
    @Override
    public void onMessage(String text, MessageType type, long timestamp) {
        // Generic message handling
    }
    
    /**
     * Handle trade request
     */
    @Override
    public void onTradeRequest(String playerName, long timestamp) {
        if (playerName == null || playerName.isEmpty()) {
            Logger.warn("[TradeListener] Received trade request with null/empty player name");
            return;
        }
        
        // Validate player name
        if (!ModernInputHandler.isValidPlayerName(playerName)) {
            Logger.warn("[TradeListener] Invalid player name format: " + playerName);
            return;
        }
        
        // Check for duplicate
        String key = playerName.toLowerCase();
        if (processedTrades.contains(key)) {
            Logger.log("[TradeListener] Ignoring duplicate trade request from: " + playerName);
            return;
        }
        
        processedTrades.add(key);
        
        try {
            // Create trade event
            TradeEvent event = new TradeEvent(
                TradeEventType.TRADE_REQUEST,
                playerName,
                0,
                timestamp
            );
            
            // Queue the event
            if (!tradeQueue.offer(event)) {
                Logger.warn("[TradeListener] Trade queue full, dropping request from: " + playerName);
                return;
            }
            
            tradesProcessed++;
            currentTrader = playerName;
            currentPhase = TradePhase.REQUEST_RECEIVED;
            tradeStartTime = timestamp;
            
            Logger.log("[TradeListener] Trade request received from: " + playerName);
            
            // Notify if configured
            if (config.verboseLogging) {
                Logger.log("[TradeListener] Trade request queued for: " + playerName);
            }
        } catch (Exception e) {
            Logger.error("[TradeListener] Error handling trade request: " + e.getMessage());
        }
    }
    
    /**
     * Handle trade declined
     */
    @Override
    public void onTradeDeclined(String reason, long timestamp) {
        try {
            tradesDeclined++;
            currentPhase = TradePhase.DECLINED;
            
            Logger.log("[TradeListener] Trade declined: " + reason);
            
            // Create event
            TradeEvent event = new TradeEvent(
                TradeEventType.TRADE_DECLINED,
                currentTrader != null ? currentTrader : "Unknown",
                0,
                timestamp
            );
            
            tradeQueue.offer(event);
            
            // Reset state
            resetTradeState();
        } catch (Exception e) {
            Logger.error("[TradeListener] Error handling trade declined: " + e.getMessage());
        }
    }
    
    /**
     * Handle game result
     */
    @Override
    public void onGameResult(String playerName, String result, long timestamp) {
        // Game result handling
    }
    
    /**
     * Handle trade screen 1 (initial offer)
     */
    public void handleTradeScreen1() {
        try {
            if (!Trade.isOpen(1)) {
                return;
            }
            
            currentPhase = TradePhase.SCREEN_1_OPEN;
            
            // Get trading partner name
            String tradingWith = Trade.getTradingWith();
            if (tradingWith == null || tradingWith.isEmpty()) {
                Logger.warn("[TradeListener] Could not determine trading partner on screen 1");
                return;
            }
            
            currentTrader = tradingWith;
            
            // Calculate trade value
            long tradeValue = calculateTradeValue();
            currentTradeAmount = tradeValue;
            
            Logger.log("[TradeListener] Screen 1 - Trading with: " + tradingWith + ", Value: " + tradeValue);
            
            // Validate bet
            if (tradeValue < 100000) { // Minimum bet
                Logger.log("[TradeListener] Trade value below minimum, declining");
                Trade.declineTrade();
                tradesDeclined++;
                return;
            }
            
            // Accept trade
            if (Trade.acceptTrade()) {
                tradesAccepted++;
                currentPhase = TradePhase.SCREEN_1_ACCEPTED;
                Logger.log("[TradeListener] Screen 1 accepted for: " + tradingWith);
            }
        } catch (Exception e) {
            Logger.error("[TradeListener] Error handling screen 1: " + e.getMessage());
        }
    }
    
    /**
     * Handle trade screen 2 (confirmation)
     */
    public void handleTradeScreen2() {
        try {
            if (!Trade.isOpen(2)) {
                return;
            }
            
            currentPhase = TradePhase.SCREEN_2_OPEN;
            
            // Get trading partner name
            String tradingWith = Trade.getTradingWith();
            if (tradingWith == null || tradingWith.isEmpty()) {
                Logger.warn("[TradeListener] Could not determine trading partner on screen 2");
                return;
            }
            
            // Calculate new trade value
            long screen2Value = calculateTradeValue();
            
            Logger.log("[TradeListener] Screen 2 - Trading with: " + tradingWith + ", Value: " + screen2Value);
            
            // Verify value hasn't changed (scam detection)
            if (screen2Value != currentTradeAmount) {
                Logger.warn("[TradeListener] SCAM DETECTED: Value changed from " + currentTradeAmount + " to " + screen2Value);
                scamsDetected++;
                Trade.declineTrade();
                ModernInputHandler.typeScamAlert(tradingWith, "Value changed between screens");
                return;
            }
            
            // Accept trade
            if (Trade.acceptTrade()) {
                currentPhase = TradePhase.SCREEN_2_ACCEPTED;
                Logger.log("[TradeListener] Screen 2 accepted for: " + tradingWith + ", Amount: " + screen2Value);
                
                // Create completion event
                TradeEvent event = new TradeEvent(
                    TradeEventType.TRADE_COMPLETED,
                    tradingWith,
                    screen2Value,
                    System.currentTimeMillis()
                );
                tradeQueue.offer(event);
                
                // Notify
                // Send confirmation
                ModernInputHandler.typeSafeTradeConfirmation(tradingWith, screen2Value);
            }
        } catch (Exception e) {
            Logger.error("[TradeListener] Error handling screen 2: " + e.getMessage());
        }
    }
    
    /**
     * Calculate trade value in GP
     */
    private long calculateTradeValue() {
        try {
            long total = 0;
            
            // Get items from other player
            var items = Trade.getTheirItems();
            if (items == null) {
                return 0;
            }
            
            for (var item : items) {
                if (item == null) continue;
                
                int itemId = item.getID();
                int amount = item.getAmount();
                
                // Coins (ID 995)
                if (itemId == 995) {
                    total += amount;
                }
                // Platinum tokens (ID 13204) - 1000 GP each
                else if (itemId == 13204) {
                    total += (long) amount * 1000L;
                }
            }
            
            return total;
        } catch (Exception e) {
            Logger.error("[TradeListener] Error calculating trade value: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Get next trade event
     */
    public TradeEvent getNextTradeEvent() {
        return tradeQueue.poll();
    }
    
    /**
     * Get next trade event with timeout
     */
    public TradeEvent getNextTradeEvent(long timeoutMs) {
        try {
            return tradeQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    /**
     * Check if trades are pending
     */
    public boolean hasPendingTrades() {
        return !tradeQueue.isEmpty();
    }
    
    /**
     * Get pending trade count
     */
    public int getPendingTradeCount() {
        return tradeQueue.size();
    }
    
    /**
     * Get current trader name
     */
    public String getCurrentTrader() {
        return currentTrader;
    }
    
    /**
     * Get current trade amount
     */
    public long getCurrentTradeAmount() {
        return currentTradeAmount;
    }
    
    /**
     * Get current trade phase
     */
    public TradePhase getCurrentPhase() {
        return currentPhase;
    }
    
    /**
     * Reset trade state
     */
    public void resetTradeState() {
        currentTrader = null;
        currentTradeAmount = 0;
        currentPhase = TradePhase.IDLE;
        tradeStartTime = 0;
        
        // Clean up old processed trades
        if (System.currentTimeMillis() % 10 == 0) {
            processedTrades.clear();
        }
    }
    
    /**
     * Get statistics
     */
    public TradeStatistics getStatistics() {
        return new TradeStatistics(
            tradesProcessed,
            tradesAccepted,
            tradesDeclined,
            scamsDetected,
            tradeQueue.size()
        );
    }
    
    /**
     * Trade event
     */
    public static class TradeEvent {
        public final TradeEventType type;
        public final String playerName;
        public final long amount;
        public final long timestamp;
        
        public TradeEvent(TradeEventType type, String playerName, long amount, long timestamp) {
            this.type = type;
            this.playerName = playerName;
            this.amount = amount;
            this.timestamp = timestamp;
        }
    }
    
    public enum TradeEventType {
        TRADE_REQUEST,
        TRADE_SCREEN_1,
        TRADE_SCREEN_2,
        TRADE_COMPLETED,
        TRADE_DECLINED,
        SCAM_DETECTED
    }
    
    /**
     * Trade statistics
     */
    public static class TradeStatistics {
        public final long processed;
        public final long accepted;
        public final long declined;
        public final long scamsDetected;
        public final int queueSize;
        
        public TradeStatistics(long processed, long accepted, long declined, long scamsDetected, int queueSize) {
            this.processed = processed;
            this.accepted = accepted;
            this.declined = declined;
            this.scamsDetected = scamsDetected;
            this.queueSize = queueSize;
        }
        
        @Override
        public String toString() {
            return String.format(
                "TradeStats{processed=%d, accepted=%d, declined=%d, scams=%d, queue=%d}",
                processed, accepted, declined, scamsDetected, queueSize
            );
        }
    }
}
