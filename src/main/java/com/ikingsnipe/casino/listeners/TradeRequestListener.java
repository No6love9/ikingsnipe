package com.ikingsnipe.casino.listeners;

import com.ikingsnipe.casino.managers.TradeManager;
import com.ikingsnipe.casino.models.TradeConfig;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.dreambot.api.wrappers.widgets.message.MessageType;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listener for detecting trade requests from game messages
 * Captures trade requests even when not actively looking
 */
public class TradeRequestListener implements ChatListener {
    
    private final TradeManager tradeManager;
    private final TradeConfig tradeConfig;
    
    // Pattern to match trade request messages
    private static final Pattern TRADE_REQUEST_PATTERN = Pattern.compile(
        "^(.+?) wishes to trade with you\\.$"
    );
    
    // Queue of detected trade requests
    private final ConcurrentLinkedQueue<TradeRequestEvent> tradeRequests = new ConcurrentLinkedQueue<>();
    
    // Track recently processed requests to avoid duplicates
    private final Set<String> recentRequests = new HashSet<>();
    private long lastCleanup = 0;
    
    // Callback for trade request notifications
    private TradeRequestCallback callback;
    
    public TradeRequestListener(TradeManager tradeManager, TradeConfig tradeConfig) {
        this.tradeManager = tradeManager;
        this.tradeConfig = tradeConfig;
    }
    
    @Override
    public void onMessage(Message message) {
        if (message == null) return;
        
        // Check for trade request messages
        // MessageType.GAME covers trade-related messages
        if (message.getType() == MessageType.GAME) {
            
            String text = message.getMessage();
            if (text == null) return;
            
            // Check for trade request pattern
            Matcher matcher = TRADE_REQUEST_PATTERN.matcher(text);
            if (matcher.matches()) {
                String playerName = matcher.group(1);
                handleTradeRequest(playerName);
            }
            
            // Also check for direct trade request format
            if (text.contains("wishes to trade")) {
                String[] parts = text.split(" wishes to trade");
                if (parts.length > 0) {
                    String playerName = parts[0].trim();
                    handleTradeRequest(playerName);
                }
            }
        }
        
        // Check for trade declined/cancelled messages
        if (message.getType() == MessageType.GAME) {
            String text = message.getMessage();
            if (text != null) {
                if (text.contains("Other player has declined") || 
                    text.contains("Trade cancelled") ||
                    text.contains("Too far away")) {
                    handleTradeEnded();
                }
            }
        }
    }
    
    /**
     * Handle a detected trade request
     */
    private void handleTradeRequest(String playerName) {
        if (playerName == null || playerName.isEmpty()) return;
        
        // Clean up old requests periodically
        cleanupOldRequests();
        
        // Check for duplicate
        String key = playerName.toLowerCase();
        if (recentRequests.contains(key)) {
            return;
        }
        
        // Add to recent requests
        recentRequests.add(key);
        
        // Create trade request event
        TradeRequestEvent event = new TradeRequestEvent(playerName, System.currentTimeMillis());
        tradeRequests.offer(event);
        
        // Log the request
        if (tradeConfig.verboseLogging) {
            Logger.log("[TradeRequestListener] Trade request detected from: " + playerName);
        }
        
        // Queue the request in trade manager
        if (tradeManager != null && tradeConfig.enableTradeQueue) {
            tradeManager.queueTradeRequest(playerName);
        }
        
        // Notify callback if set
        if (callback != null) {
            callback.onTradeRequest(event);
        }
    }
    
    /**
     * Handle trade ended event
     */
    private void handleTradeEnded() {
        if (tradeConfig.verboseLogging) {
            Logger.log("[TradeRequestListener] Trade ended/cancelled");
        }
    }
    
    /**
     * Clean up old requests from tracking
     */
    private void cleanupOldRequests() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup > 10000) { // Clean every 10 seconds
            recentRequests.clear();
            
            // Remove expired requests from queue
            while (!tradeRequests.isEmpty()) {
                TradeRequestEvent peek = tradeRequests.peek();
                if (peek != null && peek.isExpired(30000)) {
                    tradeRequests.poll();
                } else {
                    break;
                }
            }
            
            lastCleanup = now;
        }
    }
    
    /**
     * Get the next pending trade request
     */
    public TradeRequestEvent getNextRequest() {
        cleanupOldRequests();
        
        while (!tradeRequests.isEmpty()) {
            TradeRequestEvent event = tradeRequests.poll();
            if (event != null && !event.isExpired(30000)) {
                return event;
            }
        }
        return null;
    }
    
    /**
     * Check if there are pending trade requests
     */
    public boolean hasPendingRequests() {
        cleanupOldRequests();
        return !tradeRequests.isEmpty();
    }
    
    /**
     * Get count of pending requests
     */
    public int getPendingRequestCount() {
        cleanupOldRequests();
        return tradeRequests.size();
    }
    
    /**
     * Clear all pending requests
     */
    public void clearRequests() {
        tradeRequests.clear();
        recentRequests.clear();
    }
    
    /**
     * Set callback for trade request notifications
     */
    public void setCallback(TradeRequestCallback callback) {
        this.callback = callback;
    }
    
    /**
     * Trade request event
     */
    public static class TradeRequestEvent {
        public final String playerName;
        public final long timestamp;
        
        public TradeRequestEvent(String playerName, long timestamp) {
            this.playerName = playerName;
            this.timestamp = timestamp;
        }
        
        public boolean isExpired(long timeout) {
            return System.currentTimeMillis() - timestamp > timeout;
        }
        
        public long getAge() {
            return System.currentTimeMillis() - timestamp;
        }
    }
    
    /**
     * Callback interface for trade request notifications
     */
    public interface TradeRequestCallback {
        void onTradeRequest(TradeRequestEvent event);
    }
}
