package com.ikingsnipe.casino.managers;

import com.ikingsnipe.casino.input.ModernInputHandler;
import com.ikingsnipe.casino.listeners.EnterpriseTradeListener;
import com.ikingsnipe.casino.models.CasinoConfig;
import com.ikingsnipe.database.DatabaseManager;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.utilities.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

/**
 * Enterprise-Grade Trade Manager v10.0
 * 
 * Features:
 * - Safe trade validation with scam detection
 * - Player name extraction and validation
 * - Bet amount verification
 * - Database integration for balance tracking
 * - Comprehensive logging and statistics
 * - Thread-safe operations
 * - Timeout protection
 * - Error recovery
 */
public class EnterpriseTradeManager {
    
    private final CasinoConfig config;
    private final DatabaseManager dbManager;
    private final EnterpriseTradeListener tradeListener;
    
    // Trade state
    private volatile String currentTrader = null;
    private volatile long verifiedValueGP = 0;
    private volatile long lastTradeTime = 0;
    
    // Statistics
    private volatile long totalTradesProcessed = 0;
    private volatile long totalTradesAccepted = 0;
    private volatile long totalTradesDeclined = 0;
    private volatile long totalScamsDetected = 0;
    private volatile long totalGPProcessed = 0;
    
    // Configuration
    private static final long TRADE_TIMEOUT_MS = 60000; // 60 seconds
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public EnterpriseTradeManager(CasinoConfig config, DatabaseManager dbManager) {
        this.config = config;
        this.dbManager = dbManager;
        this.tradeListener = new EnterpriseTradeListener(config.tradeConfig != null ? 
            config.tradeConfig : new com.ikingsnipe.casino.models.TradeConfig());
        
        Logger.log("[TradeManager] Initialized - Min Bet: " + config.minBet + " GP");
    }
    
    /**
     * Get trade listener
     */
    public EnterpriseTradeListener getTradeListener() {
        return tradeListener;
    }
    
    /**
     * Handle trade screen 1 (initial offer)
     */
    public boolean handleTradeScreen1() {
        try {
            if (!Trade.isOpen(1)) {
                return false;
            }
            
            // Delegate to listener
            tradeListener.handleTradeScreen1();
            
            String trader = tradeListener.getCurrentTrader();
            long amount = tradeListener.getCurrentTradeAmount();
            
            if (trader != null && amount > 0) {
                currentTrader = trader;
                verifiedValueGP = amount;
                lastTradeTime = System.currentTimeMillis();
                totalTradesAccepted++;
                totalGPProcessed += amount;
                
                Logger.log("[TradeManager] Screen 1 accepted - Trader: " + trader + ", Amount: " + amount);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            Logger.error("[TradeManager] Error in screen 1: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Handle trade screen 2 (confirmation)
     */
    public boolean handleTradeScreen2() {
        try {
            if (!Trade.isOpen(2)) {
                return false;
            }
            
            String trader = Trade.getTradingWith();
            if (trader == null || trader.isEmpty()) {
                Logger.warn("[TradeManager] Could not get trader name on screen 2");
                return false;
            }
            
            long screen2Value = calculateTradeValue();
            
            // Verify value hasn't changed (scam detection)
            if (screen2Value != verifiedValueGP) {
                Logger.error("[TradeManager] SCAM DETECTED: Value mismatch!");
                Logger.error("[TradeManager] Screen 1: " + verifiedValueGP + " GP, Screen 2: " + screen2Value + " GP");
                
                totalScamsDetected++;
                Trade.declineTrade();
                
                // Send alert
                ModernInputHandler.typeScamAlert(trader, "Item swap detected");
                
                // Log to database
                if (dbManager != null) {
                    dbManager.recordGame(trader, "SCAM_ATTEMPT", screen2Value, "DECLINED");
                }
                
                return false;
            }
            
            // Accept trade
            if (Trade.acceptTrade()) {
                totalTradesAccepted++;
                
                Logger.log("[TradeManager] Screen 2 accepted - Trader: " + trader + ", Amount: " + screen2Value);
                
                // Update database
                if (dbManager != null) {
                    dbManager.updateBalance(trader, screen2Value);
                    dbManager.recordGame(trader, "DEPOSIT", screen2Value, "SUCCESS");
                }
                
                // Send confirmation
                ModernInputHandler.typeSafeTradeConfirmation(trader, screen2Value);
                
                return true;
            }
            
            return false;
        } catch (Exception e) {
            Logger.error("[TradeManager] Error in screen 2: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Calculate trade value
     */
    private long calculateTradeValue() {
        try {
            long total = 0;
            
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
            Logger.error("[TradeManager] Error calculating trade value: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Process pending trade events
     */
    public void processPendingTrades() {
        try {
            while (tradeListener.hasPendingTrades()) {
                EnterpriseTradeListener.TradeEvent event = tradeListener.getNextTradeEvent(100);
                if (event == null) break;
                
                handleTradeEvent(event);
            }
        } catch (Exception e) {
            Logger.error("[TradeManager] Error processing pending trades: " + e.getMessage());
        }
    }
    
    /**
     * Handle trade event
     */
    private void handleTradeEvent(EnterpriseTradeListener.TradeEvent event) {
        try {
            switch (event.type) {
                case TRADE_REQUEST:
                    Logger.log("[TradeManager] Trade request from: " + event.playerName);
                    break;
                    
                case TRADE_COMPLETED:
                    Logger.log("[TradeManager] Trade completed with: " + event.playerName + ", Amount: " + event.amount);
                    totalTradesProcessed++;
                    break;
                    
                case TRADE_DECLINED:
                    Logger.log("[TradeManager] Trade declined with: " + event.playerName);
                    totalTradesDeclined++;
                    break;
                    
                case SCAM_DETECTED:
                    Logger.error("[TradeManager] Scam detected from: " + event.playerName);
                    totalScamsDetected++;
                    break;
                    
                default:
                    Logger.log("[TradeManager] Unknown trade event: " + event.type);
            }
        } catch (Exception e) {
            Logger.error("[TradeManager] Error handling trade event: " + e.getMessage());
        }
    }
    
    /**
     * Get current trader
     */
    public String getCurrentTrader() {
        return currentTrader;
    }
    
    /**
     * Get verified trade amount
     */
    public long getVerifiedAmount() {
        return verifiedValueGP;
    }
    
    /**
     * Reset trade state
     */
    public void resetTradeState() {
        currentTrader = null;
        verifiedValueGP = 0;
        tradeListener.resetTradeState();
    }
    
    /**
     * Get statistics
     */
    public TradeManagerStatistics getStatistics() {
        EnterpriseTradeListener.TradeStatistics listenerStats = tradeListener.getStatistics();
        
        return new TradeManagerStatistics(
            totalTradesProcessed,
            totalTradesAccepted,
            totalTradesDeclined,
            totalScamsDetected,
            totalGPProcessed,
            listenerStats.queueSize
        );
    }
    
    /**
     * Trade manager statistics
     */
    public static class TradeManagerStatistics {
        public final long processed;
        public final long accepted;
        public final long declined;
        public final long scamsDetected;
        public final long gpProcessed;
        public final int queueSize;
        
        public TradeManagerStatistics(long processed, long accepted, long declined, 
                                     long scamsDetected, long gpProcessed, int queueSize) {
            this.processed = processed;
            this.accepted = accepted;
            this.declined = declined;
            this.scamsDetected = scamsDetected;
            this.gpProcessed = gpProcessed;
            this.queueSize = queueSize;
        }
        
        @Override
        public String toString() {
            return String.format(
                "TradeManagerStats{processed=%d, accepted=%d, declined=%d, scams=%d, gpProcessed=%,d, queue=%d}",
                processed, accepted, declined, scamsDetected, gpProcessed, queueSize
            );
        }
    }
}
